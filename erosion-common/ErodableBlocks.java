package com._13rac1.erosion.common;

import java.util.HashMap;

import net.minecraft.block.Blocks;
import net.minecraft.tag.BlockTags;
import net.minecraft.block.Block;

import java.util.Random;

// Differing erosion resistance depending on the type of block.
// leaves < sand < gravel < dirt < grass
public class ErodableBlocks {
  private static final Integer MAX_RESIST_ODDS = 0; // 1/0 = never
  // Odds less (a larger number) than source resistance will never be broken by
  // a source block. Primarily to block cobblestone from ever being broken.
  private static final Integer SOURCE_BREAK_RESIST_ODDS = 8;

  // Odds the block type will erode into air or decay into another block
  private static final Integer LEAF_RESIST_ODDS = 1;
  private static final Integer SAND_RESIST_ODDS = 1;
  private static final Integer WOOL_RESIST_ODDS = 1;
  private static final Integer GRAVEL_RESIST_ODDS = 2;
  private static final Integer CLAY_RESIST_ODDS = 5; // Clay sticks around
  private static final Integer DIRT_RESIST_ODDS = 4;
  private static final Integer GRASS_RESIST_ODDS = 6;
  private static final Integer COBBLE_RESIST_ODDS = 20;

  // Odds the block type will decay instead of turning into air. Note: Water is
  // not listed on purpose. If needing to change a block to Water check for Air.
  private static final Integer DECAY_ALWAYS_ODDS = 0;
  private static final Integer DECAY_NEVER_ODDS = 100;
  // Always become air, obviously.
  private static final Integer DECAY_TO_AIR_ODDS = 0;
  // Sometimes the dirt or gravel washes away before becoming Sand.
  private static final Integer DECAY_TO_SAND_ODDS = 1;
  // Always become gravel.
  private static final Integer DECAY_TO_GRAVEL_ODDS = 0;
  // Clay is created but still rare.
  private static final Integer DECAY_TO_CLAY_ODDS = 20;
  // If Dirt is set to 0 there may be blocks switching between grass and dirt
  // forever. A 50/50 chance is better.
  private static final Integer DECAY_TO_DIRT_ODDS = 1;
  private static final Integer DECAY_TO_COARSE_DIRT_ODDS = 1;
  // Cobblestone gets mossy first.
  // TODO: Convert cobblestone around the flow to mossy, not just the edge.
  private static final Integer DECAY_TO_MOSSY_COBBLE_ODDS = 0;

  // Erodable describes block metadata which controls the odds of erosion and
  // what block it may decay to.
  static class Erodable {
    Integer resistanceOdds;
    Block decayBlock;

    Erodable(Integer resistanceOdds, Block decayBlock) {
      this.resistanceOdds = resistanceOdds;
      this.decayBlock = decayBlock;
    }
  }

  // decayables lists the Blocks other blocks can decay to. If a "decay to" block
  // is not listed in the hashmap the odds are assumed to be ALWAYS.
  private static HashMap<Block, Integer> decayables;

  private static HashMap<Block, Integer> getDecayables() {
    // Note: Cannot be initialized during startup because the code runs before
    // Blocks is initialized.
    if (decayables == null) {
      decayables = new HashMap<Block, Integer>();
      decayables.put(Blocks.AIR, DECAY_TO_AIR_ODDS);
      decayables.put(Blocks.CLAY, DECAY_TO_CLAY_ODDS);
      decayables.put(Blocks.SAND, DECAY_TO_SAND_ODDS);
      decayables.put(Blocks.GRAVEL, DECAY_TO_GRAVEL_ODDS);
      decayables.put(Blocks.CLAY, DECAY_TO_CLAY_ODDS);
      decayables.put(Blocks.DIRT, DECAY_TO_DIRT_ODDS);
      decayables.put(Blocks.COARSE_DIRT, DECAY_TO_COARSE_DIRT_ODDS);
      decayables.put(Blocks.MOSSY_COBBLESTONE, DECAY_TO_MOSSY_COBBLE_ODDS);
      decayables.put(Blocks.MOSSY_COBBLESTONE_SLAB, DECAY_TO_MOSSY_COBBLE_ODDS);
      decayables.put(Blocks.MOSSY_COBBLESTONE_STAIRS, DECAY_TO_MOSSY_COBBLE_ODDS);
      decayables.put(Blocks.MOSSY_COBBLESTONE_WALL, DECAY_TO_MOSSY_COBBLE_ODDS);
    }
    return decayables;
  }

  // erodables lists the erodable blocks, their odds of erosion, and what they
  // decay to.
  private static HashMap<Block, Erodable> erodables;

  private static HashMap<Block, Erodable> getErodables() {
    // Note: Cannot be initialized during startup because the code runs before
    // Blocks is initialized.
    if (erodables == null) {
      erodables = new HashMap<Block, Erodable>();
      erodables.put(Blocks.RED_SAND, new Erodable(SAND_RESIST_ODDS, Blocks.CLAY));
      erodables.put(Blocks.SAND, new Erodable(SAND_RESIST_ODDS, Blocks.CLAY));
      erodables.put(Blocks.GRAVEL, new Erodable(GRAVEL_RESIST_ODDS, Blocks.SAND));
      erodables.put(Blocks.CLAY, new Erodable(CLAY_RESIST_ODDS, Blocks.AIR));
      erodables.put(Blocks.FARMLAND, new Erodable(DIRT_RESIST_ODDS, Blocks.COARSE_DIRT));
      erodables.put(Blocks.DIRT, new Erodable(DIRT_RESIST_ODDS, Blocks.COARSE_DIRT));
      erodables.put(Blocks.COARSE_DIRT, new Erodable(DIRT_RESIST_ODDS, Blocks.SAND));
      erodables.put(Blocks.PODZOL, new Erodable(DIRT_RESIST_ODDS, Blocks.COARSE_DIRT));
      erodables.put(Blocks.GRASS_PATH, new Erodable(GRASS_RESIST_ODDS, Blocks.DIRT));
      erodables.put(Blocks.GRASS_BLOCK, new Erodable(GRASS_RESIST_ODDS, Blocks.DIRT));
      erodables.put(Blocks.GRASS, new Erodable(GRASS_RESIST_ODDS, Blocks.DIRT));
      erodables.put(Blocks.COBBLESTONE, new Erodable(COBBLE_RESIST_ODDS, Blocks.MOSSY_COBBLESTONE));
      erodables.put(Blocks.COBBLESTONE_SLAB, new Erodable(COBBLE_RESIST_ODDS, Blocks.MOSSY_COBBLESTONE_SLAB));
      erodables.put(Blocks.COBBLESTONE_STAIRS, new Erodable(COBBLE_RESIST_ODDS, Blocks.MOSSY_COBBLESTONE_STAIRS));
      erodables.put(Blocks.COBBLESTONE_WALL, new Erodable(COBBLE_RESIST_ODDS, Blocks.MOSSY_COBBLESTONE_WALL));
      erodables.put(Blocks.MOSSY_COBBLESTONE, new Erodable(COBBLE_RESIST_ODDS, Blocks.GRAVEL));
      // Directly to air because the gravel block is a larger volume.
      erodables.put(Blocks.MOSSY_COBBLESTONE_SLAB, new Erodable(COBBLE_RESIST_ODDS, Blocks.AIR));
      erodables.put(Blocks.MOSSY_COBBLESTONE_STAIRS, new Erodable(COBBLE_RESIST_ODDS, Blocks.AIR));
      erodables.put(Blocks.MOSSY_COBBLESTONE_WALL, new Erodable(COBBLE_RESIST_ODDS, Blocks.AIR));
    }
    return erodables;
  }

  // Given a block, returns an Integer from 0->100 representing the erosion
  // resistance of the block.
  public static Integer getResistance(Block block) {

    if (getErodables().containsKey(block)) {
      return getErodables().get(block).resistanceOdds;
    }

    // Check block tags to erode leaves, wool, and mod-provided blocks.
    // TODO: Less CPU to fill the hashmap with all values once.
    if (BlockTags.LEAVES.contains(block)) {
      return LEAF_RESIST_ODDS;
    } else if (BlockTags.SAND.contains(block)) {
      return SAND_RESIST_ODDS;
      // FIXME: DIRT_LIKE is missing?
      // error: cannot find symbol
      // } else if (BlockTags.DIRT_LIKE.contains(block)) {
      // symbol: variable DIRT_LIKE
      // location: class BlockTags
      // 1 error
      // } else if (BlockTags.DIRT_LIKE.contains(block)) {
      // return DIRT_RESIST;
    } else if (BlockTags.WOOL.contains(block)) {
      return WOOL_RESIST_ODDS;
    }

    return MAX_RESIST_ODDS;
  }

  public static boolean canErode(Block block) {
    return getResistance(block) != MAX_RESIST_ODDS;
  }

  public static boolean maybeErode(Random rand, Block block) {
    return rand.nextInt(getResistance(block)) == 0;
  }

  // maybeDecay decides which block the passed block should decay to. It returns
  // Blocks.AIR by default, but some callers may place water instead.
  public static Block maybeDecay(Random rand, Block block) {
    Integer odds = DECAY_NEVER_ODDS;

    Block decayToBlock = Blocks.AIR;

    if (getErodables().containsKey(block)) {
      decayToBlock = getErodables().get(block).decayBlock;
    }

    if (getDecayables().containsKey(decayToBlock)) {
      odds = getDecayables().get(decayToBlock);
    } else {
      return Blocks.AIR;
    }

    if (odds == DECAY_NEVER_ODDS) {
      return Blocks.AIR;
    }

    if (odds == DECAY_ALWAYS_ODDS) {
      return decayToBlock;
    }

    if (rand.nextInt(odds) == 0) {
      return decayToBlock;
    }
    return Blocks.AIR;
  }

  public static Block decayTo(Block block) {

    HashMap<Block, Erodable> erodables = getErodables();

    if (erodables.containsKey(block)) {
      return erodables.get(block).decayBlock;
    }
    return Blocks.AIR;
  }

  // TODO: This is simple method to block the breakage of cobblestone by source
  // blocks, but ideally it would still be allowed if there is enough "pressure"
  // behind the current source block.
  public static boolean canSourceBreak(Block block) {
    return getResistance(block) < SOURCE_BREAK_RESIST_ODDS;
  }
}
