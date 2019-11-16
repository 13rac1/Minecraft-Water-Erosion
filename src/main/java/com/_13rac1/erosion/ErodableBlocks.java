package com._13rac1.erosion;

import java.util.HashMap;

import net.minecraft.block.Blocks;
import net.minecraft.tag.BlockTags;
import net.minecraft.block.Block;

// Differing erosion resistance depending on the type of block.
// leaves < sand < gravel < dirt < grass
public class ErodableBlocks {
  public static final Integer MAX_RESISTANCE = 100;
  private static final Integer LEAF_RESISTANCE = 5;
  private static final Integer SAND_RESISTANCE = 5;
  private static final Integer WOOL_RESISTANCE = 10;
  private static final Integer GRAVEL_RESISTANCE = 20;
  private static final Integer DIRT_RESISTANCE = 30;
  private static final Integer GRASS_RESISTANCE = 40;

  // Warning: Blocks.SAND is null during startup, only run after start.
  private static HashMap<Block, Integer> blocks;

  private static HashMap<Block, Integer> getList() {
    if (blocks == null) {
      blocks = new HashMap<Block, Integer>();
      blocks.put(Blocks.SAND, SAND_RESISTANCE);
      blocks.put(Blocks.GRAVEL, GRAVEL_RESISTANCE);
      blocks.put(Blocks.FARMLAND, DIRT_RESISTANCE);
      blocks.put(Blocks.DIRT, DIRT_RESISTANCE);
      blocks.put(Blocks.GRASS_PATH, GRASS_RESISTANCE);
      blocks.put(Blocks.GRASS_BLOCK, GRASS_RESISTANCE);
      blocks.put(Blocks.GRASS, GRASS_RESISTANCE);
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
      return LEAF_RESISTANCE;
    } else if (BlockTags.SAND.contains(block)) {
      return SAND_RESISTANCE;
    } else if (BlockTags.DIRT_LIKE.contains(block)) {
      return DIRT_RESISTANCE;
    } else if (BlockTags.WOOL.contains(block)) {
      return WOOL_RESISTANCE;
    }

    return MAX_RESISTANCE;
  }
}
