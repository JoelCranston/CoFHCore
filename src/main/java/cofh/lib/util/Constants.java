package cofh.lib.util;

import cofh.lib.common.block.CropType;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.Arrays;
import java.util.UUID;
import java.util.function.Supplier;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;
import static net.minecraft.world.entity.EquipmentSlot.*;

public class Constants {

    private Constants() {

    }

    public static final CropType FUNGUS = CropType.NETHER;

    public static final VoxelShape FULL_CUBE_COLLISION = Block.box(1.0D, 0.0D, 1.0D, 15.9375D, 15.9375D, 15.9375D);

    // region GLOBALS
    public static final int AOE_BREAK_FACTOR = 8;
    public static final int BOTTLE_VOLUME = FluidType.BUCKET_VOLUME / 4;
    public static final int BUCKET_VOLUME = FluidType.BUCKET_VOLUME;
    public static final int ENTITY_TRACKING_DISTANCE = 64;
    public static final int ITEM_TIMER_DURATION = 40;
    public static final int MAGMATIC_TEMPERATURE = 1000;
    public static final int MAX_AUGMENTS = 9;
    public static final int MAX_CAPACITY = Integer.MAX_VALUE;
    public static final int MAX_ENCHANT_LEVEL = 10;
    public static final int MAX_FOOD_LEVEL = 20;
    public static final int MAX_POTION_AMPLIFIER = 3;
    public static final int MAX_POTION_DURATION = 72000;
    public static final int MB_PER_XP = 20;
    public static final int NETWORK_UPDATE_DISTANCE = 192;
    public static final int RF_PER_FURNACE_UNIT = 10;

    public static final float BASE_CHANCE = 1.0F;
    public static final float BASE_CHANCE_LOCKED = -1.0F;

    public static final int TANK_SMALL = BUCKET_VOLUME * 4;
    public static final int TANK_MEDIUM = BUCKET_VOLUME * 8;
    public static final int TANK_LARGE = BUCKET_VOLUME * 16;

    public static final EquipmentSlot[] ARMOR_SLOTS = new EquipmentSlot[]{HEAD, CHEST, LEGS, FEET};
    public static final Direction[] DIRECTIONS = Direction.values();
    public static final Direction[] POSITIVE_DIRECTIONS = Arrays.stream(Direction.Axis.values()).map(axis -> Direction.get(Direction.AxisDirection.POSITIVE, axis)).toArray(Direction[]::new);
    public static final Direction[] NEGATIVE_DIRECTIONS = Arrays.stream(Direction.Axis.values()).map(axis -> Direction.get(Direction.AxisDirection.NEGATIVE, axis)).toArray(Direction[]::new);
    // endregion

    // region CONSTANTS
    public static final Supplier<Boolean> TRUE = () -> true;
    public static final Supplier<Boolean> FALSE = () -> false;

    public static final Supplier<Block> EMPTY_BLOCK = () -> Blocks.AIR;
    public static final Supplier<ItemStack> EMPTY_ITEM = () -> ItemStack.EMPTY;
    public static final Supplier<FluidStack> EMPTY_FLUID = () -> FluidStack.EMPTY;

    public static final String DAMAGE_ARROW = "arrow";
    public static final String DAMAGE_PLAYER = "player";

    public static final UUID EMPTY_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    // Attribute modifier ids; the UUID_ names are kept so dependent mods need no rename.
    public static final Identifier UUID_ARMOR_TOUGHNESS = Identifier.fromNamespaceAndPath(ID_COFH_CORE, "armor_toughness");
    public static final Identifier UUID_WEAPON_KNOCKBACK = Identifier.fromNamespaceAndPath(ID_COFH_CORE, "weapon_knockback");
    public static final Identifier UUID_WEAPON_RANGE = Identifier.fromNamespaceAndPath(ID_COFH_CORE, "weapon_range");
    public static final Identifier UUID_TOOL_REACH = Identifier.fromNamespaceAndPath(ID_COFH_CORE, "tool_reach");
    public static final Identifier UUID_DUAL_WIELD_ATTACK_SPEED = Identifier.fromNamespaceAndPath(ID_COFH_CORE, "dual_wield_attack_speed");

    public static final Identifier UUID_EFFECT_CHILLED_MOVEMENT_SPEED = Identifier.fromNamespaceAndPath(ID_COFH_CORE, "effect_chilled_movement_speed");
    public static final Identifier UUID_EFFECT_CHILLED_ATTACK_SPEED = Identifier.fromNamespaceAndPath(ID_COFH_CORE, "effect_chilled_attack_speed");

    public static final Identifier UUID_EFFECT_SHOCKED_ATTACK_DAMAGE = Identifier.fromNamespaceAndPath(ID_COFH_CORE, "effect_shocked_attack_damage");
    public static final Identifier UUID_EFFECT_SUNDERED_ARMOR = Identifier.fromNamespaceAndPath(ID_COFH_CORE, "effect_sundered_armor");
    public static final Identifier UUID_EFFECT_SUNDERED_ARMOR_TOUGHNESS = Identifier.fromNamespaceAndPath(ID_COFH_CORE, "effect_sundered_armor_toughness");

    public static final Identifier UUID_ENCH_BULWARK_KNOCKBACK_RESISTANCE = Identifier.fromNamespaceAndPath(ID_COFH_CORE, "ench_bulwark_knockback_resistance");
    public static final Identifier UUID_ENCH_PHALANX_MOVEMENT_SPEED = Identifier.fromNamespaceAndPath(ID_COFH_CORE, "ench_phalanx_movement_speed");
    public static final Identifier UUID_ENCH_REACH_DISTANCE = Identifier.fromNamespaceAndPath(ID_COFH_CORE, "ench_reach_distance");
    public static final Identifier UUID_ENCH_VITALITY_HEALTH = Identifier.fromNamespaceAndPath(ID_COFH_CORE, "ench_vitality_health");

    public static final int RGB_DURABILITY_FLUX = 0xD01010;
    public static final int RGB_DURABILITY_WATER = 0x4060FF;
    public static final int RGB_DURABILITY_ENDER = 0x14594D;
    public static final int RGB_DURABILITY_XP = 0x7AAC52;

    public static float AUG_SCALE_MIN = 0.0F;
    public static float AUG_SCALE_MAX = 100.0F;
    // endregion

    // region FONTS
    public static final Identifier ENDER_FONT = Identifier.fromNamespaceAndPath(ID_COFH_CORE, "ender");
    public static final Identifier INVIS_FONT = Identifier.fromNamespaceAndPath(ID_COFH_CORE, "invis");

    public static final Style ENDER_STYLE = Style.EMPTY.withFont(ENDER_FONT);
    public static final Style INVIS_STYLE = Style.EMPTY.withFont(INVIS_FONT);
    // endregion

    // region TEXTURES
    public static final String PATH_GUI = ID_COFH_CORE + ":textures/gui/";
    public static final String PATH_ELEMENTS = PATH_GUI + "elements/";
    public static final String PATH_ICONS = PATH_GUI + "icons/";
    // endregion
}
