# CoFHCore — NeoForge port (foundation library)

## Where things are

| File | Read it when |
|---|---|
| **This file** | Always. Context, decisions, and current state. |
| [docs/port-plan.md](docs/port-plan.md) | **Starting any porting work.** The approved plan (2026-09-21): 1.20.6-partial → 1.21.1 → 26.1.2 directly, with every version, coordinate, replacement API and reference location stated. Phase/category order lives there, not here. |
| `docs/reference/` (**local only, gitignored**) | Confirming an API shape. Vendored primers (1.20.5 → 26.1), NeoForge release notes, the 26.1 and 1.21.1 docs, NeoForge/JEI source file lists and key source files. `scripts/fetch_reference.sh` recreates it. `grep` it. |
| [docs/api-notes-1.20.6.md](docs/api-notes-1.20.6.md) | Writing any code against a 1.20.5/1.20.6 API (all still valid on 1.21.1). Every API shape confirmed by decompiling the real mapped jar, organized by category, in the order they were found. Successors: `api-notes-1.21.1.md`, `api-notes-26.1.2.md` as each phase lands. |
| [docs/TODO.md](docs/TODO.md) | Picking up work. The live, current-priority list of what's still broken, by error count. **Anything noticed mid-session goes in its Inbox.** |
| [docs/progress-log.md](docs/progress-log.md) | The story behind a decision, or the chronology of the hop so far — what's already fixed, in what order, why a number is what it is. Append-only. |
| `docs/context/` (**local only, gitignored**) | The progress log doesn't have the detail you need. Full session transcript exports — this whole 4-repo porting effort runs in one shared session, exported under `ThermalExpansion/docs/context/` (that's the session's project directory) rather than duplicated into each repo. `grep` it, don't read it whole. |

## Context

CoFHCore is the foundation library for the whole "Thermal Series" — **ThermalCore**,
**ThermalDynamics**, and **ThermalExpansion** (sibling repos under
`/Volumes/Mac_External/Developer/Minecraft/`) all depend on it, so its port comes first
at every version hop and the others follow. All four repos are worked in **one shared
Claude Code session**, not independently — session transcripts live under
`ThermalExpansion/docs/context/`.

This is a port of the existing NeoForge 1.20.4 codebase (already in place before this
effort began) to **26.1.2**, in exactly two hops: **1.21.1** (a required stop — the
long-lived modding version, and where the SPLIGAN reference forks live) and then
**26.1.2** directly. It is not a from-scratch rewrite and, since 2026-09-21, no longer a
one-version-at-a-time primer climb — see progress-log.md's Phase 2 entry for the change
and docs/port-plan.md §1/§3 for why. No NeoForge port of CoFHCore exists anywhere to
build on instead, official or community.

**Reference implementations on this machine** (read before deriving a shape yourself):
`../Pyronetics` — Joel's own from-scratch mod, **built and running on NeoForge 26.1.2.109**
(MDG, screens, block-entity renderers, the transfer API, fluids, recipes, networking); its
`docs/api-notes-26.1.2.md` is the first stop for any 26.1 shape. `../ThermalCoreForNeoForge`,
`../ThermalExpansionForNeoForge`, `../ThermalDynamicsForNeoForge` — SPLIGAN's minimal-diff
**1.21.1** ports of the three Thermal repos (they compiled against a private CoFHCore build
we don't have, so CoFHCore itself is ported by hand).

Pyronetics is otherwise unrelated — a separate from-scratch mod in its own repo/session,
inspired by classic Thermal Expansion's designs, with zero code dependency on this codebase.
It is a *reference* here, never a dependency.

## Decisions already made

- **No official or community NeoForge port of CoFHCore exists** to build on. The
  community fork (SPLIGAN's `ThermalExpansionForNeoForge`/`ThermalCoreForNeoForge`,
  `ThermalDynamicsForNeoForge`, cloned as sibling repos) is a real 1.21.1 port of the
  three Thermal repos but depends on a `cofh_core:1.21.1-11.0.2.0` artifact that only
  existed in SPLIGAN's `mavenLocal()` — it can't compile as checked out, and CoFHCore is
  ported here by hand from its existing 1.20.4 codebase. The forks are the Phase A
  reference for the other three repos.
- **Target: NeoForge 26.1.2.109 via 1.21.1 (21.1.251)**, no other intermediates
  (Joel, 2026-09-21). The primers for every skipped version are still *read* (vendored in
  `docs/reference/`), they are just not *built against*.
- **Build plugin: ModDevGradle 2.0.147**, switched once in Phase 0 (replacing NeoGradle
  userdev) — it is what Pyronetics builds 26.1.2 with on this machine, and it produces the
  patched-sources jar used to confirm shapes. See docs/port-plan.md §1.4 / §4.3.
- **Patchouli stays** through 26.1.2 (GitHub-release jar `patchouli-neoforge-26.1-94.jar`
  as a `libs/` dependency in ThermalCore until it reaches maven).
- **License**: "CoFH - Don't Be a Jerk (Learn, Don't Steal)" — permits forking,
  modifying, and copying portions of CoFH's code. Art/sound assets are separately
  licensed CC BY-SA 4.0.
- **Branch per target version** (`1.20.4`, `1.20.6`, `1.21.1`, `26.1.2`), climbing in
  place rather than one long-lived branch — matches the sibling repos' branch strategy.
  `1.20.6` is a dead end kept for history; `1.21.1` was branched from it.
- **Verify every API shape against the real jar**, never against summarized docs or
  recollection of older versions. With ModDevGradle that is the patched *sources* jar:
  `unzip -p build/moddev/artifacts/neoforge-<neo_version>-sources.jar net/minecraft/.../X.java` (1.21.1 naming; 26.1's is `minecraft-patched-<neo_version>-sources.jar`)
  (NeoForge's own: `~/.gradle/caches/modules-2/files-2.1/net.neoforged/neoforge/<ver>/*/neoforge-<ver>-sources.jar`),
  or `javap -p` on `minecraft-patched-<ver>.jar`. This has been the single most reliable
  practice of the port so far — see progress-log.md's "working method" section.

## Current state

Branch **`1.21.1`**, building with **ModDevGradle 2.0.147 against NeoForge 21.1.251**
(Phase 0.3/0.4 and A.0 done here on 2026-09-21; `neoforge.mods.toml`; the 15 dead access
transformer lines that `validateAccessTransformers` rejected are gone). The twelve 1.20.6
commits are kept and still valid. Baseline was 849 errors / 175 files; **124 / 48** after Phase A.1 categories 1-11
(2026-09-22). See docs/TODO.md for what each category covered and what remains.

Shape oracle on this target: `build/moddev/artifacts/neoforge-21.1.251-sources.jar`
(`unzip -p … net/minecraft/…/X.java`); the MDG artifact names differ from 26.1's
`minecraft-patched-<ver>` layout.

The other three repos are still on their NeoGradle 1.20.6-bumped (uncommitted) build files —
their Phase 0.3/0.4 happens when their Phase A starts, after this repo builds clean.

**Next step**: the remaining tail (docs/TODO.md lists it by file). Categories 1-11 of
docs/port-plan.md §5 A.1 are done; 12-16 (datagen entrypoints, mixins, the AT sweep, resources,
Curios) plus ~48 files of assorted signature changes are what stands between here and a clean
compile. One commit and one error count per root cause, as before.
