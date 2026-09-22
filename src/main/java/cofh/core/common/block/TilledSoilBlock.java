package cofh.core.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.TriState;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class TilledSoilBlock extends SoilBlock {

    public TilledSoilBlock(Properties properties) {

        super(properties);
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, @Nullable Orientation orientation, boolean isMoving) {

        if (!state.canSurvive(worldIn, pos)) {
            turnToDirt(state, worldIn, pos);
        }
        super.neighborChanged(state, worldIn, pos, blockIn, orientation, isMoving);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {

        return SHAPE_TILLED;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {

        return false;
    }

    @Override
    public TriState canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction facing, BlockState plant) {

        return canSustainPlant(state, world, pos, facing, plant, true);
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {

        return true;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {

        BlockState blockstate = worldIn.getBlockState(pos.above());
        return !blockstate.isSolid() || blockstate.getBlock() instanceof FenceGateBlock || blockstate.getBlock() instanceof MovingPistonBlock;
    }

    public void turnToDirt(BlockState state, Level worldIn, BlockPos pos) {

        worldIn.setBlockAndUpdate(pos, pushEntitiesUp(state, otherBlock.get().defaultBlockState(), worldIn, pos));
    }

}
