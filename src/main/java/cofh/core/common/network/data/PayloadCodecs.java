package cofh.core.common.network.data;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class PayloadCodecs {

    private PayloadCodecs() {

    }

    /**
     * Passes the rest of the buffer through verbatim, for payloads carrying a pre-written blob.
     */
    public static final StreamCodec<FriendlyByteBuf, FriendlyByteBuf> REMAINING_BYTES = StreamCodec.of(
            (buf, value) -> buf.writeBytes(value.copy()),
            buf -> new FriendlyByteBuf(Unpooled.buffer().writeBytes(buf.readBytes(buf.readableBytes())))
    );

}
