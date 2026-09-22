package cofh.core.init;

import cofh.core.client.PostBuffer;
import cofh.core.common.config.CoreClientConfig;
import cofh.core.util.helpers.RenderHelper;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

@EventBusSubscriber (value = Dist.CLIENT, modid = ID_COFH_CORE)
public class CoreShaders {

    private static final BlendFunction ALPHA_BLEND = new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
    private static final DepthStencilState NO_DEPTH_WRITE = new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false);
    private static final DepthStencilState NO_DEPTH_TEST = new DepthStencilState(CompareOp.ALWAYS_PASS, false);

    public static final RenderPipeline PARTICLE_OVER = particle("particle_over", BlendFunction.TRANSLUCENT);
    public static final RenderPipeline PARTICLE_ADDITIVE_MULTIPLY = particle("particle_add", ALPHA_BLEND);
    public static final RenderPipeline PARTICLE_ADDITIVE_SCREEN = particle("particle_screen", ALPHA_BLEND);

    public static final RenderPipeline LINES_NO_DEPTH = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(ID_COFH_CORE, "pipeline/lines_no_depth"))
            .withDepthStencilState(NO_DEPTH_TEST)
            .build();
    public static final RenderPipeline POSITION_COLOR_NO_DEPTH = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(ID_COFH_CORE, "pipeline/position_color_no_depth"))
            .withDepthStencilState(NO_DEPTH_TEST)
            .build();
    public static final RenderPipeline ENTITY_TRANSLUCENT = translucent("entity_translucent", true, DepthStencilState.DEFAULT);
    public static final RenderPipeline ENTITY_TRANSLUCENT_NO_CULL = translucent("entity_translucent_no_cull", false, NO_DEPTH_WRITE);
    public static final RenderPipeline ENTITY_TRANSLUCENT_NO_DEPTH_WRITE = translucent("entity_translucent_no_depth_write", true, NO_DEPTH_WRITE);

    public static final PostBuffer PIXELATE = new PostBuffer(Identifier.fromNamespaceAndPath(ID_COFH_CORE, "pixelate")) {

        @Override
        public boolean isEnabled() {

            return super.isEnabled() && RenderHelper.isFabulousGraphics() && CoreClientConfig.stylizedGraphics.get();
        }

        @Override
        public RenderType getRenderType(Identifier texture) {

            return RenderType.create(outputName, RenderSetup.builder(ENTITY_TRANSLUCENT)
                    .withTexture("Sampler0", texture)
                    .useLightmap()
                    .setOutputTarget(getOutputTarget())
                    .bufferSize(buffer.getSize())
                    .createRenderSetup());
        }

    };

    @SubscribeEvent
    public static void registerPipelines(final RegisterRenderPipelinesEvent event) {

        event.registerPipeline(PARTICLE_OVER);
        event.registerPipeline(PARTICLE_ADDITIVE_MULTIPLY);
        event.registerPipeline(PARTICLE_ADDITIVE_SCREEN);
        event.registerPipeline(LINES_NO_DEPTH);
        event.registerPipeline(POSITION_COLOR_NO_DEPTH);
        event.registerPipeline(ENTITY_TRANSLUCENT);
        event.registerPipeline(ENTITY_TRANSLUCENT_NO_CULL);
        event.registerPipeline(ENTITY_TRANSLUCENT_NO_DEPTH_WRITE);
    }

    private static RenderPipeline particle(String id, BlendFunction blend) {

        return RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(ID_COFH_CORE, "pipeline/" + id))
                .withVertexShader(Identifier.fromNamespaceAndPath(ID_COFH_CORE, "core/particle"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(ID_COFH_CORE, "core/" + id))
                .withColorTargetState(new ColorTargetState(blend))
                .withDepthStencilState(NO_DEPTH_WRITE)
                .build();
    }

    private static RenderPipeline translucent(String id, boolean cull, DepthStencilState depth) {

        return RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(ID_COFH_CORE, "pipeline/" + id))
                .withShaderDefine("NO_CARDINAL_LIGHTING")
                .withShaderDefine("NO_OVERLAY")
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withCull(cull)
                .withDepthStencilState(depth)
                .build();
    }

}
