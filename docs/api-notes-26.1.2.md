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
