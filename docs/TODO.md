# TODO — CoFHCore

Current, outstanding work only. The phase/category plan is [port-plan.md](port-plan.md);
this file tracks where we are in it and what has been noticed along the way. See
[progress-log.md](progress-log.md) for what's already done and why, and
[api-notes-1.20.6.md](api-notes-1.20.6.md) for API shapes already confirmed (all still valid
on 1.21.1).

**Snapshot**: branch `1.21.1`, ModDevGradle 2.0.147, `neo_version=21.1.251`. **25 errors / 5
files** (2026-09-22; baseline was `849 / 175`). Everything outside client rendering compiles.
Re-run `./gradlew compileJava` before trusting these.

## Phase 0 — preparation (port-plan.md §4)

- [x] 0.1 `scripts/fetch_reference.sh` written and run → `docs/reference/` (126 files);
      `../ThermalDynamicsForNeoForge` cloned.
- [x] 0.2 `1.21.1` branches in all four repos; `.DS_Store` ignored here (do the same in the
      other three when their Phase 0 starts).
- [x] CoFHCore / [ ] TC / [ ] TD / [ ] TE — 0.3 **Switch all four repos to ModDevGradle 2.0.147** (template in port-plan.md §4.3,
      source `../Pyronetics/build.gradle`). `rm -rf build .gradle` first. Keep the
      `MixinConfigs` manifest attribute, publishing, curse/modrinth, signing blocks.
- [x] CoFHCore / [ ] TC / [ ] TD / [ ] TE — 0.4 `git mv META-INF/mods.toml META-INF/neoforge.mods.toml` in all four; fix the
      hardcoded `versionRange = "1.20.4"`; update `processResources` `filesMatching`.
- [x] 0.5 docs updated (this file, CLAUDE.md, progress-log.md, port-plan.md copied in).

## Phase A — 1.21.1 (port-plan.md §5), CoFHCore first

- [x] A.0 bump `gradle.properties` (values in §5 A.0), first compile → baseline `849 / 175` (above).
- [ ] A.1 categories 1–16, in order (§5 A.1). Tick each as it lands with its before/after count:
  1. [x] mod metadata & bus (`@EventBusSubscriber`, `Bus.GAME`, `ModContainer#registerConfig`)
  2. [x] `ResourceLocation` factories (all four repos in one sweep)
  3. [x] ItemStack NBT → data components (remaining ~140 sites family-wide)
  4. [x] vertex/rendering API (`Matrix3f`→`Pose`, `addVertex`/`setColor`/…, `buildOrThrow`)
  5. [x] `MobEffect`→`Holder<MobEffect>`, `PotionUtils`→`PotionContents`, `PotionColorCalculationEvent` gone (849→829→664→608→434→312 across 1-5+7)
  6. [x] enchantments → datapack (312→261) (`HoldingEnchantment` JSON + key, `EnchantmentHelperCoFH.getLevel`)
  7. [x] attribute modifier ids
  8. [x] tools/armor/crossbow (261→228) (`CHARGED_PROJECTILES`, `hurtAndBreak`, `getUseDuration`)
  9. [x] events (228→208) (damage pipeline, item pickup, spawn placements, `Event.Result` leftovers)
  10. [x] blocks (208→153) (crop hooks `canCropGrow`/`fireCropGrowPost`, `SpecialPlantable`, `FoodProperties` record)
  11. [x] recipes (153→124) (`RecipeInput`, stream codecs, `IShapedRecipe` gone)
  12. [ ] loot/datagen, `ItemAbilities`, `DamageSource#isDirect` — partly done in 6/10 (loot
      providers, `CopyCustomDataFunction`, `ItemAbilities`); `RecipeProviderCoFH` and the
      datagen entrypoints remain
  13. [ ] mixins re-targeted (10 classes; 2 not in `mixins.cofhcore.json` — dead?)
  14. [~] access transformers — the 15 lines `validateAccessTransformers` rejected were deleted in A.0 (all dead since 1.20.x); re-check after A.1 for members the code still needs
  15. [ ] resources: singular tag/data folders, `forge:`→`c:` (392 files family-wide)
  16. [ ] Curios 9.5.1 API check
- [ ] A exit: `build` clean, `verify_runserver.sh` passes, Joel's `runClient` pass,
      `docs/api-notes-1.21.1.md` written.
- [ ] Then ThermalCore (SPLIGAN diff-driven), ThermalExpansion, ThermalDynamics (§5 A.2–A.3).

## Phase B — 26.1.2 (port-plan.md §6)

Not started. Branch `26.1.2` is created from `1.21.1` only after Phase A's exit criteria.

## Remaining: the client model/render cluster (25 errors, 5 files)

All five need the same two root causes read properly before touching them — see
`docs/reference/primers/1.21.md` ("Oh Rendering, why must you change so?") and NeoForge's
`ModelEvent`/`IGeometryLoader` in `docs/reference/neoforge-src/files-1.21.1.txt`:

1. **`ElementsModelWrapped`, `SimpleModel`, `FluidContainerItemModel`** — NeoForge's unbaked
   geometry contract changed: `SimpleUnbakedGeometry#addQuads` takes an `IModelBuilder<?>` (not a
   `List<BlockElement>`), `bake` has a new parameter list, `BlockModel#bakeFace` changed, and
   `BlockElementFace`'s `texture`/`cullForDirection` are private (they are record components now —
   use the accessors). `FluidContainerItemModel` also still reads `stack.getTag()` and
   `Item#getDescriptionId()`.
2. **`RenderTypes` (vfx) and `CoreClientEvents`** — `BufferUploader`, `Tesselator#begin`/`end` and
   `RenderType.CompositeState` leftovers, plus two `Registries.ENCHANTMENT`-shaped lookups in the
   enchantment-description tooltip that still assume a static registry.

None of this is deep design work — it is the tail of the 1.21 vertex/model rewrite — but it is the
part where a wrong guess compiles and then renders nothing, so confirm each shape in
`build/moddev/artifacts/neoforge-21.1.251-sources.jar` first.

## Inbox

- `RecipeManager#byType(RecipeType)` AT line dropped (descriptor changed; it now returns a
  `Collection<RecipeHolder<T>>`, private). ThermalCore's fuel/recipe managers call it (9 sites) —
  switch them to the public `getAllRecipesFor(type)` in TC's Phase A rather than re-adding an AT.
- MDG artifact names for 1.21.1 are `build/moddev/artifacts/neoforge-21.1.251{,-sources,-merged}.jar`
  (Pyronetics' `minecraft-patched-<ver>` naming is the 26.1 layout). Use the `-sources` jar.

- `ThermalExpansion`'s hand-written machine recipes use `{"item": …, "count": n}` /
  `{"tag": "forge:…"}` parsed by CoFH's own `RecipeJsonUtils` — survives the 1.21.2 ingredient
  format change, but check `RecipeJsonUtils.parseIngredient` compiles (`Ingredient.fromJson` is gone).
- ~~`PotionColorCalculationEvent`~~ resolved in category 5: replaced by
  `CustomParticleMobEffect#createParticleOptions` returning null (per-effect, not global).
- **Cross-repo API changes ThermalCore/TD/TE must follow** (all from category 3/7):
  `IEnergyContainerItem#getOrCreateEnergyTag` → `getEnergyTag` + `mutateEnergyTag`,
  `IFluidContainerItem#getOrCreateTankTag` → `getTankTag` + `mutateTankTag`,
  `IMultiModeItem#getOrCreateModeTag` → `getModeTag`/`setModeTag`, `ConfigManager#register` takes
  the `ModContainer`, and `Constants`' `UUID_*` modifier ids are `ResourceLocation`s now.
  TC's `EnergyCellBlockItem` overrides two of those.
- Two mixin classes (`ClientPacketListenerMixin`, `ClientboundSetEntityMotionPacketMixin`) exist
  in `cofh.core.mixin` but are not listed in `mixins.cofhcore.json` — decide keep/delete in A.1.13.
