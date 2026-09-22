package cofh.core.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Credit to jozufozu and Flywheel for this mixin <3
 * <p>
 * Created by covers1624 on 1/1/22.
 */
@Mixin (LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Inject (
            method = "renderLevel",
            at = @At (
                    value = "INVOKE",
                    ordinal = 1,
                    target = "Lnet/minecraft/client/renderer/PostChain;process(F)V"
            )
    )
    private void disableTransparencyShaderDepth(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {

        GlStateManager._depthMask(false);
    }

}
