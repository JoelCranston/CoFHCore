# TODO — CoFHCore

Current, outstanding work only. See [progress-log.md](progress-log.md) for what's already
done and why, and [api-notes-1.20.6.md](api-notes-1.20.6.md) for the API shapes already
confirmed this hop.

**Snapshot**: `396 errors / 104 files` from a fresh `./gradlew compileJava` on branch
`1.20.6` at commit `2db3521` (2026-09-22). Re-run before trusting these numbers — they
shift every session. Anything noticed mid-session that doesn't fit elsewhere goes in the
Inbox at the bottom.

## Next up, by error count (this compile run)

1. **Raw `ItemStack` NBT API, everywhere else** (by far the largest remaining bucket —
   dozens of call sites across the codebase). `ItemHelper`/`ItemStorageCoFH` were
   already migrated to `DataComponents.CUSTOM_DATA` (see api-notes), but that was only
   two files — most callers of `stack.getTag()`/`setTag()`/`getOrCreateTag()`/
   `getTagElement()`/`addTagElement()`/`hasCustomHoverName()`/`setHoverName()`/
   `getEnchantmentTags()` elsewhere in the repo are still broken. This is a mechanical
   sweep once the `ItemStorageCoFH`/`CustomData` pattern is settled — the risk is
   missing a call site where the old NBT shape meant something subtly different (e.g. a
   tag that was implicitly shared/mutable vs. `CustomData`'s copy-on-read).
2. **`Matrix3f` → `Pose` incompatible (44 occurrences)** — not yet triaged. Likely a
   rendering-math API change (`PoseStack`/`Quaternion` rework); confirm the real 1.20.6
   shape via `javap` before touching, same discipline as every category in api-notes.
3. **`MobEffect` → `Holder<MobEffect>` incompatible (34 occurrences)** — the same shape
   change already fixed once in `ElectricField` (api-notes, entity category) recurs
   throughout the codebase. Likely another mechanical sweep, but check for spots
   holding onto a raw `MobEffect` reference longer-term (equality/lookup code) where
   `Holder` identity semantics might matter.
4. **`Mod.EventBusSubscriber` cannot find symbol (10 occurrences, several files)** —
   previously assumed to be javac error-recovery cascades from unrelated breaks in the
   same file (confirmed true for the TickEvent and armor/dispenser categories in
   api-notes). **Re-verify this assumption now** — with those files otherwise fixed,
   these may be a real annotation move/rename in 1.20.6, not a cascade. Check one file
   in isolation (temporarily silence its other errors, or check a file that has only
   this error) before assuming either way.
5. **`CommonHooks.onCropsGrowPre`/`onCropsGrowPost` signature changed** —
   `CropBlockCoFH`, `CropBlockTall`, `CropBlockMushroom`. Flagged in api-notes' Block#use
   commit, not yet started.
6. **`Enchantments.BLOCK_FORTUNE`** — import broken, constant relocated/renamed
   upstream. `CropBlockCoFH`, `CropBlockTall`. Flagged in api-notes, not yet started.
7. **`FoodProperties#getNutrition`/`getSaturationModifier`/`getEffects`** —
   `CakeBlockCoFH`, `FeastBlock`. Flagged in api-notes, not yet started.
8. **`event.getResult() == Event.Result.DENY`** (2 occurrences) — the old
   cancelable-with-`Result` event pattern; find what replaced it for these specific
   events before assuming it's the same `setCanceled(true)` swap used for
   `SaplingGrowTreeEvent`/`BlockGrowFeatureEvent` in api-notes.
9. **`PotionUtils` import broken** (5 occurrences) — class relocated or restructured;
   not yet looked at.
10. **`EnchantmentPredicate`/`MinMaxBounds.Ints`** in a loot table condition builder —
    loot condition API shape likely changed alongside the enchantment system rewrite;
    check against the `EnchantmentDefinition`/`ItemEnchantments` shapes already
    confirmed in api-notes before assuming a fresh API.
11. **`entity.getArmorSlots()`** — `LivingEntity` iterable-armor-slots method appears
    gone or renamed; not yet looked at.
12. **`Reference<SoundEvent>` vs `SoundEvent`** (3), **`getRegistryName(Holder<MobEffect>)`
    no suitable method** (3) — smaller, not yet triaged.
13. **`CrossbowItemCoFH`** — the whole ammo-loading system is incompatible with
    vanilla's `ChargedProjectiles` data component rewrite (`CrossbowItem.setCharged`
    doesn't exist in the old shape anymore). Comparable in size to the enchantment
    rewrite — deliberately deferred, see api-notes. Don't half-fix it.

## After CoFHCore compiles clean on 1.20.6

- Commit the `1.20.6` version bump in **ThermalCore**, **ThermalDynamics**, and
  **ThermalExpansion** (currently sitting uncommitted in each — see their own
  `docs/TODO.md`) and begin each repo's own 1.20.6 migration. Many of the API shapes
  above (ItemStack NBT, MobEffect/Holder, particle system if any repo has custom
  particles) will very likely recur — check this file and api-notes-1.20.6.md first
  before re-deriving them.
- Continue the primer climb: 1.20.6 → 1.21 is next
  (`docs.neoforged.net/primer/docs/1.21/`, Neo changes at `/primer/docs/1.21/neo`). Full
  chain to the 26.1.2 target: 1.21 → 1.21.1 → 1.21.2/3 → 1.21.4 → 1.21.5 → 1.21.6 →
  1.21.7 → 1.21.8 → 1.21.9 → 1.21.10 → 1.21.11 → 26.1 (stop within the 26.1.x line at
  26.1.2 — no need for 26.2).

## Inbox

_(nothing yet — add anything noticed mid-session here rather than letting it get lost)_
