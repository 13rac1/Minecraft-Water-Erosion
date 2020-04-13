package com._13rac1.erosion.common;

import static org.mockito.Mockito.*;

import java.util.Random;

import com.google.common.collect.ImmutableMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.state.IProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class TasksTest {
  private static Tasks tasks = new Tasks();

  @BeforeAll
  static void beforeAll() throws Exception {
    FakeBlock.setupFakeBlocks();
  }

  // Simple test to confirm BlockState is working as expected.
  @Test
  void testBlockState() {
    final BlockState state = new BlockState(Blocks.AIR, ImmutableMap.of());

    Assertions.assertEquals(Blocks.AIR, state.getBlock());

    final ImmutableMap<IProperty<?>, Comparable<?>> propertiesWater = ImmutableMap.of(FluidBlock.LEVEL,
        FluidLevel.SOURCE);
    final BlockState stateWater = new BlockState(Blocks.WATER, propertiesWater);

    Assertions.assertEquals(Blocks.WATER, stateWater.getBlock());
    Assertions.assertEquals(FluidLevel.SOURCE, stateWater.get(FluidBlock.LEVEL));
  }

  @Test
  void testDirLeftRight() {
    // Verbosely test both the Minecraft libraries and the Task methods work as
    // expected.
    BlockPos posStart = new BlockPos(0, 0, 0);
    Vec3i dirForward = Direction.NORTH.getDirectionVec();
    BlockPos posForward = posStart.add(dirForward);

    Assertions.assertNotEquals(posStart.south(), posForward);
    Assertions.assertEquals(posStart.north(), posForward);

    Vec3i dirLeft = dirForward.crossProduct(Direction.DOWN.getDirectionVec());
    BlockPos posLeft = posStart.add(dirLeft);
    Assertions.assertEquals(posStart.west(), posLeft);
    Assertions.assertEquals(dirLeft, tasks.dirTurnLeft(dirForward));

    Vec3i dirRight = dirForward.crossProduct(Direction.UP.getDirectionVec());
    BlockPos posRight = posStart.add(dirRight);
    Assertions.assertEquals(posStart.east(), posRight);
    Assertions.assertEquals(dirRight, tasks.dirTurnRight(dirForward));
  }

  @Test
  void testIsEdgeNorth() {
    // isEdge() checks north first, so this is just a test of the first return.
    // TODO: Check failure cases.
    final ErosionWorld world = mock(ErosionWorld.class);
    final BlockPos pos = new BlockPos(0, 0, 0);

    when(world.getBlock(pos.north().down())).thenReturn(Blocks.WATER);
    when(world.isFluidBlock(Blocks.WATER)).thenReturn(true);
    Assertions.assertTrue(tasks.isEdge(world, pos));
  }

  @Test
  void testAirFlowInPath() {
    final ErosionWorld world = mock(ErosionWorld.class);
    final BlockPos pos = new BlockPos(0, 0, 0);
    final Vec3i dir = new Vec3i(1, 0, 0); // South is positive

    final Vec3i airDirection7 = new Vec3i(7, 0, 0);
    final Vec3i airDirection14 = new Vec3i(14, -1, 0);
    final BlockPos pos7 = pos.add(airDirection7);
    final BlockPos pos14 = pos.add(airDirection14);

    when(world.getBlock(pos7)).thenReturn(Blocks.AIR);
    Assertions.assertTrue(tasks.airInFlowPath(world, pos, dir));
    verify(world).getBlock(pos7);

    when(world.getBlock(pos7)).thenReturn(Blocks.DIRT);
    when(world.getBlock(pos14)).thenReturn(Blocks.AIR);
    Assertions.assertTrue(tasks.airInFlowPath(world, pos, dir));
    verify(world, times(2)).getBlock(pos7); // Total calls since world create.
    verify(world).getBlock(pos14);

    when(world.getBlock(pos7)).thenReturn(Blocks.DIRT);
    when(world.getBlock(pos14)).thenReturn(Blocks.DIRT);
    Assertions.assertFalse(tasks.airInFlowPath(world, pos, dir));
    verify(world, times(3)).getBlock(pos7);
    verify(world, times(2)).getBlock(pos14);
  }

  @Test
  void testMaybeDecayUnder() {
    final ErosionWorld world = mock(ErosionWorld.class);
    final BlockPos pos = new BlockPos(0, 0, 0);
    final BlockState stateWater = Blocks.WATER.getDefaultState();
    final Random rand = new Random(); // unused, in tests

    // No decay under source blocks
    Integer level = FluidLevel.SOURCE;
    Assertions.assertFalse(tasks.maybeDecayUnder(stateWater, world, pos, rand, level));

    // No decay of water on top of water.
    level = FluidLevel.FLOW1;
    when(world.getBlock(pos.down())).thenReturn(Blocks.WATER);
    Assertions.assertFalse(tasks.maybeDecayUnder(stateWater, world, pos, rand, level));

    // No decay if block will become air.
    when(world.getBlock(pos.down())).thenReturn(Blocks.CLAY);
    Assertions.assertFalse(tasks.maybeDecayUnder(stateWater, world, pos, rand, level));

    // No decay for 45 degree
    when(world.getBlock(pos.down())).thenReturn(Blocks.COBBLESTONE);
    when(world.getFlowVelocity(any(BlockState.class), any(BlockPos.class))).thenReturn(new Vec3d(0.707, 0, 0.707));
    Assertions.assertFalse(tasks.maybeDecayUnder(stateWater, world, pos, rand, level));

    // Decay dirt
    when(world.getBlock(pos.down())).thenReturn(Blocks.DIRT);
    when(world.getFlowVelocity(any(BlockState.class), any(BlockPos.class))).thenReturn(new Vec3d(0.0, 0, 1)); // south
    when(world.getBlock(pos.down().south())).thenReturn(Blocks.SAND);
    Assertions.assertTrue(tasks.maybeDecayUnder(stateWater, world, pos, rand, level));

  }

  @Test
  void testIsBlockType() {
    Assertions.assertTrue(tasks.isCobbleStone(Blocks.COBBLESTONE));
    Assertions.assertFalse(tasks.isCobbleStone(Blocks.DIRT));

    Assertions.assertTrue(tasks.isStoneBricks(Blocks.STONE_BRICKS));
    Assertions.assertFalse(tasks.isStoneBricks(Blocks.DIRT));
  }

  @Test
  void testMaybeAddMoss() {
    final ErosionWorld world = mock(ErosionWorld.class);
    final BlockPos pos = new BlockPos(0, 0, 0);
    final Random rand = new Random(); // unused, in tests

    // Found water
    when(world.getBlock(any(BlockPos.class))).thenReturn(Blocks.WATER);
    Assertions.assertFalse(tasks.maybeAddMoss(world, pos, rand));

    // Found cobble, which is DECAY_ALWAYS_ODDS, adds moss
    when(world.getBlock(any(BlockPos.class))).thenReturn(Blocks.COBBLESTONE);
    Assertions.assertTrue(tasks.maybeAddMoss(world, pos, rand));
    verify(world).setBlockState(any(BlockPos.class), eq(Blocks.MOSSY_COBBLESTONE.getDefaultState()), anyInt());
  }
}
