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

### 1.21.1 hop — Phase 0.3/0.4 and A.0 (2026-09-21)

Switched this repo to ModDevGradle 2.0.147 (`build.gradle` rewritten around `neoForge {}`,
`settings.gradle` with the foojay toolchain resolver, `gradle.properties` at the Phase A values:
`neo_version=21.1.251`, JEI 19.57.0.446 as `compileOnly` API + `localRuntime` full jar, Curios
9.5.1+1.21.1), renamed `mods.toml` → `neoforge.mods.toml` (and its stale `versionRange = "1.20.4"`
→ `[${mc_version}]`), and applied the `commonManifest` (with `MixinConfigs`) to the jar task —
it had been defined but never used, so production jars would have shipped without their mixins.

`validateAccessTransformers = true` immediately rejected 15 AT lines whose targets no longer
exist (`Material$Builder`, `BlockLoot`, `PotionBrewing#POTION_MIXES`, SRG-named `Ingredient`/
`FishingHook` members, `RecipeManager#byType`, …) — all dead since 1.20.x, never noticed because
NeoGradle didn't validate. Deleted; `byType` noted for ThermalCore (TODO Inbox).

First compile on 1.21.1: **849 errors / 175 files**. (The old 396/104 was against 20.6.141 and
is not comparable.) Breakdown in TODO.md; it lines up with port-plan.md §5 A.1's categories.

### 1.21.1 hop — Phase A.1 categories 1-12 (2026-09-22)

**849 errors / 175 files → 25 / 5.** One commit per root cause; the error count after each is in
its message. Categories, in the order docs/port-plan.md §5 A.1 lists them: mod metadata and bus;
`ResourceLocation` factories (343 sites, swept across all four repos at once); ItemStack NBT →
data components; the vertex/rendering rewrite; `MobEffect` holders and `PotionContents`;
enchantments → datapack objects; attribute modifier ids; crossbow ammo and projectiles; the
damage-event pipeline; blocks (plantable system, crop hooks, food, block hooks); recipes (custom
ingredients, conditions, `CraftingInput`, codecs); then a long tail of entity, item, persistence,
datagen and networking signature changes.

Decisions worth remembering, all also commented at the call site:

- **Three things could not be preserved.** CoFH's hand-rolled eager config load (`ILoadedConfig`
  is sealed to FML), `PotionColorCalculationEvent` (deleted in NeoForge 21.0 — replaced per effect
  by `createParticleOptions` returning null), and Holding's runtime "valid items" IMC hook (what
  an enchantment applies to is datapack data now; the hook logs a pointer at the
  `cofh_core:enchantable/holding` tag instead).
- **`ProxyUtils.registryAccess()`** is the stand-in for a `HolderLookup.Provider` in the
  `ItemStack`-only APIs (augments, container items) that have no registry context to thread.
- **Enchantment "enable" survives** as a disabled-id set consulted by `Utils`' level lookups;
  "treasure" does not — it is a datapack tag now.
- **CoFH's crops keep their own `CropType`**, since NeoForge deleted the shared plant-category
  vocabulary along with `PlantType`/`IPlantable`.

### The other three repos — Phase 0 done in parallel (2026-09-22)

Three subagents took ThermalCore, ThermalExpansion and ThermalDynamics through Phase 0.3/0.4, A.0
and the resources sweep while CoFHCore's categories continued here. All three now build with
ModDevGradle 2.0.147 against 21.1.251, ship `neoforge.mods.toml`, and configure cleanly
(`./gradlew projects` succeeds; compilation still blocks on CoFHCore, as expected).

The sweep turned up more than the plan anticipated, and the findings crossed between repos:

- **`forge:` → `c:` is not a pure namespace swap.** Several convention tags were also *renamed*,
  mostly pluralised: `glass`→`glass_blocks`, `sand`→`sands`, `cobblestone`→`cobblestones`,
  `gravel`→`gravels`, `stone`→`stones`, `obsidian`→`obsidians`, `string`→`strings`,
  `leather`→`leathers`, `gunpowder`→`gunpowders`. A blind swap leaves a tag that exists nowhere
  and silently matches nothing. Caught in ThermalDynamics first, propagated to the other two.
- **`forge:` ids that are not tags** go to the `neoforge` namespace, not `c:` —
  `neoforge:not`, `neoforge:mod_loaded`.
- **A recipe's condition list is keyed `neoforge:conditions`**, not `conditions`; unprefixed, the
  conditions are ignored and the recipe always loads.
- **`required = true` on a dependency is a Forge-ism** NeoForge does not read; it is
  `type = "required"`. Found in ThermalExpansion, ThermalCore and ThermalDynamics.
- **`commonManifest` was defined but never applied to the jar** in CoFHCore, ThermalCore and
  ThermalExpansion (ThermalDynamics applies it to its shadow jar) — production jars would have
  shipped without their `MixinConfigs` attribute.
- **SPLIGAN's forks are not a guide for resources**: `ThermalExpansionForNeoForge` depluralised
  only `src/main/generated`, so its ~470 hand-written machine recipes silently never load, and it
  left every `forge:` tag in place. Its Java diff is still the Phase A worklist; its `data/` tree
  is not.
- Checking a tag against `Tags.java` alone gives false negatives: `c:dyes/<colour>` and
  `c:dyed/<colour>` come from NeoForge's `DyeColor` patch, not from literal `tag(...)` calls.

Also of note: MDG's `validateAccessTransformers` rejected 13 stale lines in ThermalCore's AT
(all dead since 1.20.x), which is why its AT is now byte-identical to CoFHCore's, as the plan
prescribes.

---

## Phase A complete — all four repos on 1.21.1 (2026-09-22)

CoFHCore 849 → 0, ThermalCore 575 → 0, ThermalDynamics and ThermalExpansion 0 on their first
real compile. All four `./gradlew build` clean and reach `Done (…)` headless with no registry,
recipe or loot-table errors. Every confirmed API shape is written up in
[api-notes-1.21.1.md](api-notes-1.21.1.md).

### How it was ordered

CoFHCore's last cluster was the client models and particles — the tail of the 1.21 vertex/model
rewrite, and the part where a wrong guess compiles and then renders nothing, so each signature
came out of the sources jar first. Once it built, ThermalCore went by root-cause category the
same way (tags → event bus → recipe lookups → NBT components → persistence → enchantments →
recipe serializers → entities → armour → vertex → potions/food → projectiles → Patchouli →
datagen → brewing), one commit each with before/after counts.

ThermalDynamics and ThermalExpansion were ported **in parallel by subagents** while ThermalCore
was still broken. They could not compile — their builds `includeBuild` ThermalCore — so they
worked source-level against the sources jar and CoFHCore's already-ported code, then compiled
clean on the first try once ThermalCore landed. That parallelism was worth it: their combined
~19 commits cost no critical-path time.

### Things that only a boot could have caught

- **`ModConfigEvent` handled as one event kills the server on shutdown.** `Unloading` fires with
  the spec detached, so any `.get()` throws "Cannot get config value before config is loaded".
  The build was clean and the server reached `Done` — it died on exit.
- **A negative burn time now throws.** CoFH's `-1` "no opinion" default made every CoFH item
  throw when Thermal's Stirling dynamo enumerated furnace fuels. Must fall through to the
  default, which reads the `neoforge:furnace_fuels` data map.
- **198 ThermalCore recipes failed to parse**, and 30 more after the first fix — the result key
  is `id`, cooking results are objects, and (the part that cost a round trip) **ingredients keep
  their object form on 1.21.1**; the bare-string shape is 1.21.2. Reverting that half was the
  difference between 0 and 259 broken recipes.
- **A client-only mixin listed under `mixins`** rather than `client` fails to apply on a
  dedicated server.

### Cross-repo API the family now shares

`FluidHelper.writeFluidStack/readFluidStack` (the tile and menu packet buffers are plain
`FriendlyByteBuf`s with no registry context — casting one to `RegistryFriendlyByteBuf`, which
both SPLIGAN forks do, is a runtime `ClassCastException`), `Utils.getLevel(ItemEnchantments,
ResourceKey)`, and `saveOptional` guards in `FluidStorageCoFH#write` / `ItemStorageCoFH`, which
were calling `save` unguarded and only survived because their callers pre-filtered.

### Owed verification

Everything client-side. A headless boot cannot see model, texture or GUI breakage, and this hop
rewrote the whole vertex/model/particle surface. §A.4 of the port plan has the checklist.

One pre-existing content gap surfaced rather than regressed: ThermalExpansion's
`insolator_rubberwood_sapling` recipe references items ThermalCore never registered (filed in
its Inbox) — on 1.20.4 the old parser produced an empty recipe silently; 1.21's codec path logs
the error.


---

## Phase B started — B.0-B.2 on CoFHCore (2026-09-22)

Branch `26.1.2`, cut from `1.21.1`. Error counts are CoFHCore's `compileJava`.

- **B.0** (`6eb201f`): `gradle.properties` to 26.1.2.109 / Java 25, JEI 29, Curios 15. The
  surprise was ordering: with `validateAccessTransformers = true`, `createMinecraftArtifacts`
  fails before `compileJava` runs, so **B.9's AT half has to come first**, not last. 21 of 132
  lines had dead targets. `Slot.slot` had to go for a subtler reason: widening it shadows a local
  in vanilla's own `ItemCombinerMenu` subclasses and breaks NeoForm's recompile. Baseline after
  B.0: 2445 errors / 318 files.
- **B.1** (`6f0c7a7`): `ResourceLocation` → `Identifier` (481 sites, pure rename), package moves,
  `isClientSide()`/`getRandom()`, and `InteractionResult` collapsing its two siblings.
  2445 → 1546. A follow-up (`e7896aa`) caught seven sites the regexes missed: `isClientSide`
  reads on `tile.world()` rather than `level`, `level().random`, and one `sidedSuccess`. That
  brought it to 1537.
- **B.2** (`f02ba1b`): ids before construction (`DeferredRegisterCoFH#register(String,
  Function<Identifier, I>)`), `new BlockEntityType<>(…)`, `EntityType.Builder#build(ResourceKey)`,
  `EnumProperty<Direction>`, `KeyMapping.Category`. Only 1546 → 1545, because CoFHCore
  registers very little itself; the overload is groundwork for B.10.

B.2's key-category change was written by re-serializing `en_us.json`, which collapsed the
file's repeated `"_comment"` section headers and blank-line grouping. No key was lost (checked
by parsing both versions), but the layout was restored in `d029eb0`. **Edit lang files as text,
not through a JSON round-trip**: duplicate keys are how these files mark their sections.

Shapes are in [api-notes-26.1.2.md](api-notes-26.1.2.md). Next is B.3 (persistence).

---

## Phase A follow-up — `runData` in all four repos (2026-09-22)

Run on the `1.21.1` branches (CoFHCore temporarily checked out there, since the Thermal repos
`includeBuild('../CoFHCore')`). It was Phase A's second owed item, and it turned up more than
expected.

**The data run had never worked on 1.21.1.** All four `build.gradle`s declared `clientData()`,
which MDG only offers from 1.21.4 on, so `prepareDataRun` failed with "unknown run: clientData".
1.21.1's run type is plain `data()`; `clientData()` is right again on the 26.1.2 branch.
ThermalExpansion's run also needs `--existing-mod thermal`, because its item textures
(`slot_seal` and the rest) live in ThermalCore's namespace and `ModelBuilder#texture` rejects
textures it can't find.

**The data run is a client-dist launch, so it caught a client crash.** CoFHCore's
`LevelRendererMixin` still declared `renderLevel`'s 1.20 parameters. Mixin rejected it with
"Invalid descriptor" as soon as ThermalCore's run loaded `LevelRenderer`. Headless servers never
load the class, which is how Phase A's boots missed it, and the client pass would have died on
it. Fixed in CoFHCore `fa87214`. Checking the other client mixins against the sources jar found
`GameRendererMixin` and `MultiPlayerGameModeMixin` fine, but `MouseHandlerMixin`'s
`ordinal = 3` local capture probably drifted when `turnPlayer` gained a parameter. That one is
filed in TODO for the client pass rather than changed blind.

**The hand-migrated generated output was silently wrong in ways no boot reports.** 1.21's codecs
ignore unknown fields instead of rejecting them, so stale 1.20 JSON loads without error and
simply does something else:

| Repo | What regenerating fixed |
|---|---|
| CoFHCore (`a9ce116`) | 19 `c:` tags and a loot table still in the 1.20 plural folders (`tags/items`, `loot_tables`), which 1.21 does not read at all. Contents byte-identical; only the paths moved. |
| ThermalCore (`5a6ea0a`) | 77 recipe-unlock advancements on the 1.20 `{"tag": …}` item predicate, so each matched any item. 4 glass loot tables on the 1.20 `enchantments` `match_tool` form, so the glass dropped itself without silk touch. 2 stonecutting recipes with a top-level `count`. |
| ThermalDynamics (`c179946`) | 8 recipe-unlock advancements that were never committed. |
| ThermalExpansion (`ab68ab2`) | 9 more `{"tag": …}` advancements. Item models came out unchanged. |

The rest of the diff is cosmetic: explicit `"count": 1`, single-item lists as bare strings, no
trailing newline. The lesson for B.8 is to **regenerate rather than hand-migrate**, and diff the
result: a clean boot proves nothing about data files.
