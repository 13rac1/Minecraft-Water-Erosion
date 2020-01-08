package com._13rac1.erosion.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.block.FluidBlock;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

// TODO: Turn upper edge blocks of pools to sand or add a mud block?

// TODO: Add Menu to allow disable of some features:
// https://www.curseforge.com/minecraft/mc-mods/modmenu

// TODO: Change Lake generation to create more water sources on hills
// https://fabricmc.net/wiki/tutorial:fluids

// IDEA: Avoid carving channels in sand since it is affected by gravity.

// TODO: Should destroyed blocks drop items?

// IDEA: Eroded ponds/lakes turn into source blocks?

// TODO: Count leaves as air for source break calculations.

// IDEA: Flows touching sea level turn to source blocks! Wait. No. Because then
// water will flow forever until it gets to the sea. Should it?

// IDEA: Flows INTO a source block wall should turn into a source block

// TODO: Level7 flows delete the block under, they should delete block in the
// flow direction too. There are odd cases where a downward dig will not go
// forward.

public class Tasks {
  // blockFlags is used with world.setBlockState() when blocks are replaced with
  // air.
  private static final Integer blockFlags = BlockFlag.PROPAGATE_CHANGE | BlockFlag.NOTIFY_LISTENERS;

  public static void maybeErodeEdge(BlockState state, ErosionWorld world, BlockPos pos, Random rand, Integer level) {
    // Get the block under us.
    BlockPos underPos = pos.down();
    BlockState underState = world.getBlockState(underPos);
    Block underBlock = underState.getBlock();

    // Return if the block below us is not erodable.
    // TODO: Technically this call is redundant now.
    if (!ErodableBlocks.canErode(underBlock)) {
      // System.out.println(underBlock.getName().asFormattedString());
      return;
    }
    if (!ErodableBlocks.maybeErode(rand, underBlock)) {
      return;
    }

    // Return if we are not a water edge block and not level 7. Level 7, the
    // last one, is allowed to dig down to extend the water flow.
    if (!isEdge(world, pos) && level != FluidLevel.FLOW7) {
      return;
    }

    // System.out.println("pos x:" + pos.getX() + " y:" + pos.getY() + " z:" +
    // pos.getZ() + " water level: " + level);
    // System.out.println("Removing block: " +
    // underBlock.getName().asFormattedString());

    Integer underBlocklevel = level < FluidLevel.FALLING7 ? level + 1 : FluidLevel.FALLING7;
    world.setBlockState(underPos, Blocks.WATER.getDefaultState().with(FluidBlock.LEVEL, underBlocklevel), blockFlags);

    // Don't delete source blocks
    if (state.get(FluidBlock.LEVEL) == FluidLevel.SOURCE) {
      return;
    }

    // Delete the water block
    // TODO: Maybe the water block itself shouldn't be deleted?
    world.setBlockState(pos, Blocks.AIR.getDefaultState(), blockFlags);

    // Delete upwards until there's no water or a water source is found. The
    // goal is to lower the water level naturally without having to reflow the
    // entire stream/creek/river.
    BlockPos posUp = pos.up();
    while (world.getBlockState(posUp).getBlock() instanceof FluidBlock) {
      if (world.getBlockState(posUp).get(FluidBlock.LEVEL) == FluidLevel.SOURCE) {
        break;
      }
      // TODO: Go up until finding the last water block and delete that. The
      // rest are fine.
      world.setBlockState(posUp, Blocks.AIR.getDefaultState(), blockFlags);
      posUp = posUp.up();
    }
    // TODO: Anything further to do since we are deleting ourself? Cancel the
    // callback?
  }

  public static boolean isEdge(ErosionWorld world, BlockPos pos) {
    List<BlockPos> listSidePos = Arrays.asList(pos.north(), pos.south(), pos.east(), pos.west());

    for (BlockPos sidePos : listSidePos) {
      BlockPos underPos = sidePos.down();
      BlockState underState = world.getBlockState(underPos);
      Block underBlock = underState.getBlock();

      if (!(underBlock instanceof FluidBlock)) {
        // System.out.println("Did not find side under water block: " +
        // underBlock.getClass().getName());
        continue;
      }
      // System.out.println("Found side under water block");

      return true;
    }
    return false;
  }

  public static boolean maybeFlowingWall(BlockState state, ErosionWorld world, BlockPos pos, Random rand,
      Integer level) {
    if (level == FluidLevel.SOURCE || level > FluidLevel.FLOW6) {
      // level Flow7 goes down, never to the side.
      return false;
    }

    // Find flow direction: Velocity is a 3D vector normalized to 1 pointing the
    // direction the water is flowing.
    Vec3d velocity = world.getFlowVelocity(state, pos);

    if (Math.abs(velocity.x) < 1 && Math.abs(velocity.z) < 1) {
      // Skip 45 degree flows.
      // TODO: Should the flow randomly go one of the ways?
      //
      // The velocity vector is normalized, therefore 45 degree flows are
      // represented by two floats of +/- 0.707.
      return false;
    }

    // Find the position of the block in the flow direction.
    BlockPos flowPos = pos.add(new Vec3i(velocity.x, velocity.y, velocity.z));

    // Block above cannot be wood, keep trees standing on dirt.
    // TODO: Look more than one block up for wood.
    BlockPos aboveFlowPos = flowPos.up();
    BlockState aboveFlowState = world.getBlockState(aboveFlowPos);
    Block aboveFlowBlock = aboveFlowState.getBlock();
    if (BlockTags.LOGS.contains(aboveFlowBlock)) {
      return false;
    }
    // TODO: Do not remove an erodable block if the stack above is unsupported.
    // Search around the stack to confirm a block other than air and water is
    // touching it.

    BlockState flowState = world.getBlockState(flowPos);

    if (!ErodableBlocks.canErode(flowState.getBlock())) {
      return false;
    }
    if (!ErodableBlocks.maybeErode(rand, flowState.getBlock())) {
      return false;
    }
    // TODO: The block behind must have the same flow direction.

    // System.out.println("Removing block to side:" +
    // flowState.getBlock().getName().asFormattedString());
    world.setBlockState(flowPos, Blocks.AIR.getDefaultState(), blockFlags);
    return true;
  }

  private static final int SOURCE_BREAKS_ABOVE_SEA_LEVEL = 0;

  public static void maybeSourceBreak(BlockState state, ErosionWorld world, BlockPos pos, Random rand, Integer level) {
    // Source blocks only.
    if (level != FluidLevel.SOURCE) {
      return;
    }
    // TODO: Break when there's less than three blocks to air

    // Skip blocks less than sea level+, because there are a lot of them.
    if (pos.getY() <= world.getSeaLevel() + SOURCE_BREAKS_ABOVE_SEA_LEVEL) {
      // System.out.println("Too Low" + pos.getY() + "sea:" +
      // world.getSeaLevel());
      return;
    }

    // Skip blocks without air above.
    Block upBlock = world.getBlockState(pos.up()).getBlock();
    if (upBlock != Blocks.AIR && upBlock != Blocks.CAVE_AIR) {
      // System.out.println("Not Surface Water:" +
      // world.getBlockState(pos.up()).getBlock().getName().asFormattedString());
      // System.out.println("Y:" + pos.getY() + "sea:" + world.getSeaLevel());
      return;
    }

    // Skip blocks already flowing
    Vec3d velocity = world.getFlowVelocity(state, pos);
    if (velocity.length() > 0) {
      return;
    }
    // System.out.println("length:" + velocity.length());

    // Blocks near an erodable surface only.
    List<Vec3i> listDirection = Arrays.asList(new Vec3i(1, 0, 0), new Vec3i(-1, 0, 0), new Vec3i(0, 0, 1),
        new Vec3i(0, 0, -1));
    // Randomize the list each run.
    Collections.shuffle(listDirection);

    for (Vec3i dir : listDirection) {
      BlockPos sidePos = pos.add(dir);

      Block sideBlock = world.getBlockState(sidePos).getBlock();
      if (sideBlock == Blocks.WATER) {
        // Short circuit water blocks. Should save CPU as this will be the most
        // common result.
        continue;
      }

      if (!ErodableBlocks.canErode(sideBlock)) {
        // Skip unbreakable.
        continue;
      }
      if (!ErodableBlocks.canSourceBreak(sideBlock)) {
        // Skip stronger blocks.
        continue;
      }
      // Found a breakable block in the direction.

      // Check forward 7, 14, etc for air. Check a level lower each seventh
      // block, to be sure the flow can make it to the selected block, rather
      // than just make a 7 block creek.
      boolean foundAir = true;
      int yDeeper = 0;
      // TODO: Should the odds of breakage increase when block 21 is clear?
      // TODO: Should all blocks in the potential route be checked? May find
      // smaller gaps and crevasses.
      // TODO: If 14 is clear, but 7 isn't the break should happen anyway,
      // because then water will flow underground a ways before exiting.
      for (int airMultipler : Arrays.asList(7, 14)) {
        Vec3i airDirection = new Vec3i(dir.getX() * airMultipler, dir.getY() - yDeeper, dir.getZ() * airMultipler);

        yDeeper++;
        BlockPos maybeAirPos = pos.add(airDirection);
        BlockState maybeAirState = world.getBlockState(maybeAirPos);

        Block maybeAirBlock = maybeAirState.getBlock();
        if (maybeAirBlock != Blocks.AIR && maybeAirBlock != Blocks.CAVE_AIR) {
          foundAir = false;
          break;
        }
      }
      if (!foundAir) {
        // Skip if air was not found.
        continue;
      }

      // Check behind. Is there enough "pressure" to break a wall? More blocks
      // increases the odds, but there must be at least two in a row to avoid
      // breaking generated farms.
      int waterFound = 0;
      for (int waterMultipler : Arrays.asList(1, 2, 3)) {
        Vec3i waterDirection = new Vec3i(-dir.getX() * waterMultipler, dir.getY(), -dir.getZ() * waterMultipler);
        // System.out.println("maybewaterdir:" + waterDirection);
        BlockPos maybeWaterPos = pos.add(waterDirection);
        BlockState maybeWaterState = world.getBlockState(maybeWaterPos);
        // System.out.println("maybe water:" +
        // maybeWaterState.getBlock().getName().asFormattedString());
        if (maybeWaterState.getBlock() != Blocks.WATER) {
          // TODO: Must be source water block
          break;
        }
        waterFound++;
      }
      if (waterFound < 1) {
        // Skip if not enough sequential water blocks found behind.
        // System.out.println("not enough water");
        continue;
      }
      // TODO: Better odds for waterFound > 1.

      // TODO: Check depth. Greater depth increases odds of a wall breakthrough.

      if (!ErodableBlocks.maybeErode(rand, sideBlock)) {
        // TODO: Should this chance check occur earlier?
        return;
      }

      // System.out.println(
      // "Removing block to source side:" +
      // world.getBlockState(sidePos).getBlock().getName().asFormattedString());
      world.setBlockState(sidePos, Blocks.AIR.getDefaultState(), blockFlags);

      // Only process the first erodable side found.
      return;
    }
  }
}