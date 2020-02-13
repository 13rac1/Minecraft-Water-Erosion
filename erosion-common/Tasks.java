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

// TODO: Add Menu to allow disable of some features:
// https://www.curseforge.com/minecraft/mc-mods/modmenu

// IDEA: Change Lake generation to create more water sources on hills
// https://fabricmc.net/wiki/tutorial:fluids

// IDEA: Can eroded ponds/lakes turn into source blocks? Can they fill up?

// IDEA: Flows touching sea level turn to source blocks! Wait. No. Because then
// water will flow forever until it gets to the sea. Should it?

// IDEA: Flows INTO a source block wall should turn into a source block. That
// never happens though, because the source block will have a flow itself. What
// if flows towards each other become source blocks?

// TODO: Level7 flows delete the block under. If the block below is water, they
// should delete block in the flow direction too. There are odd cases where a
// downward dig will not go forward.

// IDEA: Turn upper edge blocks of pools to sand or add a mud block?

// IDEA: Wall breaking should check for air one block down in a flow reachable
// radius to seek downhill rather than always going straight.

// IDEA: Avoid carving channels in sand/gravel since it is affected by gravity.
// Perhaps better better if seeking downhill.

// IDEA: IRL streams have rocks. Minecraft world gen doesn't place these rocks.
// Can they be added to world gen? Could large flows find/place stones?

public class Tasks {
  // blockFlags is used with world.setBlockState() when blocks are replaced with
  // air.
  private static final Integer blockFlags = BlockFlag.PROPAGATE_CHANGE | BlockFlag.NOTIFY_LISTENERS;

  public static void run(BlockState state, ErosionWorld world, BlockPos pos, Random rand) {

    Integer level = state.get(FluidBlock.LEVEL);

    maybeSourceBreak(state, world, pos, rand, level);

    // Skip source blocks, only flowing water.
    if (level == FluidLevel.SOURCE) {
      return;
    }

    if (maybeFlowingWall(state, world, pos, rand, level)) {
      // Return if the flow breaks a wall.
      return;
    }

    maybeErodeEdge(state, world, pos, rand, level);
    maybeDecayUnder(state, world, pos, rand, level);
  }

  private static void maybeErodeEdge(BlockState state, ErosionWorld world, BlockPos pos, Random rand, Integer level) {
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

    Block decayBlock = ErodableBlocks.maybeDecay(rand, underBlock);
    if (decayBlock == Blocks.AIR) {
      // Removing the block under the water block.
      Integer underBlocklevel = level < FluidLevel.FALLING7 ? level + 1 : FluidLevel.FALLING7;
      world.setBlockState(underPos, Blocks.WATER.getDefaultState().with(FluidBlock.LEVEL, underBlocklevel), blockFlags);
    } else {
      // Decay the block and do nothing else.
      world.setBlockState(underPos, decayBlock.getDefaultState(), blockFlags);
      return;
    }
    // Don't delete source blocks
    if (state.get(FluidBlock.LEVEL) == FluidLevel.SOURCE) {
      // Technically this should never happen.
      return;
    }

    // Delete the water block
    // TODO: Maybe the water block itself shouldn't be deleted?
    world.setBlockState(pos, Blocks.AIR.getDefaultState(), blockFlags);

    // Delete upwards until there's no water, a water source is found, or three
    // water blocks have been removed. The goal is to lower the water level
    // naturally without having to reflow the entire stream/creek/river. and
    // delete enough to fix water rapids, but not disrupt the flow of
    // waterfalls which results in incorrect
    Integer upDeleteCount = 0;
    BlockPos posUp = pos.up();
    while (world.isFluidBlock(world.getBlockState(posUp).getBlock())) {
      upDeleteCount++;
      if (upDeleteCount > 3) {
        break;
      }
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

  private static boolean isEdge(ErosionWorld world, BlockPos pos) {
    List<BlockPos> listSidePos = Arrays.asList(pos.north(), pos.south(), pos.east(), pos.west());

    for (BlockPos sidePos : listSidePos) {
      BlockPos underPos = sidePos.down();
      BlockState underState = world.getBlockState(underPos);
      Block underBlock = underState.getBlock();

      if (!world.isFluidBlock(underBlock)) {
        // System.out.println("Did not find side under water block: " +
        // underBlock.getClass().getName());
        continue;
      }
      // System.out.println("Found side under water block");

      return true;
    }
    return false;
  }

  private static List<Integer> wallBreakers = Arrays.asList(FluidLevel.FLOW1, FluidLevel.FLOW2, FluidLevel.FLOW3,
      FluidLevel.FLOW4, FluidLevel.FLOW5, FluidLevel.FLOW6, FluidLevel.FLOW7);

  private static boolean maybeFlowingWall(BlockState state, ErosionWorld world, BlockPos pos, Random rand,
      Integer level) {

    if (!wallBreakers.contains(level)) {
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
    Integer Flow7Adjust = 0;
    if (level == FluidLevel.FLOW7) {
      // Level7 must dig down one block
      Flow7Adjust = 1;
    }

    // Find the position of the block in the flow direction.
    BlockPos flowPos = pos.add(new Vec3i(velocity.x, velocity.y - Flow7Adjust, velocity.z));
    // TODO: Search left/right from straight for "downhill" and break in that
    // direction. Downhill is the direction of air. This will keep streams going
    // further downhill rather than going straight when there is a cliff one
    // block to the side.

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
    Block wallBlock = flowState.getBlock();
    if (!ErodableBlocks.canErode(wallBlock)) {
      return false;
    }
    if (!ErodableBlocks.maybeErode(rand, wallBlock)) {
      return false;
    }
    // TODO: The block behind must have the same flow direction.

    // System.out.println("Removing block to side:" +
    // flowState.getBlock().getName().asFormattedString());

    // TODO: decay walls
    // Block decayBlock = ErodableBlocks.maybeDecay(rand, wallBlock);
    // world.setBlockState(flowPos, decayBlock.getDefaultState(), blockFlags);
    // TODO: Consider placing a water block with correct level instead of assuming
    // it will flow.
    world.setBlockState(flowPos, Blocks.AIR.getDefaultState(), blockFlags);
    return true;
  }

  private static final int SOURCE_BREAKS_ABOVE_SEA_LEVEL = 0;

  private static void maybeSourceBreak(BlockState state, ErosionWorld world, BlockPos pos, Random rand, Integer level) {
    // Source blocks only.
    if (level != FluidLevel.SOURCE) {
      return;
    }
    // TODO: Break when there's less than three blocks to air. This will create
    // more crevasse waterfalls.

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
        if (maybeAirBlock != Blocks.AIR && maybeAirBlock != Blocks.CAVE_AIR
            && !BlockTags.LEAVES.contains(maybeAirBlock)) {
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

  // TODO: Increase odds. This should happen any time all of the checks pass.
  private static void maybeDecayUnder(BlockState state, ErosionWorld world, BlockPos pos, Random rand, Integer level) {
    // return if water is source or falling or FLOW7
    if (level == FluidLevel.SOURCE || level > FluidLevel.FLOW7) {
      return;
    }
    // Get the block under us.
    BlockPos underPos = pos.down();
    BlockState underState = world.getBlockState(underPos);
    Block underBlock = underState.getBlock();

    if (!ErodableBlocks.canErode(underBlock)) {
      return;
    }
    // TODO: return if is edge?

    // calculate decayto for block below
    Block decayTo = ErodableBlocks.decayTo(underBlock);
    if (decayTo == Blocks.AIR) {
      // Nothing to do if block will become air.
      return;
    }

    // Find flow direction: Velocity is a 3D vector normalized to 1 pointing the
    // direction the water is flowing.
    Vec3d velocity = world.getFlowVelocity(state, pos);

    if (Math.abs(velocity.x) < 1 && Math.abs(velocity.z) < 1) {
      // Skip 45 degree flows.
      //
      // The velocity vector is normalized, therefore 45 degree flows are
      // represented by two floats of +/- 0.707.
      return;
    }

    // Find the position of the block in the flow direction.
    BlockPos flowPos = underPos.add(new Vec3i(velocity.x, velocity.y, velocity.z));
    BlockState flowState = world.getBlockState(flowPos);
    Block flowBlock = flowState.getBlock();

    // decay if decayto is same as block in flow direction.
    if (flowBlock == decayTo) {
      world.setBlockState(underPos, decayTo.getDefaultState(), blockFlags);
      System.out.println("maybeDecayUnder!"); // NOT WORKING?
    }
  }

  private static void maybeAddSideMoss(BlockState state, ErosionWorld world, BlockPos pos, Random rand, Integer level) {
    // IDEA: Should source water add moss?
    // return if water is source or more than FLOW7
    // return if sides are not cobblestone
    // calculate odds for sides separately and change to mossy cobblestone

  }
}
