package cofh.core.client;

import cofh.core.util.helpers.RenderHelper;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

public abstract class PostBuffer extends PostEffect implements MultiBufferSource {

    protected final BasicBufferSource buffer = new BasicBufferSource(256);
    protected final String targetName;
    protected final String outputName;
    protected final OutputTarget fallback;
    protected OutputTarget output;
    protected RenderTarget target;
    protected boolean active;

    public PostBuffer(Identifier shader, String targetName, OutputTarget fallback) {

        super(shader);
        this.targetName = targetName;
        this.outputName = shader.toString();
        this.fallback = fallback;
        this.output = fallback;
    }

    public PostBuffer(Identifier shader) {

        this(shader, "final", OutputTarget.MAIN_TARGET);
    }

    @Override
    public VertexConsumer getBuffer(RenderType type) {

        if (isEnabled()) {
            active = true;
            return buffer.getBuffer(type);
        }
        return RenderHelper.bufferSource().getBuffer(type);
    }

    public VertexConsumer getBuffer(Identifier texture) {

        return getBuffer(getRenderType(texture));
    }

    public abstract RenderType getRenderType(Identifier texture);

    //public RenderTarget getRenderTarget() {
    //
    //    return target;
    //}

    public OutputTarget getOutputTarget() {

        return isEnabled() ? output : fallback;
    }

    @Override
    public void begin(float partialTick) {

        active = false;
    }

    @Override
    public void end(float partialTick) {

        if (active) {
            buffer.endBatch();
            super.end(partialTick);
        }
    }

    @Override
    public void apply(Window window) {

        if (active) {
            target.blitToScreen();
        }
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {

        target = null;
        output = OutputTarget.MAIN_TARGET;
        super.onResourceManagerReload(manager);
    }

    @Override
    protected void onChainLoad() {

        output = new OutputTarget(outputName, () -> target);
    }

}
