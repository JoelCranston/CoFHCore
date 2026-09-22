# TODO — CoFHCore

Current, outstanding work only. The phase/category plan is [port-plan.md](port-plan.md);
this file tracks where we are in it and what has been noticed along the way. See
[progress-log.md](progress-log.md) for what's already done and why, and
[api-notes-1.20.6.md](api-notes-1.20.6.md) for API shapes already confirmed (all still valid
on 1.21.1).

**Snapshot**: branch `1.21.1`, ModDevGradle 2.0.147, `neo_version=21.1.251`. **Baseline on
1.21.1: `849 errors / 175 files`** (fresh `./gradlew compileJava`, 2026-09-21, log at
`/tmp/cofh-A0.log` for that session). Top kinds: `cannot find symbol` 467, `ResourceLocation`
constructor 164, "does not override" 51, `MobEffect`→`Holder<MobEffect>` 34, enchantment
key/holder mismatches 16. Re-run before trusting these — they shift every session.

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
  1. [ ] mod metadata & bus (`@EventBusSubscriber`, `Bus.GAME`, `ModContainer#registerConfig`)
  2. [ ] `ResourceLocation` factories (all four repos in one sweep)
  3. [ ] ItemStack NBT → data components (remaining ~140 sites family-wide)
  4. [ ] vertex/rendering API (`Matrix3f`→`Pose`, `addVertex`/`setColor`/…, `buildOrThrow`)
  5. [ ] `MobEffect`→`Holder<MobEffect>`, `PotionUtils`→`PotionContents`, `PotionColorCalculationEvent` gone
  6. [ ] enchantments → datapack (`HoldingEnchantment` JSON + key, `EnchantmentHelperCoFH.getLevel`)
  7. [ ] attribute modifier ids
  8. [ ] tools/armor/crossbow (`CHARGED_PROJECTILES`, `hurtAndBreak`, `getUseDuration`)
  9. [ ] events (damage pipeline, item pickup, spawn placements, `Event.Result` leftovers)
  10. [ ] blocks (crop hooks `canCropGrow`/`fireCropGrowPost`, `SpecialPlantable`, `FoodProperties` record)
  11. [ ] recipes (`RecipeInput`, stream codecs, `IShapedRecipe` gone)
  12. [ ] loot/datagen, `ItemAbilities`, `DamageSource#isDirect`
  13. [ ] mixins re-targeted (10 classes; 2 not in `mixins.cofhcore.json` — dead?)
  14. [~] access transformers — the 15 lines `validateAccessTransformers` rejected were deleted in A.0 (all dead since 1.20.x); re-check after A.1 for members the code still needs
  15. [ ] resources: singular tag/data folders, `forge:`→`c:` (392 files family-wide)
  16. [ ] Curios 9.5.1 API check
- [ ] A exit: `build` clean, `verify_runserver.sh` passes, Joel's `runClient` pass,
      `docs/api-notes-1.21.1.md` written.
- [ ] Then ThermalCore (SPLIGAN diff-driven), ThermalExpansion, ThermalDynamics (§5 A.2–A.3).

## Phase B — 26.1.2 (port-plan.md §6)

Not started. Branch `26.1.2` is created from `1.21.1` only after Phase A's exit criteria.

## Inbox

- `RecipeManager#byType(RecipeType)` AT line dropped (descriptor changed; it now returns a
  `Collection<RecipeHolder<T>>`, private). ThermalCore's fuel/recipe managers call it (9 sites) —
  switch them to the public `getAllRecipesFor(type)` in TC's Phase A rather than re-adding an AT.
- MDG artifact names for 1.21.1 are `build/moddev/artifacts/neoforge-21.1.251{,-sources,-merged}.jar`
  (Pyronetics' `minecraft-patched-<ver>` naming is the 26.1 layout). Use the `-sources` jar.

- `ThermalExpansion`'s hand-written machine recipes use `{"item": …, "count": n}` /
  `{"tag": "forge:…"}` parsed by CoFH's own `RecipeJsonUtils` — survives the 1.21.2 ingredient
  format change, but check `RecipeJsonUtils.parseIngredient` compiles (`Ingredient.fromJson` is gone).
- `PotionColorCalculationEvent` was deleted in NeoForge 21.0 with no replacement event;
  `EffectEvents` needs a decision (use `PotionContents#getColor()` or drop the tweak).
- Two mixin classes (`ClientPacketListenerMixin`, `ClientboundSetEntityMotionPacketMixin`) exist
  in `cofh.core.mixin` but are not listed in `mixins.cofhcore.json` — decide keep/delete in A.1.13.
