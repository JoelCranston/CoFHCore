package cofh.core.util;

import cofh.core.client.event.CoreClientSetupEvents;
import cofh.lib.api.IProxyItemPropertyGetter;
import cofh.lib.api.block.entity.IAreaEffectTile;
import cofh.lib.util.helpers.SoundHelper;
import cofh.lib.util.helpers.StringHelper;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;

import javax.annotation.Nullable;
import java.util.*;

public class ProxyClient extends Proxy {

    protected static final Map<Identifier, Object> MODEL_MAP = new Object2ObjectOpenHashMap<>();
    protected static final Map<Identifier, Map<Item, IProxyItemPropertyGetter>> ITEM_PROPERTY_GETTERS = new Object2ObjectOpenHashMap<>();
    protected static final Set<IAreaEffectTile> AREA_EFFECT_TILES = Collections.newSetFromMap(new WeakHashMap<>());

    // region HELPERS
    @Override
    public void setOverlayMessage(Component message) {

        Minecraft.getInstance().gui.setOverlayMessage(message, false);
    }

    @Override
    public void playSimpleSound(SoundEvent sound, float volume, float pitch) {

        SoundHelper.playSimpleSound(sound, volume, pitch);
    }

    @Override
    public Player getClientPlayer() {

        return Minecraft.getInstance().player;
    }

    @Override
    public Level getClientWorld() {

        return Minecraft.getInstance().level;
    }

    @Override
    public boolean isClient() {

        return true;
    }

    @Override
    public boolean canLocalize(String key) {

        return StringHelper.canLocalize(key);
    }

    @Override
    protected Object addModel(Identifier loc, Object model) {

        return MODEL_MAP.put(loc, model);
    }

    @Override
    public Object getModel(Identifier loc) {

        return MODEL_MAP.get(loc);
    }

    @Override
    public void addColorable(Item colorable) {

        CoreClientSetupEvents.addColorable(colorable);
    }

    @Override
    public void registerItemModelProperty(Item item, Identifier resourceLoc, IProxyItemPropertyGetter propertyGetter) {

        ITEM_PROPERTY_GETTERS.computeIfAbsent(resourceLoc, k -> new Object2ObjectOpenHashMap<>()).put(item, propertyGetter);
    }

    @Override
    public void addAreaEffectTile(IAreaEffectTile tile) {

        AREA_EFFECT_TILES.add(tile);
    }

    @Override
    public void removeAreaEffectTile(IAreaEffectTile tile) {

        AREA_EFFECT_TILES.remove(tile);
    }
    // endregion

    public static Set<IAreaEffectTile> getAreaEffectTiles() {

        return AREA_EFFECT_TILES;
    }

    public static void registerItemModelProperties(RegisterRangeSelectItemModelPropertyEvent event) {

        for (Identifier resourceLoc : ITEM_PROPERTY_GETTERS.keySet()) {
            event.register(resourceLoc, new ModelPropertyWrapper(resourceLoc).codec);
        }
    }

    protected static class ModelPropertyWrapper implements RangeSelectItemModelProperty {

        Identifier resourceLoc;
        MapCodec<ModelPropertyWrapper> codec;

        ModelPropertyWrapper(Identifier resourceLoc) {

            this.resourceLoc = resourceLoc;
            this.codec = MapCodec.unit(this);
        }

        @Override
        public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {

            IProxyItemPropertyGetter propertyGetter = ITEM_PROPERTY_GETTERS.get(resourceLoc).get(stack.getItem());
            return propertyGetter == null ? 0.0F : propertyGetter.call(stack, level, owner == null ? null : owner.asLivingEntity(), seed);
        }

        @Override
        public MapCodec<ModelPropertyWrapper> type() {

            return codec;
        }

    }

}
