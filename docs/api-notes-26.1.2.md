# NeoForge 26.1.2 API notes (CoFHCore, Phase B)

Successor to [api-notes-1.21.1.md](api-notes-1.21.1.md). Same rule as that file: **every shape here
was confirmed against the real 26.1.2.109 mapped jar**, not against a primer, a doc page, or
recollection. Entries are in the port plan's Phase B category order.

Shape oracles, in precedence order (port plan §6):

| What | Where |
|---|---|
| A mod already built and running on 26.1.2.109 | `../Pyronetics` source + [its `docs/api-notes-26.1.2.md`](../../Pyronetics/docs/api-notes-26.1.2.md) — read this first |
| Vanilla + NeoForge patches, as source | `build/moddev/artifacts/minecraft-patched-26.1.2.109-sources.jar` (`unzip -p … net/minecraft/…/X.java`) |
| NeoForge's own classes, as source | `~/.gradle/caches/modules-2/files-2.1/net.neoforged/neoforge/26.1.2.109/*/neoforge-26.1.2.109-sources.jar` |
| Signatures only | `javap -p -cp build/moddev/artifacts/minecraft-patched-26.1.2.109.jar <fqcn>` |
| Migration primers | `docs/reference/primers/26.1.md` (and 1.21.2 … 1.21.11 for the path) |
| NeoForge docs (current site = 26.1) | `docs/reference/docs-26.1/**` |

Note the artifact naming differs from Phase A: 26.1's is `minecraft-patched-<neo_version>-sources.jar`,
1.21.1's was `neoforge-<neo_version>-sources.jar`.

---

## B.0 Build

`gradle.properties` for this hop:

```
java_version=25
mc_version=26.1.2
minecraft_version_range=[26.1.2]
neo_version=26.1.2.109
jei_version=29.40.0.101          # artifact prefix becomes jei-26.1.2-…
curios_version=15.0.0+26.1.2
```

`neoforge.mods.toml` needed no edit — it already interpolates `${mc_version}`/`${neo_version}` from
`gradle.properties` through `processResources`. `build.gradle` needed none either: the toolchain
reads `java_version`, and there was no Parchment block to remove. Temurin 25.0.4 is installed, so
foojay provisions nothing.

**`createMinecraftArtifacts` fails before `compileJava` ever runs** when
`validateAccessTransformers = true` and any AT line has a dead target — so the AT sweep (B.9's
second half) is not optional cleanup at the end of the hop, it is the **first** thing Phase B
forces you to do. See below.

---

## B.9 (forced early) Access transformers

21 of the 132 AT lines had dead targets on 26.1.2. The validator prints the file and line for each,
which makes this mechanical. Three outcomes, all confirmed in the sources jar:

**Moved class** — retarget the package:

| Was | Now |
|---|---|
| `net.minecraft.client.particle.ParticleEngine` `spriteSets` | `net.minecraft.client.particle.ParticleResources` `spriteSets` |
| `net.minecraft.world.entity.projectile.AbstractArrow` (`*`, `shouldFall()Z`, `startFalling()V`) | `net.minecraft.world.entity.projectile.arrow.AbstractArrow` |

**Changed signature** — retarget the descriptor:

| Member | 1.21.1 | 26.1.2 |
|---|---|---|
| `LevelRenderer#renderHitOutline` | `(PoseStack, VertexConsumer, Entity, DDD, BlockPos, BlockState)V` | `(PoseStack, VertexConsumer, DDD, BlockOutlineRenderState, int, float)V` — and `BlockOutlineRenderState` lives in `net.minecraft.client.renderer.state.level`, not `…renderer.blockentity` |
| `Projectile#checkLeftOwner` | `()Z` | `()V` |
| `Biome#getTemperature` | `(BlockPos)F` | `(BlockPos, int seaLevel)F` |
| `BlockHitResult#<init>` | `(Z, Vec3, Direction, BlockPos, Z)V` | `(Z, Vec3, Direction, BlockPos, Z, Z)V` — gained `worldBorderHit` |
| `AbstractCookingRecipe` `result` field | field on `AbstractCookingRecipe` | `protected ItemStackTemplate result()` on **`SingleItemRecipe`**; AT the accessor, and every read becomes a call |

**Gone outright** — the line is deleted (left in place as a `# REMOVED 26.1.2:` comment so the next
hop can see what was dropped and why):

- `ChatComponent#addMessage(…)` both overloads — the one surviving `addMessage` is private with a
  new `GuiMessageSource` parameter; the public entry points are now `addClientSystemMessage`,
  `addServerSystemMessage`, `addPlayerMessage`.
- `LevelRenderer` `capturedFrustum`, `frustumPos`, and both `addParticleInternal` overloads.
- `BlockEntityWithoutLevelRenderer` — **the whole class is deleted** (`tridentModel`,
  `entityModelSet`). Item-with-BE rendering is `RegisterSpecialModelRendererEvent` now (B.7b).
- `RenderStateShard` and `RenderStateShard$LineStateShard` — deleted; a render type is built from
  `net.minecraft.client.renderer.rendertype.RenderSetup` over a `RenderPipeline` (B.7g).
- `LivingEntity#onEffectRemoved(MobEffectInstance)` and the `ServerPlayer` override — the method no
  longer exists; NeoForge routes it through `EventHooks.onEffectRemoved`, called from
  `LivingEntity` at `:987`/`:1083`. `onEffectAdded`/`onEffectUpdated` are unchanged.

`net.minecraft.client.renderer.rendertype` is the new home of `RenderType`, `RenderTypes`,
`RenderSetup`, `LayeringTransform`, `OutputTarget`, `TextureTransform`.

---

## B.1 Mechanical renames

All confirmed in `minecraft-patched-26.1.2.109-sources.jar`. 2445 → 1537 errors across B.1 and its
stragglers.

| 1.21.1 | 26.1.2 | Note |
|---|---|---|
| `net.minecraft.resources.ResourceLocation` | `net.minecraft.resources.Identifier` | Every static factory kept (`fromNamespaceAndPath`, `parse`, `withDefaultNamespace`, `tryParse`) — 481 sites in 95 files were a pure rename |
| `FriendlyByteBuf#readResourceLocation/writeResourceLocation` | `readIdentifier/writeIdentifier` | |
| `ResourceKey#location()` | `ResourceKey#identifier()` | **`TagKey#location()` is unchanged** — only `ResourceKey`'s was renamed; a blind `.location()` → `.identifier()` sweep breaks tag code |
| `net.minecraft.Util` | `net.minecraft.util.Util` | |
| `net.minecraft.advancements.critereon.*` | `net.minecraft.advancements.criterion.*` | spelling fix |
| `world.entity.projectile.AbstractArrow` | `world.entity.projectile.arrow.AbstractArrow` | |
| `world.entity.projectile.ThrowableItemProjectile` | `world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile` | |
| `world.entity.vehicle.AbstractMinecart` | `world.entity.vehicle.minecart.AbstractMinecart` | |
| `world.entity.vehicle.Boat` / `ChestBoat` | `world.entity.vehicle.boat.Boat` / `ChestBoat` | |
| `client.model.BoatModel` | `client.model.object.boat.BoatModel` | |
| `neoforge.client.model.data.ModelData/ModelProperty` | `neoforge.model.data.ModelData/ModelProperty` | |
| `Level#isClientSide` (field) | `Level#isClientSide()` | **Receivers other than `level` are easy to miss** — the first sweep missed five `tile.world().isClientSide` reads |
| `Level#random` (public field) | `Level#getRandom()` | the field is `protected` now |
| `FMLEnvironment.dist` / `.production` | `FMLEnvironment.getDist()` / `isProduction()` | |
| `RegistryAccess#registryOrThrow` / `Registry#getHolderOrThrow` | `lookupOrThrow` / `getOrThrow` | |

### `InteractionResult` (collapsed in 1.21.2)

`InteractionResultHolder<ItemStack>` and `ItemInteractionResult` are both gone; everything returns
`InteractionResult`.

| Was | Now |
|---|---|
| `InteractionResultHolder.pass/fail/consume/success(stack)` | `InteractionResult.PASS/FAIL/CONSUME/SUCCESS` |
| `InteractionResult.sidedSuccess(level.isClientSide())` | `InteractionResult.SUCCESS` |
| `ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION` | `InteractionResult.TRY_WITH_EMPTY_HAND` |

The sealed interface's constants are `SUCCESS` (client swing), `SUCCESS_SERVER` (server swing),
`CONSUME` (no swing) — all three `InteractionResult.Success` — plus `FAIL`, `PASS` and
`TRY_WITH_EMPTY_HAND`.

Every CoFHCore site returned the held stack unchanged, so none needed
`InteractionResult.SUCCESS.heldItemTransformedTo(stack)` — ThermalCore's bucket-like items will.
`useItemOn`'s parameter list is unchanged; only its return type moved.

---

## B.2 Registration

### Blocks and items need their id before construction

`BlockBehaviour.Properties` and `Item.Properties` both require `setId(ResourceKey)` before the
`Block`/`Item` constructor runs. The registration therefore has to see its own id, and
`DeferredRegisterCoFH` gained the overload NeoForge's own `DeferredRegister` already has:

```java
public synchronized <I extends T> DeferredHolder<T, I> register(String name, Function<Identifier, ? extends I> func)
```

Call shape (`CoreBlocks`):

```java
BLOCKS.register(ID_GLOSSED_MAGMA, id -> new GlossedMagmaBlock(ofFullCopy(Blocks.MAGMA_BLOCK)
        .lightLevel(lightValue(6)).setId(ResourceKey.create(Registries.BLOCK, id))));
```

Items are the same with `Registries.ITEM`. CoFHCore registers only five blocks and no plain items
this way; the overload exists for ThermalCore/TD/TE (B.10), which register hundreds through
`RegistrationHelper`.

### Block entity and entity types

| 1.21.1 | 26.1.2 |
|---|---|
| `BlockEntityType.Builder.of(Ctor::new, blocks…).build(null)` | `new BlockEntityType<>(Ctor::new, blocks…)` — the builder is gone; NeoForge makes the constructor public |
| `EntityType.Builder#build(String)` | `build(ResourceKey<EntityType<?>>)` — so entity registration takes the `Function<Identifier, …>` overload too: `id -> …build(ResourceKey.create(Registries.ENTITY_TYPE, id))` |

### `DirectionProperty` is deleted (1.21.2)

Vanilla's `FACING`/`HORIZONTAL_FACING` are plain `EnumProperty<Direction>` now:

```java
EnumProperty.create("facing", Direction.class, Direction.Plane.HORIZONTAL)
```

### Key mapping categories

A category is `KeyMapping.Category`, a record over an `Identifier`, not a free string.

- **Build it with `new KeyMapping.Category(id)`**, not the deprecated
  `KeyMapping.Category.register(Identifier)`. That one registers into a static set and **throws on a
  duplicate id**. The port plan suggested `register`; the constructor is what works.
- Register it with `RegisterKeyMappingsEvent#registerCategory(category)`, which is what puts it in
  the controls screen's sort order.
- Its label is `id.toLanguageKey("key.category")`: CoFH's category `cofh_core:cofh` reads
  `key.category.cofh_core.cofh`, so en_us.json needs that key. The old category was a bare `"CoFH"`
  string with no lang entry.

### Deferred to later categories

`CoreRecipeSerializers` (the `RecipeSerializer` record and the `RecipeType` `minecraft:` prefix) is
B.6, and `CoreShaders` is B.7g. Both still fail to compile after B.2.

---

## B.3 Persistence

**Decision (Joel, 2026-09-22): bridge at the edge, don't convert the chain.** CoFH's controls,
storages, filters and augments all `read`/`write` a `CompoundTag`, and they keep doing so. Only
the vanilla hooks change. This keeps the upstream diff small and the on-disk layout identical to
1.21.1's.

### Block entities: `BlockEntityCoFH` owns the bridge

```java
@SuppressWarnings ("deprecation")
@Override
protected final void loadAdditional(ValueInput input) {

    super.loadAdditional(input);
    loadAdditional(input.read(MapCodec.assumeMapUnsafe(CompoundTag.CODEC)).orElseGet(CompoundTag::new), input.lookup());
}

@Override
protected final void saveAdditional(ValueOutput output) {

    super.saveAdditional(output);
    CompoundTag nbt = new CompoundTag();
    saveAdditional(nbt, level != null ? level.registryAccess() : RegistryAccess.EMPTY);
    output.store(nbt);
}
```

- **Subclasses keep their 1.21.1 overrides unchanged**: `loadAdditional(CompoundTag,
  HolderLookup.Provider)` and `saveAdditional(CompoundTag, HolderLookup.Provider)` are now CoFH
  overloads. ThermalCore's 26 and TD's block entities need no edits if they extend
  `BlockEntityCoFH`; any that extend `BlockEntity` directly need the same bridge.
- `ValueOutputExtension#store(CompoundTag)` (NeoForge) writes a tag's entries at the output's
  root. Reading the whole root back needs `MapCodec.assumeMapUnsafe(CompoundTag.CODEC)`, which is
  deprecated; NeoForge's own `ValueInputExtension#keySet()` uses it the same way.
- `ValueOutput` has no `lookup()`. The registries for the save side come from
  `level.registryAccess()`, falling back to `RegistryAccess.EMPTY`, which is exactly what
  `BlockEntity#saveAdditional` itself does (`BlockEntity.java:117`).
- `getUpdateTag(HolderLookup.Provider)` / `saveWithoutMetadata(HolderLookup.Provider)` are
  unchanged. `onDataPacket`/`handleUpdateTag` default to `loadWithComponents(ValueInput)`, which
  reaches the bridge.

### `onRemove` → `BlockEntity#preRemoveSideEffects(BlockPos, BlockState oldState)`

`LevelChunk#setBlockState` (`:311`) calls it only when the block changes, on the server, just
before removing the block entity. That's the same guard `EntityBlockCoFH#onRemove` had
(`state.getBlock() != newState.getBlock()`). By then the chunk already holds the new state, so
`BlockEntityCoFH` passes `level.getBlockState(pos)` as `newState`, and
**`ITileCallback#onReplaced` keeps its signature**. The base implementation only drops `Container`
contents; no CoFH block entity is a `Container`, so it isn't called. `EntityBlockCoFH#onRemove` is
gone.

### Entities: converted natively (no common CoFH base)

| 1.21.1 | 26.1.2 |
|---|---|
| `readAdditionalSaveData(CompoundTag)` / `addAdditionalSaveData(CompoundTag)` | `(ValueInput input)` / `(ValueOutput output)` |
| `tag.getInt(k)` etc. | `input.getIntOr(k, 0)`, `getFloatOr`, … |
| `putUUID` / `hasUUID` / `getUUID` | `output.storeNullable(k, UUIDUtil.CODEC, uuid)` / `input.read(k, UUIDUtil.CODEC)`. Same int-array format |
| codec + `registryAccess().createSerializationContext(NbtOps.INSTANCE)` | `input.read(k, CODEC)` / `output.store(k, CODEC, v)`. The ValueIO already carries registry ops |
| `stack.save(registries)` / `ItemStack.parseOptional(registries, tag)` | `output.store(k, ItemStack.OPTIONAL_CODEC, stack)` / `input.read(k, ItemStack.OPTIONAL_CODEC)` |

### `SavedData`

`new SavedData.Factory<>(ctor, (nbt, registries) -> …)` and `computeIfAbsent(factory, "name")`
became `SavedDataType<T>(Identifier id, Supplier<T>, Codec<T>[, DataFixTypes])` and
`computeIfAbsent(TYPE)`. `SavedData#save` is no longer abstract. Minimal bridge:
`CompoundTag.CODEC.xmap(T::new, data -> data.save(new CompoundTag()))`.

**The file path comes from the id**: `id.withSuffix(".dat")` resolved under `data/`
(`SavedDataStorage#getDataFile`). `cofh:friends` is now `data/cofh/friends.dat`, where 1.21.1
wrote `data/cofh:friends.dat`, so **friend lists from a 1.21.1 world don't carry over**.
`ServerPlayer#serverLevel()` is gone; `ServerPlayer#level()` returns `ServerLevel`.

### `CompoundTag` / `ListTag` getters return `Optional`

`getInt(k)` → `getIntOr(k, 0)` (likewise `Byte`/`Short`/`Long`/`Float`/`Double`/`Boolean`/`String`
with the old absent-key default), `getCompound(k)` → `getCompoundOrEmpty(k)`,
`getList(k, TAG_X)` → `getListOrEmpty(k)` (no element-type argument), `ListTag#getCompound(i)` →
`getCompoundOrEmpty(i)`, `getByteArray(k)` → `Optional<byte[]>` (`.orElse(new byte[0])`),
`getAllKeys()` → `keySet()`, and `contains(k, TAG_X)` → `contains(k)`. The UUID helpers are gone.

**A leftover `Optional` getter can still compile** wherever an `Object` is accepted: in a ternary
or string concatenation it yields `"Optional[…]"` at runtime. javac reports the ternary case as
"bad type in conditional expression", not "incompatible types".

### Stacks ↔ `CompoundTag`

`ItemStack`/`FluidStack` `parseOptional(registries, tag)`, `save(registries[, prefix])` and
`saveOptional(registries)` are gone; only the codecs remain. CoFH wraps them as
`ItemHelper`/`FluidHelper` `parseOptional(provider, tag)` / `saveOptional(provider, stack)`:
`OPTIONAL_CODEC` through `provider.createSerializationContext(NbtOps.INSTANCE)`. Extra keys in
the tag (a slot index) are ignored on parse, as before, and an empty stack round-trips as `{}`.

### `INBTSerializable` is deleted

NeoForge 26.1.2 has only `ValueIOSerializable` (`serialize(ValueOutput)` /
`deserialize(ValueInput)`). Every `serializeNBT`/`deserializeNBT` caller in the family is CoFH or
TD code, so the interface is simply dropped from `IFilter`, `EnergyStorageCoFH` and `XpStorage`,
and the methods stay as plain CoFH API. TD's `Grid`, `GridNode`, `IAttachment`, `EnergyGridStorage`
and `FluidGridStorage` take the same treatment in B.10.

### authlib 7: `GameProfile` is a record

`getId()`/`getName()` → `id()`/`name()`, and **`equals` now compares `properties()` too**. A
logged-in player's profile carries textures while a stored or command-built one doesn't, so
`set.contains(player.getGameProfile())` silently fails. Compare
`new GameProfile(profile.id(), profile.name())` instead (`SocialUtils`).
`GameProfileArgument.getGameProfiles` returns `Collection<NameAndId>` (`record NameAndId(UUID id,
String name)`).

### B.1 stragglers found here

`Registry#get(Identifier)` returns `Optional<Holder.Reference<T>>`; the plain lookup is
`getValue(Identifier)` (8 sites).

---

## B.4 Transfer API

**Same principle as B.3: bridge at the edge.** NeoForge 26.1.2 keeps `IItemHandler`,
`IFluidHandler` and `IEnergyStorage`, deprecated for removal, but **capabilities speak only
`ResourceHandler<ItemResource>` / `ResourceHandler<FluidResource>` / `EnergyHandler`**. NeoForge
adapts only new → old (`IItemHandler.of`, `IFluidHandler.of`, `IEnergyStorage.of`; the adapters
are the package-private `*ResourceHandlerAdapter` / `EnergyHandlerAdapter`). There is no old →
new adapter, and a general one can't honour transactions. So:

- **CoFH internals keep the legacy interfaces.** Machines, ThermalCore and ThermalExpansion keep
  calling `insertItem`/`fill`/`receiveEnergy` on the storages. That produces removal warnings,
  not errors.
- **Exposing (CoFH → other mods):** CoFH's handlers *also* implement the new interfaces. The two
  sets of method names don't clash (`getSlots`/`insertItem` vs `size`/`insert`).
- **Consuming (other mods → CoFH):** query sites wrap the capability result with
  `IItemHandler.of` / `IFluidHandler.of` / `IEnergyStorage.of`.

### Transactions: one journal per storage, callbacks on root commit

`SnapshotJournal<T>`: `createSnapshot()`, `revertToSnapshot(T)`, `onRootCommit(T original)`, and
`updateSnapshots(TransactionContext)` before the first mutation at each depth. The pattern is
mutate immediately, revert on abort, the same as `StacksResourceHandler`.

- **The journal lives on the storage** (`ItemStorageCoFH`, `FluidStorageCoFH`, `EnergyStorageCoFH`,
  exposed as `updateSnapshots(tx)`), not on the handler. `ManagedItemInv` builds five handlers over
  the same slots, and `Transaction#close` walks `journalsToClose` **in registration order**. Two
  handler-level journals snapshotting one slot would revert out of order.
- Snapshots copy the stack (`item.copy()`, `fluid.copy()`), because the legacy paths mutate in
  place (`setCount`, `grow`). Revert assigns the field directly, bypassing setters and subclass
  side effects.
- **Change callbacks wait for root commit.** Each handler keeps a `BitSet` of touched indices in
  its own journal (reverted on abort) and calls `onInventoryChange`/`onTankChange` from
  `onRootCommit`. This matters: `MachineBlockEntity#onInventoryChanged` re-validates inputs and
  can `processOff()`, so a pipe that merely *simulates* pulling an input (nested transaction,
  aborted) would stop an active machine if the callback fired immediately.
- New-API calls go to the **storage directly**, not through the handler's legacy
  `insertItem`/`fill`, which fire their callbacks inline. Handler rules are protected hooks:
  `canInsert(index)`/`canExtract(index)` on `SimpleItemHandler`/`SimpleFluidHandler`, overridden by
  `ManagedItemHandler`/`ManagedFluidHandler` (inputs accept, outputs give, `restrict()` blocks input
  extraction) and by `IOItemHandler` (its allow suppliers). The legacy methods are unchanged.
- Fluid handlers were never per-tank in the legacy API (`fill` walks tanks, first match wins).
  The new API is per index, so each index maps to its tank.

### Surface for B.10

- `SimpleItemInv`/`IOItemInv`/`ManagedItemInv#getHandler` now return `SimpleItemHandler`, and
  `SimpleTankInv`/`ManagedTankInv#getHandler` return `SimpleFluidHandler`. Both still are the
  legacy types, so existing callers compile, and registration needs no cast.
- `FluidStorageCoFH` is itself a one-index `ResourceHandler<FluidResource>` (the fluid cell
  exposes it directly), and `EnergyStorageCoFH` is an `EnergyHandler`.
- `FluidHandlerRestrictionWrapper`/`EnergyHandlerRestrictionWrapper` take
  `<T extends IFluidHandler & ResourceHandler<FluidResource>>` (or the energy pair), so the energy
  and fluid cells' call sites compile unchanged and the wrapper holds a typed view of both.
- Registration (ThermalCore/TD/TE) becomes
  `event.registerBlockEntity(Capabilities.Item.BLOCK, type, (be, side) -> …)`, with
  `Capabilities.Fluid.BLOCK` and `Capabilities.Energy.BLOCK` likewise. `AugmentableBlockEntity`'s
  cached `IItemHandler itemCap` etc. need their field types narrowed to match.

### Query sites

| 1.21.1 | 26.1.2 |
|---|---|
| `Capabilities.ItemHandler/FluidHandler/EnergyStorage.BLOCK` | `Capabilities.Item/Fluid/Energy.BLOCK`, then `IItemHandler.of(h)` / `IFluidHandler.of(h)` / `IEnergyStorage.of(h)` |
| `stack.getCapability(Capabilities.FluidHandler.ITEM)` | `FluidUtil.getFluidHandler(stack).orElse(null)`. This is the deprecated `neoforge.fluids.FluidUtil`, which returns an `IFluidHandlerItem` whose `getContainer()` still reports a changed item (bucket → water bucket) |
| `stack.getCapability(Capabilities.EnergyStorage.ITEM)` | `ItemAccess.forStack(stack).getCapability(Capabilities.Energy.ITEM)`. **`forStack` throws on an empty stack**, and it mutates the stack in place but can never change its `Item` |
| charging a player's items | `ItemAccess.forPlayerSlot(player, i)` over `0 … inventory.getContainerSize()`, then `insert` inside `Transaction.openRoot()` … `commit()` |
| `FluidStack#isFluidEqual(other)` / `areFluidStackTagsEqual` | `FluidStack.isSameFluidSameComponents(a, b)` (it was already the deprecated alias) |
| `Slot.slot` | `getContainerSlot()` (the field is private again since B.0) |

Other 26.1 changes met here: `Inventory#items`/`armor`/`offhand` are private, with a single index
space (0–35 main, 36–39 armor, 40 offhand, 41 body armor). `ChunkAccess#setUnsaved(true)` is
`markUnsaved()`. **New-API `insert`/`extract` reject negative amounts** (`TransferPreconditions`).
`EnergyChargeMobEffect` had passed its negative drain amount straight through, which on the
legacy API *added* energy, so it now negates at the call site.

### Not covered (parity with 1.21.1)

No item capabilities (energy/fluid items, satchels) are registered on 1.21.1 either;
`EnergyCellBlockItem`'s registration is commented out. `EnergyContainerItemWrapper`,
`FluidContainerItemWrapper` and `InventoryContainerItemWrapper` still take an `ItemStack`, and
exposing CoFH items to other mods would need `ItemAccess`-based handlers. That's a feature gap,
not a regression.

**Owed verification**: nothing in B.4 has run. Once 26.1.2 compiles and boots, move items/fluids/energy
in and out of a machine with a pipe mod (or a GameTest), including an aborted simulation.

---

## B.5 Items, tools, armour

### Tools: `ToolMaterial` + `Item.Properties`

`SwordItem`, `DiggerItem`, `PickaxeItem`, `TieredItem` and `Tier` are gone.
`ToolMaterial(TagKey<Block> incorrectBlocksForDrops, int durability, float speed, float attackDamageBonus,
int enchantmentValue, TagKey<Item> repairItems)` is a record, and a tool is a plain `Item` built with
`Item.Properties#sword/pickaxe/axe/hoe/shovel(material, damage, speed)` or
`tool(material, TagKey<Block> minesEfficiently, damage, speed, float disableBlockingSeconds)`.
**`AxeItem`, `HoeItem` and `ShovelItem` survive** as `Item` subclasses taking
`(ToolMaterial, float, float, Properties)`, and they still carry stripping, tilling and pathing.

- **The tool properties overwrite durability** from the material (`applyCommonProperties`), so
  `builder.durability(n).pickaxe(...)` loses `n`. CoFH's hammer, excavator and sickle scale the
  *material* instead: `new ToolMaterial(…, material.durability() * 4, …)`.
- `canDisableShield` is gone. Shield-disabling is `Weapon(itemDamagePerAttack, disableBlockingSeconds)`,
  which `tool(...)` sets. The axe uses 5.0 s, and CoFH's hammer copies it.
- `Item#getDestroySpeed` is gone; mining speed is only the `TOOL` component's rules. To add a
  special case (the sickle's cobweb, speed 15), replace the component after `tool(...)` with
  `new Tool(List.of(Tool.Rule.overrideSpeed(HolderSet.direct(Blocks.COBWEB.builtInRegistryHolder()), 15.0F),
  Tool.Rule.deniesDrops(…incorrect…), Tool.Rule.minesAndDrops(…tag…, speed)), 1.0F, 1, true)`. Resolve
  tags with `BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK)`, as vanilla does.
- `ItemTierCoFH` is no longer a `Tier`. It holds a `ToolMaterial` (`getMaterial()`) plus CoFH's numeric
  `getLevel()`, and takes a repair **tag** rather than an `Ingredient` supplier.

### Armour: the equipment `ArmorMaterial` record

`ArmorItem`, `AnimalArmorItem` and the `ArmorMaterial` *registry* are gone.
`net.minecraft.world.item.equipment.ArmorMaterial(int durability, Map<ArmorType, Integer> defense,
int enchantmentValue, Holder<SoundEvent> equipSound, float toughness, float knockbackResistance,
TagKey<Item> repairIngredient, ResourceKey<EquipmentAsset> assetId)` is a plain record.
`Item.Properties#humanoidArmor(material, ArmorType)` sets durability, attributes, enchantability, repair
and the `EQUIPPABLE` component. `horseArmor(material)` and `wolfArmor(material)` do the same for animals.

- `ArmorItem.Type` → `ArmorType` (`HELMET, CHESTPLATE, LEGGINGS, BOOTS, BODY`, with `getSlot()` and
  `getDurability(int)`).
- `ArmorItemCoFH` keeps a `getType()` returning the `ArmorType`, because ThermalCore's armour calls
  `getType().getSlot()` throughout.
- `ArmorMaterialCoFH.create(durability, int[] defense, enchantability, equipSound, toughness, kbResist,
  TagKey<Item> repairItems, ResourceKey<EquipmentAsset> assetId)`. The defense array is in `ArmorType`
  order (helmet first), as upstream's was in `ArmorItem.Type` order.
- **B.8/B.10**: each material needs `assets/<ns>/equipment/<name>.json`, and its textures move to
  `textures/entity/equipment/humanoid[_leggings]/`. The repair ingredient must be an **item tag**.
- Dyeability is the `minecraft:dyeable` item tag plus the `DYED_COLOR` component, not a class, so the
  `Dyeable*` classes are now the same as their parents.

### `IItemExtension` / `Item` signatures

| 1.21.1 | 26.1.2 |
|---|---|
| `getCreatorModId(ItemStack)` | `getCreatorModId(HolderLookup.Provider registries, ItemStack)` |
| `getBurnTime(ItemStack, RecipeType<?>)` | `getBurnTime(ItemStack, @Nullable RecipeType<?>, FuelValues)` |
| `appendHoverText(ItemStack, TooltipContext, List<Component>, TooltipFlag)` | `appendHoverText(ItemStack, TooltipContext, TooltipDisplay, Consumer<Component>, TooltipFlag)` |
| `isEnchantable(ItemStack)`, `getEnchantmentValue(ItemStack)` | **gone**. `DataComponents.ENCHANTABLE`, set with `Properties#enchantable(n)`; `EnchantmentHelper` reads only the component. A per-stack value means `stack.set(ENCHANTABLE, …)` |
| `releaseUsing(…)` returns `void` | returns `boolean` (whether the release did something) |
| `getUseAnimation` → `UseAnim` | `ItemUseAnimation` |
| `getDescriptionId()` overridable, `getOrCreateDescriptionId()` | **final**. Use `Properties#useItemDescriptionPrefix()` / `useBlockDescriptionPrefix()` / `overrideDescription(id)` |
| `ItemCooldowns#addCooldown(Item, int)` | `addCooldown(ItemStack, int)` (cooldown group from the stack) |
| `LivingEntity.getSlotForHand(hand)` | `hand.asEquipmentSlot()` |
| `ExperienceOrb.value` | `getValue()` / `setValue(int)` |
| `RailShape#isAscending()` | `isSlope()` |
| `SignItem(Properties, Block, Block)` | `SignItem(Block sign, Block wallSign, Properties)` |
| `ChargedProjectiles.of(ItemStack)` / `getItems()` | `ofNonEmpty(List<ItemStack>)` (or `of(ItemStackTemplate)`) / `itemCopies()` |
| `Screen.hasShiftDown()` | `Minecraft.getInstance().hasShiftDown()` |
| NeoForge `DeferredSpawnEggItem(Supplier<EntityType>, bg, hl, props)` | vanilla `SpawnEggItem(props.spawnEgg(type))`. Entity types now register before items, so the supplier resolves at construction. The egg's tint colours are no longer vanilla's concern: eggs have individual textures since 1.21.5 (B.7) |

Enchantability had been set **after** construction (`setEnchantability(n)`, chained). Components are
fixed at construction, so CoFHCore's setters are removed and its container items pass
`builder.enchantable(5)`. B.10: ThermalCore's `setEnchantability` calls move into the properties, and its
augmentable items' **per-stack** scaling (`getEnchantmentValue` × augment modifier) must write the
`ENCHANTABLE` component when augments change.

### Entities, effects, events

| 1.21.1 | 26.1.2 |
|---|---|
| `Entity#hurt` → boolean, overridable | `final void hurt`. Override `hurtServer(ServerLevel, DamageSource, float)`, and call `hurtOrSimulate(src, amt)` when you need the result |
| `Entity#moveTo(...)` | `snapTo(...)` (same overloads) |
| `Entity#makeBoundingBox()` overridable | `final`. Override `makeBoundingBox(Vec3 position)` |
| `Entity#spawnAtLocation(stack[, y])` | `spawnAtLocation(ServerLevel, stack[, y])` |
| `Entity#causeFallDamage(float, …)` | `(double, float, DamageSource)` |
| `Entity#getCommandSenderWorld()` | `level()` |
| `entity.getType().is(TagKey)` | `entity.is(TagKey)` (`TypedInstance`) |
| `EntityType#create(Level)` | `create(Level, EntitySpawnReason)` |
| `walkDist`, `updateInWaterStateAndDoFluidPushing`, `checkInsideBlocks` | gone. Use `updateFluidInteraction()` and `applyEffectsFromBlocks()` |
| `Boat`/`ChestBoat(type, level)` | `(type, level, Supplier<Item> dropItem)`. Drops and pick result come from the supplier |
| `VehicleEntity#destroy(DamageSource)` | `destroy(ServerLevel, DamageSource)` |
| `AbstractMinecart#getMinecartType()`/`Type` | gone (`isRideable()`/`isFurnace()`); `activateMinecart(ServerLevel, x, y, z, powered)` |
| `ThrowableItemProjectile(type, x, y, z, level)` | takes a trailing `ItemStack` (or `super(type, level)` + `setPos`) |
| `PrimedTnt.owner` | `@Nullable EntityReference<LivingEntity>` via `EntityReference.of(entity)` |
| `GameRules` (`world.level`), `getBoolean(RULE_DOENTITYDROPS)` | `world.level.gamerules.GameRules`, `get(GameRules.ENTITY_DROPS)` |
| `LivingEntity#getArmorSlots/getAllSlots` | loop over `EquipmentSlot`s with `getItemBySlot` |
| `Inventory#items`/`armor`/`selected` | `getNonEquipmentItems()` (0–35) / `getItemBySlot(slot)` / `getSelectedSlot()` |
| `AbstractArrow#getBaseDamage()` | gone. Read the `baseDamage` field (AT) |
| `MobEffect#applyEffectTick(LivingEntity, int)` | `(ServerLevel, LivingEntity, int)`, **server only**, so client particles must be sent with `ServerLevel#sendParticles` |
| `MobEffect#applyInstantenousEffect(src, …)` | gains a leading `ServerLevel` |
| NeoForge `EffectCure` / `fillEffectCures` | **removed**. `removeAllEffects` (milk) clears everything |
| `LivingEntity#onEffectRemoved(instance)` | gone. Use `removeEffect(holder)`, which posts `MobEffectEvent.Remove` and calls `onEffectsRemoved` |
| `EntityTeleportEvent.ChorusFruit` | `EntityTeleportEvent.ItemConsumption` |
| `BlockEvent.BreakEvent` | `event.level.block.BreakBlockEvent` |
| `LivingShieldBlockEvent` cancellable | not cancellable. Check `getBlocked()` |
| `DiggerItem` / `*_DIG` abilities | gone. "Is a mining tool" means `stack.has(DataComponents.TOOL)` |
| `BlockTags.TALL_FLOWERS`, `MossBlock` | gone / `BonemealableFeaturePlacerBlock` |

### Blocks and fluids

| 1.21.1 | 26.1.2 |
|---|---|
| `neoforge.common.util.TriState` | `net.minecraft.util.TriState` (`canSustainPlant` unchanged) |
| `BushBlock` as "any plant" | `VegetationBlock`. `BushBlock` is now just the bush plant; `DeadBushBlock` → `DryVegetationBlock`, `FungusBlock` → `NetherFungusBlock` |
| `neighborChanged(…, BlockPos fromPos, boolean)` | `(…, @Nullable Orientation, boolean)`. **The neighbour's position is gone**, and `Orientation` is null outside experimental redstone |
| `getAnalogOutputSignal(state, level, pos)` | `(…, Direction)` |
| `getCloneItemStack(LevelReader, pos, state)` | `(…, boolean includeData)` |
| `Block#getDescriptionId()` | `final`. Override `getName()` |
| `updateShape(state, dir, nState, LevelAccessor, pos, nPos)` | `updateShape(state, LevelReader, ScheduledTickAccess, pos, dir, nPos, nState, RandomSource)` |
| `entityInside(state, level, pos, entity)` | `(…, InsideBlockEffectApplier, boolean isPrecise)` |
| `fallOn(…, float)` / `updateEntityAfterFallOn` | `fallOn(…, double)` / `updateEntityMovementAfterFallOn` |
| `onCaughtFire` → void, `wasExploded(Level, …)` | returns `boolean` (TNT only removes itself when `true`), `wasExploded(ServerLevel, …)` |
| `getMaxBuildHeight()` | `getMaxY()` (inclusive, one lower) |
| `IBaseRailBlockExtension#getRailMaxSpeed` | removed, no hook (`MinecartBehavior`) |
| `Equipable` | gone. Use the `EQUIPPABLE` component on the item |
| `FoodProperties.effects()` / `PossibleEffect` | gone. Effects live on the `Consumable` component |
| `ParticleTypes.INSTANT_EFFECT` | `SpellParticleOption.create(type, color, power)` |
| `PotionContents(potion, color, effects)` | gains `Optional<String> customName`. `Potion.getName(…)` → `PotionContents#getName(prefix)` |
| `DataComponents.HIDE_ADDITIONAL_TOOLTIP` | `TOOLTIP_DISPLAY` |

### Commands, network, menus, util

| 1.21.1 | 26.1.2 |
|---|---|
| `CommandSourceStack#hasPermission(int)` | `source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(n)))` (`server.permissions`). CoFH wraps it as `CoFHCommand.hasPermission(source, level)` |
| `Player#hasPermissions(4)` | `permissions().hasPermission(Permissions.COMMANDS_OWNER)` |
| `PacketDistributor.sendToServer` | `client.network.ClientPacketDistributor.sendToServer` |
| `ServerPlayer#serverLevel()` | `level()` |
| `FriendlyByteBuf#writeVec3/readVec3` | `Vec3.STREAM_CODEC` |
| `ClickType` | `ContainerInput` |
| `Style#withFont(Identifier)` | `withFont(new FontDescription.Resource(id))` |
| `Component.Serializer.toJson/fromJsonLenient` | `ComponentSerialization.CODEC` with `createSerializationContext(JsonOps.INSTANCE)` |
| `WeightedEntry.IntrusiveBase` | gone. `WeightedRandom` takes a weight `ToIntFunction` |
| `Registry#getOrCreateTag` / `getHolder(id)` | `getOrThrow(tag)`/`get(tag)` / `get(id)` → `Optional<Holder.Reference>` |
| `BlockHitResult(…)` | gains a trailing `worldBorderHit` |
| `Item#getCraftingRemainingItem(stack)` | NeoForge `getCraftingRemainder(ItemInstance)` → nullable `ItemStackTemplate` (`.create()`) |
| `BLOCK_ENTITY_DATA` as `CustomData` | `TypedEntityData<BlockEntityType<?>>`: `TypedEntityData.of(type, tag)` / `copyTagWithoutId()`. `BlockItem` applies it only when the type matches. CoFH's `ItemHelper.setBlockEntityData` infers the type (the stack's, else the first type valid for the item's block) |
| JEI `IIngredientSubtypeInterpreter#apply` → String | `ISubtypeInterpreter<T>#getSubtypeData(T, UidContext)` → Object (`null` = none); `IRecipeManagerPlugin` takes `IRecipeType<T>` |

## B.6 Recipes, loot functions, datagen

### `Recipe<T extends RecipeInput>` (1.21.2 "Recipe Changes", 26.1 serializer records)

| 1.21.1 | 26.1.2 |
|---|---|
| `assemble(input, HolderLookup.Provider)` | `assemble(input)` |
| `getResultItem(registries)`, `getIngredients()`, `canCraftInDimensions(w, h)`, `getToastSymbol()` | **gone**. There is no result accessor; `display()` (`List<RecipeDisplay>`, default empty) is what the recipe book and JEI see |
| — | **new abstract**: `showNotification()`, `group()`, `placementInfo()` (`PlacementInfo.NOT_PLACEABLE` for anything not placeable by the recipe book), `recipeBookCategory()` (`RecipeBookCategories.*`; only read while iterating `display()`, so any value works for a recipe with no display) |
| `getSerializer()` → `RecipeSerializer<?>` | `RecipeSerializer<? extends Recipe<T>>`; `getType()` → `RecipeType<? extends Recipe<T>>`. **B.10**: ThermalCore's overrides must narrow to these |
| `RecipeSerializer` interface with `codec()`/`streamCodec()` | `record RecipeSerializer<T>(MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec)`; register `() -> new RecipeSerializer<>(CODEC, STREAM_CODEC)` |
| `SimpleCraftingRecipeSerializer<>(Factory)` | gone. A `CustomRecipe` serializer is `new RecipeSerializer<>(MapCodec.unit(X::new), StreamCodec.unit(new X()))`, as vanilla's `RepairItemRecipe` does |
| `CustomRecipe(CraftingBookCategory)` | no-arg; `category()` is `MISC`, `isSpecial()` true, `placementInfo()` not placeable |
| `ShapedRecipe(group, category, pattern, ItemStack)` | `ShapedRecipe(Recipe.CommonInfo(showNotification), CraftingRecipe.CraftingBookInfo(category, group), ShapedRecipePattern, ItemStackTemplate)`; `MAP_CODEC` composes `CommonInfo.MAP_CODEC`, `CraftingBookInfo.MAP_CODEC`, `ShapedRecipePattern.MAP_CODEC` and `ItemStackTemplate.CODEC.fieldOf("result")`. `pattern` and `result` are public fields; `commonInfo`/`bookInfo` are protected on `NormalCraftingRecipe`, so a wrapper rebuilds them from `showNotification()`/`category()`/`group()` |
| `ItemStack` result | `ItemStackTemplate(Holder<Item>, count, DataComponentPatch)`: `create()` → validated `ItemStack`, `fromNonEmptyStack(stack)`, `apply(patch)` |

`ShapedPotionNBTRecipe` keeps wrapping a `ShapedRecipe` and delegates `placementInfo`/`display`/
`group`/`showNotification`; `assemble` starts from `wrappedRecipe.assemble(input)`. `SerializableRecipe`
(the shim base for Thermal's machine recipes) answers the new methods with `false`/`""`/`NOT_PLACEABLE`/
`CRAFTING_MISC`.

### `Ingredient`

- `Ingredient.EMPTY` is gone and **an `Ingredient` cannot be empty**: the constructor throws on an empty
  direct `HolderSet` (and on air). Vanilla models absence as `Optional<Ingredient>`. CoFH's lenient JSON
  parser needs a matches-nothing value, so `EmptyIngredient` (`cofh_core:empty`, an `ICustomIngredient`
  with no items and `SlotDisplay.Empty`) provides `EmptyIngredient.EMPTY`. Custom ingredients sync by
  type over NeoForge connections (`IngredientCodecs`), so an empty one survives the trip.
- `Ingredient.of(TagKey)` is gone: `Ingredient.of(HolderSet<Item>)`. At runtime a stack test is just
  `stack.is(tag)` (`SecureRecipe`); in datagen use `items.getOrThrow(tag)` (the `HolderGetter<Item>`
  field on `RecipeProvider`), which is what `ShapedRecipeBuilder.define(char, TagKey)` does.
- `Ingredient#getItems()` (`ItemStack[]`) → `items()` (`Stream<Holder<Item>>`, deprecated) and
  `getValues()` (`HolderSet`, throws for custom ingredients). `ICustomIngredient#getItems()` →
  `items()` returning holders; `display()` is the hook for showing stacks with a count:
  `new SlotDisplay.Composite(items().<SlotDisplay>map(h -> new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(h, count))).toList())`.
- `IngredientWithCount` stays as CoFH's counted ingredient (`cofh_core:with_count`). NeoForge's
  equivalent is `SizedIngredient(Ingredient, int)` with `NESTED_CODEC` (`{"ingredient": …, "count": n}`).
- `TagParser.parseTag(String)` → `TagParser.parseCompoundFully(String)`.

### Loot functions

| 1.21.1 | 26.1.2 |
|---|---|
| `LootItemFunctionType` registered in `BuiltInRegistries.LOOT_FUNCTION_TYPE`; `getType()` | the registry holds `MapCodec<? extends LootItemFunction>` directly; implement `MapCodec<X> codec()` |
| `LootContext#getParamOrNull(key)` | `getOptionalParameter(ContextKey<T>)`; `getParameter`/`hasParameter` |
| `CopyNameFunction.copyName(NameSource.BLOCK_ENTITY)` | `copyName(LootContext.BlockEntityTarget.BLOCK_ENTITY)` (a `LootContextArg`) |
| `CopyCustomDataFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)` | the block-entity `NbtProvider` has no public factory (only `forContextEntity`); build it from `ContextNbtProvider.INLINE_CODEC.parse(JavaOps.INSTANCE, "block_entity")`. Vanilla's own tile drops use `CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY).include(...)` instead |

### Datagen

- `GatherDataEvent` is abstract; subscribe to **`GatherDataEvent.Client`** (all data, client and
  server, in one run — `clientData()` in `build.gradle`). `includeServer()`/`includeClient()`,
  `getExistingFileHelper()` and `ExistingFileHelper` are gone. Providers are added with
  `event.createProvider(Ctor::new)` (`(PackOutput)` or `(PackOutput, CompletableFuture<Provider>)`)
  and block+item tags with `event.createBlockAndItemTags(Block::new, Item::new)`, where the item
  provider's factory takes `(PackOutput, lookup, CompletableFuture<TagLookup<Block>>)`.
- `@EventBusSubscriber` has no `bus` parameter; the bus is inferred from the event type.
- Tag providers drop the trailing `ExistingFileHelper`: NeoForge `BlockTagsProvider(output, lookup, modId)`,
  `BlockTagCopyingItemTagProvider(output, lookup, blockTags, modId)` (has `copy(blockTag, itemTag)`;
  vanilla's `ItemTagsProvider` is gone), vanilla `FluidTagsProvider(output, lookup, modId)` and
  `DamageTypeTagsProvider(output, lookup, modId)`. `tag(key).add(T...)` and `addTags(...)` are unchanged.
- `RecipeProvider` is no longer a `DataProvider`. Constructor `(HolderLookup.Provider registries,
  RecipeOutput output)`, fields `registries`, `items` (`HolderGetter<Item>`), `output`; `buildRecipes()`
  takes no argument, `has(...)` is an instance method. A mod registers a `RecipeProvider.Runner`
  (`(PackOutput, CompletableFuture<Provider>)`, `createRecipeProvider(registries, output)`, `getName()`).
  `RecipeProviderCoFH` keeps its `RecipeOutput consumer` helper parameters, so **B.10**: TC/TE's
  providers pass `this.output` (or the argument) unchanged and gain a `Runner`. `IConditionBuilder`
  is gone (nothing used it); conditions go through `RecipeOutput#withConditions(...)`.
- Builders: `ShapedRecipeBuilder.shaped(HolderGetter<Item>, category, ItemLike[, count] | ItemStackTemplate)`,
  `ShapelessRecipeBuilder.shapeless(items, …)`, `SimpleCookingRecipeBuilder.smelting/blasting(ingredient,
  RecipeCategory, CookingBookCategory, result, xp, time)` (`smoking`/`campfireCooking` imply `FOOD`),
  `SingleItemRecipeBuilder.stonecutting(ingredient, category, result, count)`. `save(output, String id)`
  still exists but throws if the id equals the default one.
- NeoForge's `BlockStateProvider`/`ItemModelProvider` and `client.model.generators` are gone. Since
  every generated model is committed, `BlockStateProviderCoFH`, `ItemModelProviderCoFH` and the Core
  providers are deleted rather than rewritten on vanilla's `ModelProvider`. **B.10**: the same for
  `TCoreBlockStateProvider`/`TCoreItemModelProvider`, `TDynItemModelProvider`, `TExpBlockStateProvider`/
  `TExpItemModelProvider`.
