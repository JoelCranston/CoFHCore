package cofh.core.init.data;

import cofh.core.init.data.providers.CoreLootTableProvider;
import cofh.core.init.data.providers.CoreTagsProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

@EventBusSubscriber (modid = ID_COFH_CORE)
public class CoreDataGen {

    @SubscribeEvent
    public static void gatherData(final GatherDataEvent.Client event) {

        // TileNBTSync.setup();

        event.createBlockAndItemTags(CoreTagsProvider.Block::new, CoreTagsProvider.Item::new);
        event.createProvider(CoreTagsProvider.Fluid::new);
        event.createProvider(CoreTagsProvider.DamageType::new);

        event.createProvider(CoreLootTableProvider::new);
    }

}
