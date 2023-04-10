package com._13rac1.erosion.common;

import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.tags.BlockTags;

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
// IDEA: Source breaks should count number of flowing7 in the break direction plane, and only break when it will not cause entire sides of lakes to disappear.
// IDEA: Source blocks check 3-4 blocks under for air and erode straight down. Flood the caves, but also made them more obvious.
// IDEA: Return "Reasons" for not eroding, which can be unit tested for the correct reason not just any.

public class Tasks {

  // Copied from net.minecraft.util(.math).Direction to reduce imports
  private static final Vec3i VECTOR_DOWN = new Vec3i(0, -1, 0);
  private static final Vec3i VECTOR_UP = new Vec3i(0, 1, 0);
  private static final Vec3i VECTOR_NORTH = new Vec3i(0, 0, -1);
  private static final Vec3i VECTOR_NORTH_EAST = new Vec3i(1, 0, -1);
  private static final Vec3i VECTOR_EAST = new Vec3i(1, 0, 0);
  private static final Vec3i VECTOR_SOUTH_EAST = new Vec3i(1, 0, 1);
  private static final Vec3i VECTOR_SOUTH = new Vec3i(0, 0, 1);
  private static final Vec3i VECTOR_SOUTH_WEST = new Vec3i(-1, 0, 1);
  private static final Vec3i VECTOR_WEST = new Vec3i(-1, 0, 0);
  private static final Vec3i VECTOR_NORTH_WEST = new Vec3i(-1, 0, -1);

  private static final List<Vec3i> posFourEdges = Arrays.asList(VECTOR_NORTH, VECTOR_EAST, VECTOR_SOUTH, VECTOR_WEST);

  private static final List<Vec3i> posEightAround = Arrays.asList(VECTOR_NORTH, VECTOR_NORTH_EAST, VECTOR_EAST,
      VECTOR_SOUTH_EAST, VECTOR_SOUTH, VECTOR_SOUTH_WEST, VECTOR_WEST, VECTOR_NORTH_WEST);

  private static final List<Vec3i> posEightAroundUp = Arrays.asList(new Vec3i(1, 1, 1), new Vec3i(1, 1, 0),
      new Vec3i(1, 1, -1), new Vec3i(0, 1, -1), new Vec3i(-1, 1, -1), new Vec3i(-1, 1, 0), new Vec3i(-1, 1, 1),
      new Vec3i(0, 1, 1));

  // Primary run function
  public void run(Level world, BlockState state, BlockPos pos, RandomSource rand) {

    Integer level = state.getValue(LiquidBlock.LEVEL);

    maybeSourceBreak(world, state, pos, rand, level);

    if (maybeAddMoss(world, pos, rand)) {
      // Return if moss is added.
      return;
    }
    // Skip source blocks, only flowing water.
    if (level == FluidLevel.SOURCE) {
      return;
    }

    if (maybeFlowingWall(world, state, pos, rand, level)) {
      // Return if the flow breaks a wall.
      return;
    }

    maybeErodeEdge(world, state, pos, rand, level);
    maybeDecayUnder(world, state, pos, rand, level);
  }

  /**
   * Find flow direction: Velocity is a 3D vector normalized to 1 pointing the 2D
   * direction the water is flowing, ignoring up and down.
   */
  // Range of possible values include:
  // (-1.0, 0.0, 0.0)
  // (0.0, 0.0, -1.0)
  // (0.8944271606898969, 0.0, -0.44721361033295565)
  // (0.7071067613036184, 0.0, -0.7071067613036184)
  // (0.9999999664723898, 0.0, 0.0)
  // (0.3162277853996985, 0.0, -0.9486833137896449) - Rare
  public Vec3 getFlowVelocity(Level world, BlockPos pos, BlockState state) {
    FluidState fluidState = state.getFluidState();
    return fluidState.getFlow(world, pos);
  }

  public Boolean isFluidBlock(Block block) {
    return block instanceof LiquidBlock;
  }

  public Block getBlock(Level world, BlockPos pos) {
    BlockState bs = world.getBlockState(pos);
    return bs.getBlock();
  }

  protected boolean maybeErodeEdge(Level world, BlockState state, BlockPos pos, RandomSource rand, Integer level) {
    // Level 7, the last one, is allowed to dig down to extend the water flow.
    if (level != FluidLevel.FLOW7) {
      return false;
    }
    // Get the block under us.
    BlockPos underPos = pos.below();
    Block underBlock = getBlock(world, underPos);

    // Return if the block below us is not erodable.
    if (!ErodableBlocks.canErode(underBlock)) {
      return false;
    }
    if (!ErodableBlocks.maybeErode(rand, underBlock)) {
      return false;
    }

    // Return if we are not a water edge block.
    if (!isEdge(world, pos)) {
      return false;
    }

    // System.out.println("pos x:" + pos.getX() + " y:" + pos.getY() + " z:" +
    // pos.getZ() + " water level: " + level);
    // System.out.println("Removing block: " +
    // underBlock.getName().asFormattedString());

    Block decayBlock = ErodableBlocks.maybeDecay(rand, underBlock);
    if (decayBlock == Blocks.AIR) {
      // Removing the block under the water block.
      Integer underBlocklevel = level < FluidLevel.FALLING7 ? level + 1 : FluidLevel.FALLING7;

      world.setBlockAndUpdate(underPos, Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, underBlocklevel));
    } else {
      // Decay the block and do nothing else.
      world.setBlockAndUpdate(underPos, decayBlock.defaultBlockState());
      return true;
    }
    // Don't delete source blocks
    if (state.getValue(LiquidBlock.LEVEL) == FluidLevel.SOURCE) {
      // Technically this should never happen.
      return false;
    }

    // Delete the water block
    world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());

    // Delete upwards until there's no water, a water source is found, or three
    // water blocks have been removed. The goal is to lower the water level
    // naturally without having to reflow the entire stream/creek/river. and
    // delete enough to fix water rapids, but not disrupt the flow of
    // waterfalls which results in incorrect
    Integer upDeleteCount = 0;
    BlockPos posUp = pos.above();

    while (isFluidBlock(getBlock(world, posUp))) {
      upDeleteCount++;
      if (upDeleteCount > 3) {
        break;
      }
      if (world.getBlockState(posUp).getValue(LiquidBlock.LEVEL) == FluidLevel.SOURCE) {
        break;
      }
      world.setBlockAndUpdate(posUp, Blocks.AIR.defaultBlockState());
      posUp = posUp.above();
    }
    // TODO: Anything further to do since we are deleting ourself?
    return true;
  }

  protected boolean isEdge(Level world, BlockPos pos) {
    List<BlockPos> listSidePos = Arrays.asList(pos.north(), pos.south(), pos.east(), pos.west());

    for (BlockPos sidePos : listSidePos) {
      BlockPos underPos = sidePos.below();
      Block underBlock = getBlock(world, underPos);

      if (!isFluidBlock(underBlock)) {
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
  protected Vec3i dirTurnLeft(Vec3i in) {
    return in.cross(VECTOR_DOWN);
  }

  // Rotate a given direction vector to the right: North-East.
  protected Vec3i dirTurnRight(Vec3i in) {
    return in.cross(VECTOR_UP);
  }

  // Search upward for a stack of logs to avoid eroding under trees
  protected boolean treeInColumn(Level world, BlockPos pos) {
    final Integer MAX_UP = 5;
    Integer count = 0;
    BlockPos currentPos = pos.above();
    while (count < MAX_UP) {
      BlockState bs = world.getBlockState(currentPos);
      Block currentBlock = bs.getBlock();

      if (bs.is(BlockTags.LOGS)) {
        return true;
      }
      if (isAir(currentBlock)) {
        return false;
      }
      count++;
      currentPos = currentPos.above();
    }
    return false;
  }

  private static List<Integer> wallBreakers = Arrays.asList(FluidLevel.FLOW1, FluidLevel.FLOW2, FluidLevel.FLOW3,
      FluidLevel.FLOW4, FluidLevel.FLOW5, FluidLevel.FLOW6, FluidLevel.FLOW7);

  private class wallBreakOption {
    Vec3i dir;
    Integer distance;

    public wallBreakOption(Vec3i dir, Integer distance) {
      this.dir = dir;
      this.distance = distance;
    }
  }

  protected boolean maybeFlowingWall(Level world, BlockState state, BlockPos pos, RandomSource rand, Integer level) {
    if (!wallBreakers.contains(level)) {
      // level Flow7 goes down, never to the side.
      return false;
    }

    // Find flow direction: Velocity is a 3D vector normalized to 1 pointing the
    // direction the water is flowing.
    Vec3 velocity = getFlowVelocity(world, pos, state);
    // 0.8 is a good number to ignore 45 degree angle flows, but allow anything else
    // with a more definitive direction such as 0, 90, or 22.5.
    if (Math.abs(velocity.x) < 0.8 && Math.abs(velocity.z) < 0.8) {
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

    Vec3i dirForward = new Vec3i((int) Math.round(velocity.x), (int) velocity.y + Flow7Adjust,
        (int) Math.round(velocity.z));
    Vec3i shortestDir = findShortestDirectionToAirOrWater(world, pos, level, dirForward);
    if (shortestDir == null) {
      return false;
    }

    // Find the position of the block in the flow direction.
    BlockPos flowPos = pos.offset(shortestDir);

    Block wallBlock = getBlock(world, flowPos);
    if (!ErodableBlocks.maybeErode(rand, wallBlock)) {
      return false;
    }

    // TODO: decay walls? Problem is it'll slow down the wall breaks a lot, which
    // means the flows will not go as far as blocks erode below. Perhaps this is
    // more realistic limits.
    // Block decayBlock = ErodableBlocks.maybeDecay(rand, wallBlock);
    // world.setBlockState(flowPos, decayBlock.getDefaultState());
    // TODO: Place a water block with correct level instead of assuming
    // it will flow. Should resolve holes.
    world.setBlockAndUpdate(flowPos, Blocks.AIR.defaultBlockState());
    return true;
  }

  protected Vec3i findShortestDirectionToAirOrWater(Level world, BlockPos pos, Integer level, Vec3i dirForward) {
    BlockPos posForward = pos.offset(dirForward);
    Block blockForward = getBlock(world, posForward);
    // The block in the direction of flow must be a solid block, not
    // air/water/lava. This is the defining feature of a "wall break", there
    // must be a wall.
    if (isAir(blockForward) || blockForward == Blocks.WATER || blockForward == Blocks.LAVA) {
      return null;
    }
    Vec3i dirLeft = dirTurnLeft(dirForward);
    Vec3i dirRight = dirTurnRight(dirForward);
    BlockPos posLeft = pos.offset(dirLeft);
    BlockPos posRight = pos.offset(dirRight);

    // Search left/right from straight for "downhill" and break in that
    // direction. Downhill is the direction of air. This will keep streams going
    // further downhill rather than going straight when there is a cliff one
    // block to the side.

    // Must be an erodable block without a tree above, not air, water, or
    // something not erodable.
    Boolean canErodeForward = ErodableBlocks.canErode(blockForward) && !treeInColumn(world, posForward);
    Boolean canErodeLeft = ErodableBlocks.canErode(getBlock(world, posLeft)) && !treeInColumn(world, posLeft);
    Boolean canErodeRight = ErodableBlocks.canErode(getBlock(world, posRight)) && !treeInColumn(world, posRight);
    if (!canErodeForward && !canErodeLeft && !canErodeRight) {
      return null;
    }

    List<wallBreakOption> options = new ArrayList<>();
    if (canErodeForward) {
      Integer dist = distanceToAirWaterInFlowPath(world, pos, dirForward, level);
      if (dist != AIR_WATER_NOT_FOUND) {
        options.add(new wallBreakOption(dirForward, dist));
      }
    }

    if (canErodeLeft) {
      Integer dist = distanceToAirWaterInFlowPath(world, pos, dirLeft, level);
      if (dist != AIR_WATER_NOT_FOUND) {
        options.add(new wallBreakOption(dirLeft, dist));
      }
    }

    if (canErodeRight) {
      Integer dist = distanceToAirWaterInFlowPath(world, pos, dirRight, level);
      if (dist != AIR_WATER_NOT_FOUND) {
        options.add(new wallBreakOption(dirRight, dist));
      }
    }

    if (options.size() == 0) {
      return null;
    }

    Integer shortestDistance = 128;
    Vec3i shortestDir = null;
    for (wallBreakOption option : options) {
      if (option.distance < shortestDistance) {
        shortestDistance = option.distance;
        shortestDir = option.dir;
      }
    }
    return shortestDir;
  }

  private void maybeSourceBreak(Level world, BlockState state, BlockPos pos, RandomSource rand, Integer level) {
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
    Block upBlock = world.getBlockState(pos.above()).getBlock();
    if (!isAir(upBlock)) {
      // System.out.println("Not Surface Water:" +
      // world.getBlockState(pos.up()).getBlock().getName().asFormattedString());
      // System.out.println("Y:" + pos.getY() + "sea:" + world.getSeaLevel());
      return;
    }

    // Skip blocks already flowing
    Vec3 velocity = getFlowVelocity(world, pos, state);
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
      BlockPos sidePos = pos.offset(dir);

      Block sideBlock = getBlock(world, sidePos);
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
        Vec3i waterDirection = new Vec3i(-dir.getX() * waterMultipler, dir.getY(), -dir.getZ() * waterMultipler);
        // System.out.println("maybewaterdir:" + waterDirection);
        BlockPos maybeWaterPos = pos.offset(waterDirection);
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
      world.setBlockAndUpdate(sidePos, Blocks.AIR.defaultBlockState());
      // Only process the first erodable side found.
      return;
    }
  }

  private boolean isAir(Block block) {
    return block == Blocks.AIR || block == Blocks.CAVE_AIR;
  }

  public final int AIR_WATER_NOT_FOUND = 128;

  // Trace the flow path from the current position given the flow level to find
  // the distance to the closest open space: air, cave air, water, or leaves.
  protected Integer distanceToAirWaterInFlowPath(Level world, BlockPos pos, Vec3i dir, Integer level) {
    if (level > FluidLevel.FLOW7) {
      return AIR_WATER_NOT_FOUND;
    }
    // level is now [0,1,2,3,4,5,6,7]
    Integer distanceToAirWater = 0;

    // The Minecraft in-game UI shows the opposite water level value than the
    // true block metadata. Data `level==1`, the closest to the source block, is
    // displayed as Targeted Fluid level:7. Data `level==7`, the furthest from
    // the source block, is displayed as Targeted Fluid level:1.
    Integer flowDistanceRemaining = 7 - level;

    BlockPos posCurrent = pos;
    Block blockCurrent;
    BlockState blockstateCurrent;
    // Check how far as the current flow can go at the current height.
    while (flowDistanceRemaining > 0) {
      flowDistanceRemaining -= 1;
      distanceToAirWater += 1;

      posCurrent = posCurrent.offset(dir);
      blockstateCurrent = world.getBlockState(posCurrent);
      blockCurrent = blockstateCurrent.getBlock();

      if (isAir(blockCurrent) || blockstateCurrent.is(BlockTags.LEAVES) || blockCurrent == Blocks.WATER) {
        return distanceToAirWater;
      }
      if (!ErodableBlocks.canErode(blockCurrent)) {
        // Fail if unerodable block is found in path before air/water.
        return AIR_WATER_NOT_FOUND;
      }
    }

    // Still here? Dig down and search up to 14 blocks
    posCurrent = posCurrent.below();
    flowDistanceRemaining = 8; // Because the first is falling an is effectively a source block
    while (flowDistanceRemaining > 0) {
      // Check the falling aka "source" block first at same position as above.
      blockstateCurrent = world.getBlockState(posCurrent);
      blockCurrent = blockstateCurrent.getBlock();

      if (isAir(blockCurrent) || blockstateCurrent.is(BlockTags.LEAVES) || blockCurrent == Blocks.WATER) {
        return distanceToAirWater;
      }
      if (!ErodableBlocks.canErode(blockCurrent)) {
        // Fail if unerodable block is found in path before air/water.
        return AIR_WATER_NOT_FOUND;
      }

      flowDistanceRemaining -= 1;
      distanceToAirWater += 1;
      posCurrent = posCurrent.offset(dir);
    }

    return AIR_WATER_NOT_FOUND;
  }

  protected boolean maybeDecayUnder(Level world, BlockState state, BlockPos pos, RandomSource rand, Integer level) {
    // TODO: Should we be using rand?
    // return if water is source or falling or FLOW7
    if (level == FluidLevel.SOURCE || level > FluidLevel.FLOW7) {
      return false;
    }
    // Get the block under us.
    BlockPos underPos = pos.below();
    Block underBlock = getBlock(world, underPos);

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

    Vec3 velocity = getFlowVelocity(world, pos, state);
    // 0.8 is a good number to ignore 45 degree angle flows, but allow anything else
    // with a more definitive direction such as 0, 90, or 22.5.
    if (Math.abs(velocity.x) < 0.8 && Math.abs(velocity.z) < 0.8) {
      // Skip 45 degree flows.
      // The velocity vector is normalized, therefore 45 degree flows are
      // represented by two floats of +/- 0.707.
      return false;
    }

    // Find the position of the block in the flow direction, round to closest 90
    // degree angle.
    BlockPos flowPos = underPos
        .offset(new Vec3i((int) Math.round(velocity.x), 0, (int) Math.round(velocity.z)));
    Block flowBlock = getBlock(world, flowPos);

    // If the block in the flow direction is any of the lesser blocks underBlocks
    // can become, then decay to the next lesser in the list.
    if (!ErodableBlocks.getDecayList(underBlock).contains(flowBlock)) {
      return false;
    }
    world.setBlockAndUpdate(underPos, decayTo.defaultBlockState());
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
  protected boolean maybeAddMoss(Level world, BlockPos pos, RandomSource rand) {
    List<Vec3i> listDirection = posEightAround;
    // TODO: Add one level above the water line
    // listDirection.addAll(posEightAroundUp);

    // Randomize the list each run.
    // TODO: Just pick a random number since everything returns now.
    Collections.shuffle(listDirection);

    for (Vec3i dir : listDirection) {
      BlockPos sidePos = pos.offset(dir);
      Block sideBlock = getBlock(world, sidePos);

      if (!isCobbleStone(sideBlock) && !isStoneBricks(sideBlock)) {
        // Stop the loop. Randomized, means 1:16 odds.
        return false;
      }

      // Change to mossy, always happens with current config.
      Block mossBlock = ErodableBlocks.maybeDecay(rand, sideBlock);

      if (mossBlock == Blocks.AIR) {
        return false; // Stop the loop
      }
      world.setBlockAndUpdate(sidePos, mossBlock.defaultBlockState());
      return true; // Stop the loop
    }
    return false;
  }
}
