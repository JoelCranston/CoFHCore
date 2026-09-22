# Progress log — CoFHCore

Append-only. Later entries correct earlier ones rather than editing them away. See
[TODO.md](TODO.md) for what's outstanding right now, [api-notes-1.20.6.md](api-notes-1.20.6.md)
for the API detail behind the 1.20.6 hop specifically.

## Phase 0 — pre-existing state

This repo already had a working NeoForge **1.20.4** port before the current porting
effort started (`29b2484` "1.20.4 Initial Port Work" and follow-ups) — CoFHCore's own
1.20.4 hop wasn't part of this effort; it was the starting point.

## Phase 1 — modern tooling, verified running

Researched whether a NeoForge port of CoFHCore existed anywhere to build on top of
(official CoFH maven, and the community fork by GitHub user SPLIGAN —
`ThermalExpansionForNeoForge`/`ThermalCoreForNeoForge`, already cloned as sibling repos).
Found: **no `CofhCoreForNeoForge` fork exists anywhere**, official or community.
SPLIGAN's Thermal forks claim NeoForge 1.21.1 support but depend on
`com.teamcofh:cofh_core:1.21.1-11.0.2.0`, which doesn't exist on the official CoFH maven
(stops at 1.20.1) — so that fork can't actually compile as checked out. Decision: port
CofhCore myself, from this repo's existing 1.20.4 codebase, and go straight for the
26.1.2 target rather than stopping at 1.21.1 to match SPLIGAN's partial attempt.

Brought the existing 1.20.4 codebase onto modern tooling and verified it actually
builds and runs before starting any version climbing:
- `f84366a` Add dev run configs (client/server/data) and Gradle heap settings.
- `d30275f` Fix broken `javafml` loaderVersion requirement in `mods.toml`.
- `506284e` Upgrade to Gradle 9.2.1 + NeoGradle userdev 7.1.38.

## Phase 2 — the primer climb

Full primer chain confirmed from `docs.neoforged.net/primer/docs/`: **1.20.4 → 1.20.5 →
1.20.6 → 1.21 → 1.21.1 → 1.21.2/3 → 1.21.4 → 1.21.5 → 1.21.6 → 1.21.7 → 1.21.8 → 1.21.9
→ 1.21.10 → 1.21.11 → 26.1** (target `26.1.2`, within the 26.1.x line — 26.2 not
needed). Each hop has its own primer page and, separately, a "Neo Changes" page for
loader-level (non-Mojang-mapping) breakage.

### 1.20.6 hop — in progress

CofhCore is the foundation library all three other repos depend on, so its migration
comes first each hop. Currently **not yet compiling clean**. Chronology (see
[api-notes-1.20.6.md](api-notes-1.20.6.md) for the full technical detail behind each
one), with the running error count after each commit:

1. `667bf76` Networking → 1.20.5+ payload API (`StreamCodec`, `IPayloadContext`,
   `PayloadRegistrar`, `PacketDistributor`). Single biggest cluster, ~26 of the
   original ~100-under-the-cap errors. **[WIP]**
2. `13ba246` TickEvent → `event.tick`/`event.client.event` Pre/Post classes, 5 files.
   **[WIP]**
3. `4494c6c` Enchantment system → `EnchantmentDefinition` (immutable record; cost
   methods no longer overridable). **[WIP]**
4. `9cc0f29` ArmorMaterial (interface → final record) / HorseArmorItem → AnimalArmorItem
   merge / dispenser → ProjectileItem interface. Also confirmed the `Mod.EventBusSubscriber`
   errors seen since step 2 were javac cascades from these breaks, not a real annotation
   change. **[WIP]**
5. `3a0d42d` Particle system → `MapCodec`/`StreamCodec`. All particle errors confirmed
   gone; file-with-errors count 43 → 40.
6. `97d6085` Entity: `defineSynchedData(Builder)`, `getEyeHeight` final, spawn-data
   buffer type, several smaller entity API changes. 40 → 36 files.
7. `1f9cbc1` **Discovered javac's `-Xmaxerrs 100` had been capping every rebuild's
   error count the whole hop** — raised to 100000. Real picture at this point: **465
   errors / 132 files**, not the 40/101 previously tracked. Every count logged after
   this one is the true, uncapped number. Also: `ItemHelper`/`ItemStorageCoFH` migrated
   off raw `ItemStack` NBT onto `DataComponents.CUSTOM_DATA`.
8. `1573b78` `HolderLookup.Provider` threaded through the rest of item/fluid storage and
   the filter system (`SimpleItemInv`, `SimpleTankInv`, `IFilter` family). 465/132 →
   **442/123**.
9. `e9d1c2b` `FluidStack(FluidStack, int)` → `copyWithAmount(int)`, all call sites.
   442/123 → **433/120**.
10. `1916a3c` `AttributeModifier.Operation` constant renames. 433/120 → **428/119**.
11. `21dc584` Tool item family constructor changes (`DiggerItem`/`SwordItem` subclasses,
    `Tier.getLevel()` removed, `ItemStack#hurtAndBreak` signature, `ProjectileItem`
    dispensing rewrite). `CrossbowItemCoFH`'s ammo system deliberately deferred — too
    large to half-fix, needs its own pass. 428/119 → **408/109**.
12. `2db3521` `Block#use` split into `useWithoutItem`/`useItemOn`. 408/109 → **396/101**.

**As of a fresh compile today (2026-09-22, same commit `2db3521`): 396 errors / 104
files.** Close to the last logged count (396/101 → 396/104 — a handful of files'
error counts shifted slightly, not a regression). Full current breakdown and priority
order in [TODO.md](TODO.md) — the largest remaining bucket is raw `ItemStack` NBT calls
outside the two files already fixed in step 7, plus two large, not-yet-triaged buckets
(`Matrix3f`/`Pose` incompatibility ×44, `MobEffect`/`Holder<MobEffect>` ×34) that hadn't
surfaced clearly before this compile.

### Working method for this hop (apply to every future hop too)

- Verify every API shape against the actual mapped jar via `javap`
  (`build/neoForm/neoFormJoined<version>-.../raw.jar`), not against summarized docs or
  memory of older versions — the primer is a good starting pointer, not a source of
  truth for exact signatures.
- Fix by category (one root API change at a time across every file it touches), not
  file-by-file — most breakage clusters around a small number of real upstream changes.
- When a file has multiple unrelated errors, fix the one you're categorizing and leave
  the rest for their own pass rather than mixing categories in one commit.
- Don't guess at a big rewrite (enchantment system, `CrossbowItemCoFH`'s ammo system) —
  confirm the full scope first and defer deliberately if it's really a separate,
  comparably-sized piece of work.
- Track the error count before/after every commit. It's the only reliable signal that a
  category is actually closed, not just moved — and the `-Xmaxerrs` discovery (step 7)
  is a reminder to sanity-check that the count itself is trustworthy.

## Phase 2, revised — two hops, not fourteen (2026-09-21)

Joel asked for the plan to be re-checked against current NeoForge documentation with no
trust in earlier assumptions, and for the route to be 1.21.1 then 26.1.2 with no other
intermediates. The re-verification (live maven metadata, the `1.21.1` and `26.1.x` NeoForge
source trees, the primers and release posts, the JEI/Curios/Patchouli mavens, and the code
on this machine) produced [port-plan.md](port-plan.md). What it changed:

- **The remaining 1.20.6 work is not finished as 1.20.6.** The `1.21.1` branch was created
  from `1.20.6` at `a33bd27`; the twelve 1.20.6 commits are all still correct on 1.21.1 and
  the ~396 outstanding errors get fixed once against 1.21.1's shapes. 1.21.1 → 26.1.2 is then
  one jump, which is the route the 26.1 release post itself recommends for 1.21.1 mods.
- **Two earlier conclusions were wrong.** `@Mod.EventBusSubscriber` → `@EventBusSubscriber`
  (top-level) and `Bus.FORGE` → `Bus.GAME` are real 1.20.5 changes (NeoForge 20.5 release
  notes, "Event System"), not javac cascades — the api-notes' armor/dispenser entry and
  TODO item 4 said otherwise. And `mods.toml` has been `neoforge.mods.toml` since 20.5; a jar
  with the old name is silently skipped. All four repos still had the old name.
- **Toolchain**: ModDevGradle 2.0.147 replaces NeoGradle userdev in Phase 0 — it is what
  `../Pyronetics` builds 26.1.2.109 with on this machine, and its patched-sources jar replaces
  `javap` against `build/neoForm/.../raw.jar` as the shape oracle. Latest versions confirmed
  2026-09-21: NeoForge 21.1.251 (1.21.1) and 26.1.2.109 (26.1.2); JEI 19.57.0.446 / 29.40.0.101;
  Curios 9.5.1+1.21.1 / 15.0.0+26.1.2; Patchouli 1.21.1-93 on maven and 26.1-94 only as a
  GitHub release jar (kept, as a `libs/` dependency in ThermalCore).
- **References found on disk**: Pyronetics is a working 26.1.2 implementation of nearly every
  subsystem this port has to migrate (screens, block-entity renderers, transfer API, fluids,
  recipes, networking) with 631 lines of confirmed shapes in its `docs/api-notes-26.1.2.md`;
  SPLIGAN's forks are minimal-diff 1.21.1 ports of ThermalCore (158 files changed),
  ThermalExpansion and — newly cloned today — ThermalDynamics. The forks compiled against a
  `cofh_core` build that only existed in SPLIGAN's `mavenLocal()`, so CoFHCore is still ported
  by hand.
- **Other findings that become work**: recipes stop being synced to clients in 1.21.2
  (`OnDatapackSyncEvent#sendRecipes` + `RecipesReceivedEvent` is the replacement for
  ThermalCore's client caches and JEI); 392 resource files reference `forge:` tags that never
  existed on NeoForge (`c:`); the transfer API rework (21.9) and the model/GUI/render-pipeline
  rewrites (1.21.4–26.1) are the two XL items of Phase B.

Phase 0 done today: reference material vendored to `docs/reference/` (gitignored,
`scripts/fetch_reference.sh`), `1.21.1` branches in all four repos, docs updated. Still to do
before Phase A: the ModDevGradle switch and the `neoforge.mods.toml` rename (TODO.md).
