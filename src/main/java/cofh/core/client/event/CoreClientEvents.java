package cofh.core.client.event;

import cofh.core.client.PostEffect;
import cofh.core.common.config.CoreClientConfig;
import cofh.lib.util.Utils;
import cofh.lib.util.constants.ModIds;
import cofh.lib.util.raytracer.VoxelShapeBlockHitResult;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Util;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.joml.Matrix4f;

import java.util.*;
import java.util.stream.Collectors;

import static cofh.core.init.CoreMobEffects.CHILLED;
import static cofh.core.init.CoreMobEffects.TRUE_INVISIBILITY;
import static cofh.lib.util.Constants.INVIS_STYLE;
import static cofh.lib.util.helpers.StringHelper.*;
import static net.minecraft.ChatFormatting.DARK_GRAY;
import static net.minecraft.ChatFormatting.GRAY;

@EventBusSubscriber (value = Dist.CLIENT, modid = ModIds.ID_COFH_CORE)
public class CoreClientEvents {

    public static int renderTime;
    public static float renderFrame;
    public static final ContextKey<Boolean> TRUE_INVISIBLE = new ContextKey<>(Identifier.fromNamespaceAndPath(ModIds.ID_COFH_CORE, "true_invisible"));

    private static final Set<String> NAMESPACES = new ObjectOpenHashSet<>();

    public static boolean addNamespace(String namespace) {

        return NAMESPACES.add(namespace);
    }

    private CoreClientEvents() {

    }

    @SubscribeEvent
    public static void handleItemTooltipEvent(ItemTooltipEvent event) {

        List<Component> tooltip = event.getToolTip();
        if (tooltip.isEmpty()) {
            return;
        }
        ItemStack stack = event.getItemStack();

        if (CoreClientConfig.enableKeywords.get() && NAMESPACES.contains(Utils.getModId(stack.getItem()))) {
            String keywordKey = stack.getItem().getDescriptionId() + ".keyword";
            if (canLocalize(keywordKey)) {
                if (tooltip.get(0) instanceof MutableComponent mutable) {
                    mutable.append(getKeywordTextComponent(keywordKey));
                }
            }
        }
        if (CoreClientConfig.enableItemDescriptions.get() && NAMESPACES.contains(Utils.getModId(stack.getItem()))) {
            String infoKey = stack.getItem().getDescriptionId() + ".desc";
            if (canLocalize(infoKey)) {
                tooltip.add(1, getInfoTextComponent(infoKey));
            }
        }
        if (CoreClientConfig.enableEnchantmentDescriptions.get()) {
            ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
            if (stored.size() == 1) {
                Holder<Enchantment> ench = stored.keySet().iterator().next();
                ResourceKey<Enchantment> key = ench.unwrapKey().orElse(null);
                if (key != null) {
                    String enchKey = Util.makeDescriptionId("enchantment", key.identifier()) + ".desc";
                    if (canLocalize(enchKey)) {
                        tooltip.add(getInfoTextComponent(enchKey));
                    }
                }
            }
        }
        //        if (CoreConfig.enableFoodDescriptions) {
        //            if (stack.isEdible()) {
        //
        //            }
        //        }
        if (CoreClientConfig.enableItemTags.get() && event.getFlags().isAdvanced()) {
            Item item = event.getItemStack().getItem();
            Block block = Block.byItem(item);

            Set<Identifier> blockTags = block == Blocks.AIR ? Collections.emptySet() : Block.byItem(item).builtInRegistryHolder().tags().map(TagKey::location).collect(Collectors.toSet());
            Set<Identifier> itemTags = item.builtInRegistryHolder().tags().map(TagKey::location).collect(Collectors.toSet());

            if (!blockTags.isEmpty() || !itemTags.isEmpty()) {
                if (Minecraft.getInstance().hasControlDown()) {
                    if (!blockTags.isEmpty()) {
                        tooltip.add(getTextComponent("info.cofh.block_tags").withStyle(GRAY));
                        blockTags.stream()
                                .map(Object::toString)
                                .map(s -> "  " + s)
                                .map(t -> getTextComponent(t).withStyle(DARK_GRAY))
                                .forEach(tooltip::add);
                    }

                    if (!itemTags.isEmpty()) {
                        tooltip.add(getTextComponent("info.cofh.item_tags").withStyle(GRAY));
                        itemTags.stream()
                                .map(Object::toString)
                                .map(s -> "  " + s)
                                .map(t -> getTextComponent(t).withStyle(DARK_GRAY))
                                .forEach(tooltip::add);
                    }
                } else {
                    tooltip.add(getTextComponent("info.cofh.hold_ctrl_for_tags").withStyle(GRAY));
                }
            }
        }
    }

    @SubscribeEvent
    public static void handleRenderTooltipEvent(RenderTooltipEvent.GatherComponents event) {

        if (event.getTooltipElements().isEmpty()) {
            return;
        }
        event.getTooltipElements().get(0).left().ifPresent((text) -> {
            if (text instanceof MutableComponent mutable) {
                mutable.getSiblings().removeIf(string -> string.getStyle().equals(INVIS_STYLE));
            }
        });
    }

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {

        renderTime++;
    }

    @SubscribeEvent
    public static void renderTick(RenderFrameEvent.Pre event) {

        renderFrame = event.getPartialTick().getGameTimeDeltaPartialTick(false);
    }

    @SubscribeEvent //(priority = EventPriority.LOWEST)
    public static void beginPostEffects(RenderLevelStageEvent.AfterSky event) {

        for (PostEffect effect : PostEffect.getAllEffects()) {
            if (effect.isEnabled()) {
                effect.begin(renderFrame);
            }
        }
    }

    @SubscribeEvent //(priority = EventPriority.LOWEST)
    public static void endPostEffects(RenderLevelStageEvent.AfterLevel event) {

        Minecraft minecraft = Minecraft.getInstance();
        for (PostEffect effect : PostEffect.getAllEffects()) {
            if (effect.isEnabled()) {
                effect.end(renderFrame);
                effect.apply(minecraft.getWindow());
            }
        }
        //RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        //for (PostEffect effect : PostEffect.getAllEffects()) {
        //    if (effect.isEnabled()) {
        //    }
        //}
    }

    @SubscribeEvent (priority = EventPriority.HIGH)
    public static <T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> void handleTrueInvisibility(RenderLivingEvent.Pre<T, S, M> event) {

        if (Boolean.TRUE.equals(event.getRenderState().getRenderData(TRUE_INVISIBLE))) {
            event.setCanceled(true);
        }
    }

    // Chilled scaled the cubed sensitivity; undo the cube so the option value scales the same.
    @SubscribeEvent
    public static void handleChilledTurn(CalculatePlayerTurnEvent event) {

        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        MobEffectInstance effect = player.getEffect(CHILLED);
        if (effect == null || effect.getAmplifier() < 0) {
            return;
        }
        float reduction = 15F / (16F + effect.getAmplifier());
        reduction *= reduction;
        reduction *= reduction;
        double scale = Math.cbrt(Math.max(reduction * reduction, 0.1F));
        double base = event.getMouseSensitivity() * 0.6D + 0.2D;
        event.setMouseSensitivity((base * scale - 0.2D) / 0.6D);
    }

    @SubscribeEvent (priority = EventPriority.HIGH)
    public static void handleTrueInvisibility(RenderHandEvent event) {

        Player player = Minecraft.getInstance().player;
        if (player != null && player.hasEffect(TRUE_INVISIBILITY) && player.isInvisible()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent (priority = EventPriority.LOW)
    public static void renderSubHitboxes(ExtractBlockOutlineRenderStateEvent event) {

        BlockHitResult hit = event.getHitResult();
        if (hit instanceof VoxelShapeBlockHitResult voxelHit) {
            BlockPos pos = voxelHit.getBlockPos();
            VoxelShape shape = voxelHit.shape;

            event.addCustomRenderer((state, buffer, stack, translucentPass, levelRenderState) -> {
                if (translucentPass == state.isTranslucent()) {
                    stack.pushPose();
                    stack.translate(pos.getX(), pos.getY(), pos.getZ());

                    bufferShapeHitBox(stack, buffer, levelRenderState.cameraRenderState.pos, shape);

                    stack.popPose();
                }
                return true;
            });
        }
    }

    // region HELPERS
    private static void bufferShapeHitBox(PoseStack pStack, MultiBufferSource buffers, Vec3 eye, VoxelShape shape) {

        pStack.translate((float) -eye.x, (float) -eye.y, (float) -eye.z);
        bufferShapeOutline(buffers.getBuffer(RenderTypes.lines()), pStack.last().pose(), shape, 0.0F, 0.0F, 0.0F, 0.4F);
    }

    private static void bufferShapeOutline(VertexConsumer builder, Matrix4f mat, VoxelShape shape, float r, float g, float b, float a) {

        float width = Minecraft.getInstance().gameRenderer.getGameRenderState().windowRenderState.appropriateLineWidth;
        shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
            double xn = x1 - x2;
            double yn = y1 - y2;
            double zn = z1 - z2;
            double d = Math.sqrt(xn * xn + yn * yn + zn * zn);
            xn /= d;
            yn /= d;
            zn /= d;

            builder.addVertex(mat, (float) x1, (float) y1, (float) z1).setColor(r, g, b, a).setNormal((float) xn, (float) yn, (float) zn).setLineWidth(width);
            builder.addVertex(mat, (float) x2, (float) y2, (float) z2).setColor(r, g, b, a).setNormal((float) xn, (float) yn, (float) zn).setLineWidth(width);
        });
    }
    // endregion
}
