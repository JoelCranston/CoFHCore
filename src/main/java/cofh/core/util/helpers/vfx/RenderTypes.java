package cofh.core.util.helpers.vfx;

import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;

import static cofh.core.init.CoreShaders.*;
import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

public class RenderTypes {

    public static final Identifier BLANK_TEXTURE = Identifier.fromNamespaceAndPath(ID_COFH_CORE, "textures/render/blank.png");
    public static final Identifier LIN_GLOW_TEXTURE = Identifier.fromNamespaceAndPath(ID_COFH_CORE, "textures/render/glow_linear.png");
    public static final Identifier RND_GLOW_TEXTURE = Identifier.fromNamespaceAndPath(ID_COFH_CORE, "textures/render/glow_round.png");

    public static final RenderType OVERLAY_LINES = RenderType.create("cofh:overlay_lines",
            RenderSetup.builder(LINES_NO_DEPTH)
                    // .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .sortOnUpload()
                    .bufferSize(256)
                    .createRenderSetup());

    public static final RenderType OVERLAY_BOX = RenderType.create("cofh:overlay_box",
            RenderSetup.builder(POSITION_COLOR_NO_DEPTH)
                    .sortOnUpload()
                    .bufferSize(256)
                    .createRenderSetup()
    );

    //    public static final RenderType BOX = RenderType.create("cofh:box_depth",
    //            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, true,
    //            RenderType.CompositeState.builder()
    //                    .setShaderState(POSITION_COLOR_SHADER)
    //                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
    //                    .setWriteMaskState(COLOR_WRITE)
    //                    .createCompositeState(false)
    //    );

    public static final RenderType FLAT_CUTOUT = opaque("cofh_core:opaque", BLANK_TEXTURE);
    public static final RenderType FLAT_TRANSLUCENT = translucentNoDepthWrite(BLANK_TEXTURE);
    public static final RenderType LINEAR_GLOW = translucentNoDepthWrite(LIN_GLOW_TEXTURE);
    public static final RenderType ROUND_GLOW = translucentNoDepthWrite(RND_GLOW_TEXTURE);

    public static RenderType opaque(String name, Identifier texture) {

        return RenderType.create(name, RenderSetup.builder(RenderPipelines.ENTITY_SOLID)
                .withTexture("Sampler0", texture)
                .useLightmap()
                .useOverlay()
                .sortOnUpload()
                .bufferSize(256)
                .createRenderSetup());
    }

    public static RenderType translucent(Identifier texture) {

        return RenderType.create("cofh_core:translucent", RenderSetup.builder(ENTITY_TRANSLUCENT)
                .withTexture("Sampler0", texture)
                .useLightmap()
                .sortOnUpload()
                .bufferSize(256)
                .createRenderSetup());
    }

    public static RenderType translucentNoCull(Identifier texture) {

        return RenderType.create("cofh_core:translucent", RenderSetup.builder(ENTITY_TRANSLUCENT_NO_CULL)
                .withTexture("Sampler0", texture)
                .useLightmap()
                .sortOnUpload()
                .bufferSize(256)
                .createRenderSetup());
    }

    public static RenderType translucentNoDepthWrite(Identifier texture) {

        return RenderType.create("cofh_core:translucent",
                //RenderType.CompositeState.builder()
                //        .setShaderState(RenderType.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                //        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                //        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                //        .setCullState(CULL)
                //        .setLightmapState(LIGHTMAP)
                //        .setOverlayState(OVERLAY)
                //        .createCompositeState(false));
                RenderSetup.builder(ENTITY_TRANSLUCENT_NO_DEPTH_WRITE)
                        .withTexture("Sampler0", texture)
                        .useLightmap()
                        .sortOnUpload()
                        .bufferSize(256)
                        .createRenderSetup());
    }

    public static SingleQuadParticle.Layer PARTICLE_SHEET_OVER = new SingleQuadParticle.Layer(true, TextureAtlas.LOCATION_PARTICLES, PARTICLE_OVER);
    public static SingleQuadParticle.Layer PARTICLE_SHEET_ADDITIVE_MULTIPLY = new SingleQuadParticle.Layer(true, TextureAtlas.LOCATION_PARTICLES, PARTICLE_ADDITIVE_MULTIPLY);
    public static SingleQuadParticle.Layer PARTICLE_SHEET_ADDITIVE_SCREEN = new SingleQuadParticle.Layer(true, TextureAtlas.LOCATION_PARTICLES, PARTICLE_ADDITIVE_SCREEN);

}
