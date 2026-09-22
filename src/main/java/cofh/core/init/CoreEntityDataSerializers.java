package cofh.core.init;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;

import static cofh.core.CoFHCore.ENTITY_DATA_SERIALIZERS;

public class CoreEntityDataSerializers {

    private CoreEntityDataSerializers() {

    }

    public static void register() {

    }

    // 1.20.5: a serializer is defined by a StreamCodec rather than write/read methods, and
    // FriendlyByteBuf's writeFluidStack/readFluidStack extensions are gone - FluidStack's own
    // OPTIONAL_STREAM_CODEC is the wire format (it needs a RegistryFriendlyByteBuf).
    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<FluidStack>> FLUID_STACK_DATA_SERIALIZER = ENTITY_DATA_SERIALIZERS.register("fluid_stack_eds",
            () -> new EntityDataSerializer<FluidStack>() {

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, FluidStack> codec() {

                    return FluidStack.OPTIONAL_STREAM_CODEC;
                }

                @Override
                public FluidStack copy(FluidStack stack) {

                    return stack.copy();
                }
            }
    );

}
