package com._13rac1.erosion.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

import com._13rac1.erosion.minecraft.EBlockTags;

// Differing erosion resistance depending on the type of block.
// leaves < sand < gravel < dirt < grass
public class ErodableBlocks {
  // May be changed during system startup.
  public static IErosionConfig Config = new DefaultErosionConfig();

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
  private static final Integer BRICK_RESIST_ODDS = 25;

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
  private static final Integer DECAY_TO_MOSSY_COBBLE_ODDS = 0;
  private static final Integer DECAY_TO_MOSSY_BRICKS_ODDS = 1;

  /**
   * Exception thrown when the decay list for an erodable block is not finite or
   * does not end at Blocks.AIR.
   */
  public static class IncompleteDecayListException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public IncompleteDecayListException(Block block, ErodableOptions options) {
      super("DecayList for " + block + " is not finite " + options.decayList);
    }
  }

  /**
   * Exception thrown when the decayToBlock is null or not found.
   */
  public static class NullDecayToBlockException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public NullDecayToBlockException(Block block, ErodableOptions options) {
      super("decayToBlock for " + block + " is null " + options.decayList);
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
      decayables.put(Blocks.DIRT, DECAY_TO_DIRT_ODDS);
      decayables.put(Blocks.COARSE_DIRT, DECAY_TO_COARSE_DIRT_ODDS);
      decayables.put(Blocks.MOSSY_COBBLESTONE, DECAY_TO_MOSSY_COBBLE_ODDS);
      decayables.put(Blocks.MOSSY_COBBLESTONE_SLAB, DECAY_TO_MOSSY_COBBLE_ODDS);
      decayables.put(Blocks.MOSSY_COBBLESTONE_STAIRS, DECAY_TO_MOSSY_COBBLE_ODDS);
      decayables.put(Blocks.MOSSY_COBBLESTONE_WALL, DECAY_TO_MOSSY_COBBLE_ODDS);
      decayables.put(Blocks.MOSSY_STONE_BRICKS, DECAY_TO_MOSSY_BRICKS_ODDS);
      decayables.put(Blocks.MOSSY_STONE_BRICK_SLAB, DECAY_TO_MOSSY_BRICKS_ODDS);
      decayables.put(Blocks.MOSSY_STONE_BRICK_STAIRS, DECAY_TO_MOSSY_BRICKS_ODDS);
      decayables.put(Blocks.MOSSY_STONE_BRICK_WALL, DECAY_TO_MOSSY_BRICKS_ODDS);
    }
    return decayables;
  }

  // erodables lists the erodable blocks, their odds of erosion, and what they
  // decay to.
  private static HashMap<Block, ErodableOptions> erodables;

  // tiny helper func to clean up getErodables
  private static void addOption(Block erodableBlock, Integer resistanceOdds, Block decayToBlock) {
    if (erodableBlock == null) {
      // skip null blocks. null is used in 1.12.2 for blocks missing vs 1.14.x+.
      // TODO: Log warn on skip
      return;
    }
    if (decayToBlock == null) {
      // Any unset decayToBlock is Air not null.
      // TODO: Log warn on Air
      decayToBlock = Blocks.AIR;
    }
    erodables.put(erodableBlock, new ErodableOptions(resistanceOdds, decayToBlock));
  }

  private static HashMap<Block, ErodableOptions> getErodables() {
    // Note: Cannot be initialized during startup because the code runs before
    // Blocks is initialized.
    if (erodables == null) {
      erodables = new HashMap<Block, ErodableOptions>();
      // Ordered weakest to strongest
      addOption(Blocks.CLAY, CLAY_RESIST_ODDS, Blocks.AIR);
      addOption(Blocks.CLAY, CLAY_RESIST_ODDS, Blocks.AIR);
      addOption(Blocks.RED_SAND, SAND_RESIST_ODDS, Blocks.CLAY);
      addOption(Blocks.SAND, SAND_RESIST_ODDS, Blocks.CLAY);
      addOption(Blocks.GRAVEL, GRAVEL_RESIST_ODDS, Blocks.SAND);

      // addOption(Blocks.COARSE_DIRT, DIRT_RESIST_ODDS, Blocks.GRAVEL);
      if (Config.GetErodeFarmLand()) {
        addOption(Blocks.FARMLAND, DIRT_RESIST_ODDS, Blocks.DIRT);
      }

      addOption(Blocks.DIRT, DIRT_RESIST_ODDS, Blocks.GRAVEL);
      addOption(Blocks.PODZOL, DIRT_RESIST_ODDS, Blocks.COARSE_DIRT);
      addOption(Blocks.GRASS_BLOCK, GRASS_RESIST_ODDS, Blocks.DIRT);
      addOption(Blocks.GRASS, GRASS_RESIST_ODDS, Blocks.DIRT);
      // Grass Paths grow into Grass
      addOption(Blocks.GRASS_PATH, GRASS_RESIST_ODDS, Blocks.GRASS);
      addOption(Blocks.MOSSY_COBBLESTONE, COBBLE_RESIST_ODDS, Blocks.GRAVEL);
      // Directly to air because the gravel block is a larger volume than the original
      addOption(Blocks.MOSSY_COBBLESTONE_SLAB, COBBLE_RESIST_ODDS, Blocks.AIR);
      addOption(Blocks.MOSSY_COBBLESTONE_STAIRS, COBBLE_RESIST_ODDS, Blocks.AIR);
      addOption(Blocks.MOSSY_COBBLESTONE_WALL, COBBLE_RESIST_ODDS, Blocks.AIR);
      addOption(Blocks.COBBLESTONE, COBBLE_RESIST_ODDS, Blocks.MOSSY_COBBLESTONE);
      addOption(Blocks.COBBLESTONE_SLAB, COBBLE_RESIST_ODDS, Blocks.MOSSY_COBBLESTONE_SLAB);
      addOption(Blocks.COBBLESTONE_STAIRS, COBBLE_RESIST_ODDS, Blocks.MOSSY_COBBLESTONE_STAIRS);
      addOption(Blocks.COBBLESTONE_WALL, COBBLE_RESIST_ODDS, Blocks.MOSSY_COBBLESTONE_WALL);
      addOption(Blocks.MOSSY_STONE_BRICKS, BRICK_RESIST_ODDS, Blocks.GRAVEL);
      addOption(Blocks.MOSSY_STONE_BRICK_SLAB, COBBLE_RESIST_ODDS, Blocks.AIR);
      addOption(Blocks.MOSSY_STONE_BRICK_STAIRS, COBBLE_RESIST_ODDS, Blocks.AIR);
      addOption(Blocks.MOSSY_STONE_BRICK_WALL, COBBLE_RESIST_ODDS, Blocks.AIR);
      addOption(Blocks.STONE_BRICKS, COBBLE_RESIST_ODDS, Blocks.MOSSY_STONE_BRICKS);
      addOption(Blocks.STONE_BRICK_SLAB, COBBLE_RESIST_ODDS, Blocks.MOSSY_STONE_BRICK_SLAB);
      addOption(Blocks.STONE_BRICK_STAIRS, COBBLE_RESIST_ODDS, Blocks.MOSSY_STONE_BRICK_STAIRS);
      addOption(Blocks.STONE_BRICK_WALL, COBBLE_RESIST_ODDS, Blocks.MOSSY_STONE_BRICK_WALL);

      erodables.forEach((block, options) -> {
        if (block == null) {
          // skip calculation of decaylist if the block is null. Shouldn't happen due to
          // check in addOption().
          // TODO: Log a warning
          return;
        }

        int count = 0;
        ErodableOptions current = options;
        // Loop and fill out the decayList, aka the ordered list of what a block will
        // decay to. Stop when AIR is found.
        while (current.decayToBlock != Blocks.AIR) {
          count++;
          if (count > 9) {
            throw new IncompleteDecayListException(block, options);
          }

          options.decayList.add(current.decayToBlock);
          current = erodables.get(current.decayToBlock);
          if (current == null) {
            throw new NullDecayToBlockException(block, options);
          }
        }
      });
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
    if (EBlockTags.LEAVES.contains(block)) {
      return LEAF_RESIST_ODDS;
    } else if (EBlockTags.SAND.contains(block)) {
      return SAND_RESIST_ODDS;
    } else if (EBlockTags.WOOL.contains(block)) {
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
      decayToBlock = getErodables().get(block).decayToBlock;
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

    HashMap<Block, ErodableOptions> erodables = getErodables();

    if (erodables.containsKey(block)) {
      return erodables.get(block).decayToBlock;
    }
    return Blocks.AIR;
  }

  // TODO: This is simple method to block the breakage of cobblestone by source
  // blocks, but ideally it would still be allowed if there is enough "pressure"
  // behind the current source block.
  public static boolean canSourceBreak(Block block) {
    return getResistance(block) < SOURCE_BREAK_RESIST_ODDS;
  }

  public static ArrayList<Block> getDecayList(Block block) {
    return getErodables().get(block).decayList;
  }
}
