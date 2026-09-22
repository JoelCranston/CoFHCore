# API notes — 1.20.4 → 1.21.1 (NeoForge 21.1.251)

Every shape below was confirmed against the real mapped jar before it was written:

```bash
unzip -p build/moddev/artifacts/neoforge-21.1.251-sources.jar net/minecraft/<path>.java
javap -cp build/moddev/artifacts/neoforge-21.1.251.jar -p <fqcn>
```

Organised by category, in the order of `docs/port-plan.md` §5 A.1. Anything here applies to all
four repos unless it says otherwise.

---

## 1. Mod metadata and the event bus

| Was | Is |
|---|---|
| `@Mod.EventBusSubscriber` | `@EventBusSubscriber` — top-level, `net.neoforged.fml.common` |
| `Mod.EventBusSubscriber.Bus.FORGE` | `EventBusSubscriber.Bus.GAME` |
| `FMLJavaModLoadingContext` | gone; the mod constructor receives `ModContainer` + `IEventBus` |
| `ModLoadingContext.get().registerConfig(...)` | `ModContainer#registerConfig(...)` |
| `META-INF/mods.toml` | `META-INF/neoforge.mods.toml` (a jar without it is **silently skipped**) |
| `required = true` in a dependency block | `type = "required"` |

**`ModConfigEvent` must not be handled as one event.** `ModConfigEvent.Unloading` fires on
shutdown with the spec already detached, so reading any value throws *"Cannot get config value
before config is loaded"* — which takes the server down on exit. Subscribe to `Loading` and
`Reloading` separately.

`ILoadedConfig` is sealed to FML, so the old hand-rolled eager config load cannot be reproduced;
FML loads registered configs before the lifecycle events anyway.

## 2. `ResourceLocation`

Constructors are private: `ResourceLocation.fromNamespaceAndPath(ns, path)`,
`ResourceLocation.parse(s)`, `ResourceLocation.withDefaultNamespace(path)`.

## 3. ItemStack NBT → data components

Raw stack NBT is gone. The mod-attached blob is `DataComponents.CUSTOM_DATA`; a placed block
entity's data is `DataComponents.BLOCK_ENTITY_DATA`.

**Both hand back a copy.** Every site that used to mutate a live tag has to read, mutate and
store back. `ItemHelper` carries the family's accessors:

```java
ItemHelper.getCustomData(stack)                  // was getTag()/getOrCreateTag(), a copy
ItemHelper.hasCustomData(stack)                  // was hasTag()
ItemHelper.setCustomData(stack, tag)             // null/empty removes the component
ItemHelper.mutateCustomData(stack, tag -> ...)   // CustomData.update - use when it must be atomic
ItemHelper.getCustomSubTag(stack, key)           // was getTagElement(key)
ItemHelper.setCustomSubTag(stack, key, value)    // was addTagElement(key, value)
ItemHelper.getBlockEntityData(stack)             // was getTagElement(TAG_BLOCK_ENTITY)
ItemHelper.setBlockEntityData(stack, tag)
```

`CustomData.update` removes the component when the mutator leaves the tag empty, so the old
`stack.setTag(null)` cleanup collapses to nothing.

Other component moves: `setHoverName`/`hasCustomHoverName` → `DataComponents.CUSTOM_NAME`;
`Item#getRarity` is gone (`DataComponents.RARITY`); `Item#isEdible` → `stack.has(DataComponents.FOOD)`;
`DyeableLeatherItem` was deleted outright — a dyed item is one carrying `DataComponents.DYED_COLOR`.

## 4. Vertex and rendering

`vertex(…)` → `addVertex(…)`, `color` → `setColor`, `uv` → `setUv`, `overlayCoords` → `setOverlay`,
two-arg `uv2` → `setUv2` but **single-arg `uv2(packedLight)` → `setLight(packedLight)`**,
`normal` → `setNormal`, `endVertex()` deleted.

Three traps this sweep fell into and had to be reverted:

- **`PoseStack.Pose#normal()` is a matrix accessor, not a vertex setter** — `.last().normal()` stays.
- `FluidHelper.color(fluid)` is an unrelated helper; a blind `.color(` → `.setColor(` renames it.
- Single-argument `uv2` is `setLight`, not `setUv2`.

`BufferBuilder#end()` → `buildOrThrow()` returning `MeshData`; `Tesselator#begin(mode, format)`
returns the `BufferBuilder`; `BufferUploader.drawWithShader(meshData)`. A `BufferBuilder` can no
longer be re-begun — build one per batch around a reusable `ByteBufferBuilder`
(see `BasicBufferSource`).

`ParticleRenderType#begin(Tesselator, TextureManager)` returns the batch's `BufferBuilder` and
`end()` is gone — `ParticleEngine` uploads the mesh and restores depthMask/blend itself.

`Model#renderToBuffer(PoseStack, VertexConsumer, int light, int overlay, int argb)` — a packed
ARGB tint, not four floats.

## 5. Mob effects and potions

`MobEffects.*` are `Holder<MobEffect>`; `MobEffectInstance(Holder<MobEffect>, …)`. `PotionUtils`
is gone — potion data is `DataComponents.POTION_CONTENTS` (`PotionContents#getAllEffects`,
`potion()`, `getColor()`), and **`FluidStack` is a component holder too**, so a potion fluid's
contents read the same way. `Potions.*` are `Holder<Potion>` and `Potions.EMPTY` no longer exists.

`PotionColorCalculationEvent` was deleted in NeoForge 21.0; there is no global hook, only
per-effect `CustomParticleMobEffect#createParticleOptions`.

`PotionBrewing.POTION_MIXES` is gone. Brewing is a per-server `PotionBrewing`
(`MinecraftServer#potionBrewing()`) whose mixes are **not enumerable** — to discover them, mix
each potion against each item the instance reports via `isIngredient`.

## 6. Enchantments are datapack objects

`Enchantment` cannot be subclassed. Holding is `data/cofh_core/enchantment/holding.json` plus a
`ResourceKey<Enchantment>`; what it applies to is the item tag `cofh_core:enchantable/holding`
(mods add their items to that tag — there is no `addValidItem` call that can work against
data-driven `supported_items`).

```java
Utils.getItemEnchantmentLevel(ResourceKey<Enchantment>, ItemStack)   // reads the component
Utils.getLevel(ItemEnchantments, ResourceKey<Enchantment>)          // when you hold the component
Utils.getEnchantmentHolder(ResourceKey<Enchantment>)                // when vanilla insists on a Holder
```

`EnchantmentHelper.getEnchantments(stack)` → `stack.getEnchantments()` (`ItemEnchantments`, keyed
by `Holder`); `BuiltInRegistries.ENCHANTMENT` is gone (datapack registry — go through the running
registries); `Enchantment#isCurse()` is the `minecraft:curse` tag; an `EnchantmentInstance` takes
a `Holder`. An `Enchantment` no longer knows its own id, so a description id comes from the
holder's key: `Util.makeDescriptionId("enchantment", key.location())`.

Renames: `BLOCK_FORTUNE`→`FORTUNE`, `INFINITY_ARROWS`→`INFINITY`, `FALL_PROTECTION`→`FEATHER_FALLING`.
Aqua Affinity is now `Attributes.SUBMERGED_MINING_SPEED` (0.2 without, 1.0 with).

## 7. Attributes

`AttributeModifier(ResourceLocation id, double, Operation)` — no UUID, no name;
`Operation.ADDITION` → `ADD_VALUE`. `Constants`' `UUID_*` are `ResourceLocation`s.

There is **no `getAttributeModifiers` hook left**: modifiers are an `ItemAttributeModifiers`
component. Override NeoForge's `IItemExtension#getDefaultAttributeModifiers(ItemStack)`, or build
one with `ItemAttributeModifiers.builder().add(attr, modifier, EquipmentSlotGroup…)`.
`Item.BASE_ATTACK_DAMAGE_ID` replaces `BASE_ATTACK_DAMAGE_UUID`.

## 8. Items, tools, armour

`ArmorMaterial` is a **registered record** (`BuiltInRegistries.ARMOR_MATERIAL`) and `ArmorItem`
takes a `Holder<ArmorMaterial>` — a mod's materials have to be registered. Durability moved off
the material onto `DataComponents.MAX_DAMAGE`: apply the old multiplier per piece with
`Item.Properties#durability(ArmorItem.Type.X.getDurability(multiplier))`.

NeoForge's `onArmorTick` is gone. `Inventory#tick` reaches the armour compartment through
`Item#inventoryTick`, so an armour piece must check it is actually worn:

```java
if (getType().getSlot() != EquipmentSlot.HEAD || !(entity instanceof Player player)
        || player.getItemBySlot(EquipmentSlot.HEAD) != stack) {
    return;
}
```

`appendHoverText(ItemStack, Item.TooltipContext, List<Component>, TooltipFlag)` — the `Level` is
`context.level()`. `Item#getUseDuration(ItemStack, LivingEntity)`.
`ItemStack#hurtAndBreak(int, LivingEntity, EquipmentSlot)` — the break broadcast is automatic; the
`(int, ServerLevel, ServerPlayer, Consumer<Item>)` overload needs `(ServerPlayer) null` to
disambiguate. `ItemStack#save(Provider)` and `FluidStack#save(Provider)` **throw on an empty
stack** — use `saveOptional`.

Per-item dispense-behaviour subclasses are gone: implement `ProjectileItem` and call
`DispenserBlock.registerProjectileBehavior(this)`.

**Burn time:** NeoForge throws `IllegalStateException` on a negative burn time, so an item with
"no opinion" (CoFH's `-1` default) must fall through to the default implementation, which reads
the `neoforge:furnace_fuels` data map.

## 9. Events

| Was | Is |
|---|---|
| `LivingAttackEvent` | `LivingIncomingDamageEvent` |
| `LivingHurtEvent` | `LivingDamageEvent.Pre` (modify) / `.Post` (react) |
| `ShieldBlockEvent` | `LivingShieldBlockEvent` |
| `EntityItemPickupEvent` | `ItemEntityPickupEvent.Pre` |
| `SpawnPlacementRegisterEvent` | `RegisterSpawnPlacementsEvent` |
| `TickEvent.*` | `*TickEvent.Pre` / `.Post` |
| `MenuScreens.register` | `RegisterMenuScreensEvent` |
| `EventHooks.onApplyBonemeal` (int-coded) | `EventHooks.fireBonemealEvent` returning a cancellable `BonemealEvent` |

`Event.Result` and `event.getResult()` are gone — each event has its own API.
`ItemEntityPickupEvent.Pre` is **not cancellable**: deny with `setCanPickup(TriState.FALSE)`, and
the accessors are `getPlayer()` / `getItemEntity()`.

`RegisterSpawnPlacementsEvent#register` takes a `SpawnPlacementType` from `SpawnPlacementTypes`,
not `SpawnPlacements.Type`.

## 10. Blocks and world

`IPlantable` and `PlantType` were **deleted** in NeoForge 21.0. Soil answers
`IBlockExtension#canSustainPlant(state, level, pos, facing, BlockState plant)` returning a
`TriState`, and "is this a plant" is the soil's answer rather than a marker interface. CoFH's own
plant vocabulary lives in `cofh.lib.common.block.CropType`.

`Block#use` split into `useItemOn(ItemStack, …)` (returns `ItemInteractionResult`) and
`useWithoutItem(…)` (returns `InteractionResult`). `ServerPlayerGameMode` calls `useItemOn` for
*every* right click including empty-handed, falling back to `useWithoutItem` only on
`PASS_TO_DEFAULT_BLOCK_INTERACTION` — so a block with an "empty hand → open GUI" branch must not
blindly delegate `useWithoutItem` into `useItemOn` with `ItemStack.EMPTY`, or an unrelated held
block becomes un-placeable.

`isPathfindable(BlockState, PathComputationType)` — no level, no position, and protected.
`BlockBehaviour#tick` is protected (`level.scheduleTick(pos, block, delay)` instead).
`BlockPathTypes` → `PathType`. `StairBlock` takes the base `BlockState`, not a supplier;
`LiquidBlock` and `BucketItem` take the `Fluid` itself.
`CommonHooks.onCropsGrowPre/Post` → `CommonHooks.canCropGrow` / `fireCropGrowPost`.

`BlockElementFace` is a record: `texture()`, `cullForDirection()`.

## 11. Persistence

`BlockEntity#load` → `loadAdditional(CompoundTag, HolderLookup.Provider)`; `saveAdditional` gains
the same parameter; `onDataPacket`, `handleUpdateTag` and `getUpdateTag` all take the registries.
CoFHCore's `SimpleItemInv` / `SimpleTankInv` / `FluidStorageCoFH` / `ItemStorageCoFH` / `IFilter`
`read`/`write` take a provider too. Outside a persistence call it comes from
`ProxyUtils.registryAccess()`.

`FluidStack` is immutable-ish: the copy constructor is `copyWithAmount(int)`,
`loadFluidStackFromNBT` is `FluidStack.parseOptional(provider, tag)`, and `writeToNBT` is
`save(provider)`.

`NbtUtils.writeBlockPos` returns a bare int-array `Tag` and `readBlockPos(CompoundTag, String)`
needs a key and returns an `Optional`.

**Buffers:** `FriendlyByteBuf` lost `writeFluidStack`/`readFluidStack`, and `FluidStack`'s only
serialization (`STREAM_CODEC`) needs a `RegistryFriendlyByteBuf` — which the tile and menu packet
buffers are **not** (they are plain scratch buffers from `new FriendlyByteBuf(Unpooled.buffer())`).
**Casting one to `RegistryFriendlyByteBuf` is a runtime `ClassCastException`.** Use
`FluidHelper.writeFluidStack` / `readFluidStack`, which encode the optional save tag and preserve
components. The same applies to `ItemStack` in those buffers.

`RegisterPayloadHandlersEvent#registrar(String)` takes a network **version**, not a namespace —
the namespace comes from each payload's `Type`. Passing the mod id still compiles.

## 12. Recipes

`Recipe<C extends Container>` → `Recipe<T extends RecipeInput>` (`CraftingInput`,
`SingleRecipeInput`; `CraftingContainer#asCraftInput()` already exists as a default).

`RecipeSerializer#codec()` returns a **`MapCodec`**, and `streamCodec()` replaces the
`fromNetwork`/`toNetwork` pair — keep those two as the codec's halves over a
`RegistryFriendlyByteBuf` and wire them up with `StreamCodec.of(Type::toNetwork, Type::fromNetwork)`.

`Ingredient` is final and lost its network methods (`Ingredient.CONTENTS_STREAM_CODEC`);
`ItemStack` likewise (`OPTIONAL_STREAM_CODEC`). `ItemStack.ITEM_WITH_COUNT_CODEC` is just
`ItemStack.CODEC`. `Ingredient.fromJson` is gone. A custom ingredient is an `ICustomIngredient`
with a registered `IngredientType` (`cofh_core:with_count`).

`RecipeManager#byType` is **private** — the public `getAllRecipesFor(type)` returns a
`List<RecipeHolder<T>>`, so loops lose their `Map.Entry` indirection. `IShapedRecipe` no longer
exists in NeoForge; `ShapedRecipe#getRecipeWidth` is `getWidth`.

A loot table is identified by `ResourceKey<LootTable>`, and `MinecraftServer#getLootData` became
`reloadableRegistries()`.

## 13. Datagen

`BlockLootSubProviderCoFH`, `EntityLootSubProviderCoFH`, `RecipeProviderCoFH` and
`LootTableProviderCoFH` all take the registries (`HolderLookup.Provider` or a
`CompletableFuture<…>`); a `SubProviderEntry`'s factory takes them too.
`PlacementModifierType#codec()` returns a `MapCodec`. `BootstapContext` → `BootstrapContext`.

**The run.** MDG's 1.21.1 run type is `data()`. `clientData()` only exists from 1.21.4 and fails
`prepareDataRun` with "unknown run: clientData". It is a client-dist launch, so client mixins
apply during it, which makes it a cheap first check for a client crash. A mod whose models point
at another mod's textures needs `'--existing-mod', '<modid>'` in its program arguments
(ThermalExpansion → `thermal`).

**Output formats a 1.20 hand-migration gets wrong.** 1.21 codecs ignore unknown fields, so none
of these errors at load; they just behave differently:

| Where | 1.20 | 1.21.1 |
|---|---|---|
| Folders | `tags/items`, `tags/blocks`, `tags/fluids`, `loot_tables`, `recipes`, `advancements` | `tags/item`, `tags/block`, `tags/fluid`, `loot_table`, `recipe`, `advancement` — plural folders are **not read** |
| Item predicate (advancements, `match_tool`) | `{"tag": "c:ingots/iron"}` / `{"items": ["a:b"]}` | `{"items": "#c:ingots/iron"}` / `{"items": "a:b"}` (a `HolderSet`) |
| Enchantment in an item predicate | `"enchantments": [{"enchantment": "minecraft:silk_touch", …}]` | `"predicates": {"minecraft:enchantments": [{"enchantments": "minecraft:silk_touch", …}]}` |
| Stonecutting count | top-level `"count"` | `result.count` |

## 14. Resources

Folders are singular: `tags/{block,item,fluid,entity_type}`, `recipe`, `advancement`,
`loot_table`. The convention tag namespace is `c:`, and several tags were **renamed as well as
re-namespaced** — mostly pluralised:

| 1.20.4 | 1.21.1 |
|---|---|
| `forge:gunpowder` | `c:gunpowders` |
| `forge:glass` | `c:glass_blocks` (**not** `c:glass`, and not `c:glass_blocks_colorless`) |
| `forge:sand` | `c:sands` |
| `forge:string` | `c:strings` |
| `forge:leather` | `c:leathers` |
| `forge:obsidian` | `c:obsidians` |
| `forge:stone` | `c:stones` |

Grepping NeoForge's `Tags.java` alone gives false negatives — `c:dyes/<colour>` comes from the
`DyeColor` patch, not from `Tags`.

Recipe conditions: `"type": "forge:not"` → `"neoforge:not"`, and a top-level `"conditions"` block
is `"neoforge:conditions"`.

**Recipe JSON.** A vanilla recipe's result is an `ItemStack` object keyed **`id`**, not `item`,
and cooking/stonecutting results are objects rather than a bare id. **Ingredients keep their
`{"item": …}` / `{"tag": …}` object form** — the bare-string shape (`"minecraft:diamond"`,
`"#c:gems/diamond"`) arrives in 1.21.2, *not* 1.21.1. CoFH's own machine recipes are parsed by
`RecipeJsonUtils`, not by a codec, and keep `item` throughout.

Loot: `minecraft:looting_enchant` → `minecraft:enchanted_count_increase`, which names the
enchantment explicitly (`"enchantment": "minecraft:looting"`) and wants a full number provider.

## 15. Models

`IUnbakedGeometry#bake` and `SimpleUnbakedGeometry#addQuads` **lost their trailing
`ResourceLocation`**, as did `BlockModel.bakeFace` and `UnbakedGeometryHelper#bakeElements`.
`StandaloneGeometryBakingContext.Builder#build` still wants one, purely as a name.

## 16. Mixins

`compatibilityLevel` should be `JAVA_21` (the loader sets it there regardless). A client-only
target listed under `mixins` rather than `client` fails to apply on a dedicated server —
`MultiPlayerGameModeMixin` was in the wrong list.

## 17. Recovered from code comments (style pass, 2026-09-22)

The port originally recorded these facts in code comments. The upstream style pass removed the
comments, so the facts live here now. Each was confirmed against the jar when the code was
written. They are grouped by the categories above.

**Items, tools, armour (§8)**
- `ItemStack#hurtAndBreak` applies Unbreaking itself, so the old `RandomSource` argument has no
  replacement. Overloads: `(int, ServerLevel, @Nullable ServerPlayer|LivingEntity, Consumer<Item>)`
  and `(int, LivingEntity, EquipmentSlot)`. Get the slot from `LivingEntity.getSlotForHand(hand)`.
- `Inventory#hurtArmor` is gone and `LivingEntity#hurtArmor` is `protected`. CoFH's
  `ArmorEvents` damages each piece directly: a quarter of the damage, minimum 1.
- `FoodProperties` is a record. Its effects are `PossibleEffect` (`effect()`, `probability()`),
  eating is `player.getFoodData().eat(food)`, and `isFastFood()` became
  `eatDurationTicks() < 32`. **`saturation` is absolute now** (it was a modifier, with
  saturation = nutrition × modifier × 2).
- `CrossbowItem`:
  - Ammo is the `CHARGED_PROJECTILES` component; `setCharged` and the `"AMMO"` tag are gone.
  - `setShotFromCrossbow` and `setPierceLevel` are gone. Use `arrow.firedFromWeapon` and
    `EnchantmentHelper.onProjectileSpawned`, which also covers `setKnockback`/`setSecondsOnFire`.
- `AbstractArrow` constructors take the weapon stack. Damage enchantments go through
  `EnchantmentHelper.modifyDamage`, and the two post-hit hooks merged into
  `doPostAttackEffectsWithItemSource`.
- Fishing bonuses are `EnchantmentHelper.getFishingLuckBonus(ServerLevel, stack, entity)` and
  `getFishingTimeReduction(…)`. Only CoFH's own tiers still carry a numeric level.
- `Enchantment#getSlotItems` is gone; walk every `EquipmentSlot` instead. Loyalty on a CoFH
  item works through a `supportsEnchantment` override.
- `ProjectileItem.DispenseConfig` replaces per-item dispense-behaviour subclasses.

**Mob effects, potions (§5)**
- `Potion.getName(Optional<Holder<Potion>>, prefix)` builds the whole translation key.
  `PotionContents#getColor` prefers the custom colour. The old `display`/`HideFlags` NBT maps to
  `CUSTOM_NAME` / `HIDE_ADDITIONAL_TOOLTIP`.
- `PotionBrewing`'s mixes are private lists that can't be enumerated, so ThermalCore's brewer
  conversion tries each potion against each ingredient.

**Enchantments (§6)**
- Frost Walker is the datapack effect `minecraft:replace_disk`; there's no `FrostWalkerEnchantment`
  to call. Blizz re-implements the level-1 disk: radius 3, frosted ice ticking 60–120.
  `Entity#onChangedBlock`'s `super` runs the location-changed effects, Soul Speed included.
- CoFH's "enable" config works through `Utils.setEnchantmentEnabled`; a disabled enchantment
  reads as level 0. The "Treasure" option is now the `minecraft:treasure` tag, which only a data
  pack can change.

**Entities, projectiles (§8/§10)**
- `AbstractHurtingProjectile`'s per-axis power fields (`xPower`/`yPower`/`zPower`) became a
  movement `Vec3` plus `accelerationPower`.
- Dimension travel is the `Portal` interface (`setAsInsidePortal`). `TheEndGatewayBlockEntity`'s
  static teleport helpers are gone.
- A `DeferredHolder` is a `Holder`; pass it without `.get()`.
- `LivingDamageEvent.Pre` can't be cancelled; set the amount to zero instead.

**Blocks (§10)**
- `Block#getExpDrop` is `(state, LevelAccessor, pos, BlockEntity, Entity breaker, ItemStack tool)`;
  read silk touch off the tool.
- `useWithoutItem` has no hand, so a main-hand-only check can't live there.

**Persistence, network (§11)**
- `SavedData.Factory`'s load function takes the registry lookup.
- Block items keep augments and security in `BLOCK_ENTITY_DATA`, which vanilla restores on
  placement. Everything else uses `CUSTOM_DATA`.
- A payload record component can't be named `type`: it clashes with `CustomPacketPayload#type()`.
- `connection.connection.isConnected()` → `connection.isAcceptingMessages()`.
- `Component.Serializer.toJson`/`fromJsonLenient` take a registries argument.
- `Player#getBlockReach()` → `blockInteractionRange()`.

**Recipes, datagen (§12/§13)**
- `Ingredient.fromJson` is gone; use `Ingredient.CODEC.parse(JsonOps.INSTANCE, json)`. A custom
  ingredient is keyed `"neoforge:ingredient_type": "cofh_core:with_count"`, and it must report
  `isSimple() == false`, since stack size can't be matched by item alone.
- `CopyNbtFunction` → `CopyCustomDataFunction`, without the `BlockEntityTag.` destination
  prefix. `SetContainerContents` takes `ContainerComponentManipulators.CONTAINER`.
- The bottled item takes the fluid's component patch through `applyComponents`, which is how
  potion contents reach the bottle.

**Client (§14/§15)**
- `BakedQuad#getIntegerSize()` is gone; use `getVertexSize() / 4`.
- `MultiBufferSource.immediateWithBuffers` takes a `SequencedMap<RenderType, ByteBufferBuilder>`,
  so use a `LinkedHashMap`.
- A `static` codec factory can't share an erased signature with a superclass's static method,
  hence `BiColorParticleOptions`/`CylindricalParticleOptions`' distinct names.

**Build**
- `-Xmaxerrs 100000`: javac otherwise caps output at 100, which hides a hop's true error count.
- Upstream's `commonManifest` was defined but never applied to the jar. Its `MixinConfigs` entry is
  what loads mixins in a production jar, so the port applies it.
