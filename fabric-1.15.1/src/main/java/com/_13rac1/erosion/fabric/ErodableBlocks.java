package com._13rac1.erosion.fabric;

import java.util.HashMap;

import net.minecraft.block.Blocks;
import net.minecraft.tag.BlockTags;
import net.minecraft.block.Block;

import java.util.Random;

// Differing erosion resistance depending on the type of block.
// leaves < sand < gravel < dirt < grass
public class ErodableBlocks {
  private static final Integer MAX_RESISTANCE_ODDS = 0; // 1/0 = never
  // Odds over the source resistance will never be broken by a source block.
  private static final Integer SOURCE_BREAK_RESISTANCE_ODDS = 15;
  private static final Integer LEAF_RESISTANCE_ODDS = 2;
  private static final Integer SAND_RESISTANCE_ODDS = 2;
  private static final Integer WOOL_RESISTANCE_ODDS = 3;
  private static final Integer GRAVEL_RESISTANCE_ODDS = 3;
  private static final Integer CLAY_RESISTANCE_ODDS = 4;
  private static final Integer DIRT_RESISTANCE_ODDS = 4;
  private static final Integer GRASS_RESISTANCE_ODDS = 5;
  private static final Integer COBBLESTONE_RESISTANCE_ODDS = 20;

  // Warning: Blocks.SAND is null during startup, only run after start.
  private static HashMap<Block, Integer> blocks;

  private static HashMap<Block, Integer> getList() {
    if (blocks == null) {
      blocks = new HashMap<Block, Integer>();
      blocks.put(Blocks.RED_SAND, SAND_RESISTANCE_ODDS);
      blocks.put(Blocks.SAND, SAND_RESISTANCE_ODDS);
      blocks.put(Blocks.GRAVEL, GRAVEL_RESISTANCE_ODDS);
      blocks.put(Blocks.CLAY, CLAY_RESISTANCE_ODDS);
      blocks.put(Blocks.FARMLAND, DIRT_RESISTANCE_ODDS);
      blocks.put(Blocks.DIRT, DIRT_RESISTANCE_ODDS);
      blocks.put(Blocks.COARSE_DIRT, DIRT_RESISTANCE_ODDS);
      blocks.put(Blocks.PODZOL, DIRT_RESISTANCE_ODDS);
      blocks.put(Blocks.GRASS_PATH, GRASS_RESISTANCE_ODDS);
      blocks.put(Blocks.GRASS_BLOCK, GRASS_RESISTANCE_ODDS);
      blocks.put(Blocks.GRASS, GRASS_RESISTANCE_ODDS);
      blocks.put(Blocks.COBBLESTONE, COBBLESTONE_RESISTANCE_ODDS);
      blocks.put(Blocks.COBBLESTONE_SLAB, COBBLESTONE_RESISTANCE_ODDS);
      blocks.put(Blocks.COBBLESTONE_STAIRS, COBBLESTONE_RESISTANCE_ODDS);
      blocks.put(Blocks.COBBLESTONE_WALL, COBBLESTONE_RESISTANCE_ODDS);
      blocks.put(Blocks.MOSSY_COBBLESTONE, COBBLESTONE_RESISTANCE_ODDS);
      blocks.put(Blocks.MOSSY_COBBLESTONE_SLAB, COBBLESTONE_RESISTANCE_ODDS);
      blocks.put(Blocks.MOSSY_COBBLESTONE_STAIRS, COBBLESTONE_RESISTANCE_ODDS);
      blocks.put(Blocks.MOSSY_COBBLESTONE_WALL, COBBLESTONE_RESISTANCE_ODDS);
    }
    return blocks;
  }

  // Given a block, returns an Integer from 0->100 representing the erosion
  // resistance of the block.
  public static Integer getErosionResistance(Block block) {
    HashMap<Block, Integer> erodableBlocks = getList();

    if (erodableBlocks.containsKey(block)) {
      return erodableBlocks.get(block);
    }

    // Check block tags to erode leaves, wool, and mod-provided blocks.
    // TODO: Less CPU to fill the hashmap with all values once.
    if (BlockTags.LEAVES.contains(block)) {
      return LEAF_RESISTANCE_ODDS;
    } else if (BlockTags.SAND.contains(block)) {
      return SAND_RESISTANCE_ODDS;
      // FIXME: DIRT_LIKE is missing?
      // error: cannot find symbol
      // } else if (BlockTags.DIRT_LIKE.contains(block)) {
      // symbol: variable DIRT_LIKE
      // location: class BlockTags
      // 1 error
      // } else if (BlockTags.DIRT_LIKE.contains(block)) {
      // return DIRT_RESISTANCE;
    } else if (BlockTags.WOOL.contains(block)) {
      return WOOL_RESISTANCE_ODDS;
    }

    return MAX_RESISTANCE_ODDS;
  }

  public static boolean canErode(Block block) {
    return getErosionResistance(block) != MAX_RESISTANCE_ODDS;
  }

  public static boolean maybeErode(Random rand, Block block) {
    return rand.nextInt(getErosionResistance(block)) == 1;
  }

  // TODO: This is simple method to block the breakage of cobblestone by source
  // blocks, but ideally it would still be allowed if there is enough "pressure"
  // behind the current source block.
  public static boolean canSourceBreak(Block block) {
    return getErosionResistance(block) < SOURCE_BREAK_RESISTANCE_ODDS;
  }
}
