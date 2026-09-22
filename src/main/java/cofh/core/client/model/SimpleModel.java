package cofh.core.client.model;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

public class SimpleModel implements CustomUnbakedBlockStateModel {

    private final SingleVariant.Unbaked model;
    private final SimpleModel.Loader loader;

    public SimpleModel(SingleVariant.Unbaked model, SimpleModel.Loader loader) {

        this.model = model;
        this.loader = loader;
    }

    @Override
    public BlockStateModel bake(ModelBaker baker) {

        return loader.factory.create(model.bake(baker));
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {

        model.resolveDependencies(resolver);
    }

    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {

        return loader.codec;
    }

    public interface IFactory<T extends BlockStateModel> {

        T create(BlockStateModel originalModel);

    }

    // region LOADER
    public static class Loader {

        private final SimpleModel.IFactory<? extends BlockStateModel> factory;
        private final MapCodec<SimpleModel> codec;

        public Loader(SimpleModel.IFactory<? extends BlockStateModel> factory) {

            this.factory = factory;
            this.codec = SingleVariant.Unbaked.MAP_CODEC.xmap(model -> new SimpleModel(model, this), model -> model.model);
        }

        public MapCodec<SimpleModel> codec() {

            return codec;
        }

    }
    // endregion
}
