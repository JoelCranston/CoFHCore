#!/bin/bash
# Vendors the porting reference material docs/port-plan.md relies on into docs/reference/
# (gitignored, local-only). Re-runnable; every file is overwritten. See docs/port-plan.md §4.1.
set -e
D="$(cd "$(dirname "$0")/.." && pwd)/docs/reference"
mkdir -p "$D/primers" "$D/neo-notes" "$D/docs-26.1" "$D/docs-1.21.1" "$D/neoforge-src"

# Vanilla migration primers (CC BY 4.0, ChampionAsh5357) - the "Mod Migration Primer" pages on docs.neoforged.net
for v in 1.20.5 1.20.6 1.21 1.21.1 1.21.2 1.21.4 1.21.5 1.21.6 1.21.7 1.21.8 1.21.9 1.21.10 1.21.11 26.1; do
  curl -sfL "https://raw.githubusercontent.com/neoforged/.github/main/primers/$v/index.md" -o "$D/primers/$v.md"
done

# NeoForge release / "Neo Changes" posts. The docs site's "Neo Changes" pages are iframes of these.
for n in 20.5release 21.0release 21.2release 21.4release 21.5release 21.6release 21.9release 21.9-transfer-rework 21.11release 26.1release; do
  curl -sfL "https://raw.githubusercontent.com/neoforged/websites/main/content/news/$n.md" -o "$D/neo-notes/$n.md"
done

# NeoForge docs: current site (= 26.1) and the archived 1.21.1 set
for p in blockentities/ber blockentities/index blocks/index concepts/events concepts/registries datastorage/attachments datastorage/codecs datastorage/nbt datastorage/saveddata datastorage/valueio entities/index entities/renderer inventories/capabilities inventories/container inventories/menus inventories/transactions items/armor items/datacomponents items/index items/interactions items/tools misc/config misc/identifier misc/keymappings networking/index networking/payload networking/streamcodecs rendering/feature rendering/particles rendering/screens resources/client/models/index resources/client/models/items resources/client/models/modelloaders resources/client/models/modelsystem resources/client/models/datagen resources/client/particles resources/client/textures resources/server/recipes/index resources/server/recipes/custom resources/server/recipes/ingredients resources/server/loottables/index resources/server/loottables/custom resources/server/tags resources/server/datamaps/index resources/server/enchantments/index advanced/accesstransformers; do
  mkdir -p "$D/docs-26.1/$(dirname "$p")"
  curl -sfL "https://raw.githubusercontent.com/neoforged/Documentation/main/docs/$p.md" -o "$D/docs-26.1/$p.md" || echo "missing docs-26.1/$p"
done
for p in blockentities/ber blocks/index concepts/events concepts/registries datastorage/attachments datastorage/nbt gui/menus gui/screens inventories/capabilities items/datacomponents items/index items/interactionpipeline items/tools misc/keymappings misc/resourcelocation networking/payload resources/client/models/bakedmodel resources/client/models/modelloaders resources/server/recipes/index resources/server/recipes/ingredients resources/server/loottables/index resources/server/tags resources/server/enchantments/index; do
  mkdir -p "$D/docs-1.21.1/$(dirname "$p")"
  curl -sfL "https://raw.githubusercontent.com/neoforged/Documentation/main/versioned_docs/version-1.21.1/$p.md" -o "$D/docs-1.21.1/$p.md" || echo "missing docs-1.21.1/$p"
done

# NeoForge source trees as file lists ("does class X still exist / where did it go")
curl -sf "https://api.github.com/repos/neoforged/NeoForge/git/trees/1.21.1?recursive=1" | python3 -c 'import json,sys;[print(t["path"]) for t in json.load(sys.stdin)["tree"] if t["path"].endswith(".java")]' > "$D/neoforge-src/files-1.21.1.txt"
curl -sf "https://api.github.com/repos/neoforged/NeoForge/git/trees/26.1.x?recursive=1"  | python3 -c 'import json,sys;[print(t["path"]) for t in json.load(sys.stdin)["tree"] if t["path"].endswith(".java")]' > "$D/neoforge-src/files-26.1.txt"

# Individual NeoForge 26.1.x files worth having verbatim
for f in src/main/java/net/neoforged/neoforge/capabilities/Capabilities.java src/client/java/net/neoforged/neoforge/client/extensions/BlockStateModelExtension.java src/client/java/net/neoforged/neoforge/client/model/DynamicBlockStateModel.java src/main/java/net/neoforged/neoforge/common/extensions/IBlockEntityExtension.java src/client/java/net/neoforged/neoforge/client/event/RenderLevelStageEvent.java src/client/java/net/neoforged/neoforge/client/event/ModelEvent.java src/client/java/net/neoforged/neoforge/client/event/RecipesReceivedEvent.java src/main/java/net/neoforged/neoforge/items/IItemHandler.java src/main/java/net/neoforged/neoforge/energy/IEnergyStorage.java tests/src/main/java/net/neoforged/neoforge/oldtest/client/model/MegaModelTest.java tests/src/main/java/net/neoforged/neoforge/oldtest/client/model/NewModelLoaderTest.java tests/src/main/java/net/neoforged/neoforge/oldtest/client/model/DynBucketModelTest.java tests/src/main/java/net/neoforged/neoforge/oldtest/fluid/NewFluidTest.java tests/src/main/java/net/neoforged/neoforge/oldtest/client/rendering/CustomParticleTypeTest.java tests/src/main/java/net/neoforged/neoforge/oldtest/item/CustomFluidContainerTest.java; do
  mkdir -p "$D/neoforge-src/26.1/$(dirname "$f")"
  curl -sfL "https://raw.githubusercontent.com/neoforged/NeoForge/26.1.x/$f" -o "$D/neoforge-src/26.1/$f" || echo "missing neoforge 26.1.x $f"
done

# JEI 29 API (26.1 branch): file list + the interfaces the Thermal repos implement
curl -sf "https://api.github.com/repos/mezz/JustEnoughItems/git/trees/26.1?recursive=1" | python3 -c 'import json,sys;[print(t["path"]) for t in json.load(sys.stdin)["tree"] if "/api/" in t["path"] and t["path"].endswith(".java")]' > "$D/neoforge-src/jei-26.1-api-files.txt"
for f in Common/src/api/java/mezz/jei/api/IModPlugin.java Common/src/api/java/mezz/jei/api/registration/IRecipeRegistration.java Common/src/api/java/mezz/jei/api/recipe/category/IRecipeCategory.java Common/src/api/java/mezz/jei/api/registration/ISubtypeRegistration.java Common/src/api/java/mezz/jei/api/helpers/IJeiHelpers.java Common/src/api/java/mezz/jei/api/helpers/IGuiHelper.java Common/src/api/java/mezz/jei/api/gui/builder/IRecipeLayoutBuilder.java Common/src/api/java/mezz/jei/api/recipe/types/IRecipeType.java Common/src/api/java/mezz/jei/api/registration/IRecipeCatalystRegistration.java Common/src/api/java/mezz/jei/api/registration/IGuiHandlerRegistration.java Common/src/api/java/mezz/jei/api/registration/IAdvancedRegistration.java Common/src/api/java/mezz/jei/api/recipe/advanced/IRecipeManagerPlugin.java Common/src/api/java/mezz/jei/api/ingredients/subtypes/ISubtypeInterpreter.java Common/src/api/java/mezz/jei/api/helpers/IPlatformFluidHelper.java NeoForge/src/api/java/mezz/jei/api/neoforge/NeoForgeTypes.java; do
  mkdir -p "$D/neoforge-src/jei-26.1/$(dirname "$f")"
  curl -sfL "https://raw.githubusercontent.com/mezz/JustEnoughItems/26.1/$f" -o "$D/neoforge-src/jei-26.1/$f" || echo "missing jei $f"
done

echo "done: $D"
