package cofh.core.client.event;

import cofh.core.common.capability.CoreCapabilities;
import cofh.core.common.capability.templates.AreaEffectItemWrapper;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;

import java.util.ArrayList;
import java.util.List;

import static cofh.core.util.helpers.AreaEffectHelper.validAreaEffectItem;
import static cofh.core.util.helpers.AreaEffectHelper.validAreaEffectMiningItem;
import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

@EventBusSubscriber (value = Dist.CLIENT, modid = ID_COFH_CORE)
public class AreaEffectClientEvents {

    private AreaEffectClientEvents() {

    }

    @SubscribeEvent (priority = EventPriority.LOW)
    public static void renderBlockHighlights(ExtractBlockOutlineRenderStateEvent event) {

        if (event.isCanceled()) {
            return;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (!validAreaEffectItem(stack)) {
            return;
        }
        ImmutableList<BlockPos> areaBlocks = getAreaEffectBlocks(stack, event.getBlockPos(), player);

        LevelRenderer levelRenderer = event.getLevelRenderer();
        CollisionContext context = event.getCollisionContext();
        Level world = player.level;

        List<BlockOutlineRenderState> outlines = new ArrayList<>(areaBlocks.size());
        for (BlockPos pos : areaBlocks) {
            if (world.getWorldBorder().isWithinBounds(pos)) {
                outlines.add(new BlockOutlineRenderState(pos, event.isInTranslucentPass(), event.isHighContrast(), world.getBlockState(pos).getShape(world, pos, context), List.of()));
            }
        }
        event.addCustomRenderer((state, buffer, poseStack, translucentPass, levelRenderState) -> {
            if (translucentPass == state.isTranslucent()) {
                drawBlockOutlines(levelRenderer, buffer, poseStack, levelRenderState, outlines);
            }
            return false;
        });
    }

    @SubscribeEvent
    public static void renderBlockDamage(ExtractLevelRenderStateEvent event) {

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        MultiPlayerGameMode gamemode = minecraft.gameMode;
        if (player == null || gamemode == null || !gamemode.isDestroying()) {
            return;
        }
        if (!(minecraft.hitResult instanceof BlockHitResult hit) || hit.getType() == HitResult.Type.MISS) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (!validAreaEffectItem(stack) || !validAreaEffectMiningItem(stack)) {
            return;
        }
        addBlockDamageStates(gamemode, event.getRenderState(), player.level, getAreaEffectBlocks(stack, hit.getBlockPos(), player));
    }

    // region HELPERS
    private static ImmutableList<BlockPos> getAreaEffectBlocks(ItemStack stack, BlockPos pos, Player player) {

        var aeCap = stack.getCapability(CoreCapabilities.AreaEffectHandler.ITEM);
        if (aeCap == null) {
            aeCap = new AreaEffectItemWrapper(stack);
        }
        return aeCap.getAreaEffectBlocks(pos, player);
    }

    private static void drawBlockOutlines(LevelRenderer levelRenderer, MultiBufferSource buffer, PoseStack poseStack, LevelRenderState levelRenderState, List<BlockOutlineRenderState> outlines) {

        Vec3 cameraPos = levelRenderState.cameraRenderState.pos;
        VertexConsumer vertexBuilder = buffer.getBuffer(RenderTypes.lines());
        float width = Minecraft.getInstance().gameRenderer.getGameRenderState().windowRenderState.appropriateLineWidth;

        for (BlockOutlineRenderState outline : outlines) {
            levelRenderer.renderHitOutline(poseStack, vertexBuilder, cameraPos.x, cameraPos.y, cameraPos.z, outline, ARGB.black(102), width);
        }
    }

    private static void addBlockDamageStates(MultiPlayerGameMode gameMode, LevelRenderState renderState, Level level, List<BlockPos> areaBlocks) {

        int progress = (int) (gameMode.destroyProgress * 10.0F) - 1;
        if (progress < 0 || progress > 10) {
            return;
        }
        progress = Math.min(progress + 1, 9); // Ensure that for whatever reason the progress level doesn't go OOB.

        for (BlockPos pos : areaBlocks) {
            renderState.blockBreakingRenderStates.add(new BlockBreakingRenderState(pos, level.getBlockState(pos), progress));
        }
    }
    // endregion
}
