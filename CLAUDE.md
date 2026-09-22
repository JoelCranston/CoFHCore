# CoFHCore — NeoForge port (foundation library)

## Where things are

| File | Read it when |
|---|---|
| **This file** | Always. Context, decisions, and current state. |
| [docs/api-notes-1.20.6.md](docs/api-notes-1.20.6.md) | Writing any code against a 1.20.5/1.20.6 API. Every API shape confirmed by decompiling the real mapped jar, organized by category, in the order they were found. |
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

This is a **primer-climbing port**: starting from CoFHCore's existing NeoForge 1.20.4
codebase (already in place before this effort began) and climbing NeoForge's official
primers one version at a time toward the target, **26.1.2** — not a from-scratch
rewrite, and not a single big jump. See progress-log.md's Phase 1 entry for why (no
NeoForge port of CoFHCore exists anywhere to build on top of instead, official or
community).

This is unrelated to **Pyronetics** (`/Volumes/Mac_External/Developer/Minecraft/Pyronetics`),
a separate from-scratch mod in its own repo/session inspired by classic Thermal Expansion's
designs but with zero dependency on this codebase.

## Decisions already made

- **No official or community NeoForge port of CoFHCore exists** to build on. The
  community fork (SPLIGAN's `ThermalExpansionForNeoForge`/`ThermalCoreForNeoForge`,
  cloned as sibling repos) claims NeoForge 1.21.1 support but depends on a
  `cofh_core` maven artifact that was never published past 1.20.1 — it can't actually
  compile as checked out. Porting this repo myself, from its existing 1.20.4 codebase.
- **Target: NeoForge 26.1.2**, climbed via the full official primer chain rather than
  jumping straight there or stopping at 1.21.1 to match SPLIGAN's (non-compiling)
  attempt — see progress-log.md.
- **License**: "CoFH - Don't Be a Jerk (Learn, Don't Steal)" — permits forking,
  modifying, and copying portions of CoFH's code. Art/sound assets are separately
  licensed CC BY-SA 4.0.
- **Branch per target version** (`1.20.4`, `1.20.6`, …), climbing in place rather than
  one long-lived branch — matches the sibling repos' branch strategy.
- **Verify every API shape against the real mapped jar** (`javap` against
  `build/neoForm/neoFormJoined<version>-.../raw.jar`), never against summarized docs or
  recollection of older versions. This has been the single most reliable practice of
  the port so far — see progress-log.md's "working method" section.

## Current state

Branch `1.20.6`, mid-hop: **396 errors / 104 files** as of the last fresh compile
(2026-09-22) — see [docs/TODO.md](docs/TODO.md) for the prioritized, current list and
[docs/progress-log.md](docs/progress-log.md) for the full chronology of what's already
fixed. `git status` also has some stray `.DS_Store` files untracked — harmless, not
part of this port, worth a `.gitignore` entry whenever convenient.

**Next step**: work through docs/TODO.md's list, largest bucket first (raw `ItemStack`
NBT call sites still using the pre-DataComponents API). Once this repo compiles clean
on 1.20.6, the other three repos can commit their own already-bumped `gradle.properties`
and start their own 1.20.6 migrations — many of the same API shapes will likely recur;
check here first.
