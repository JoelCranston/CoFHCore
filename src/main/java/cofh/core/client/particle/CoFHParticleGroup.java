package cofh.core.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Renders {@link CoFHParticle}s, which draw their own geometry through a {@link MultiBufferSource}.
 */
public class CoFHParticleGroup extends ParticleGroup<CoFHParticle> {

    public CoFHParticleGroup(ParticleEngine engine) {

        super(engine);
    }

    @Override
    public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float pTicks) {

        List<CoFHParticle> visible = new ArrayList<>();
        for (CoFHParticle particle : this.particles) {
            if (frustum.isVisible(particle.getBoundingBox())) {
                visible.add(particle);
            }
        }
        return new State(visible, camera.position(), pTicks);
    }

    private record State(List<CoFHParticle> particles, Vec3 cameraPos, float pTicks) implements ParticleGroupRenderState {

        @Override
        public void submit(SubmitNodeCollector collector, CameraRenderState camera) {

            PoseStack stack = new PoseStack();
            Recorder recorder = new Recorder();
            for (CoFHParticle particle : particles) {
                particle.render(stack, recorder, cameraPos, pTicks);
            }
            recorder.submit(collector, stack);
        }

    }

    // Custom geometry gets one buffer per render type, so record now and replay per type.
    private static class Recorder implements MultiBufferSource {

        private final Map<RenderType, Recording> recordings = new LinkedHashMap<>();

        @Override
        public VertexConsumer getBuffer(RenderType type) {

            return recordings.computeIfAbsent(type, t -> new Recording());
        }

        public void submit(SubmitNodeCollector collector, PoseStack stack) {

            recordings.forEach((type, recording) -> collector.submitCustomGeometry(stack, type, recording));
        }

    }

    private static class Recording implements VertexConsumer, SubmitNodeCollector.CustomGeometryRenderer {

        private static final int VERTEX = 0;
        private static final int COLOR = 1;
        private static final int PACKED_COLOR = 2;
        private static final int UV = 3;
        private static final int UV1 = 4;
        private static final int UV2 = 5;
        private static final int NORMAL = 6;
        private static final int LINE_WIDTH = 7;

        private final IntArrayList ints = new IntArrayList();
        private final FloatArrayList floats = new FloatArrayList();

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {

            ints.add(VERTEX);
            floats.add(x);
            floats.add(y);
            floats.add(z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {

            ints.add(COLOR);
            ints.add(r);
            ints.add(g);
            ints.add(b);
            ints.add(a);
            return this;
        }

        @Override
        public VertexConsumer setColor(int color) {

            ints.add(PACKED_COLOR);
            ints.add(color);
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {

            ints.add(UV);
            floats.add(u);
            floats.add(v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {

            ints.add(UV1);
            ints.add(u);
            ints.add(v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {

            ints.add(UV2);
            ints.add(u);
            ints.add(v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {

            ints.add(NORMAL);
            floats.add(x);
            floats.add(y);
            floats.add(z);
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float width) {

            ints.add(LINE_WIDTH);
            floats.add(width);
            return this;
        }

        @Override
        public void render(PoseStack.Pose pose, VertexConsumer buffer) {

            int i = 0;
            int f = 0;
            while (i < ints.size()) {
                switch (ints.getInt(i++)) {
                    case VERTEX -> {
                        buffer.addVertex(floats.getFloat(f), floats.getFloat(f + 1), floats.getFloat(f + 2));
                        f += 3;
                    }
                    case COLOR -> {
                        buffer.setColor(ints.getInt(i), ints.getInt(i + 1), ints.getInt(i + 2), ints.getInt(i + 3));
                        i += 4;
                    }
                    case PACKED_COLOR -> buffer.setColor(ints.getInt(i++));
                    case UV -> {
                        buffer.setUv(floats.getFloat(f), floats.getFloat(f + 1));
                        f += 2;
                    }
                    case UV1 -> {
                        buffer.setUv1(ints.getInt(i), ints.getInt(i + 1));
                        i += 2;
                    }
                    case UV2 -> {
                        buffer.setUv2(ints.getInt(i), ints.getInt(i + 1));
                        i += 2;
                    }
                    case NORMAL -> {
                        buffer.setNormal(floats.getFloat(f), floats.getFloat(f + 1), floats.getFloat(f + 2));
                        f += 3;
                    }
                    case LINE_WIDTH -> buffer.setLineWidth(floats.getFloat(f++));
                }
            }
        }

    }

}
