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
