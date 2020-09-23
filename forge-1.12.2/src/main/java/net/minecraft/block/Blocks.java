package net.minecraft.block;

/**
 * Blocks extends the original 1.12.2 Blocks class to add the CONSTANTS for the
 * blocks from 1.14.4 or newer. This allows the common Water Erosion "business
 * rules" designed for 1.14.4 or newer to work with 1.12.2 without a change.
 */
public class Blocks extends net.minecraft.init.Blocks {
  // Cave air is not a separate Block in 1.12.2, so we'll fake it here.
  public static final net.minecraft.block.Block CAVE_AIR = null;

  // Coarse Dirt can be replaced by regular sand for our needs.
  public static final net.minecraft.block.Block COARSE_DIRT = net.minecraft.init.Blocks.SAND;

  // TODO: The non-default variants need to be handled.

  // TODO: Variant of STONE_SLAB in 1.12.2
  public static final net.minecraft.block.Block COBBLESTONE_SLAB = null;
  // Is STONE_STAIRS in 1.12.2
  public static final net.minecraft.block.Block COBBLESTONE_STAIRS = net.minecraft.init.Blocks.STONE_STAIRS;
  // TODO: Default variant of STONEBRICK in 1.12.2
  public static final net.minecraft.block.Block STONE_BRICKS = null;
  // TODO: Variant of STONE_SLAB in 1.12.2
  public static final net.minecraft.block.Block STONE_BRICK_SLAB = null;
  // No equal in 1.12.2
  public static final net.minecraft.block.Block STONE_BRICK_WALL = null;

  // No equal in 1.12.2
  public static final net.minecraft.block.Block PODZOL = null;
  // TODO: Variant of SAND in 1.12.2
  public static final net.minecraft.block.Block RED_SAND = null;
  // No equal in 1.12.2
  public static final net.minecraft.block.Block GRASS_BLOCK = null;
  // No equal in 1.12.2
  public static final net.minecraft.block.Block MOSSY_COBBLESTONE_SLAB = null;
  // No equal in 1.12.2
  public static final net.minecraft.block.Block MOSSY_COBBLESTONE_STAIRS = null;
  // TODO: Variant of COBBLESTONE_WALL in 1.12.2
  public static final net.minecraft.block.Block MOSSY_COBBLESTONE_WALL = null;
  // TODO: Variant of STONEBRICK in 1.12.2
  public static final net.minecraft.block.Block MOSSY_STONE_BRICKS = null;
  // No equal in 1.12.2
  public static final net.minecraft.block.Block MOSSY_STONE_BRICK_SLAB = null;
  // No equal in 1.12.2
  public static final net.minecraft.block.Block MOSSY_STONE_BRICK_STAIRS = null;
  // No equal in 1.12.2
  public static final net.minecraft.block.Block MOSSY_STONE_BRICK_WALL = null;

}
