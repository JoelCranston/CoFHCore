package cofh.lib.common.block;

/**
 * What kind of ground a CoFH crop wants, which used to be NeoForge's {@code PlantType}. That
 * system was deleted in NeoForge 21.0 (it was "buggy and quite confusing"); soil blocks now answer
 * {@code IBlockExtension#canSustainPlant} from the plant's own {@link
 * net.minecraft.world.level.block.state.BlockState}, and there is no shared vocabulary of plant
 * categories left. CoFH's crops still have one internally, so it lives here - {@code SoilBlock}
 * recognises these blocks by type the way it used to recognise a PlantType.
 */
public enum CropType {

    CROP,
    CAVE,
    DESERT,
    PLAINS,
    BEACH,
    WATER,
    NETHER

}
