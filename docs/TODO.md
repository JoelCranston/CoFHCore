# TODO

Phase A (1.21.1) is **code-complete**: all four repos build clean and boot headless on
NeoForge 21.1.251, and `runData` now runs in all four. Phase B (26.1.2) is under way on the
`26.1.2` branch: B.0-B.4 done in CoFHCore, **1326 errors** left. See
[api-notes-1.21.1.md](api-notes-1.21.1.md) / [api-notes-26.1.2.md](api-notes-26.1.2.md) for
confirmed shapes and [progress-log.md](progress-log.md) for the chronology.

## Owed before Phase A is signed off

1. **Client pass (Joel).** The one exit criterion a headless boot cannot cover. Port plan §A.4:
   a machine GUI (TE), an energy/fluid/item cell in world and in item form (TC), a duct network
   moving items and energy (TD), a JEI machine recipe page, the Patchouli guidebook opening,
   wrench side-config, a placed fluid, particles from a dynamo or machine. This hop rewrote the
   whole vertex/model/particle surface, so **everything client-side is unverified**. The
   `runData` pass already caught one client crash (`LevelRendererMixin`, fixed in `fa87214`), so
   expect more of that kind. Also check the specific item below.
2. **`MouseHandlerMixin` (Chilled effect's mouse slowdown), unverified.** It is a
   `@ModifyVariable(method = "turnPlayer", ordinal = 3, at = @At("STORE"))` over `double`
   locals. On 1.21.1 `turnPlayer` gained a `double` parameter and NeoForge's
   `CalculatePlayerTurnEvent`, so the LVT is now `d2` (sensitivity) / `d3` (its cube) / `d4` /
   `d0`/`d1` (turn deltas) and ordinal 3 very likely lands on a different variable than on 1.20.4.
   Symptom if wrong: Chilled slows only one axis, over- or under-scales, or the mixin fails to
   apply when `MouseHandler` loads. The clean fix is to drop the mixin for a
   `CalculatePlayerTurnEvent` handler (`setMouseSensitivity`). In the client pass, apply
   Chilled II and turn the camera.
3. ~~`runData` has not been run~~ **done 2026-09-22**, all four repos. It needed `data()` rather
   than `clientData()` on 1.21.1, and ThermalExpansion needs `--existing-mod thermal`. The
   regenerated output fixed silently-broken data: CoFHCore's `c:` tags and a loot table were
   still in 1.20 plural folders, 86 advancements matched any item, 4 loot tables ignored silk
   touch, and 8 of TD's advancements were missing. Details are in the progress log.

## Phase B (26.1.2) — in progress, CoFHCore first

| Step | Commit | Errors after |
|---|---|---|
| B.0 build bump + forced AT sweep (B.9's AT half) | `6eb201f` | 2445 / 318 files (baseline) |
| B.1 mechanical renames | `6f0c7a7`, stragglers `e7896aa` | 1537 |
| B.2 registration | `f02ba1b` | (included above) |
| B.3 persistence — bridged at `BlockEntityCoFH`, entities native, `SavedDataType`, tag getters | `8bd2f62` | 1364 |
| B.4 transfer API — CoFH handlers also implement the new interfaces (per-storage journals), query sites wrap with `.of()` | (this commit) | 1326 |
| **B.5 items/tools/armour** | next | |
| B.6 recipes · B.7 client (XL) · B.8 resources · B.9 mixins · B.10 dependents | | |

[port-plan.md](port-plan.md) §6 has each category's contents. Carry-overs into Phase B:

- **B.1 table rows not yet swept**, each owned by its later category: `Screen.hasShiftDown` (7
  files), `Ingredient.EMPTY`/`getItems()` (5, B.6),
  `getStillTexture`/`getFlowingTexture` (5, B.7e), the reload/shader events (4, B.7),
  `ClickType` → `ContainerInput` (1), `getCraftingRemainingItem` (1), `DeferredSpawnEggItem` (1),
  `RenderType.*` → `RenderTypes` (1). `javax.annotation` (95 files) still resolves; leave it.
- **B.7 / B.9: `LevelRendererMixin` needs a rewrite, not a retarget.** `renderLevel` still
  exists on 26.1.2, but the transparency chain is added to a frame graph
  (`PostChain#addToFrame`, `LevelRenderer.java:547`). The `PostChain#process(F)` call it injects
  before is gone, and the depth-mask workaround may no longer be needed at all. The 1.21.1 fix
  (`fa87214`) is on the `1.21.1` branch only.
- **B.10 inherits from B.3**: TC/TD block entities keep their `(CompoundTag, Provider)` overrides
  if they extend `BlockEntityCoFH` (check TD's `DuctBlockEntity`); their entities convert natively;
  TD drops `INBTSerializable` from its grid classes and moves `GridContainer` to `SavedDataType`.
  Friend lists and grid data won't carry over from 1.21.1 worlds (the saved-data file paths moved).
- **B.10 inherits from B.4**: capability registration moves to `Capabilities.Item/Fluid/Energy.BLOCK`,
  and `AugmentableBlockEntity`'s cached `itemCap`/`fluidCap`/`energyCap` fields narrow to the CoFH
  handler types. Also call `invalidateCapabilities()` after side-config changes and wrench
  rotation. B.4 is **unverified at runtime**: test a machine with a pipe mod or a GameTest,
  including an aborted simulation.
- **B.8: regenerate, don't hand-migrate.** The `26.1.2` branch predates the 1.21.1 `runData`
  commits, so its `src/main/generated` still has the stale 1.20 layout. On 26.1 the data run is
  `clientData()`, which the 26.1.2 `build.gradle` already declares. ThermalExpansion's
  `--existing-mod thermal` must carry over.

## After the port (Joel, 2026-09-22)

1. **Energy and fluid items aren't exposed as capabilities**, and weren't on 1.21.1 either (it's
   a pre-existing gap, not a regression). Energy cells and fluid cells in item form, the RF potato,
   the florb, satchels and any other `IEnergyContainerItem`/`IFluidContainerItem`/
   `IInventoryContainerItem` item register no item capability. So other mods' chargers, tanks and
   pipes can't see them, and `EnergyCellBlockItem`'s registration is commented out. On 26.1.2 this
   means `ItemAccess`-based handlers (`ItemAccessEnergyHandler`-style, exchanging the stack through
   the access) in place of `EnergyContainerItemWrapper`/`FluidContainerItemWrapper`/
   `InventoryContainerItemWrapper`, registered with
   `event.registerItem(Capabilities.Energy.ITEM, …)` etc. in ThermalCore. See
   api-notes-26.1.2.md, B.4 "Not covered".

## Inbox

- **Behaviour differences the style pass found (2026-09-22), left alone because that pass was
  not allowed to change behaviour.** Each is a 1.21.1 fix to decide on:
  - **Fluid buffers drop components.** TD's `FluidDuctWindowedBlockEntity` and its 4 fluid
    filter/servo menus, plus CoFHCore's `FluidFilterMenu#getGuiPacket`, hand-encode fluid id and
    amount. `FluidHelper.writeFluidStack`/`readFluidStack` exists to keep components. It's
    harmless while the GUIs only show fluid and amount, but it's inconsistent with the API notes.
  - **`CropBlockCoFH` lost its main-hand-only harvest check**, because `useWithoutItem` has no
    hand. The check could move to `useItemOn`.
  - **`InventoryContainerItem` reads with `RegistryAccess.EMPTY`**, which drops registry-bound
    components. §11 of the API notes says to use `ProxyUtils.registryAccess()` outside
    persistence.
  - **`EntityBlockCoFH` routes `useWithoutItem` into `useItemOn` with an empty stack**, the
    pattern §10 of the API notes warns about.
  - **ThermalCore `WrenchItem`** applies its attribute modifiers to every slot group via
    `MAINHAND`. Check that's intended.

- ~~`RecipeManager#byType`~~ resolved: ThermalCore's 26 call sites use the public
  `getAllRecipesFor(type)`, which returns a `List<RecipeHolder<T>>` rather than a Map.
- MDG artifact names for 1.21.1 are `build/moddev/artifacts/neoforge-21.1.251{,-sources,-merged}.jar`
  (Pyronetics' `minecraft-patched-<ver>` naming is the 26.1 layout). Use the `-sources` jar.

- `ThermalExpansion`'s hand-written machine recipes use `{"item": …, "count": n}` /
  `{"tag": "forge:…"}` parsed by CoFH's own `RecipeJsonUtils` — survives the 1.21.2 ingredient
  format change, but check `RecipeJsonUtils.parseIngredient` compiles (`Ingredient.fromJson` is gone).
- ~~`PotionColorCalculationEvent`~~ resolved in category 5: replaced by
  `CustomParticleMobEffect#createParticleOptions` returning null (per-effect, not global).
- ~~Cross-repo API changes ThermalCore/TD/TE must follow~~ all applied; kept here as the record
  of what the family's own API looks like on 1.21.1 (see also api-notes-1.21.1.md):
  `IEnergyContainerItem#getOrCreateEnergyTag` → `getEnergyTag` + `mutateEnergyTag`,
  `IFluidContainerItem#getOrCreateTankTag` → `getTankTag` + `mutateTankTag`,
  `IMultiModeItem#getOrCreateModeTag` → `getModeTag`/`setModeTag`, `ConfigManager#register` takes
  the `ModContainer`, and `Constants`' `UUID_*` modifier ids are `ResourceLocation`s now.
  TC's `EnergyCellBlockItem` overrides two of those.
- ~~Two unlisted mixin classes~~ resolved: `ClientPacketListenerMixin` and
  `ClientboundSetEntityMotionPacketMixin` are **entirely commented out** upstream, so leaving
  them unlisted is correct. What did need fixing: `MultiPlayerGameModeMixin` was listed under
  `mixins` rather than `client` and so failed to apply on a dedicated server, and
  `compatibilityLevel` is `JAVA_21` now.
