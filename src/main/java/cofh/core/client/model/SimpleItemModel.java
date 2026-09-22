package cofh.core.client.model;

import cofh.core.client.renderer.model.ModelUtils;
import com.google.common.base.Suppliers;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

import static cofh.lib.util.Constants.DIRECTIONS;

public class SimpleItemModel implements ItemModel {

    private final SimpleItemModel.IFactory factory;
    private final BlockStateModelPart model;
    private final List<ItemTintSource> tints;
    private final ModelRenderProperties properties;
    private final Matrix4fc transformation;
    private final Supplier<Vector3fc[]> extents;

    public SimpleItemModel(SimpleItemModel.IFactory factory, BlockStateModelPart model, List<ItemTintSource> tints, ModelRenderProperties properties, Matrix4fc transformation) {

        this.factory = factory;
        this.model = model;
        this.tints = tints;
        this.properties = properties;
        this.transformation = transformation;
        this.extents = Suppliers.memoize(() -> CuboidItemModelWrapper.computeExtents(ModelUtils.getAllQuads(model)));
    }

    @Override
    public void update(ItemStackRenderState output, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {

        BlockStateModelPart part = factory.create(stack, model);
        output.appendModelIdentityElement(part);
        ItemStackRenderState.LayerRenderState layer = output.newLayer();
        if (stack.hasFoil()) {
            layer.setFoilType(ItemStackRenderState.FoilType.STANDARD);
            output.setAnimated();
            output.appendModelIdentityElement(ItemStackRenderState.FoilType.STANDARD);
        }
        if (!tints.isEmpty()) {
            IntList tintLayers = layer.tintLayers();
            for (ItemTintSource tintSource : tints) {
                int tint = tintSource.calculate(stack, level, owner == null ? null : owner.asLivingEntity());
                tintLayers.add(tint);
                output.appendModelIdentityElement(tint);
            }
        }
        layer.setExtents(extents);
        layer.setLocalTransform(transformation);
        properties.applyToLayer(layer, displayContext);
        List<BakedQuad> quads = layer.prepareQuadList();
        quads.addAll(part.getQuads(null));
        for (Direction dir : DIRECTIONS) {
            quads.addAll(part.getQuads(dir));
        }
        if ((part.materialFlags() & BakedQuad.FLAG_ANIMATED) != 0) {
            output.setAnimated();
        }
    }

    public interface IFactory {

        BlockStateModelPart create(ItemStack stack, BlockStateModelPart originalModel);

    }

    // region LOADER
    public static class Loader {

        private final SimpleItemModel.IFactory factory;
        private final MapCodec<SimpleItemModel.Unbaked> codec;

        public Loader(SimpleItemModel.IFactory factory) {

            this.factory = factory;
            this.codec = CuboidItemModelWrapper.Unbaked.MAP_CODEC.xmap(model -> new SimpleItemModel.Unbaked(model, this), model -> model.model);
        }

        public MapCodec<SimpleItemModel.Unbaked> codec() {

            return codec;
        }

    }

    public static class Unbaked implements ItemModel.Unbaked {

        private final CuboidItemModelWrapper.Unbaked model;
        private final SimpleItemModel.Loader loader;

        public Unbaked(CuboidItemModelWrapper.Unbaked model, SimpleItemModel.Loader loader) {

            this.model = model;
            this.loader = loader;
        }

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {

            return loader.codec;
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {

            ModelBaker baker = context.blockModelBaker();
            ResolvedModel resolvedModel = baker.getModel(model.model());
            TextureSlots textureSlots = resolvedModel.getTopTextureSlots();
            QuadCollection quads = resolvedModel.bakeTopGeometry(textureSlots, baker, BlockModelRotation.IDENTITY);
            ModelRenderProperties properties = ModelRenderProperties.fromResolvedModel(baker, resolvedModel, textureSlots);
            BlockStateModelPart part = new SimpleModelWrapper(quads, resolvedModel.getTopAmbientOcclusion(), properties.particleMaterial());
            return new SimpleItemModel(loader.factory, part, model.tints(), properties, Transformation.compose(transformation, model.transformation()));
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {

            model.resolveDependencies(resolver);
        }

    }
    // endregion
}
