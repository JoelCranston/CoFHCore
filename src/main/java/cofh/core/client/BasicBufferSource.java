package cofh.core.client;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public class BasicBufferSource implements MultiBufferSource {

    // 1.21: a BufferBuilder is created per batch around a reusable ByteBufferBuilder (it can no
    // longer be re-begun), and a batch is finished by handing its built MeshData to the type.
    protected final ByteBufferBuilder buffer;
    protected final int size;
    protected BufferBuilder builder = null;
    protected RenderType lastState = null;

    public BasicBufferSource(int size) {

        this.buffer = new ByteBufferBuilder(size);
        this.size = size;
    }

    @Override
    public VertexConsumer getBuffer(RenderType type) {

        if (builder == null || !type.equals(lastState) || !type.canConsolidateConsecutiveGeometry()) {
            endBatch();
            builder = new BufferBuilder(buffer, type.mode(), type.format());
            lastState = type;
        }
        return builder;
    }

    public void endBatch() {

        if (lastState != null && builder != null) {
            MeshData mesh = builder.build();
            if (mesh != null) {
                lastState.draw(mesh);
            }
        }
        builder = null;
        lastState = null;
    }

    public int getSize() {

        return size;
    }

}
