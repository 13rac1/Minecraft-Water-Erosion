package com._13rac1.erosion.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.block.FluidBlock;
import net.minecraft.tags.BlockTags;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import com._13rac1.erosion.minecraft.EVec3i;
import com._13rac1.erosion.minecraft.EVec3d;
import com._13rac1.erosion.minecraft.EBlockPos;

// TODO: Add Menu to allow disable of some features:
// https://www.curseforge.com/minecraft/mc-mods/modmenu

// IDEA: Turn upper edge blocks add a mud block?

// IDEA: IRL streams have rocks. Minecraft world gen doesn't place these rocks.
// Can they be added to world gen? Could large flows find/place stones?

// IDEA: Source with non-zero flow velocity should erode under to make the start of waterfalls more realistic.

// IDEA: Actual sea level can erode if there is open air nearby.

// IDEA: Instead of just distance, rate the entire flow line. Score it. Turn to the best score. Most water or air. Water weighed more than air.

// MCBUG-FIX: Check for air next to source blocks and flow there. Maybe just call update()? Solve the problem of watergen leaving water not flowing where it should be.

// IDEA: Recalc the flow math for the water blocks around, then do the math and reset the height instead of deleting. MIGHT fix random holes on incline flows.

// IDEA: Falling8: If air in front, water/or solid (non-air) below, then any non-clear block in front of that, then create source block. This is a way to fill steep pools. Look up to  7 blocks forward.

// IDEA: Lazy state lookup on a class for isEdge().

// IDEA: Source breaks should count number of flowing7 in the break direction plane, and only break when it will not cause entire sides of lakes to disappear.

// IDEA: Source blocks check 3-4 blocks under for air and erode straight down. Flood the caves, but also made them more obvious.

// IDEA: Return "Reasons" for not eroding, which can be unit tested for the correct reason not just any.

public class Tasks {

  // Copied from net.minecraft.util(.math).Direction to reduce imports and deal
  // with Forge/Fabric storing the class in different locations.
  private static final EVec3i VECTOR_DOWN = new EVec3i(0, -1, 0);
  private static final EVec3i VECTOR_UP = new EVec3i(0, 1, 0);
  private static final EVec3i VECTOR_NORTH = new EVec3i(0, 0, -1);
  private static final EVec3i VECTOR_NORTH_EAST = new EVec3i(1, 0, -1);
  private static final EVec3i VECTOR_EAST = new EVec3i(1, 0, 0);
  private static final EVec3i VECTOR_SOUTH_EAST = new EVec3i(1, 0, 1);
  private static final EVec3i VECTOR_SOUTH = new EVec3i(0, 0, 1);
  private static final EVec3i VECTOR_SOUTH_WEST = new EVec3i(-1, 0, 1);
  private static final EVec3i VECTOR_WEST = new EVec3i(-1, 0, 0);
  private static final EVec3i VECTOR_NORTH_WEST = new EVec3i(-1, 0, -1);

  // blockFlags is used with world.setBlockState() when blocks are replaced with
  // air.
  private static final Integer blockFlags = BlockFlag.PROPAGATE_CHANGE | BlockFlag.NOTIFY_LISTENERS;

  private static final List<EVec3i> posFourEdges = Arrays.asList(VECTOR_NORTH, VECTOR_EAST, VECTOR_SOUTH, VECTOR_WEST);

  private static final List<EVec3i> posEightAround = Arrays.asList(VECTOR_NORTH, VECTOR_NORTH_EAST, VECTOR_EAST,
      VECTOR_SOUTH_EAST, VECTOR_SOUTH, VECTOR_SOUTH_WEST, VECTOR_WEST, VECTOR_NORTH_WEST);

  private static final List<EVec3i> posEightAroundUp = Arrays.asList(new EVec3i(1, 1, 1), new EVec3i(1, 1, 0),
      new EVec3i(1, 1, -1), new EVec3i(0, 1, -1), new EVec3i(-1, 1, -1), new EVec3i(-1, 1, 0), new EVec3i(-1, 1, 1),
      new EVec3i(0, 1, 1));

  // Primary run function
  public void run(BlockState state, ErosionWorld world, EBlockPos pos, Random rand) {

    Integer level = state.get(FluidBlock.LEVEL);

    maybeSourceBreak(state, world, pos, rand, level);

    if (maybeAddMoss(world, pos, rand)) {
      // Return if moss is added.
      return;
    }
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

  private void maybeErodeEdge(BlockState state, ErosionWorld world, EBlockPos pos, Random rand, Integer level) {
    // Get the block under us.
    EBlockPos underPos = pos.down();
    Block underBlock = world.getBlock(underPos);

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
    EBlockPos posUp = pos.up();

    while (world.isFluidBlock(world.getBlock(posUp))) {
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

  protected boolean isEdge(ErosionWorld world, EBlockPos pos) {
    List<EBlockPos> listSidePos = Arrays.asList(pos.north(), pos.south(), pos.east(), pos.west());

    for (EBlockPos sidePos : listSidePos) {
      EBlockPos underPos = sidePos.down();
      Block underBlock = world.getBlock(underPos);

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

  // Rotate a given direction vector to the left: North->West.
  protected EVec3i dirTurnLeft(EVec3i in) {
    return in.crossProduct(VECTOR_DOWN);
  }

  // Rotate a given direction vector to the right: North-East.
  protected EVec3i dirTurnRight(EVec3i in) {
    return in.crossProduct(VECTOR_UP);
  }

  protected boolean treeInColumn(ErosionWorld world, EBlockPos pos) {
    final Integer MAX_UP = 5;
    Integer count = 0;
    EBlockPos currentPos = pos.up();
    while (count < MAX_UP) {
      Block currentBlock = world.getBlock(currentPos);
      if (BlockTags.LOGS.contains(currentBlock)) {
        return true;
      }
      if (isAir(currentBlock)) {
        return false;
      }
      count++;
      currentPos = currentPos.up();
    }
    return false;
  }

  private static List<Integer> wallBreakers = Arrays.asList(FluidLevel.FLOW1, FluidLevel.FLOW2, FluidLevel.FLOW3,
      FluidLevel.FLOW4, FluidLevel.FLOW5, FluidLevel.FLOW6, FluidLevel.FLOW7);

  private class wallBreakOption {
    EVec3i dir;
    Integer distance;
  }

  private boolean maybeFlowingWall(BlockState state, ErosionWorld world, EBlockPos pos, Random rand, Integer level) {

    if (!wallBreakers.contains(level)) {
      // level Flow7 goes down, never to the side.
      return false;
    }

    // Find flow direction: Velocity is a 3D vector normalized to 1 pointing the
    // direction the water is flowing.
    EVec3d velocity = world.getFlowVelocity(state, pos);
    // 0.8 is a good number to ignore 45 degree angle flows, but allow anything else
    // with a more definitive direction such as 0, 90, or 22.5.
    if (Math.abs(velocity.getX()) < 0.8 && Math.abs(velocity.getZ()) < 0.8) {
      // Skip 45 degree flows.
      // The velocity vector is normalized, therefore 45 degree flows are
      // represented by two floats of +/- 0.707.
      return false;
    }
    Integer Flow7Adjust = 0;
    if (level == FluidLevel.FLOW7) {
      // Level7 must dig down one block
      Flow7Adjust = -1;
    }

    EVec3i dirForward = new EVec3i(Math.round(velocity.getX()), velocity.getY() + Flow7Adjust,
        Math.round(velocity.getZ()));

    EBlockPos posForward = pos.add(dirForward);
    Block blockForward = world.getBlock(posForward);
    // The block in the direction of flow must be a solid block, not
    // air/water/lava. This is the defining feature of a "wall break", there
    // must be a wall.
    if (isAir(blockForward) || blockForward == Blocks.WATER || blockForward == Blocks.LAVA) {
      return false;
    }

    EVec3i dirLeft = dirTurnLeft(dirForward);
    EVec3i dirRight = dirTurnRight(dirForward);
    EBlockPos posLeft = pos.add(dirLeft);
    EBlockPos posRight = pos.add(dirRight);

    // Search left/right from straight for "downhill" and break in that
    // direction. Downhill is the direction of air. This will keep streams going
    // further downhill rather than going straight when there is a cliff one
    // block to the side.

    // Must be an erodable block without a tree above, not air, water, or
    // something not erodable.
    Boolean canErodeForward = ErodableBlocks.canErode(blockForward) && !treeInColumn(world, posForward);
    Boolean canErodeLeft = ErodableBlocks.canErode(world.getBlock(posLeft)) && !treeInColumn(world, posLeft);
    Boolean canErodeRight = ErodableBlocks.canErode(world.getBlock(posRight)) && !treeInColumn(world, posRight);
    if (!canErodeForward && !canErodeLeft && !canErodeRight) {
      return false;
    }

    List<wallBreakOption> options = new ArrayList<>();

    // 128 is max returned
    if (canErodeForward) {
      Integer dist = distanceToAirWaterInFlowPath(world, pos, dirForward, level);
      if (dist != 128) {
        wallBreakOption opt = new wallBreakOption();
        opt.dir = dirForward;
        opt.distance = dist;
        options.add(opt);
      }
    }

    if (canErodeLeft) {
      Integer dist = distanceToAirWaterInFlowPath(world, pos, dirLeft, level);
      if (dist != 128) {
        wallBreakOption opt = new wallBreakOption();
        opt.dir = dirLeft;
        opt.distance = dist;
        options.add(opt);
      }
    }

    if (canErodeRight) {
      Integer dist = distanceToAirWaterInFlowPath(world, pos, dirRight, level);
      if (dist != 128) {
        wallBreakOption opt = new wallBreakOption();
        opt.dir = dirRight;
        opt.distance = dist;
        options.add(opt);
      }
    }

    // Return when no directions can find Air or Water.
    if (options.size() == 0) {
      return false;
    }

    Integer shortestDistance = 128;
    EVec3i shortestDir = null;
    for (wallBreakOption option : options) {
      if (option.distance < shortestDistance) {
        shortestDistance = option.distance;
        shortestDir = option.dir;
      }
    }

    if (shortestDir == null) {
      return false;
    }

    // System.out.println("dir:" + shortestDir + " distance:" + shortestDistance);

    // Find the position of the block in the flow direction.
    EBlockPos flowPos = pos.add(shortestDir);

    // Block above cannot be wood, keep trees standing on dirt.
    // TODO: Look more than one block up for wood.
    EBlockPos aboveFlowPos = flowPos.up();
    Block aboveFlowBlock = world.getBlock(aboveFlowPos);
    if (BlockTags.LOGS.contains(aboveFlowBlock)) {
      return false;
    }
    // TODO: Do not remove an erodable block if the stack above is unsupported.
    // Search around the stack to confirm a block other than air and water is
    // touching it.

    Block wallBlock = world.getBlock(flowPos);
    if (!ErodableBlocks.canErode(wallBlock)) {
      return false;
    }
    if (!ErodableBlocks.maybeErode(rand, wallBlock)) {
      return false;
    }

    // TODO: decay walls? Problem is it'll slow down the wall breaks a lot, which
    // means the flows will not go as far as blocks erode below. Perhaps this is
    // more realistic limits.
    // Block decayBlock = ErodableBlocks.maybeDecay(rand, wallBlock);
    // world.setBlockState(flowPos, decayBlock.getDefaultState(), blockFlags);
    // TODO: Place a water block with correct level instead of assuming
    // it will flow. Should resolve holes.
    world.setBlockState(flowPos, Blocks.AIR.getDefaultState(), blockFlags);
    return true;
  }

  private void maybeSourceBreak(BlockState state, ErosionWorld world, EBlockPos pos, Random rand, Integer level) {
    // Source blocks only.
    if (level != FluidLevel.SOURCE) {
      return;
    }

    // Skip blocks less than sea level+, because there are a lot of them.
    if (pos.getY() < world.getSeaLevel()) {
      // System.out.println("Too Low" + pos.getY() + "sea:" +
      // world.getSeaLevel());
      return;
    }

    // Skip blocks without air above.
    Block upBlock = world.getBlockState(pos.up()).getBlock();
    if (!isAir(upBlock)) {
      // System.out.println("Not Surface Water:" +
      // world.getBlockState(pos.up()).getBlock().getName().asFormattedString());
      // System.out.println("Y:" + pos.getY() + "sea:" + world.getSeaLevel());
      return;
    }

    // Skip blocks already flowing
    EVec3d velocity = world.getFlowVelocity(state, pos);
    if (velocity.length() > 0) {
      return;
    }
    // System.out.println("length:" + velocity.length());

    // Blocks near an erodable surface only.
    List<EVec3i> listDirection = Arrays.asList(new EVec3i(1, 0, 0), new EVec3i(-1, 0, 0), new EVec3i(0, 0, 1),
        new EVec3i(0, 0, -1));
    // Randomize the list each run.
    Collections.shuffle(listDirection);

    for (EVec3i dir : listDirection) {
      EBlockPos sidePos = pos.add(dir);

      Block sideBlock = world.getBlock(sidePos);
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

      Integer dist = distanceToAirWaterInFlowPath(world, pos, dir, level);
      if (dist == 128) {
        // Skip if air was not found in the direction of breakage.
        continue;
      }

      // Check behind. Is there enough "pressure" to break a wall? More blocks
      // increases the odds, but there must be at least two in a row to avoid
      // breaking generated farms.
      int waterFound = 0;
      for (int waterMultipler : Arrays.asList(1, 2, 3)) {
        EVec3i waterDirection = new EVec3i(-dir.getX() * waterMultipler, dir.getY(), -dir.getZ() * waterMultipler);
        // System.out.println("maybewaterdir:" + waterDirection);
        EBlockPos maybeWaterPos = pos.add(waterDirection);
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

  private boolean isAir(Block block) {
    return block == Blocks.AIR || block == Blocks.CAVE_AIR;
  }

  // Start from the provided EBlockPos and trace
  protected boolean airInFlowPath(ErosionWorld world, EBlockPos pos, EVec3i dir) {
    int yDeeper = 0;
    for (int airMultipler : Arrays.asList(7, 14)) {
      EVec3i airDirection = new EVec3i(dir.getX() * airMultipler, dir.getY() - yDeeper, dir.getZ() * airMultipler);
      // Go deeper each iteration
      yDeeper++;
      EBlockPos maybeAirPos = pos.add(airDirection);
      Block maybeAirBlock = world.getBlock(maybeAirPos);

      if (isAir(maybeAirBlock) || BlockTags.LEAVES.contains(maybeAirBlock)) {
        return true;
      }
    }

    return false;
  }

  // Trace the flow path from the current position given the flow level to find
  // the distance to the closest open space: air, cave air, water, or leaves.
  protected Integer distanceToAirWaterInFlowPath(ErosionWorld world, EBlockPos pos, EVec3i dir, Integer level) {
    if (level > FluidLevel.FLOW7) {
      return 128;
    }
    // level is now [0,1,2,3,4,5,6,7]
    Integer distanceToAirWater = 0;

    // The Minecraft in-game UI shows the opposite water level value than the
    // true block metadata. Data `level==1`, the closest to the source block, is
    // displayed as Targeted Fluid level:7. Data `level==7`, the furthest from
    // the source block, is displayed as Targeted Fluid level:1.
    Integer flowDistanceRemaining = 7 - level;

    EBlockPos posCurrent = pos;
    Block blockCurrent;
    // Check how far as the current flow can go at the current height.
    while (flowDistanceRemaining > 0) {
      flowDistanceRemaining -= 1;
      distanceToAirWater += 1;

      posCurrent = posCurrent.add(dir);
      blockCurrent = world.getBlock(posCurrent);

      if (isAir(blockCurrent) || BlockTags.LEAVES.contains(blockCurrent) || blockCurrent == Blocks.WATER) {
        return distanceToAirWater;
      }
      if (!ErodableBlocks.canErode(blockCurrent)) {
        // Fail if unerodable block is found in path before air/water.
        return 128;
      }

    }

    // Still here? Dig down and search up to 14 blocks
    posCurrent = posCurrent.down();
    flowDistanceRemaining = 7;
    while (flowDistanceRemaining > 0) {
      flowDistanceRemaining -= 1;
      distanceToAirWater += 1;

      posCurrent = posCurrent.add(dir);
      blockCurrent = world.getBlock(posCurrent);
      // TODO: Check for Water
      if (isAir(blockCurrent) || BlockTags.LEAVES.contains(blockCurrent)) {
        return distanceToAirWater;
      }
    }

    return 128; // Meaningless high value
  }

  protected boolean maybeDecayUnder(BlockState state, ErosionWorld world, EBlockPos pos, Random rand, Integer level) {
    // TODO: Should we be using rand?
    // return if water is source or falling or FLOW7
    if (level == FluidLevel.SOURCE || level > FluidLevel.FLOW7) {
      return false;
    }
    // Get the block under us.
    EBlockPos underPos = pos.down();
    Block underBlock = world.getBlock(underPos);

    if (!ErodableBlocks.canErode(underBlock)) {
      return false;
    }
    // TODO: return if is edge?

    // calculate decayto for block below
    Block decayTo = ErodableBlocks.decayTo(underBlock);
    if (decayTo == Blocks.AIR) {
      // Nothing to do if block will become air.
      return false;
    }

    EVec3d velocity = world.getFlowVelocity(state, pos);
    // 0.8 is a good number to ignore 45 degree angle flows, but allow anything else
    // with a more definitive direction such as 0, 90, or 22.5.
    if (Math.abs(velocity.getX()) < 0.8 && Math.abs(velocity.getZ()) < 0.8) {
      // Skip 45 degree flows.
      // The velocity vector is normalized, therefore 45 degree flows are
      // represented by two floats of +/- 0.707.
      return false;
    }

    // Find the position of the block in the flow direction, round to closest 90
    // degree angle.
    EBlockPos flowPos = underPos.add(new EVec3i(Math.round(velocity.getX()), 0, Math.round(velocity.getZ())));
    Block flowBlock = world.getBlock(flowPos);

    // If the block in the flow direction is any of the lesser blocks underBlocks
    // can become, then decay to the next lesser in the list.
    if (!ErodableBlocks.getDecayList(underBlock).contains(flowBlock)) {
      return false;
    }
    world.setBlockState(underPos, decayTo.getDefaultState(), blockFlags);
    // System.out.println("maybeDecayUnder!");
    return true;
  }

  protected boolean isCobbleStone(Block block) {
    return block == Blocks.COBBLESTONE || block == Blocks.COBBLESTONE_WALL || block == Blocks.COBBLESTONE_STAIRS
        || block == Blocks.COBBLESTONE_WALL;
  }

  protected boolean isStoneBricks(Block block) {
    return block == Blocks.STONE_BRICKS || block == Blocks.STONE_BRICK_WALL || block == Blocks.STONE_BRICK_STAIRS
        || block == Blocks.STONE_BRICK_WALL;
  }

  // Cobblestone and Stone Bricks grow moss near water, check every block around.
  // Returns true when a change is made.
  protected boolean maybeAddMoss(ErosionWorld world, EBlockPos pos, Random rand) {
    List<EVec3i> listDirection = posEightAround;
    // TODO: Add one level above the water line
    // listDirection.addAll(posEightAroundUp);

    // Randomize the list each run.
    // TODO: Just pick a random number since everything returns now.
    Collections.shuffle(listDirection);

    for (EVec3i dir : listDirection) {
      EBlockPos sidePos = pos.add(dir);
      Block sideBlock = world.getBlock(sidePos);

      if (!isCobbleStone(sideBlock) && !isStoneBricks(sideBlock)) {
        // Stop the loop. Randomized, means 1:16 odds.
        return false;
      }

      // Change to mossy, always happens with current config.
      Block mossBlock = ErodableBlocks.maybeDecay(rand, sideBlock);

      if (mossBlock == Blocks.AIR) {
        return false; // Stop the loop
      }
      world.setBlockState(sidePos, mossBlock.getDefaultState(), blockFlags);
      return true; // Stop the loop
    }
    return false;
  }
}
