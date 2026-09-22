# Thermal Series port plan: 1.20.4/1.20.6-partial → **1.21.1** → **26.1.2**

Written 2026-09-21 after re-verifying every claim below against the live NeoForge maven, the
NeoForge GitHub trees (`1.21.1` and `26.1.x` branches), the official primers/release posts,
the JEI/Curios/Patchouli mavens, and the code on this machine. Nothing here is carried over
from memory or from the existing `docs/` unless it was re-checked. Where an earlier assumption
turned out wrong, the correction is called out in **§1**.

This plan is written so an agent can execute it **without further research**: every version
number, artifact coordinate, URL, replacement API, and reference file is stated, and Phase 0
vendors the primary sources locally so they can be `grep`ped instead of fetched.

---

## 0. Context

Four repos, one shared session, dependency order **CoFHCore → ThermalCore → {ThermalDynamics,
ThermalExpansion}**:

| Repo | Files / lines | State today |
|---|---|---|
| `CoFHCore` | 526 / 46k | branch `1.20.6`, mid-hop, 396 compile errors, 12 commits of 1.20.6 work |
| `ThermalCore` | 292 / 34k | branch `1.20.6` = 1.20.4 HEAD + uncommitted `gradle.properties` bump |
| `ThermalDynamics` | 105 / 11k | same |
| `ThermalExpansion` | 108 / 8k | on branch `1.20.4`, uncommitted bump |

Goal (Joel, 2026-09-21): get all four to **1.21.1**, then to **26.1.2**, skipping every other
intermediate; the plan must be the most efficient route and detailed enough to execute without
research.

---

## 1. What re-verification changed (read before anything else)

1. **Do not finish the 1.20.6 hop.** 1.20.6 is not a target. Every remaining 1.20.6 category
   is also a 1.21.1 category (changes are cumulative), so retarget CoFHCore's `1.20.6` branch
   straight to NeoForge **21.1.251** and fix the remaining ~396 errors once, against 1.21.1's
   shapes. Nothing already done is lost (networking, particles, entity, storage, Block#use are
   all still correct on 1.21.1). Some of it *will* be redone at 26.1 (tool-item classes and
   `ArmorItem` are deleted outright in 1.21.5; enchantments become datapack objects in 1.21) —
   unavoidable, since 1.21.1 is a required stop.
2. **`@Mod.EventBusSubscriber` → `@EventBusSubscriber` (top-level, `net.neoforged.fml.common`)
   and `Bus.FORGE` → `Bus.GAME` are real 1.20.5 changes** (NeoForge 20.5 release notes,
   "Event System"). `docs/TODO.md` item 4 and api-notes' "javac cascade" conclusion are wrong.
   24 files across the four repos.
3. **`META-INF/mods.toml` must be renamed `neoforge.mods.toml`** (mandatory since 20.5; jars
   without it are *silently skipped*). All four repos still ship `mods.toml`. Runtime, not
   compile — would have "worked" until the first `runServer`.
4. **Switch the build plugin from NeoGradle userdev to ModDevGradle 2.0.147** in Phase 0,
   once. Reasons: it is what Pyronetics (same machine, same author) builds 26.1.2.109 with today
   (verified working toolchain, JDK 25 provisioning, caches); it is the official MDK plugin; it
   produces a **sources** jar of patched Minecraft (`build/moddev/artifacts/minecraft-patched-<ver>-sources.jar`)
   which is better than `javap` for confirming shapes; `validateAccessTransformers = true`
   catches stale AT lines at build time. NeoGradle 7.1.39 claims 26.1 support but is unverified
   here. One switch beats two.
5. **Patchouli for 26.1 exists only as a GitHub release, not on maven** (Joel, 2026-09-21):
   `https://github.com/VazkiiMods/Patchouli/releases/download/release-26.1-94-beta/patchouli-neoforge-26.1-94.jar`
   (mod version `26.1-94`, ranges `minecraft [26.1,26.2)`, `neoforge [26.1.0.1-beta,26.2)`; the
   jar contains `vazkii.patchouli.api.IComponentProcessor/IVariable/IVariableProvider` — the
   three classes `ThermalCore/…/compat/patchouli` imports). The BlameJared maven still stops at
   `1.21.1-93-NEOFORGE`. So ThermalCore **keeps** Patchouli in Phase B via a checked-in
   `libs/patchouli-neoforge-26.1-94.jar` file dependency (§B.0), switching back to a maven
   coordinate when one appears. Verify the 3 compat processors against that jar's API.
6. **Pyronetics (`../Pyronetics`) is a verified 26.1.2.109 implementation** of almost every
   subsystem this port must migrate: MDG build, screens on `GuiGraphicsExtractor`, block-entity
   renderers on the submit API, the transfer API for energy/items/fluids, fluid registration
   with `RegisterFluidModelsEvent`, recipes with `ItemStackTemplate`, networking, data
   attachments, loot `copy_components`. Its `docs/api-notes-26.1.2.md` is 631 lines of confirmed
   shapes. **It is the first place to look for any 26.1 shape, before the primer.** (MIT, Joel's
   own code — copying is fine.)
7. **SPLIGAN's forks are a minimal-diff 1.21.1 port of ThermalCore (158 files changed) and
   ThermalExpansion**, already cloned as `../ThermalCoreForNeoForge` and
   `../ThermalExpansionForNeoForge`. A **`ThermalDynamicsForNeoForge`** (1.21.1) also exists on
   GitHub and is not cloned — Phase 0 clones it. They compiled against a private `cofh_core`
   1.21.1 build we don't have, so CoFHCore still has to be ported by hand; the three Thermal
   repos' Phase A is largely "review and apply SPLIGAN's diff".
8. **Recipes are no longer synced to clients (1.21.2+).** ThermalCore's client-side recipe
   caches (`RecipesUpdatedEvent`, `getRecipeManager()` from GUIs, the JEI plugin) need
   NeoForge's opt-in sync: `OnDatapackSyncEvent#sendRecipes(RecipeType...)` on the server +
   `RecipesReceivedEvent#getRecipeMap()` on the client. `Level#getRecipeManager()` is gone;
   `ServerLevel#recipeAccess()` returns the `RecipeManager`, the client's `RecipeAccess` cannot
   look recipes up at all.
9. **392 resource files reference `forge:` tags** (`forge:ingots/iron`, `forge:slag`, …). Those
   never existed on NeoForge (the convention namespace is `c:` since 20.5). Fix in Phase A.
10. The "full chain" listed in the old progress log (1.21 → 1.21.1 → … → 26.1) is correct as a
    *reading* list but is not the *build* path. The 26.1 release post itself says: porting from
    1.21.1, read the 21.2/21.4/21.5/21.6/21.9/21.11 notes and jump.

---

## 2. Verified versions and coordinates (use these verbatim)

| Thing | Phase A (1.21.1) | Phase B (26.1.2) | Verified how |
|---|---|---|---|
| Minecraft | `1.21.1` | `26.1.2` | — |
| NeoForge | **`21.1.251`** | **`26.1.2.109`** | `maven.neoforged.net` metadata, 2026-09-21 (latest of each line) |
| Java toolchain | 21 | **25** (`/Library/Java/JavaVirtualMachines/temurin-25.jdk` is installed) | 26.1 release post; `java_home -V` |
| Gradle | 9.2.1 (already) | 9.2.1 (≥ 9.1.0 required) | 26.1 post |
| ModDevGradle | `net.neoforged.moddev` **`2.0.147`** | same | maven metadata; Pyronetics builds with it |
| NeoForm (if ever needed) | — | `26.1.2-1` (new `<mc>-<build>` scheme) | maven |
| JEI | `mezz.jei:jei-1.21.1-neoforge:19.57.0.446` (`-common-api`/`-neoforge-api` for compileOnly) | `mezz.jei:jei-26.1.2-neoforge:29.40.0.101` (+ `-common-api`, `-neoforge-api`) | `maven.blamejared.com` |
| Curios | `top.theillusivec4.curios:curios-neoforge:9.5.1+1.21.1` | `15.0.0+26.1.2` | `maven.theillusivec4.top` |
| Patchouli | `vazkii.patchouli:Patchouli:1.21.1-93-NEOFORGE` | GitHub-release jar only: `patchouli-neoforge-26.1-94.jar` (`release-26.1-94-beta`), as a `libs/` file dependency | `maven.blamejared.com`; GitHub releases API |
| Parchment (optional) | `1.21.1` / `2024.11.17` | not needed (Mojang names ship unobfuscated) | AllTheOres `1.21`; 26.1 post |
| NeoForge source tree | github `neoforged/NeoForge` branch `1.21.1` | branch `26.1.x` | tree API |
| NeoForge docs | `versioned_docs/version-1.21.1/**` | `docs/**` (current site) | `neoforged/Documentation` |

Sources jar after the first MDG build: `build/moddev/artifacts/minecraft-patched-<neo_version>-sources.jar`
(plus `-merged.jar`, and the plain `.jar` for `javap`). NeoForge's own sources:
`~/.gradle/caches/modules-2/files-2.1/net.neoforged/neoforge/<ver>/*/neoforge-<ver>-sources.jar`.
`21.1.219` and `26.1.2.109` are already in that cache.

---

## 3. Strategy: why this route is the efficient one

- **Two compile-to-clean cycles instead of fourteen.** Each intermediate stop costs a full
  triage/fix/verify cycle; API shapes that only existed between 1.21.2 and 1.21.11 (e.g. the
  1.21.2 `BakedModel` overrides, the 1.21.5 `GuiGraphics.blit(RenderType::guiTextured…)`, the
  1.21.6 `render(GuiGraphics…)` GUI phase) would be written and thrown away.
- **Fix by root-cause category, not by file** (existing working method, still right). Phase B
  categories are ordered so foundations (renames → registration → persistence → transfer →
  items/recipes → client) come first; the client is last because it depends on everything
  else compiling.
- **Use working code as the oracle**: Pyronetics for 26.1.2, SPLIGAN forks for 1.21.1,
  vanilla's own sources jar for everything. Reading a decompiled `HopperScreen` beats any doc.
- **Every category ends with a commit, an error count, and a line in `docs/progress-log.md`.**

Branch layout (matches the existing branch-per-target convention):

- `1.21.1` branch in each repo — CoFHCore from `1.20.6` HEAD (keeps the 12 commits), the other
  three from their `1.20.4` HEAD. `1.20.6` branches are left as-is (dead ends, not deleted).
- `26.1.2` branch in each repo, created from `1.21.1` once Phase A is verified.

---

## 4. Phase 0 — Preparation (all four repos, before any porting)

### 0.1 Vendor the reference material (local-only, gitignored)

Create `CoFHCore/docs/reference/` (add `docs/reference/` to `.gitignore` next to
`docs/context/`) and commit a script `CoFHCore/scripts/fetch_reference.sh` that does exactly:

```bash
#!/bin/bash
# Vendors the porting reference material this plan relies on. Re-runnable.
set -e
D="$(cd "$(dirname "$0")/.." && pwd)/docs/reference"; mkdir -p "$D/primers" "$D/neo-notes" "$D/docs-26.1" "$D/docs-1.21.1" "$D/neoforge-src"
# Vanilla migration primers (CC BY 4.0, ChampionAsh5357)
for v in 1.20.5 1.20.6 1.21 1.21.1 1.21.2 1.21.4 1.21.5 1.21.6 1.21.7 1.21.8 1.21.9 1.21.10 1.21.11 26.1; do
  curl -sfL "https://raw.githubusercontent.com/neoforged/.github/main/primers/$v/index.md" -o "$D/primers/$v.md"
done
# NeoForge release/"Neo Changes" posts (these ARE the docs site's "Neo Changes" pages — iframes of neoforged.net/news)
for n in 20.5release 21.0release 21.2release 21.4release 21.5release 21.6release 21.9release 21.9-transfer-rework 21.11release 26.1release; do
  curl -sfL "https://raw.githubusercontent.com/neoforged/websites/main/content/news/$n.md" -o "$D/neo-notes/$n.md"
done
# NeoForge docs: current (=26.1) and archived 1.21.1
for p in blockentities/ber blockentities/index blocks/index concepts/events concepts/registries datastorage/attachments datastorage/codecs datastorage/nbt datastorage/saveddata datastorage/valueio entities/index entities/renderer inventories/capabilities inventories/container inventories/menus inventories/transactions items/armor items/datacomponents items/index items/interactions items/tools misc/config misc/identifier misc/keymappings networking/index networking/payload networking/streamcodecs rendering/feature rendering/particles rendering/screens resources/client/models/index resources/client/models/items resources/client/models/modelloaders resources/client/models/modelsystem resources/client/models/datagen resources/client/particles resources/client/textures resources/server/recipes/index resources/server/recipes/custom resources/server/recipes/ingredients resources/server/loottables/index resources/server/loottables/custom resources/server/tags resources/server/datamaps/index resources/server/enchantments/index advanced/accesstransformers; do
  mkdir -p "$D/docs-26.1/$(dirname $p)"; curl -sfL "https://raw.githubusercontent.com/neoforged/Documentation/main/docs/$p.md" -o "$D/docs-26.1/$p.md" || true
done
for p in blockentities/ber blocks/index concepts/events concepts/registries datastorage/attachments datastorage/nbt gui/menus gui/screens inventories/capabilities items/datacomponents items/index items/interactionpipeline items/tools misc/keymappings misc/resourcelocation networking/payload resources/client/models/bakedmodel resources/client/models/modelloaders resources/server/recipes/index resources/server/recipes/ingredients resources/server/loottables/index resources/server/tags resources/server/enchantments/index; do
  mkdir -p "$D/docs-1.21.1/$(dirname $p)"; curl -sfL "https://raw.githubusercontent.com/neoforged/Documentation/main/versioned_docs/version-1.21.1/$p.md" -o "$D/docs-1.21.1/$p.md" || true
done
# NeoForge source trees as file lists (for "does class X still exist / where did it go")
curl -sf "https://api.github.com/repos/neoforged/NeoForge/git/trees/1.21.1?recursive=1" | python3 -c 'import json,sys;[print(t["path"]) for t in json.load(sys.stdin)["tree"] if t["path"].endswith(".java")]' > "$D/neoforge-src/files-1.21.1.txt"
curl -sf "https://api.github.com/repos/neoforged/NeoForge/git/trees/26.1.x?recursive=1"  | python3 -c 'import json,sys;[print(t["path"]) for t in json.load(sys.stdin)["tree"] if t["path"].endswith(".java")]' > "$D/neoforge-src/files-26.1.txt"
# Individual NeoForge 26.1.x files worth having verbatim
for f in src/main/java/net/neoforged/neoforge/capabilities/Capabilities.java src/client/java/net/neoforged/neoforge/client/extensions/BlockStateModelExtension.java src/client/java/net/neoforged/neoforge/client/model/DynamicBlockStateModel.java src/main/java/net/neoforged/neoforge/common/extensions/IBlockEntityExtension.java src/client/java/net/neoforged/neoforge/client/event/RenderLevelStageEvent.java src/client/java/net/neoforged/neoforge/client/event/ModelEvent.java src/client/java/net/neoforged/neoforge/client/event/RecipesReceivedEvent.java src/main/java/net/neoforged/neoforge/items/IItemHandler.java src/main/java/net/neoforged/neoforge/energy/IEnergyStorage.java tests/src/main/java/net/neoforged/neoforge/oldtest/client/model/MegaModelTest.java tests/src/main/java/net/neoforged/neoforge/oldtest/client/model/NewModelLoaderTest.java tests/src/main/java/net/neoforged/neoforge/oldtest/client/model/DynBucketModelTest.java tests/src/main/java/net/neoforged/neoforge/oldtest/fluid/NewFluidTest.java tests/src/main/java/net/neoforged/neoforge/oldtest/client/rendering/CustomParticleTypeTest.java tests/src/main/java/net/neoforged/neoforge/oldtest/item/CustomFluidContainerTest.java; do
  mkdir -p "$D/neoforge-src/26.1/$(dirname $f)"; curl -sfL "https://raw.githubusercontent.com/neoforged/NeoForge/26.1.x/$f" -o "$D/neoforge-src/26.1/$f" || true
done
# JEI 29 API (26.1 branch) file list + key interfaces
curl -sf "https://api.github.com/repos/mezz/JustEnoughItems/git/trees/26.1?recursive=1" | python3 -c 'import json,sys;[print(t["path"]) for t in json.load(sys.stdin)["tree"] if "/api/" in t["path"] and t["path"].endswith(".java")]' > "$D/neoforge-src/jei-26.1-api-files.txt"
for f in Common/src/api/java/mezz/jei/api/IModPlugin.java Common/src/api/java/mezz/jei/api/registration/IRecipeRegistration.java Common/src/api/java/mezz/jei/api/recipe/category/IRecipeCategory.java Common/src/api/java/mezz/jei/api/registration/ISubtypeRegistration.java Common/src/api/java/mezz/jei/api/helpers/IJeiHelpers.java Common/src/api/java/mezz/jei/api/helpers/IGuiHelper.java Common/src/api/java/mezz/jei/api/gui/builder/IRecipeLayoutBuilder.java Common/src/api/java/mezz/jei/api/recipe/types/IRecipeType.java Common/src/api/java/mezz/jei/api/registration/IRecipeCatalystRegistration.java Common/src/api/java/mezz/jei/api/registration/IGuiHandlerRegistration.java Common/src/api/java/mezz/jei/api/registration/IAdvancedRegistration.java Common/src/api/java/mezz/jei/api/recipe/advanced/IRecipeManagerPlugin.java Common/src/api/java/mezz/jei/api/ingredients/subtypes/ISubtypeInterpreter.java Common/src/api/java/mezz/jei/api/helpers/IPlatformFluidHelper.java NeoForge/src/api/java/mezz/jei/api/neoforge/NeoForgeTypes.java; do
  mkdir -p "$D/neoforge-src/jei-26.1/$(dirname $f)"; curl -sfL "https://raw.githubusercontent.com/mezz/JustEnoughItems/26.1/$f" -o "$D/neoforge-src/jei-26.1/$f" || true
done
echo "done: $D"
```

Also clone the missing SPLIGAN reference: `git clone https://github.com/SPLIGAN/ThermalDynamicsForNeoForge ../ThermalDynamicsForNeoForge`
(default branch `1.20.x` is the 1.21.1 port, like the other two). AllTheOres already has
`upstream/26.1` fetched (`git -C ../AllTheOres show upstream/26.1:<path>`) — a second 26.1
reference for worldgen/tags/datagen.

### 0.2 Branches and housekeeping

- CoFHCore: `git checkout -b 1.21.1` from `1.20.6`. Others: `git checkout -b 1.21.1` from
  `1.20.4` (ThermalExpansion is still on `1.20.4`; the never-checked-out local `1.20.6`
  branches can stay).
- Add `.DS_Store` to every repo's `.gitignore`; remove the stray tracked/untracked ones.
- Commit the (already present, uncommitted) `gradle.properties` bumps only *after* editing
  them to the Phase A values (§4.3) — never commit the 1.20.6 values.

### 0.3 Build switch: NeoGradle → ModDevGradle 2.0.147

Template = `../Pyronetics/build.gradle` + `settings.gradle` + `gradle.properties`, with the
CoFH-specific bits (publishing, curseforge/modrinth, jar signing, `mixins.cofhcore.json`
manifest attribute) carried over from the current `build.gradle`. Per repo:

`settings.gradle`:
```groovy
pluginManagement { repositories { gradlePluginPortal(); maven { url 'https://maven.neoforged.net/releases' } } }
plugins { id 'org.gradle.toolchains.foojay-resolver-convention' version '1.0.0' }
rootProject.name = 'CoFHCore'
// ThermalCore / ThermalDynamics / ThermalExpansion keep their existing includeBuild(...) blocks:
// includeBuild('../CoFHCore') { dependencySubstitution { substitute module('com.teamcofh:cofh_core') using project(':') } }
// (ThermalDynamics/ThermalExpansion also '../ThermalCore' → 'com.teamcofh:thermal_core')
```

`build.gradle` (essentials; keep the rest of the CoFH script):
```groovy
plugins {
    id 'java-library'; id 'maven-publish'; id 'idea'
    id 'com.matthewprenger.cursegradle' version '1.4.0'
    id 'com.modrinth.minotaur' version '2.+'
    id 'net.neoforged.moddev' version '2.0.147'
}
java.toolchain.languageVersion = JavaLanguageVersion.of(java_version as int)   // 21 in Phase A, 25 in Phase B
sourceSets.main.resources.srcDirs += 'src/main/generated'
tasks.withType(JavaCompile).configureEach { options.compilerArgs << '-Xmaxerrs' << '100000'; options.encoding = 'UTF-8' }

neoForge {
    version = project.neo_version
    validateAccessTransformers = true          // fails the build on stale AT lines — wanted
    // accesstransformer.cfg at the default path is picked up automatically
    runs {
        client { client(); systemProperty 'neoforge.enabledGameTestNamespaces', project.mod_id }
        server { server(); programArgument '--nogui' }
        gameTestServer { type = 'gameTestServer' }
        data {
            data()            // 1.21.1; B.0 switches it to clientData() (1.21.4+)
            programArguments.addAll '--mod', project.mod_id, '--all', '--output', file('src/main/generated/').absolutePath, '--existing', file('src/main/resources/').absolutePath
        }
        configureEach { systemProperty 'forge.logging.markers', 'REGISTRIES'; logLevel = org.slf4j.event.Level.DEBUG }
    }
    mods { "${mod_id}" { sourceSet sourceSets.main } }
}
configurations { runtimeClasspath.extendsFrom localRuntime }   // for optional runtime-only mods (JEI full jar)
repositories { mavenLocal(); maven { url 'https://maven.blamejared.com/' }; maven { url 'https://maven.theillusivec4.top/' }; maven { url 'https://maven.covers1624.net/' } }
dependencies {
    // CoFHCore:
    compileOnly "mezz.jei:jei-${mc_version}-common-api:${jei_version}"
    compileOnly "mezz.jei:jei-${mc_version}-neoforge-api:${jei_version}"
    localRuntime "mezz.jei:jei-${mc_version}-neoforge:${jei_version}"
    implementation("top.theillusivec4.curios:curios-neoforge:${curios_version}") { transitive = false }
    // ThermalCore/TD/TE additionally:  implementation "com.teamcofh:cofh_core:${mc_version}-${cofh_core_version}.0"  (+ thermal_core) — resolved by includeBuild substitution
}
// mods.toml expansion: rename the file and the filesMatching pattern
processResources { filesMatching('META-INF/neoforge.mods.toml') { expand 'file': ['jarVersion': mod_version], 'mc_version': mc_version, 'neo_version': neo_version, 'lang_version': '2' } }
```

Notes that matter:
- Keep the `'MixinConfigs': 'mixins.cofhcore.json'` manifest attribute in CoFHCore's `jar`.
- MDG's `data { data() }` no longer exists for 1.21.4+, but `clientData()` doesn't exist *before*
  it: on 1.21.1 `prepareDataRun` fails with "unknown run: clientData" (found 2026-09-22, see the
  progress log's runData entry). Use `data()` on 1.21.1 and switch in B.0.
- Delete `build/`, `.gradle/` before the first MDG sync (`./gradlew --stop; rm -rf build .gradle`).
- Included builds: their jars have `neoforge.mods.toml`, so MDG loads them as mods in runs
  (MDG README, "External Dependencies: Runs"). Verify once with `runServer` in ThermalCore.
- The old `runs { modSource … }`, `minecraft { accessTransformers… }` NeoGradle blocks go away.

### 0.4 `mods.toml` → `neoforge.mods.toml` (all four)

`git mv src/main/resources/META-INF/mods.toml src/main/resources/META-INF/neoforge.mods.toml`;
`versionRange` for `minecraft` = `"[1.21.1]"` (Phase B: `"[26.1.2]"`), for `neoforge` =
`"[${neo_version},)"`. Fix CoFHCore's stale hardcoded `versionRange = "1.20.4"` for minecraft.

### 0.5 Docs

Update each repo's `CLAUDE.md` "Current state"/"Next step" to point at this plan
(`/Users/joel/.claude/plans/i-want-you-to-parsed-spark.md`, and copy it to
`CoFHCore/docs/port-plan.md` so it lives in the repo), rewrite `docs/TODO.md` around the phases
below, and append a progress-log entry recording the decisions in §1.

---

## 5. Phase A — everything to 1.21.1 (NeoForge 21.1.251)

Order: CoFHCore to clean compile → `runServer` → ThermalCore → ThermalDynamics + ThermalExpansion.

### A.0 Version bump (each repo, `gradle.properties`)
```
java_version=21
mc_version=1.21.1
minecraft_version_range=[1.21.1]
neo_version=21.1.251
jei_version=19.57.0.446
curios_version=9.5.1+1.21.1
patchouli_version=1.21.1-93-NEOFORGE        # ThermalCore only
```
First `./gradlew compileJava` gives the true baseline count; log it.

### A.1 CoFHCore — categories in order

Each item: what breaks → replacement → where confirmed. "primer 1.20.5 §X" means
`docs/reference/primers/1.20.5.md`; "notes 21.0" means `docs/reference/neo-notes/21.0release.md`.
Confirm every exact signature in `build/moddev/artifacts/minecraft-patched-21.1.251-sources.jar`
(`unzip -p … net/minecraft/…/Class.java | less`) before writing code.

1. **Mod metadata & bus** (S). `@Mod.EventBusSubscriber` → `@EventBusSubscriber`
   (`net.neoforged.fml.common.EventBusSubscriber`), `Bus.FORGE` → `Bus.GAME` (14 files).
   `ModLoadingContext.get().registerConfig(...)` → `ModContainer#registerConfig` (the mod
   constructor receives `ModContainer`; `ConfigManager` has 5 sites). `FMLJavaModLoadingContext`
   is gone (notes 21.0 "Deprecations").
2. **`ResourceLocation` construction** (M, 393 lines/97 files — regex). `new ResourceLocation(a, b)`
   → `ResourceLocation.fromNamespaceAndPath(a, b)`; `new ResourceLocation(s)` →
   `ResourceLocation.parse(s)`; `new ResourceLocation("minecraft", p)` → `withDefaultNamespace(p)`
   (primer 1.21 "ResourceLocation, now Private"). Do it in all four repos in one sweep now;
   Phase B's `Identifier` rename is then a pure token replace.
3. **ItemStack NBT → data components** (L, ~140 sites/51 files family-wide). Rules already in
   `docs/api-notes-1.20.6.md` (ItemHelper/ItemStorageCoFH). Mechanical mapping:
   `getTag()/hasTag()/getOrCreateTag()` → `get(DataComponents.CUSTOM_DATA)`/`has(...)`/
   `CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> …)`; `getTagElement(TAG_BLOCK_ENTITY)`
   → `stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag()` (SPLIGAN
   does exactly this in `EnergyCellBakedModel`); `setHoverName/hasCustomHoverName` →
   `DataComponents.CUSTOM_NAME`; `getEnchantmentTags()` → `stack.getEnchantments()`
   (`ItemEnchantments`). Watch for the copy-on-read semantics change (TODO item 1). Keep CUSTOM_DATA
   blobs for now; do **not** design new component types in this phase.
4. **Vertex/rendering API** (M; the "Matrix3f→Pose ×44" bucket). 1.20.5 folded the
   `Matrix4f`+`Matrix3f` pair into `PoseStack.Pose`, and 1.21 renamed everything (primer 1.21
   "Vertex System"): `vertex(...)`→`addVertex`, `color`→`setColor`, `uv`→`setUv`,
   `overlayCoords`→`setOverlay`, `uv2`→`setLight`, `normal`→`setNormal`, `endVertex()` deleted,
   `BufferBuilder#end()`→`buildOrThrow()` (returns `MeshData`), `Tesselator.getInstance().begin(mode, format)`
   returns the `BufferBuilder`, `BufferUploader.drawWithShader(meshData)`,
   `ParticleRenderType#begin(Tesselator, TextureManager)` now returns the `BufferBuilder` and
   `end()` is gone, `RenderTarget#blitToScreen` → `blitAndBlendToScreen` (1.21.2 — not yet).
   Files: `VFXHelper`, `RenderTypes`, `CoreRenderType`, `PostBuffer/PostEffect`, particles,
   `ElectricFieldRenderer`, `ModelUtils`, quad helpers. Shaders (`ShaderInstance`,
   `RegisterShadersEvent`, core-shader JSONs) still exist on 1.21.1 — leave them.
5. **`MobEffect` → `Holder<MobEffect>`** (M, 34 sites). `MobEffects.*` are holders in 1.21;
   `MobEffectInstance(Holder, …)`; `PotionUtils` → `PotionContents` (`DataComponents.POTION_CONTENTS`,
   `PotionContents#getAllEffects/customEffects/potion()`); `AreaEffectCloud#setPotion` → `setPotionContents`
   (primer 1.20.5 lines ~600, 878-891, 971). `PotionColorCalculationEvent` was deleted in 21.0 —
   use `PotionContents#getColor()`; the `EffectEvents` listener that used it is removed.
6. **Enchantments are datapack objects** (M). `Enchantment` cannot be subclassed for
   behaviour; delete `EnchantmentCoFH`, `EnchantmentOverride`, `DamageEnchantmentCoFH`,
   `CoreEnchantConfig`'s dead options, `CoreEnchantments.Types` (all confirmed unused in the
   family by api-notes). `HoldingEnchantment` becomes `data/cofh_core/enchantment/holding.json`
   (primer 1.21 "The Enchantment Datapack Object" has the full JSON shape) plus a
   `ResourceKey<Enchantment> HOLDING = ResourceKey.create(Registries.ENCHANTMENT, …)`.
   Reading the level without registry access: iterate `stack.getEnchantments().entrySet()` and
   match `entry.getKey().is(HOLDING)` — do this in one helper (`EnchantmentHelperCoFH.getLevel(stack, key)`)
   and use it everywhere the old `EnchantmentHelper.getItemEnchantmentLevel(HOLDING, stack)` was.
   `Enchantments.BLOCK_FORTUNE` → `Enchantments.FORTUNE` (a `ResourceKey`), `INFINITY_ARROWS`→`INFINITY`,
   `FALL_PROTECTION`→`FEATHER_FALLING`; `EnchantmentHelper#getDamageBonus(stack, EntityType)`;
   loot: `LootItemRandomChanceWithLootingCondition` → `LootItemRandomChanceWithEnchantedBonusCondition`,
   `EnchantmentPredicate(HolderSet<Enchantment>, MinMaxBounds.Ints)` (TODO items 6, 10).
7. **Attribute modifiers** (S). `AttributeModifier(ResourceLocation id, double, Operation)`
   — no UUID/name; `AttributeInstance#hasModifier/removeModifier(ResourceLocation)`
   (primer 1.21 "Attribute Modifiers"). `CoreMobEffects`, `Utils`, TC's item attribute code.
8. **Tools / armor / crossbow** (M). 1.21.1 still has `DiggerItem`/`SwordItem`/`ArmorItem`/`Tier`
   (they die in Phase B), so keep the 1.20.6 conversions. Finish `CrossbowItemCoFH` on
   `DataComponents.CHARGED_PROJECTILES` (`ChargedProjectiles.of(list)`, `isEmpty`, `getItems`)
   — read vanilla `CrossbowItem` in the sources jar; `ItemStack#hurtAndBreak(int, ServerLevel, @Nullable ServerPlayer, Consumer<Item>)`
   and `hurtAndBreak(int, LivingEntity, EquipmentSlot)`; `Item#getUseDuration(ItemStack, LivingEntity)`;
   `ProjectileWeaponItem#shoot(ServerLevel, …)`; `Item#getAttackDamageBonus(Entity, float, DamageSource)`
   (primer 1.21 "Changes").
9. **Events** (S). `LivingAttackEvent`→`LivingIncomingDamageEvent`, `LivingHurtEvent`→
   `LivingDamageEvent.Pre` (modify) / `.Post` (react), `ShieldBlockEvent`→`LivingShieldBlockEvent`,
   `EntityItemPickupEvent`→`ItemEntityPickupEvent.Pre`, `SpawnPlacementRegisterEvent`→
   `RegisterSpawnPlacementsEvent`, `TickEvent` leftovers in other repos → `*TickEvent.Pre/Post`,
   remaining `event.getResult() == DENY` sites → each event's own API (`Event.Result` removed in 20.6;
   TODO item 8: open the event class in NeoForge sources).
10. **Blocks** (S). Crop hooks: `CommonHooks.onCropsGrowPre` → `CommonHooks.canCropGrow(level, pos, state, boolean)`,
    `onCropsGrowPost` → `fireCropGrowPost(level, pos, state)` (confirmed in NeoForge `1.21.1` `CommonHooks.java:919-925`).
    `IPlantable`/`PlantType` were deleted in 21.0.39 → `SpecialPlantable` on the item +
    `IBlockExtension#canSustainPlant(state, level, pos, facing, plantState)` on the soil
    (`SoilBlock`, `TilledSoilBlock`, `CropBlock*`, TC `ChargedSoilBlock`, `FertilizerItem`).
    `FoodProperties` is a record: `nutrition()`, `saturation()`, `effects()` (`FoodProperties.PossibleEffect`),
    and the 1.21 ctor takes a `usingConvertsTo` stack. `LivingEntity#getArmorSlots()` still exists
    in 1.21.1 (it is `Entity` that lost it — TODO item 11).
11. **Recipes** (M). Generic `Recipe<C extends Container>` → `Recipe<T extends RecipeInput>`
    (`CraftingInput`, `SingleRecipeInput`; `CraftingContainer#asCraftInput()`),
    `RecipeSerializer#codec()` returns `MapCodec` (already?) and `streamCodec()` replaces
    `fromNetwork/toNetwork` (`Ingredient.CONTENTS_STREAM_CODEC`, `FluidStack.STREAM_CODEC`),
    `IShapedRecipe` no longer exists in NeoForge 1.21.1 — `ShapedRecipe#getWidth/getHeight` are
    on the vanilla class (`ShapedRecipeInternal`, `ShapedPotionNBTRecipe`),
    `RecipeManager#getRecipeFor(type, input, level)` returns `Optional<RecipeHolder<T>>`.
12. **Loot/datagen** (S). `BlockLootSubProvider(Set<Item>, FeatureFlagSet, HolderLookup.Provider)`,
    `LootTableProvider.SubProviderEntry(Function<Provider, LootTableSubProvider>, LootContextParamSet)`,
    `LootContextParams.KILLER_ENTITY`→`ATTACKING_ENTITY`, `LootContext.EntityTarget.KILLER`→`ATTACKER`
    (primer 1.21 "Changes"). `ToolActions`→`ItemAbilities`/`ItemAbility` (notes 21.0).
    `DamageSource#isIndirect()` → `!isDirect()`.
13. **Mixins** (S/M). For each of the 10 mixin classes, open the target class in the sources
    jar and re-check every `@Inject`/`@Redirect` target descriptor; `HorseArmorItemMixin` targets
    `AnimalArmorItem`. `mixins.cofhcore.json` lists only 8 — decide whether the other 2 are dead.
14. **Access transformers** (S). `validateAccessTransformers = true` reports every stale line;
    fix or delete each (`renderHitOutline` signature, `ChatComponent#addMessage` overloads, etc.).
    The three Thermal repos ship copies of the same list — keep them byte-identical to CoFHCore's.
15. **Resources** (S, scriptable). Depluralise folders in `src/main/resources` and
    `src/main/generated` of all four repos: `data/*/tags/{blocks,items,fluids,entity_types}` →
    `tags/{block,item,fluid,entity_type}`, `recipes`→`recipe`, `advancements`→`advancement`,
    `loot_tables`→`loot_table` (primer 1.21). Replace `"forge:` → `"c:` in all data JSON
    (392 files; then grep for tags that aren't `c:` conventions, e.g. `c:slag`, and make sure the
    mod defines them under `data/c/tags/item/`). Also in Java constants (`cofh.lib.util.constants`).
16. **Curios** (S). `CuriosIntegration` against Curios 9.5.1 API — read
    `top/theillusivec4/curios/api/CuriosApi` in the resolved jar; stub if the API moved.

Exit criteria: `./gradlew build` clean; `../Pyronetics/scripts/verify_runserver.sh ../CoFHCore /tmp/cofh-server.log`
prints "SERVER STARTED SUCCESSFULLY" with `cofh_core` constructed; a `runClient` boot to the
title screen and into a world (Joel runs it); write `docs/api-notes-1.21.1.md` in the same
style as the 1.20.6 file; commit per category with `1.21.1: <category>` messages.

### A.2 ThermalCore

1. Bump + MDG (Phase 0) + `neoforge.mods.toml`; `implementation 'vazkii.patchouli:Patchouli:1.21.1-93-NEOFORGE'`.
2. `diff -ru ../ThermalCore/src ../ThermalCoreForNeoForge/src` (158 files). Apply the hunks that
   are 1.21.1 API changes (verified sample: `FluidStack.copyWithAmount`, `ResourceLocation.parse`,
   `BLOCK_ENTITY_DATA` component); skip their build/IDE noise. Where their hunk calls a
   CoFHCore method we named differently in A.1, adapt to ours.
3. Compile, then the same category order as A.1 for whatever remains (their fork predates some
   of our CoFHCore renames; expect `IFilter`/`HolderLookup.Provider` threading and
   `SimpleItemInv#read/write(Provider)` call sites — see api-notes-1.20.6 "HolderLookup.Provider threading").
4. Resources: §A.1.15 sweep (this repo has the bulk of the `forge:` tags and plural folders).
5. `verify_runserver.sh`; commit.

### A.3 ThermalExpansion, then ThermalDynamics

Same procedure with `../ThermalExpansionForNeoForge` and the freshly cloned
`../ThermalDynamicsForNeoForge`. ThermalExpansion's 470 hand-written machine recipes: the
`forge:`→`c:` sweep applies; the `{"item":…,"count":n}` ingredient shape is parsed by CoFH's own
`RecipeJsonUtils`, not by `Ingredient.CODEC`, so it survives (re-check `RecipeJsonUtils.parseIngredient`
compiles — `Ingredient.fromJson` is gone since 1.20.5; use `Ingredient.CODEC`/`Ingredient.of(...)`).

### A.4 Phase A done when
All four `build` clean, all four boot headless, a client session with a ThermalExpansion
machine GUI open, JEI showing a machine recipe, and Patchouli book opening. Tag `1.21.1` in each
repo is optional; branch `26.1.2` is created from here.

---

## 6. Phase B — 1.21.1 → 26.1.2 (NeoForge 26.1.2.109)

Expect **thousands** of errors on the first compile; that is the deal with a direct jump. Do the
sweeps in the order below — each one is designed to be `sed`/IDE-refactor-able and to unmask
the next. Do all four repos' *mechanical* sweeps (B.1) at once; everything else in dependency
order. **Reference precedence for any shape: `../Pyronetics` source + its `docs/api-notes-26.1.2.md`
→ vanilla/NeoForge sources jar → `docs/reference/primers/26.1.md` (and 1.21.2…1.21.11 for the
path) → `docs/reference/docs-26.1/**`.**

### B.0 Build
```
java_version=25
mc_version=26.1.2
minecraft_version_range=[26.1.2]
neo_version=26.1.2.109
jei_version=29.40.0.101              # artifact prefix becomes jei-26.1.2-…
curios_version=15.0.0+26.1.2
patchouli_version=26.1-94            # ThermalCore only; not on maven — see below
```
ThermalCore's Patchouli dependency: download
`https://github.com/VazkiiMods/Patchouli/releases/download/release-26.1-94-beta/patchouli-neoforge-26.1-94.jar`
into `ThermalCore/libs/` (commit it; ~670 KB) and replace the maven line with
`implementation files("libs/patchouli-neoforge-${patchouli_version}.jar")` (compile classpath and, since the
jar has `neoforge.mods.toml`, loaded as a mod in runs). When a `26.1` build reaches
`maven.blamejared.com/vazkii/patchouli/Patchouli/`, switch back to the coordinate. Keep
`src/main/java/cofh/thermal/core/compat/patchouli/` and re-verify its three classes against the
jar's `vazkii.patchouli.api` (`javap -cp libs/… vazkii.patchouli.api.IComponentProcessor`).
`java.toolchain.languageVersion = 25` (MDG/foojay provisions or uses temurin-25). Mixin JSON:
`"compatibilityLevel": "JAVA_21"` is the highest FabricMixin accepts; keep `JAVA_17` unless the
loader complains. `neoforge.mods.toml`: minecraft `"[26.1.2]"`, neoforge `"[26.1.2.109,)"`.
Remove any Parchment config. First compile = baseline count.

### B.1 Mechanical renames (all four repos, one sweep, regex/IDE)

| From | To | Source |
|---|---|---|
| `ResourceLocation` (type, imports, `ResourceLocation.fromNamespaceAndPath/parse/withDefaultNamespace`) | `Identifier` (`net.minecraft.resources.Identifier`, same static factories) | 1.21.11 |
| `buf.readResourceLocation()/writeResourceLocation()` | `readIdentifier()/writeIdentifier()` | 1.21.11 |
| `ResourceKey#location()` | `identifier()` | 1.21.11 |
| `net.minecraft.Util` | `net.minecraft.util.Util` | 1.21.11 |
| `net.minecraft.advancements.critereon.*` | `…advancements.criterion.*` | 1.21.11 |
| entity/model subpackages: `entity.projectile.AbstractArrow`→`entity.projectile.arrow.AbstractArrow`, `ThrowableItemProjectile`→`…projectile.throwableitemprojectile.…`, `AbstractHurtingProjectile`→`…projectile.hurtingprojectile.…`, `vehicle.AbstractMinecart`→`vehicle.minecart.…`, `vehicle.Boat/ChestBoat`→`vehicle.boat.…`, `monster.EnderMan`→`monster.enderman.EnderMan`(check), `Endermite`→`monster.endermite.…`, `animal.Animal` unchanged, `client.model.BoatModel`→`client.model.object.boat.…`, `PlayerModel`→`client.model.player.…` | see `primers/1.21.11.md` lines 39-293 for the full table | 1.21.11 |
| `RenderType.solid()/translucent()/entityCutout(..)/…` | `RenderTypes.solid()/…` in `net.minecraft.client.renderer.rendertype` | 1.21.11 |
| `level.isClientSide` (field) | `level.isClientSide()` | 1.21.9 |
| `level.random` | `level.getRandom()` | 26.1 |
| `new ChunkPos(blockPos)` / `new ChunkPos(long)` / `ChunkPos.asLong`/`toLong` | `ChunkPos.containing(pos)` / `ChunkPos.unpack(l)` / `pack()` | 26.1 |
| `FMLEnvironment.dist` / `.production` / `FMLLoader.getGamePath()` | `FMLEnvironment.getDist()` / `isProduction()` / `FMLLoader.getCurrent().getGameDir()` | 21.9 |
| `InteractionResultHolder<ItemStack>`, `ItemInteractionResult` | `InteractionResult` (`SUCCESS`, `SUCCESS_SERVER`, `CONSUME`, `FAIL`, `PASS`, `TRY_WITH_EMPTY_HAND`; `SUCCESS.heldItemTransformedTo(stack)`) | 1.21.2 |
| `registryAccess.registryOrThrow(K)` / `registry.getHolderOrThrow(key)` / `registry.get(rl)` | `lookupOrThrow` / `getOrThrow` / `getValue` | 21.2 |
| `Ingredient.of(TagKey)` / `Ingredient.EMPTY` / `getItems()` | `Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(tag))` / none (use `Optional<Ingredient>`) / `items()` | 1.21.2 |
| `stack.getItemHolder()` / `getTags()`, `EntityType#is(entity)`, `state.getBlockHolder()` | `typeHolder()` / `tags()`, `entity.is(...)`, `state.typeHolder()` | 26.1 "Typed Instance" |
| `EquipmentSlot#getFilterFlag` | `getId` | 1.21.2 |
| `Item#getDescriptionId` / `getDescription` | `DataComponents.ITEM_NAME` / `getName` | 1.21.2 |
| `Screen.hasShiftDown()/hasControlDown()/hasAltDown()` | `Minecraft.getInstance().hasShiftDown()` etc., or `event.hasShiftDown()` inside input handlers | 1.21.9 |
| `ClickType` (menus) | `ContainerInput` (`AbstractContainerMenu#clicked(int, int, ContainerInput, Player)`) | 26.1 |
| `Item#getCraftingRemainingItem/hasCraftingRemainingItem` | `getCraftingRemainder()` (returns `ItemStackTemplate`, nullable) | 1.21.2 / 26.1 |
| `javax.annotation.Nullable/Nonnull` (265 lines) | only if it stops resolving: `org.jspecify.annotations.Nullable`; type-use placement (`Map.@Nullable Entry`) | 21.11 |
| `net.neoforged.neoforge.client.model.data.ModelData/ModelProperty` | `net.neoforged.neoforge.model.data.ModelData/ModelProperty` | 26.1.x tree |
| `RecipesUpdatedEvent` / `RegisterClientReloadListenersEvent` / `AddReloadListenerEvent` / `RenderHighlightEvent` / `RegisterShadersEvent` | `RecipesReceivedEvent` / `AddClientReloadListenersEvent` / `AddServerReloadListenersEvent` / `ExtractBlockOutlineRenderStateEvent` (+`CustomBlockOutlineRenderer`) / `RegisterRenderPipelinesEvent` | 26.1.x tree |
| `INBTSerializable<CompoundTag>` | `ValueIOSerializable` (`serialize(ValueOutput)`, `deserialize(ValueInput)`) | 21.6 |
| `DeferredSpawnEggItem` | vanilla `SpawnEggItem(Item.Properties)` + `Item.Properties#spawnEgg(EntityType)` (verify in sources) | 1.21.9 |
| `IClientFluidTypeExtensions#getStillTexture/getFlowingTexture/getTintColor` | gone — `RegisterFluidModelsEvent` (B.7.e) | Pyronetics notes |

### B.2 Registration (all repos; CoFHCore helpers first)

- **`setId` is mandatory** on `BlockBehaviour.Properties` and `Item.Properties` before the
  constructor runs (throws "Block id not set"; the failure cascades into unrelated-looking
  "unbound value" errors — Pyronetics notes, Registration). Change `DeferredRegisterCoFH#register`
  to also offer `register(String, Function<Identifier, I>)`, and change
  `RegistrationHelper.registerBlock/registerItem` (ThermalCore) and every direct
  `register(ID, () -> new X(of()...))` to receive the id: for blocks
  `id -> new X(BlockBehaviour.Properties.of()….setId(ResourceKey.create(Registries.BLOCK, id)))`,
  for items `.setId(ResourceKey.create(Registries.ITEM, id))`; block items also call
  `.useBlockDescriptionPrefix()`. Regex hint for TC/TE/TD registries:
  `\(\)\s*->\s*new (\w+)\(of\(\)` → `id -> new $1(idProps(id)` with a static
  `idProps(Identifier)` helper in `RegistrationHelper`. Prefer `DeferredRegister.Blocks#registerBlock(name, Function<Properties,B>, Properties)`
  / `DeferredRegister.Items#registerItem` where a plain block/item is registered.
- `BlockEntityType.Builder.of(...).build(null)` → `new BlockEntityType<>(Ctor::new, Set.of(blocks…))`
  (NeoForge makes the ctor public; 21.2 notes).
- `EntityType.Builder#build(String)` → `build(ResourceKey<EntityType<?>>)` (1.21.2+; check sources).
- `DirectionProperty` deleted → `EnumProperty.create("facing", Direction.class, …)`; property
  classes are final (1.21.2 "Properties Changes").
- `RecipeSerializer` is a **record** `(MapCodec, StreamCodec)`; `RecipeType.register(String)`
  prepends `minecraft:` — build an anonymous `RecipeType` with `toString` (Pyronetics `PyroneticsRecipes`).
- `KeyMapping(name, key, KeyMapping.Category)`; category via `KeyMapping.Category.register(Identifier)`
  and **also** `RegisterKeyMappingsEvent#registerCategory` (21.9 notes). `CoreKeys`.
- `Minecraft.getInstance()` is **not** available during mod construction (21.5 "Client Mod
  Initialization") — move any such access in `CoFHCore`/`ThermalCore` client entrypoints to
  `FMLClientSetupEvent`.
- Creative tabs: `BuildCreativeModeTabContentsEvent` unchanged in name; check `CreativeModeTab.Builder`.
- Sounds: `SoundEvent` registry unchanged; `DeferredHolder<SoundEvent,SoundEvent>` is a `Holder`.

### B.3 Persistence (CoFHCore `BlockEntityCoFH` family, entities, `IFilter`, storages, `SavedData`)

- `BlockEntity#saveAdditional(ValueOutput)` / `loadAdditional(ValueInput)` — no `CompoundTag`, no
  `HolderLookup.Provider`. `ValueInput`: `getIntOr("k", 0)`, `getStringOr`, `getBooleanOr`,
  `child("k")` (`Optional<ValueInput>`), `childOrEmpty`, `childrenList`/`childrenListOrEmpty`,
  `read("k", CODEC)` (`Optional`), `list("k", CODEC)`; `ValueOutput`: `putInt`, `putString`,
  `child("k")`, `childrenList("k")`, `store("k", CODEC, v)`, `storeNullable`, `list("k", CODEC)`,
  `discard("k")`. Item stacks: `out.store("Item", ItemStack.OPTIONAL_CODEC, stack)` /
  `in.read("Item", ItemStack.OPTIONAL_CODEC).orElse(EMPTY)`; `ContainerHelper.saveAllItems(ValueOutput, NonNullList)`.
  Fluids: `FluidStack.OPTIONAL_CODEC`. Where CoFH passes raw `CompoundTag` between layers
  (`SimpleItemInv#read/write`, `IFilter`, `IConveyableData`, augment data, `TileNBTSync` loot
  function), convert the whole chain to `ValueInput/ValueOutput`; to bridge at the edges use
  `TagValueInput.create(problemReporter, registries, tag)` / `TagValueOutput.createWithContext(reporter, registries)`
  + `buildResult()` with `ProblemReporter.DISCARDING` (21.6 primer "Generic Encoding and Decoding").
- `CompoundTag` getters now return `Optional` (`getInt` → `Optional<Integer>`; use `getIntOr`,
  `getCompoundOrEmpty`, `getListOrEmpty`, `getStringOr`) — 1.21.5 "Tags and Parsing". `tag.store("k", CODEC, v)` / `tag.read("k", CODEC)`.
- Client sync: `getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }`,
  `getUpdateTag(HolderLookup.Provider) { return saveCustomOnly(registries); }`; NeoForge
  `IBlockEntityExtension#onDataPacket(Connection, ValueInput)` / `handleUpdateTag(ValueInput)`
  default to `loadWithComponents` (Pyronetics notes, Block entities).
- `BlockBehaviour#onRemove` → split: `BlockEntity#preRemoveSideEffects(BlockPos, BlockState)`
  (drop contents — **only automatic for `Container` implementors**; CoFH inventories are not
  `Container`s, so override it and drop `SimpleItemInv` contents yourself) and
  `BlockBehaviour#affectNeighborsAfterRemoval` (neighbor updates only). 5 sites Core, 2 TD.
- Item ↔ block-entity data: `collectImplicitComponents(DataComponentMap.Builder)` /
  `applyImplicitComponents(DataComponentGetter)` / `removeComponentsFromTag(ValueOutput)`;
  `DataComponents.BLOCK_ENTITY_DATA` is now `TypedEntityData<BlockEntityType<?>>` (1.21.9), not
  `CustomData`. This replaces the `TAG_BLOCK_ENTITY` custom-data blob used by cells/machines.
- `Entity#readAdditionalSaveData(ValueInput)` / `addAdditionalSaveData(ValueOutput)`;
  `Entity#interact(Player, InteractionHand, Vec3)` replaces `interactAt` (26.1).
- `SavedData` → `SavedDataType<T>("name", ctx -> new T(), ctx -> CODEC, DataFixTypes)` and
  `storage.computeIfAbsent(TYPE)` (1.21.5 "Saved Data, now with Types"). 1 site Core, 1 TD.
- Data attachments (if any) — `AttachmentType.builder(...)`; `serialize(MapCodec)` (Pyronetics notes, Worldgen).

### B.4 Transfer API (CoFHCore storage stack, then every capability site)

This is the largest design change. Old `IItemHandler`/`IFluidHandler`/`IEnergyStorage` still
exist in 26.1.2 but are `@Deprecated(forRemoval)`, **the capabilities only speak the new
types**, and there is no old→new wrapper (only new→old: `IItemHandler.of(handler)`,
`IFluidHandler.of(handler)`, `IEnergyStorage.of(handler)`). Capabilities:
`Capabilities.Item.BLOCK/ENTITY/ENTITY_AUTOMATION/ITEM` (`ResourceHandler<ItemResource>`),
`Capabilities.Fluid.BLOCK/ENTITY/ITEM` (`ResourceHandler<FluidResource>`),
`Capabilities.Energy.BLOCK/ENTITY/ITEM` (`EnergyHandler`); item capabilities take an `ItemAccess`
context, not the stack. Package `net.neoforged.neoforge.transfer.*` (full file list in
`docs/reference/neoforge-src/files-26.1.txt`; overview in `neo-notes/21.9-transfer-rework.md`;
Pyronetics notes "Capabilities and energy" / "Fluids").

Staged, as the 21.9 post recommends:

1. **Compile first**: comment out every `RegisterCapabilitiesEvent` provider registration;
   at every *query* site wrap: `var h = level.getCapability(Capabilities.Item.BLOCK, pos, side); IItemHandler old = h == null ? null : IItemHandler.of(h);`
   (energy: `IEnergyStorage.of`, fluid: `IFluidHandler.of`; items-in-stacks:
   `ItemAccess.forStack(stack).getCapability(Capabilities.Energy.ITEM)`, fluid items:
   `FluidUtil.getFluidHandler(stack)` — the deprecated `fluids.FluidUtil`). `EnergyHelper`,
   `FluidHelper`, `InventoryHelper`, TD grids, TE machines.
2. **Rebuild the storages natively** (this is the real port; Pyronetics `common/energy/EnergyBuffer`,
   `common/inventory/MachineInventory`, `SidedEnergyHandler`, `SidedFluidHandler`, `MachineFluidTank`
   are the working models):
   - `EnergyStorageCoFH` → extends `SimpleEnergyHandler(capacity, maxInsert, maxExtract)`
     (journaled; `set(int)` for exact restores; `onEnergyChanged` hook → `setChanged()`); keep
     CoFH's `modify(delta)` as a non-transactional internal mutation. A closed side = cap 0.
   - `ItemStorageCoFH` (single slot + validator) and `SimpleItemInv`/`ManagedItemInv`
     (slot groups) → one `ResourceHandler<ItemResource>` over the slot list, built on
     `ItemStacksResourceHandler` (or `StacksResourceHandler`) with `isValid(index, resource)`
     from the slot validators, `getCapacity(index, resource)` for slot limits, `set(index, …)`
     for GUI/menu writes, `onContentsChanged(index, previous)` → `setChanged()`; sided access
     views via `RangedResourceHandler`/`CombinedResourceHandler` or a `DelegatingResourceHandler`
     that filters by the side's group. Save format: `ItemStacksResourceHandler` writes `"stacks"`
     — keep CoFH's own `ValueOutput` layout by overriding `serialize/deserialize` to preserve
     existing worlds' slot keys.
   - `FluidStorageCoFH` → one-index `FluidStacksResourceHandler(1, capacity)` (+ validator);
     `SimpleTankInv`/`ManagedTankInv` → `CombinedResourceHandler`.
   - Menus: `SlotCoFH` (extends `Slot` over `IItemHandler`) → `ResourceHandlerSlot(handler, indexModifier, index, x, y)`
     (returns copies from `getItem()`; `mayPlace` = `isValid`, `mayPickup` = simulated extract).
   - Every transactional op from tick code: `try (Transaction tx = Transaction.openRoot()) { …; tx.commit(); }`;
     helpers: `ResourceHandlerUtil.move(from, to, filter, max, tx)`, `insertStacking`,
     `EnergyHandlerUtil.move(from, to, amount, tx)`; passing `null` opens+commits its own root
     transaction but throws if one is already open.
   - Item-form storage (energy cells as items, fluid cells, satchels): `ItemAccessEnergyHandler`
     (`DataComponentType<Integer>`), `ItemAccessFluidHandler(access, DataComponentType<SimpleFluidContent>, capacity)`,
     `ItemAccessItemHandler(access, DataComponentType<ItemContainerContents>, slots)`; register with
     `event.registerItem(Capabilities.Item.ITEM, (stack, access) -> …, items…)`.
   - Re-enable provider registrations: `event.registerBlockEntity(Capabilities.Item.BLOCK, type, (be, side) -> be.getItemHandler(side))` etc.
   - **`invalidateCapabilities()`** after every side-config change and wrench rotation
     (`BlockCapabilityCache` is not invalidated on state changes — Pyronetics "Open risks").
3. Delete the remaining `net.neoforged.neoforge.items/fluids.capability/energy` imports.

### B.5 Items, tools, armor, entities

- `SwordItem`, `DiggerItem` (+Pickaxe/Axe/Hoe/Shovel as separate classes — `AxeItem/HoeItem/ShovelItem`
  survive as thin `Item` subclasses), `ArmorItem`, `AnimalArmorItem`, `TieredItem`, `Tier`,
  `ArmorMaterial` (registry) are gone (1.21.5 "Weapons, Tools, and Armor"). Replace CoFH's
  `*ItemCoFH` tool family with `Item` + `Item.Properties#sword(ToolMaterial, dmg, speed)`/
  `pickaxe`/`axe`/`hoe`/`shovel`/`tool(...)`, `ToolMaterial(TagKey incorrectBlocks, uses, speed, dmgBonus, enchantValue, TagKey repair)`;
  `ItemTierCoFH` → `ToolMaterial` constants; armor → `Item.Properties#humanoidArmor(ArmorMaterial, ArmorType)`
  with the `net.minecraft.world.item.equipment.ArmorMaterial` record and an
  `assets/<ns>/equipment/<id>.json` (1.21.2 "Armor Materials, Equipment"); `Equippable` component
  for wearable non-armor. `ShieldItemCoFH` → `BLOCKS_ATTACKS` component. Mining ability checks
  → `ItemAbilities` (already) / `DataComponents.TOOL`.
- `Item#appendHoverText(ItemStack, TooltipContext, TooltipDisplay, Consumer<Component>, TooltipFlag)`;
  `Item#use(Level, Player, InteractionHand)` returns `InteractionResult`; `Item#inventoryTick(ItemStack, ServerLevel, Entity, @Nullable EquipmentSlot)`;
  `Item#getName(ItemStack)`; `IItemExtension#getMaxStackSize(ItemStack)`.
- `LivingEntity#getArmorSlots/getHandSlots/getAllSlots` removed → `entity.equipment` (`EntityEquipment`)
  and `getItemBySlot`; `Inventory#getSelected()` → `getSelectedItem()`, `selected` → `getSelectedSlot()`;
  `Inventory#armor/offhand` lists gone. (`ItemTracker`, `ArcheryHelper`, `Utils`.)
- `MobEffect` field renames (1.21.5 "Mob Effects Field Renames"); `DamageSource` builders via
  `level.damageSources()`; `DamageType` tags moved with the `criterion` rename.
- Entities: `EntityType.Builder#build(ResourceKey)`; `Entity#getAddEntityPacket(ServerEntity)`;
  `IEntityWithComplexSpawn` unchanged; `Entity#interact(Player, InteractionHand, Vec3)`;
  `Leashable`; `Mob#createEquipment`.

### B.6 Recipes (ThermalCore's `ThermalRecipe`/`MachineRecipeSerializer` family, CoFHCore crafting recipes, TE)

- `Recipe<T extends RecipeInput>` now requires `placementInfo()` (return `PlacementInfo.NOT_PLACEABLE`
  for machine recipes), `display()` (`List<RecipeDisplay>`; `List.of()` is acceptable for
  machine recipes shown only by JEI), `recipeBookCategory()`, `group()`, `showNotification()`,
  `isSpecial()`; `getResultItem`/`getIngredients`/`canCraftInDimensions`/`getToastSymbol` are gone
  (1.21.2 "Recipe Changes"). Results are `ItemStackTemplate` (`create()` → `ItemStack`;
  `ItemStackTemplate.fromNonEmptyStack`); `assemble(input)` has no registries param (26.1).
  `RecipeSerializer` record (B.2); `AbstractCookingRecipe`/`ShapedRecipe`/`ShapelessRecipe` take
  `Recipe.CommonInfo` + `CraftingBookInfo`/`CookingBookInfo` (26.1 "Serializer Records").
  `NormalCraftingRecipe` is the base for custom crafting recipes (Pyronetics notes, Recipes).
- CoFH's own JSON parser stays; `Ingredient.of(HolderSet)`, `SizedIngredient.NESTED_CODEC`
  for counted inputs, `FluidIngredient`/`SizedFluidIngredient` (NeoForge `fluids.crafting`).
- **Client access**: register `OnDatapackSyncEvent` (game bus, both sides) →
  `event.sendRecipes(TCoreRecipeTypes.*)`; on the client `RecipesReceivedEvent#getRecipeMap()`
  fills `ThermalRecipeManagers`' caches; clear on `ClientPlayerNetworkEvent.LoggingOut`
  (`docs-26.1/resources/server/recipes/index.md` §"Client-Side Recipes"). Server-side lookups:
  `serverLevel.recipeAccess().getRecipeFor(type, input, level)`.
- **JEI 29** (`docs/reference/neoforge-src/jei-26.1/**`): `mezz.jei.api.recipe.RecipeType` →
  `IRecipeType` (`RecipeType.create(...)` factory still exists, check), `IRecipeCategory#draw(T, IRecipeSlotsView, GuiGraphicsExtractor, double, double)`,
  `getWidth()/getHeight()` mandatory (no `getBackground`), `ISubtypeInterpreter` replaces
  `IIngredientSubtypeInterpreter` (`registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, item, interp)`
  or `registerFromDataComponentTypes(item, types…)`), `IRecipeRegistration#addRecipes(IRecipeType, List)`,
  `NeoForgeTypes.FLUID_STACK`, `IPlatformFluidHelper`. TE's 27 JEI files and TC's 8.
- Datagen: `RecipeProvider` is no longer a `DataProvider` — `RecipeProvider.Runner` +
  `createRecipeProvider(HolderLookup.Provider, RecipeOutput)`; builders take `ItemStackTemplate`;
  `ExistingFileHelper` no longer exists; `GatherDataEvent.Client` only. Since all generated
  output is committed, the cheapest correct move is to **delete the model/blockstate providers**
  (NeoForge `BlockStateProvider`/`ItemModelProvider` are gone; vanilla `ModelProvider` uses
  `BlockModelGenerators`/`ItemModelGenerators`) and keep tags/loot/recipes on the new shapes.

### B.7 Client

**(a) GUI framework** (`cofh.core.client.gui.*`, ~40 files; TC/TD/TE screens). `GuiGraphics` →
`GuiGraphicsExtractor`; `Screen#render` → `extractRenderState(GuiGraphicsExtractor, mouseX, mouseY, partialTick)`;
`AbstractContainerScreen#renderBg` → `extractBackground(...)`, `renderLabels` → `extractLabels(g, mouseX, mouseY)`,
do **not** override `render` (it calls `renderTooltip` itself); `imageWidth/imageHeight` are final
→ pass to `super(menu, inv, title, w, h)`; `tick()` is final → `containerTick()`;
`drawString/drawCenteredString` → `g.text(font, String|Component, x, y, argb, dropShadow)`
(**colors need an alpha byte** — `0xFF000000 | rgb` — or text is invisible); `blit(texture, x, y, u, v, w, h)`
→ `g.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, w, h, texW, texH[, argb])`;
`blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, w, h[, argb])`; `fill`, `hLine/vLine` →
`horizontalLine/verticalLine`; items: `fakeItem(stack, x, y)` + `itemDecorations(font, stack, x, y)`;
tooltips: `setTooltipForNextFrame(font, stack|lines, x, y)`; scissor: `enableScissor/disableScissor`;
`renderOutline` → `outline`. Input: `mouseClicked(MouseButtonEvent e, boolean doubleClick)`,
`mouseReleased(MouseButtonEvent)`, `mouseDragged(MouseButtonEvent, double dx, double dy)`,
`keyPressed(KeyEvent)`, `charTyped(CharacterEvent)`; `e.x()/y()/button()`, `e.hasShiftDown()`;
`AbstractWidget#onClick(MouseButtonEvent)`; `AbstractButton#onPress(InputWithModifiers)`.
Fluid in a GUI: `Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState())`
→ `FluidModel` → `stillMaterial().sprite()` + `fluidTintSource()` (Pyronetics notes, Fluids;
`ElementFluid`/`GuiHelper`/`RenderHelper`). Vanilla `AbstractFurnaceScreen`/`HopperScreen` in the
sources jar and Pyronetics `client/screen/*` are the reference implementations. Menu button
packets: `minecraft.gameMode.handleInventoryButtonClick(containerId, id)` → `clickMenuButton`.

**(b) Models** (CoFHCore `client/model`, `lib/client/renderer/block/model`, `ModelUtils`; TC's
six `*BakedModel`; TD `DuctBakedModel/DuctModelData`). `BakedModel`, `IDynamicBakedModel`,
`BakedModelWrapper`, `CompositeModel`, `IGeometryLoader/IUnbakedGeometry/SimpleUnbakedGeometry`,
`IModelBuilder`, `QuadTransformers/IQuadTransformer`, `SimpleModelState`, `RenderTypeGroup`,
`ItemOverrides`, `ItemProperties`, `ItemBlockRenderTypes` are all gone. Targets:
- Per-position dynamic block models (cells, dynamos, reconfigurable machines, ducts): implement
  `net.neoforged.neoforge.client.model.DynamicBlockStateModel` (or `DelegateBlockStateModel`)
  overriding `collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts)`
  and read `level.getModelData(pos).get(PROPERTY)`; the block entity supplies
  `IBlockEntityExtension#getModelData()` and calls `requestModelDataUpdate()`. Wire it via a
  custom `UnbakedModelLoader` registered in `ModelEvent.RegisterLoaders` (JSON `"loader": "thermal:…"`)
  or `ModelEvent.ModifyBakingResult` (swap the baked `BlockStateModel` for the states). Working
  example: `docs/reference/neoforge-src/26.1/tests/.../MegaModelTest.java` (both mechanisms) and
  `NewModelLoaderTest.java`; docs `docs-26.1/resources/client/models/modelloaders.md`.
- Quads: `BakedQuad` has explicit vertex/uv fields (no `int[]`); build/retexture with NeoForge's
  `MutableQuad` (`setSpriteAndMoveUv`, `setCubeFace`, `bakeUvsFromPosition`, `recalculateWinding`)
  and cache vectors through `ModelBaker.parts()` (21.11 notes). Replaces `RetexturedBakedQuad`,
  `BackfaceBakedQuad`, `ModelUtils` quad code.
- Render layers: no `RenderType` per model — chosen **per quad from the sprite's alpha**;
  `"render_type"` in model JSON is ignored; textures with stray partial alpha land in the
  translucent layer (Pyronetics notes, Client). `force_translucent` in a model's `textures` entry
  forces translucency.
- Item models: every item needs `assets/<ns>/items/<name>.json` (`{"model":{"type":"minecraft:model","model":"<ns>:item/<name>"}}`;
  block items point at the block model). Generate these with a script from the existing
  `models/item/*.json` files (Pyronetics `scripts/gen_*` do this). `ItemProperties.register(...)`
  predicates → client-item JSON `minecraft:condition`/`minecraft:range_dispatch`/`minecraft:select`
  models, or NeoForge `RegisterRangeSelectItemModelPropertyEvent`/`RegisterConditionalItemModelPropertyEvent`/`RegisterSelectItemModelPropertyEvent`
  for custom properties (`docs-26.1/resources/client/models/items.md`). `FluidContainerItemModel`
  → NeoForge's `DynamicFluidContainerModel` (`"type": "neoforge:fluid_container"`, keys
  `base/fluid/cover`, `flip_gas`, `apply_fluid_luminosity`) — delete CoFH's copy. Custom item
  models in code: `ItemModel`/`ItemModel.Unbaked` + `RegisterItemModelsEvent`; special renderers
  via `RegisterSpecialModelRendererEvent`.
- Colors: `RegisterColorHandlersEvent.Block` now registers `BlockTintSource` (`color(state)`,
  `colorInWorld(state, level, pos)`), item tints are JSON tint sources or `RegisterColorHandlersEvent.ItemTintSources`.

**(c) Entity renderers/models** (Core 9 files, TC 12). Already render-state based in spirit
since 1.21.2; final shape (1.21.9): `EntityRenderer<E, S extends EntityRenderState>` with
`createRenderState()`, `extractRenderState(entity, state, partialTick)`,
`submit(state, poseStack, SubmitNodeCollector, CameraRenderState)`; `getTextureLocation(state)`
on `LivingEntityRenderer`; `EntityModel<S>` with `setupAnim(S)`, no `HierarchicalModel`/`ListModel`;
`RenderLayer<S, M>#submit(poseStack, collector, light, state, yRot, xRot)`; `collector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> …)`
for hand-built geometry (`ElectricFieldRenderer`, the spell/field renderers); `submitModel`,
`submitModelPart`. Registration events unchanged (`EntityRenderersEvent.RegisterRenderers/RegisterLayerDefinitions`).
Primers 1.21.2 "Entity Render States" and 1.21.9 "Entity Renderer".

**(d) Block-entity renderers** (TC `ItemCellRenderer`; anything else that rendered in
`RenderLevelStageEvent`): `BlockEntityRenderer<T, S extends BlockEntityRenderState>` —
`createRenderState()`, `extractRenderState(be, state, partialTick, cameraPos, crumblingOverlay)`
(call super first), `submit(state, poseStack, collector, camera)`; items inside via
`ItemModelResolver#updateForTopItem(state.item, stack, ItemDisplayContext.NONE, level, owner, seed)`
then `state.item.submit(...)`; block models inside via `BlockModelResolver#update(state.block, blockState, BlockDisplayContext)` +
`state.block.submit(...)` (26.1 "Block Models"). Pyronetics `PortableTankRenderer`, `SideConfigRenderer`.

**(e) Fluids** (Core `FluidType`s, TC 14 files). Registration shape unchanged
(`FluidType`, `BaseFlowingFluid.Source/Flowing`, `LiquidBlock`, `BucketItem`). Textures/tint
moved out of `IClientFluidTypeExtensions` into `RegisterFluidModelsEvent`
(`event.register(new FluidModel.Unbaked(new Material(stillId), new Material(flowId), overlayOrNull, tintOrNull), source, flowing)`,
`Material` = `net.minecraft.client.resources.model.sprite.Material`, `FluidTintSources.constant(argb)`);
`IClientFluidTypeExtensions` remains for overlay/fog only. `FluidStackTemplate` for fluids in data
files; `FluidStack`/`FluidResource` need registries loaded. `FluidType#isLighterThanAir()`.

**(f) Particles** (Core 15 particle impls + 3 custom `ParticleRenderType`s + `SpriteParticle`).
`TextureSheetParticle` merged into `SingleQuadParticle(level, x, y, z, TextureAtlasSprite)`;
`getRenderType()` → `getLayer()` returning `SingleQuadParticle.Layer` (`TERRAIN`, `OPAQUE_*`,
`TRANSLUCENT_*`, or `Layer.bySprite(sprite)`; custom `new Layer(translucent, atlas, RenderPipeline)`);
`ParticleProvider#createParticle(options, level, x, y, z, xd, yd, zd, RandomSource)`;
`SpriteSet#first()`, `setSpriteFromAge`. CoFH particles that did their own tessellation
(`CoFHParticle` custom render types, `SparkParticle`, the beam/field particles) have no
drop-in: either express them as quads on a custom `Layer`, or implement a `ParticleGroup` +
`ParticleGroupRenderState#submit(SubmitNodeCollector)` (1.21.9 "From the Ground Up"; NeoForge
`CustomParticleTypeTest`). Recommend the quad route first; defer exotic ones to a TODO.
`ParticleEngine#spriteSets` AT line → `ParticleResources`.

**(g) Render types, shaders, post effects** (`CoreShaders`, `RenderTypes`, `CoreRenderType`,
`PostBuffer`, `PostEffect`, `VFXHelper`, `assets/cofh_core/shaders/*`). Core-shader JSONs and
`ShaderInstance` are gone (1.21.5 "Render Pipeline Rework"): a shader is a `RenderPipeline`
built in code (`RenderPipeline.builder().withLocation(...).withVertexShader(...).withFragmentShader(...).withVertexFormat(format, mode).withSampler("Sampler0").withColorTargetState(...).withDepthStencilState(...).build()`,
26.1 shape in primer 26.1 "Pipeline Depth and Color") registered in `RegisterRenderPipelinesEvent`;
GLSL files stay under `assets/<ns>/shaders/core/` (`.vsh/.fsh`, `#moj_import`). A custom
`RenderType` = `RenderType.create(name, RenderSetup.builder(pipeline).withTexture("Sampler0", id, sampler).useLightmap().useOverlay().sortOnUpload().setLayeringTransform(...).setOutputTarget(...).createRenderSetup())`
(1.21.11 "Custom Types"). `RenderStateShard` shards (`TRANSLUCENT_TRANSPARENCY`, `NO_DEPTH_TEST`,
`COLOR_DEPTH_WRITE`, `RENDERTYPE_TRANSLUCENT_SHADER`) map onto `BlendFunction`/`CompareOp`/
`ColorTargetState` flags. Post-processing chains (`PostChain`, `PostEffect`, `PostBuffer`,
`RenderTarget` juggling): vanilla now loads `assets/<ns>/post_effect/<name>.json` (`PostChainConfig`)
and runs them through `PostChain.process(FrameGraphBuilder, …)`; see 1.21.5 primer "Post Effects"
and vanilla `GameRenderer`/`LevelRenderer` in the sources jar. If CoFH's post effects are only
cosmetic (they are — `VFXHelper` visuals), stub them behind a TODO rather than block the port.
`GlStateManager` direct calls → `RenderSystem`/`GpuDevice` (`RenderSystem.getDevice()`).

**(h) Client events**. `RenderLevelStageEvent` is now sub-classed (`AfterSky`,
`AfterOpaqueBlocks`, `AfterOpaqueFeatures`, `AfterTranslucentFeatures`, `AfterTranslucentBlocks`,
`AfterTranslucentParticles`, `AfterWeather`, `AfterLevel`) with `getLevelRenderState()`,
`getPoseStack()` (nullable), `getModelViewMatrix()` — no `getStage()`/camera; subscribe to the
subclass (`AreaEffectClientEvents`, `CoreClientEvents`, `TCoreClientEvents`, TD `DebugRenderer`).
`RenderHighlightEvent` → `ExtractBlockOutlineRenderStateEvent` + `CustomBlockOutlineRenderer`
(21.9 notes). `RenderFrameEvent`, `ClientTickEvent.Post`, `RegisterKeyMappingsEvent`
(+ `registerCategory`), `RegisterParticleProvidersEvent`, `RegisterMenuScreensEvent`,
`TextureAtlasStitchedEvent`, `RegisterColorHandlersEvent` all still exist — re-check signatures.
`GameRendererMixin`/`LevelRendererMixin`/`MouseHandlerMixin` almost certainly have no target
methods left: replace with `RenderLevelStageEvent`/`RenderFrameEvent`/`InputEvent`s.

### B.8 Resources (mostly scriptable; do after B.7b so the model layout decisions are known)

1. `assets/<ns>/items/*.json` for every item (script over `models/item/`); item property
   overrides → client-item `select/range_dispatch/condition` JSON.
2. Remove `"render_type"` from all model JSON; audit textures for stray alpha
   (`python3 -c` PIL scan: any pixel with `0 < a < 255` on a block texture that must be solid →
   flatten to 255). Pyronetics `docs/progress-log.md` has the exact script pattern.
3. Recipes: results as `{"id": …, "count": n}`; ingredient strings for vanilla-format recipes
   (`"minecraft:diamond"`, `"#c:gems/diamond"`), custom ingredient `"neoforge:ingredient_type"`;
   CoFH-format machine recipes unchanged (own parser).
4. Loot: `match_tool` predicate shape `{"predicate":{"predicates":{"minecraft:enchantments":[{"enchantments":"minecraft:silk_touch","levels":{"min":1}}]}}}`;
   block-entity data on drops via `{"function":"minecraft:copy_components","source":"block_entity","include":[…]}`
   (replaces `TileNBTSync` loot function).
5. `equipment/<id>.json` for any armor (only if B.5 keeps armor).
6. Tags: `data/neoforge/data_maps/item/furnace_fuels.json` for fuel values (data maps keyed by
   the *map's* namespace); `c:` tags confirmed present in the NeoForge universal jar before use.
7. Patchouli book assets stay and remain live (Patchouli 26.1-94 is kept, §B.0); check the
   book still opens in the client pass — the beta's known issues are multiblock rendering only.
8. Lang: `fluid_type.<ns>.<name>`, `key.categories.*` → `key.category.<ns>.<name>` (check `KeyMapping.Category#label`).

### B.9 Mixins and ATs

Re-target all 10 mixins against the 26.1.2 sources jar; expect `GameRendererMixin`,
`LevelRendererMixin`, `MouseHandlerMixin`, `ClientboundSetEntityMotionPacketMixin` to need
rewriting or replacing with events (B.7h); `LivingEntityMixin`/`ShieldItemMixin`/`ShearsItemMixin`
targets moved with the item-component rework (shield logic lives in `BlocksAttacks`). AT: rebuild
the list from the `validateAccessTransformers` report; drop entries for deleted members
(`BlockEntityWithoutLevelRenderer`, `ParticleEngine#spriteSets`, `LevelRenderer#renderHitOutline`…).

### B.10 Dependent repos and exit criteria

ThermalCore → ThermalDynamics → ThermalExpansion, each: B.0 → B.1 sweep (already done in the
all-repo pass) → compile → categories B.2–B.8 as they surface, reusing CoFHCore's helpers
(`EnchantmentHelperCoFH`, `RegistrationHelper.idProps`, storage base classes) — never re-derive
a shape CoFHCore already confirmed; write it to `CoFHCore/docs/api-notes-26.1.2.md` instead.

Done when: all four `./gradlew build` clean on JDK 25; `verify_runserver.sh` passes for each
(ThermalExpansion's run loads all four mods); a `runClient` pass by Joel covering: machine GUI
(TE), energy/fluid/item cell in world + item form (TC), a duct network moving items/energy (TD),
JEI machine recipe page, the Patchouli guidebook opening, wrench side-config, a placed fluid,
particles from a dynamo/machine.
`docs/api-notes-26.1.2.md`, `progress-log.md`, `TODO.md` (with the deferred visual items: post
effects, exotic particles) updated; merge/tag as `26.1.2`.

---

## 7. Working method (unchanged in spirit, updated in mechanics)

- **Confirm shapes in the patched sources jar**, not in memory and not in the primer:
  `unzip -p build/moddev/artifacts/minecraft-patched-<ver>-sources.jar net/minecraft/client/gui/screens/inventory/AbstractFurnaceScreen.java`.
  NeoForge classes: `unzip -p ~/.gradle/caches/modules-2/files-2.1/net.neoforged/neoforge/<ver>/*/neoforge-<ver>-sources.jar net/neoforged/neoforge/transfer/ResourceHandler.java`.
  `javap -cp build/moddev/artifacts/minecraft-patched-<ver>.jar -p <fqcn>` when only signatures matter.
- **A mod already built against the target is evidence and cheaper than a decompile**:
  Pyronetics (26.1.2), SPLIGAN forks (1.21.1), AllTheOres `upstream/26.1`.
- Fix one root-cause category across every file it touches; commit with `1.21.1: …` / `26.1.2: …`
  messages; record the before/after error count in `docs/progress-log.md`; anything noticed
  out-of-category goes in `docs/TODO.md` Inbox.
- `-Xmaxerrs 100000` stays; trust only uncapped counts.
- Never hand-edit `src/main/generated` output that a provider still owns; delete the provider
  first if it is being retired (B.6 datagen).

## 8. Verification

- Compile: `./gradlew compileJava` per repo, `./gradlew build` at phase end.
- Headless: `../Pyronetics/scripts/verify_runserver.sh <repo> /tmp/<repo>.log 240` (already
  handles MDG's `devlaunch.Main` process). Look for `constructed`/`Done (` and no `LanguageLoadingProvider`,
  registry, or model errors in the log; a headless boot cannot see model/texture/GUI breakage.
- Client (Joel): the checklists in §A.4 and §B.10. Client-only work is "owed verification" until then.
- Registry sanity after B.2: no "Block id not set", no unbound `DeferredHolder` on first access.
- Data sanity after B.8: `grep -rL '"model"' src/main/resources/assets/*/items` is empty; no
  `forge:` strings remain; tag folders singular.

## 9. Rough sizing (for ordering, not promises)

Phase 0: ½ day. Phase A: CoFHCore L (the 16 categories, ~2–3 days of agent time), ThermalCore M
(diff-driven), TE S–M, TD S. Phase B: B.1 S (scripted), B.2 M, B.3 L, **B.4 XL**, B.5 M, B.6 L,
**B.7 XL** (GUI M, models L, renderers M, particles M, shaders/post L), B.8 M, B.9 S, dependents
L total. Client rendering (B.7b/g) is the highest-risk area; everything else is mechanical
once the reference shape is read.
