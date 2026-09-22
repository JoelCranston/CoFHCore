package cofh.core.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.util.TriState;

import java.util.function.Supplier;


public class SoilBlock extends Block {

    protected static final VoxelShape SHAPE_TILLED = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 15.0D, 16.0D);

    protected Supplier<Block> otherBlock = () -> Blocks.DIRT;

    public SoilBlock(Properties properties) {

        super(properties);
    }

    public SoilBlock otherBlock(Supplier<Block> dirt) {

        this.otherBlock = dirt;
        return this;
    }

    // NeoForge 21.0 deleted the PlantType/IPlantable system (it was "buggy and quite confusing"):
    // a soil block is now asked about the plant's own BlockState and answers with a TriState, and
    // the plant categories are expressed by what the plant block itself will accept. The checks
    // below reproduce the old categories from the plant state rather than from a PlantType.
    @Override
    public TriState canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction facing, BlockState plant) {

        return canSustainPlant(state, world, pos, facing, plant, false);
    }

    protected TriState canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction facing, BlockState plant, boolean tilled) {

        Block plantBlock = plant.getBlock();
        if (plantBlock instanceof AttachedStemBlock) {
            return TriState.TRUE;
        }
        // CROP: only on tilled soil.
        if (plantBlock instanceof CropBlock || plantBlock instanceof StemBlock) {
            return tilled ? TriState.TRUE : TriState.FALSE;
        }
        // CAVE / DESERT / PLAINS / FUNGUS: on untilled soil.
        if (plantBlock instanceof BushBlock || plantBlock instanceof MushroomBlock || plantBlock instanceof NetherWartBlock
                || plantBlock instanceof CactusBlock || plantBlock instanceof DeadBushBlock || plantBlock instanceof FungusBlock) {
            return tilled ? TriState.FALSE : TriState.TRUE;
        }
        // BEACH: sugar cane and the like, next to water.
        if (plantBlock instanceof SugarCaneBlock) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos qPos = pos.relative(direction);
                if (world.getFluidState(qPos).is(FluidTags.WATER) || world.getBlockState(qPos).getBlock() == Blocks.FROSTED_ICE) {
                    return TriState.TRUE;
                }
            }
        }
        return TriState.DEFAULT;
    }

    @Override
    public boolean isFertile(BlockState state, BlockGetter world, BlockPos pos) {

        return true;
    }

    @Override
    public BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility itemAbility, boolean simulate) {

        if (ItemAbilities.HOE_TILL == itemAbility && context.getItemInHand().canPerformAction(ItemAbilities.HOE_TILL)) {
            if (context.getLevel().getBlockState(context.getClickedPos().above()).isAir()) {
                return otherBlock.get().defaultBlockState();
            }
        }
        return state;
    }

}
