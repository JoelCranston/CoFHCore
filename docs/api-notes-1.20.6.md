# API notes — 1.20.5/1.20.6 hop

Everything below was confirmed by decompiling the real mapped jar this project compiles
against (`build/neoForm/neoFormJoined1.20.6-.../raw.jar`) via `javap`, not by reading
summarized docs — the primer at `docs.neoforged.net/primer/docs/1.20.6/` covers the
Mojang-mapping-level changes, but CoFHCore's own break/fix shape needed the real jar to
verify each time. Ordered chronologically, matching the commit history
(`git log --oneline` for the exact sequence) and the error-count trajectory each one left
behind.

## Networking → the 1.20.5+ payload API

The single biggest change-cluster this hop (~26 of CoFHCore's original ~100-under-the-cap
errors). All 25 `CustomPacketPayload` records and their 25 handler classes needed
rebuilding:

- `id()`/`write()`/`FriendlyByteBuf` constructor → `Type<T>` + `StreamCodec`
  (`StreamCodec.composite`/`.map`/`.unit` as the shape needs). Added
  `PayloadCodecs.REMAINING_BYTES` for the 8 payloads that forward an opaque
  tile/container-specific blob rather than fixed fields.
- `PlayPayloadContext` → `IPayloadContext`; `workHandler().submitAsync()` →
  `enqueueWork()`; the `Optional<Player>` dance → `context.player()` (returns `Player`
  directly now).
- `PacketHandler.java`: `RegisterPayloadHandlerEvent` → `RegisterPayloadHandlersEvent`,
  `IPayloadRegistrar` → `PayloadRegistrar`, `registrar(modId)` → `registrar(version)`,
  `.play(type, codec, handler -> handler.server/client(...))` → `playToServer`/
  `playToClient`.
- `PacketDistributor`: `SERVER.noArg().send()` → `sendToServer()`, `PLAYER.with(p).send()`
  → `sendToPlayer(p, ...)`, `NEAR.with(TargetPoint).send()` → `Utils.sendNear(...)`
  helpers wrapping `sendToPlayersNear()` (`TargetPoint` no longer exists).

## TickEvent → `event.tick`/`event.client.event` Pre/Post classes

`TickEvent` and its `Phase`-checking pattern were removed upstream; replaced across 5
files:
- `ItemTracker`: `PlayerTickEvent.Pre` (`event.player` → `event.getEntity()`).
- `AreaEffectEvents`, `CoreCommonEvents`: `ServerTickEvent.Pre`/`.Post`.
- `TransientLightManager`, `CoreClientEvents`: `ClientTickEvent.Post`.
- `CoreClientEvents`: `RenderTickEvent` → `RenderFrameEvent.Pre`
  (`event.renderTickTime` → `event.getPartialTick()`).

Rebuilding after this surfaced separate breakage in some of the same files
(`Enchantments.FALL_PROTECTION`, `SaplingGrowTreeEvent`) plus some
`Mod.EventBusSubscriber` resolution errors — those turned out to be javac
error-recovery cascades from unrelated breaks in the same file, not a real break of the
annotation, and cleared once the real error in each file was fixed (see the
armor/dispenser entry below for the confirmation).

## Enchantment system → `EnchantmentDefinition`

`Enchantment` is no longer subclassed with overridable cost methods — it's built from a
single immutable `EnchantmentDefinition` record (`Enchantment.definition(...)`), with
`getMinCost`/`getMaxCost`/`getMaxLevel` all final.

- `EnchantmentCoFH`: constructor now takes an `EnchantmentDefinition`; dropped the
  now-impossible cost/level overrides. `enable`/`treasure`/`allowOnBooks`/`discoverable`/
  `tradeable` overrides are untouched — still open in the new API.
- `EnchantmentOverride`, `DamageEnchantmentCoFH`: same constructor update;
  `DamageEnchantmentCoFH`'s cost formula became static `Cost`-builder helpers for
  subclasses to use at construction time. **Both classes are currently unused anywhere
  in the 4-repo family** — unverified at runtime, flag for whoever adds a concrete
  subclass later.
- `HoldingEnchantment` (the only concrete enchantment in the whole family right now):
  builds its `EnchantmentDefinition` directly, matching the old cost formula via
  `Enchantment.Cost.dynamicCost(...)`.
- `CoreEnchantConfig`: the "Max Level" config option is gone — it can't take effect at
  runtime anymore since `getMaxLevel()` is baked into the definition at construction. A
  real, permanent capability loss from the redesign, not something to route around.
- `CoreEnchantments`: `EnchantmentCategory` was deleted upstream (replaced by item tags)
  — removed the dead `Types` inner class (no other callers in the family).
- `Utils.java`: `MobType.UNDEAD` removed → `entity.getType().is(EntityTypeTags.UNDEAD)`;
  `removeEnchantment()` rewritten against the `ItemEnchantments` data component
  (`DataComponents.ENCHANTMENTS`) instead of raw NBT.
- `CoreCommonEvents`: `Enchantments.FALL_PROTECTION` renamed to `FEATHER_FALLING`;
  `SaplingGrowTreeEvent` → `BlockGrowFeatureEvent` (`setResult(DENY)` →
  `setCanceled(true)`).

## ArmorMaterial / HorseArmorItem / dispenser

- `ArmorMaterial` became a final record upstream (was an interface CoFH implemented) —
  `ArmorMaterialCoFH` is now a factory building the record instead of a base class;
  durability moved off the material entirely onto the item's own
  `DataComponents.MAX_DAMAGE` (not fixed here — nothing in this repo constructs an
  actual armor item yet).
- `ArmorItem`'s constructor takes `Holder<ArmorMaterial>` now, not a raw `ArmorMaterial`
  — updated `ArmorItemCoFH`/`DyeableArmorItemCoFH`.
- `HorseArmorItem`/`DyeableHorseArmorItem` were merged upstream into a single
  `AnimalArmorItem` (`BodyType.EQUESTRIAN`/`CANINE`, for wolf armor) with dyeability as
  a constructor boolean instead of a subclass — `HorseArmorItemCoFH` now extends
  `AnimalArmorItem` directly; `DyeableHorseArmorItemCoFH` is a thin fixed-dyeable=true
  subclass of it. `HorseArmorItemMixin` retargeted to `AnimalArmorItem`.
- `DyeableLeatherItem` (the old dyeability marker interface) was removed upstream —
  dyeability is a data component (`DYED_COLOR`) now, not something to type-check for.
- `AbstractProjectileDispenseBehavior` renamed to `ProjectileDispenseBehavior`
  (`ArrowItemCoFH`'s dispenser behavior).
- **None of `HorseArmorItemCoFH`/`DyeableHorseArmorItemCoFH`/`DyeableArmorItemCoFH` are
  used anywhere in the 4-repo family currently** — reasonable-effort conversions, not
  verified against a real call site.

This commit also confirmed the `Mod.EventBusSubscriber` "cannot find symbol" errors seen
during the TickEvent category were javac cascades from these HorseArmorItem-related
breaks in the same files, not a real annotation break — they cleared here.

## Particle system → `MapCodec`/`StreamCodec`

`ParticleType<T>`'s old `(boolean, Deserializer<T>)` constructor is gone, replaced by a
plain `(boolean)` one plus two new abstract methods: `MapCodec<T> codec()` (command/
persistence) and `StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec()`
(network). `ParticleOptions` dropped `writeToNetwork`/`writeToString`/the `Deserializer`
inner class entirely — down to just `getType()`.

Following vanilla's `ColorParticleOption` pattern: each leaf `ParticleOptions` class now
exposes static `codec(ParticleType<T>)`/`streamCodec(ParticleType<T>)` factories that
close over the owning `ParticleType`.

- `CoFHParticleOptions`: dropped `writeToNetwork`/`writeToString`/the `StringReader` ctor
  and an unused ctor overload. Still just the shared size/duration/delay fields — never
  registered as its own leaf `ParticleType`.
- `ColorParticleOptions`/`CylindricalParticleOptions`/`BiColorParticleOptions`: replaced
  `CODEC`/`DESERIALIZER` with static `codec(type)`/`streamCodec(type)` factories
  (`RecordCodecBuilder.mapCodec` + `StreamCodec.composite` over `ByteBufCodecs.FLOAT`/
  `INT`). The two subclasses name theirs `cylindricalCodec`/`cylindricalStreamCodec` and
  `biColorCodec`/`biColorStreamCodec` rather than reusing `codec`/`streamCodec` — reusing
  the same name across the hierarchy is a static-method "erasure name clash" in javac
  (generic return types aren't substitutable the way an instance override would be).
- `ColorParticleType`/`CylindricalParticleType`/`PointToPointParticleType`: constructors
  just call `super(overrideLimit)`; `codec()`/`streamCodec()` delegate to the options
  class's static factories, passing `this`.

All particle-related errors confirmed gone after this. CofhCore's file-with-errors count
dropped 43 → 40; a few files' errors had been masked by particle-adjacent breakage in the
same file and became newly visible (`ItemHelper`, `ItemStorageCoFH`, `BaseFluidFilter`,
`CoreMobEffects` — the NBT→DataComponents category below).

## Entity: `defineSynchedData` / spawn-data / misc

`Entity#defineSynchedData()` lost its no-arg form — it's abstract now and takes a
`SynchedEntityData.Builder` that subclasses call `.define(...)` on, with `Entity` itself
building and assigning the actual `SynchedEntityData` afterward.

- `AbstractSpell`: signature update only (still a no-op — no synced data of its own).
- `ThrownKnife`: same signature change, plus three unrelated breaks in the same file:
  - `AbstractArrow`'s abstract contract flipped — `getPickupItem()` has a base impl now,
    the new abstract method is `getDefaultPickupItem()`; delegates to `getPickupItem()`.
  - `IItemExtension#getDamage()` gained an `ItemStack` parameter.
  - `LivingEntity#getMobType()` is gone (`MobType` deleted) — `EnchantmentHelper
    #getDamageBonus`'s second param changed `MobType` → `EntityType<?>` to match; now
    just `target.getType()`.
  - `Entity#setSecondsOnFire(int)` renamed to `igniteForSeconds(int)`.
  - `ItemStack.of(CompoundTag)`/`ItemStack#save(CompoundTag)` both now need a
    `HolderLookup.Provider` — used `this.registryAccess()` (`RegistryAccess` implements
    `HolderLookup.Provider`) with `ItemStack.parseOptional(Provider, CompoundTag)`/
    `save(Provider)`.
- `ElectricField`: `MobEffectInstance`'s constructor wants `Holder<MobEffect>` now;
  `DeferredHolder<MobEffect, MobEffect>` already implements `Holder<MobEffect>` — dropped
  the stray `.get()`.
- `AbstractFieldSpell`:
  - `getEyeHeight(Pose, EntityDimensions)` was removed — `Entity#getEyeHeight(Pose)` is
    final now, driven purely by `getDimensions(Pose)`. Folded the old 0.45F eye-height
    factor into `getDimensions(Pose)` via `EntityDimensions#withEyeHeight(...)`.
  - `EntityDimensions#height` is a private field now — use the `height()` accessor
    (also affected `makeBoundingBox()`).
  - `IEntityWithComplexSpawn`'s `writeSpawnData`/`readSpawnData` now take
    `RegistryFriendlyByteBuf`, not `FriendlyByteBuf`.

Confirmed clean for all five files touched (`ThrownKnife`, `ElectricField`, `FrostField`,
`AbstractSpell`, `AbstractFieldSpell`). File-with-errors count 40 → 36.
`FluidStorageCoFH.java` newly surfaced two errors here, unmasked but belonging to the
NBT→DataComponents category below.

## ItemStack NBT → DataComponents (`ItemHelper`/`ItemStorageCoFH`)

Raw `ItemStack#hasTag`/`getTag`/`setTag`/`of`/`save` are all gone — custom mod-attached
NBT lives in the `DataComponents.CUSTOM_DATA` component now, and structural persistence
needs a `HolderLookup.Provider`.

- `ItemHelper`: `copyTag`/`itemsEqualWithTags`/`areItemStacksEqualIgnoreTags` rewritten
  against `DataComponents.CUSTOM_DATA` + `CustomData` (`get`/`has`/`getOrDefault` come
  from the `DataComponentHolder` interface `ItemStack` implements);
  `isSameItemSameTags` renamed upstream to `isSameItemSameComponents`.
- `ItemStorageCoFH`: `read`/`write`/`loadItemStack`/`saveItemStack` now take a
  `HolderLookup.Provider`, using `ItemStack.parseOptional(Provider, CompoundTag)` and
  `save(Provider)` (returns a fresh `Tag` to merge in, rather than mutating in place like
  the old `save(CompoundTag)` did).

**Also discovered mid-category**: javac's default `-Xmaxerrs 100` had been capping every
rebuild's error output the whole hop — the count hovering ~100 across many earlier
"category done" commits wasn't the real total, it was the cap. Raised to `100000` in
`build.gradle`. Real picture at that point: **465 errors across 132 files**, not the
40/101 previously tracked — substantially more remaining work than visible before. Every
error count logged after this commit is the true, uncapped count.

## HolderLookup.Provider threading (Item/Fluid storage + filters)

Continuation of the NBT→DataComponents migration — tracing `IFilter`'s
`INBTSerializable` requirement outward turned into threading a `HolderLookup.Provider`
through every layer between it and wherever one is actually available.

- `SimpleItemInv`/`SimpleTankInv`: `read`/`write` and all the `*SlotsToNBT*`/
  `*SlotsUnordered*` helpers take a `Provider` now, passed down to each slot's
  `ItemStorageCoFH`/`FluidStorageCoFH`. `InventoryContainerItem` is this hop's one real
  caller of `SimpleItemInv#read` — its whole call chain
  (`IInventoryContainerItem.getContainerInventory(ItemStack)` down) is `ItemStack`-only
  with no registry context reaching it (a pre-existing shape problem, already marked
  "TODO: Re-implement if a Holding solution is found", unused by any concrete item in
  the family) — used `RegistryAccess.EMPTY` as a stand-in rather than redesigning
  dead/TODO code, with a comment explaining why.
- `EmptyItemStorage`/`NullFluidStorage`/`EmptyFluidStorage`: matching no-op updates.
- `IInventoryContainerItem#getOrCreateInvTag`: `ItemStack#getOrCreateTagElement` is gone
  — rebuilt against `DataComponents.CUSTOM_DATA` (returns a copy now, not a
  live-mutable reference — fine, its only caller only reads it).
- `IFilter`/`EmptyFilter`/`BaseFluidFilter`/`BaseItemFilter`/`IFilterFactory`/
  `FluidFilter`/`ItemFilter`/`FilterRegistry`: `read`/`write`/`serializeNBT`/
  `deserializeNBT`/`createFilter`/`getFilter` all gained a `Provider` param, threaded
  end to end. `BaseFluidFilter`/`BaseItemFilter`'s `read`/`write` also switched
  `FluidStack.loadFluidStackFromNBT`/`writeToNBT` and `ItemStack.of`/`save` to
  `parseOptional(Provider, tag)`/`save(Provider)`.
- `ItemFilterMenu`/`FluidFilterMenu`: `filterStack.getOrCreateTag()` is gone — switched
  to `CustomData.update(DataComponents.CUSTOM_DATA, filterStack, tag -> filter.write(...))`.
- `FluidFilterMenu` also had an unrelated break: `FriendlyByteBuf`'s
  `writeFluidStack`/`readFluidStack` extension methods were deleted entirely (`FluidStack`'s
  only wire format now is `STREAM_CODEC`/`OPTIONAL_STREAM_CODEC`, both needing a
  `RegistryFriendlyByteBuf`) — this GUI-sync packet is a plain scratch `FriendlyByteBuf`
  with no registry context (`ContainerGuiPacket`'s `new FriendlyByteBuf(Unpooled.buffer())`),
  and widening that shared buffer type is its own large, separate change
  (`IPacketHandlerTile`'s config/control/gui/redstone packets go far beyond this one
  menu). Hand-encoded fluid id + amount instead
  (`BuiltInRegistries.FLUID.getKey`/`get` + `writeResourceLocation`/`writeVarInt`) since
  this packet only syncs what the client renders.

CofhCore: 465/132 → **442 errors / 123 files**.

## `FluidStack(FluidStack, int)` → `copyWithAmount(int)`

The "copy with a different amount" constructor was removed upstream in favor of an
explicit `copyWithAmount(int)` (same shape as `ItemStack`'s copy-with-count helpers).
Fixed every call site: `FluidHelper` (4 sites: `extractFromAdjacent` x2,
`insertIntoAdjacent`, `fillItemFromHandler` x2), `FluidStorageCoFH#fill`/`#drain`,
`FluidStorageRestrictable#fill`, `ModelUtils.FluidCacheWrapper`'s constructor.

CofhCore: 442/123 → **433 errors / 120 files**.

## `AttributeModifier.Operation` renames

`ADDITION`/`MULTIPLY_BASE`/`MULTIPLY_TOTAL` → `ADD_VALUE`/`ADD_MULTIPLIED_BASE`/
`ADD_MULTIPLIED_TOTAL`. Fixed `CoreMobEffects`'s three `addAttributeModifier` calls
(CHILLED/SHOCKED/SUNDERED); the only other matches in the repo are commented-out code.

CofhCore: 433/120 → **428 errors / 119 files**.

## Tool item family: `(Tier, int/float, float, Properties)` ctors removed

`DiggerItem`'s four subclasses (Pickaxe/Axe/Hoe/ShovelItem) and `SwordItem` all dropped
their attack-damage/attack-speed constructor params — combat stats moved to the
`ItemAttributeModifiers` data component, built via each class's own static
`createAttributes(Tier, ...)` helper. Kept CoFH's own constructor shape (callers still
pass attackDamage/attackSpeed) and routed them into `Properties#attributes(...)` instead
of passing straight to `super(...)`. One shared root cause across all five `*ItemCoFH`
classes (same pattern as `ArmorItemCoFH` earlier).

`Tier` lost `getLevel()` (the old numeric harvest level) entirely — mining eligibility is
purely tag-based now via `getIncorrectBlocksForDrops()` (`TagKey<Block>`, a new required
constructor param with no old equivalent to derive it from). `ItemTierCoFH`:
implemented `getIncorrectBlocksForDrops()`; kept `getLevel()` as a plain (non-override)
method since `FishingRodItemCoFH`'s luck modifier still derives from it for CoFH-authored
tiers — instanceof-checked with a 0 (old WOOD-tier) fallback for any non-CoFH `Tier`.

`ItemStack#hurtAndBreak`'s third param is now the `EquipmentSlot` to broadcast the break
event for directly, not a callback lambda (`LivingEntity#broadcastBreakEvent(hand)` is
gone too) — fixed every call site (`FishingRodItemCoFH` x2, `ArcheryBowItemWrapper`,
`GunpowderBlock`) using `LivingEntity.getSlotForHand(hand)`. **`CrossbowItemCoFH` is NOT
fixed** — needs its own pass (see Outstanding below).

Vanilla dispensing was rebuilt around the `ProjectileItem` interface
(`asProjectile(Level, Position, ItemStack, Direction)` +
`DispenserBlock.registerProjectileBehavior(item)`), replacing the old per-item anonymous
`(Abstract)ProjectileDispenseBehavior` subclass pattern:
- `ArrowItemCoFH`: removed its custom `DISPENSER_BEHAVIOR`, overrode `asProjectile`
  directly (`ArrowItem` already implements `ProjectileItem`).
- `KnifeItem`: now implements `ProjectileItem` itself; `createDispenseConfig()` rebuilt
  from `ProjectileItem.super.createDispenseConfig()` to preserve the old 3.0F dispense
  uncertainty without hardcoding power/position defaults.
- `ArrowItemCoFH#isInfinite`'s third param type was `Player`; upstream's is
  `LivingEntity` — narrowed override, fixed alongside `Enchantments.INFINITY_ARROWS` →
  `INFINITY`.

CofhCore: 428/119 → **408 errors / 109 files**.

## `Block#use` split (`useWithoutItem`/`useItemOn`)

`Block#use(BlockState, Level, BlockPos, Player, InteractionHand, BlockHitResult)` split
into `useWithoutItem(BlockState, Level, BlockPos, Player, BlockHitResult)` (no
hand/stack) and `useItemOn(ItemStack, BlockState, Level, BlockPos, Player,
InteractionHand, BlockHitResult)` (returns `ItemInteractionResult`). `useItemOn` is
tried first when the player holds an item; returning
`PASS_TO_DEFAULT_BLOCK_INTERACTION` falls back to `useWithoutItem`. Confirmed via
vanilla's own `CakeBlock` (which declares both) as the reference.

Six files share one root cause — a wrench check against the held item — and move
wholesale to `useItemOn` (`RailBlockCoFH`, `DetectorRailBlockCoFH`,
`PoweredRailBlockCoFH`, `CrossoverRailBlock`, `DirectionalBlock4Way`,
`DirectionalBlock6Way`). The rest needed individual judgment:
- `EntityBlockCoFH`: mixed item-dependent (wrench) and item-independent (security
  check, GUI open, activation delegate) logic — kept it all in `useItemOn`, with
  `useWithoutItem` delegating in via `ItemStack.EMPTY` + `MAIN_HAND` (the wrench check
  naturally no-ops on an empty stack).
- `CakeBlockCoFH`: item-independent, but the old code branched on the held stack's
  emptiness for the client-side return code — implemented both methods, mirroring
  vanilla `CakeBlock`'s own shape.
- `FeastBlock`: same shape as `CakeBlockCoFH`, but its own `use()` override
  unconditionally shadowed its parent `DirectionalBlock4Way`'s wrench-rotate — kept
  `useItemOn` fully shadowing rather than calling `super`, preserving that (probably
  accidental) behavior rather than silently fixing it mid-port.
- `CropBlockCoFH`: purely item-independent — `useWithoutItem` only; the old
  `MAIN_HAND`-only restriction has no equivalent (`useWithoutItem` carries no hand at
  all) and was dropped, documented inline.
- `GunpowderBlock`: purely item-dependent (flint & steel / fire charge check) —
  `useItemOn` wholesale, `super.use(...)` → `super.useItemOn(...)`.

`CakeBlockCoFH`/`FeastBlock`/`CropBlockCoFH` each have other, unrelated errors now
visible in the same files (`FoodProperties#getNutrition`/`getSaturationModifier`/
`getEffects`, `CommonHooks.onCropsGrowPre`/`Post`, `Enchantments.BLOCK_FORTUNE`, one more
"does not override" in `FeastBlock`) — not touched here, next category (see TODO).

CofhCore: 408/109 → **396 errors / 101 files**.

## Outstanding — see [TODO.md](TODO.md) for the live, current list

The categories below were identified but not yet started as of the last commit
(`2db3521`); confirm counts against a fresh `./gradlew compileJava` before resuming,
they'll have shifted:

- `FoodProperties#getNutrition`/`getSaturationModifier`/`getEffects` — signature/shape
  changed upstream (`CakeBlockCoFH`, `FeastBlock`).
- `CommonHooks.onCropsGrowPre`/`onCropsGrowPost` — signature changed (`CropBlockCoFH`,
  `CropBlockTall`, `CropBlockMushroom`).
- `Enchantments.BLOCK_FORTUNE` — constant relocated/renamed (`CropBlockCoFH`,
  `CropBlockTall`).
- `FeastBlock`: one more "does not override" error, cause not yet triaged.
- `CrossbowItemCoFH`: the whole ammo-loading system (`isLoaded`/`setLoaded`/`loadAmmo`/
  `getLoadedAmmo`, built on `CrossbowItem.setCharged(ItemStack, boolean)` + a raw
  `"AMMO"` NBT tag) is incompatible with vanilla's `ChargedProjectiles` data component
  rewrite — `setCharged` doesn't exist in that shape anymore. Comparable in size to the
  enchantment-system rewrite; deliberately left alone rather than half-fixed.
