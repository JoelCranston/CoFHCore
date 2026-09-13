package cofh.core.common.network.data;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Shared {@link StreamCodec}s for payload patterns that recur across several
 * {@link net.minecraft.network.protocol.common.custom.CustomPacketPayload} records.
 */
public class PayloadCodecs {

    private PayloadCodecs() {

    }

    /**
     * Passes through the remainder of the buffer verbatim. Used by payloads that carry
     * an opaque, tile/container-specific blob written elsewhere (e.g., a synced tag or
     * a menu's custom data) rather than a fixed schema of fields.
     */
    public static final StreamCodec<FriendlyByteBuf, FriendlyByteBuf> REMAINING_BYTES = StreamCodec.of(
            (buf, value) -> buf.writeBytes(value.copy()),
            buf -> new FriendlyByteBuf(Unpooled.buffer().writeBytes(buf.readBytes(buf.readableBytes())))
    );

}
