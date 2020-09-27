package com._13rac1.erosion.common;

import static org.mockito.Mockito.*;

import java.util.Random;

import com.google.common.collect.ImmutableMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.IProperty;
import net.minecraft.util.Direction;

import com._13rac1.erosion.minecraft.EFluidBlock;
import com._13rac1.erosion.minecraft.EVec3i;
import com._13rac1.erosion.minecraft.EVec3d;
import com._13rac1.erosion.minecraft.EBlockPos;

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

    final ImmutableMap<IProperty<?>, Comparable<?>> propertiesWater = ImmutableMap.of(EFluidBlock.LEVEL,
        FluidLevel.SOURCE);
    final BlockState stateWater = new BlockState(Blocks.WATER, propertiesWater);

    Assertions.assertEquals(Blocks.WATER, stateWater.getBlock());
    Assertions.assertEquals(FluidLevel.SOURCE, stateWater.get(EFluidBlock.LEVEL));
  }

  @Test
  void testDirLeftRight() {
    // Verbosely test both the Minecraft libraries and the Task methods work as
    // expected.
    EBlockPos posStart = new EBlockPos(0, 0, 0);
    EVec3i dirForward = new EVec3i(Direction.NORTH.getDirectionVec());
    EBlockPos posForward = posStart.add(dirForward);

    Assertions.assertNotEquals(posStart.south(), posForward);
    Assertions.assertEquals(posStart.north(), posForward);

    EVec3i dirLeft = dirForward.crossProduct(new EVec3i(Direction.DOWN.getDirectionVec()));
    EBlockPos posLeft = posStart.add(dirLeft);
    Assertions.assertEquals(posStart.west(), posLeft);
    Assertions.assertEquals(dirLeft, tasks.dirTurnLeft(dirForward));

    EVec3i dirRight = dirForward.crossProduct(new EVec3i(Direction.UP.getDirectionVec()));
    EBlockPos posRight = posStart.add(dirRight);
    Assertions.assertEquals(posStart.east(), posRight);
    Assertions.assertEquals(dirRight, tasks.dirTurnRight(dirForward));
  }

  @Test
  void testIsEdgeNorth() {
    // isEdge() checks north first, so this is just a test of the first return.
    // TODO: Check failure cases.
    final IWorld world = mock(IWorld.class);
    final EBlockPos pos = new EBlockPos(0, 0, 0);

    when(world.getBlock(pos.north().down())).thenReturn(Blocks.WATER);
    when(world.isFluidBlock(Blocks.WATER)).thenReturn(true);
    Assertions.assertTrue(tasks.isEdge(world, pos));
  }

  @Test
  void testAirFlowInPath() {
    final IWorld world = mock(IWorld.class);
    final EBlockPos pos = new EBlockPos(0, 0, 0);
    final EVec3i dir = new EVec3i(1, 0, 0); // South is positive

    final EVec3i airDirection7 = new EVec3i(7, 0, 0);
    final EVec3i airDirection14 = new EVec3i(14, -1, 0);
    final EBlockPos pos7 = pos.add(airDirection7);
    final EBlockPos pos14 = pos.add(airDirection14);

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
    final IWorld world = mock(IWorld.class);
    final EBlockPos pos = new EBlockPos(0, 0, 0);
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
    when(world.getFlowVelocity(any(BlockState.class), any(EBlockPos.class))).thenReturn(new EVec3d(0.707, 0, 0.707));
    Assertions.assertFalse(tasks.maybeDecayUnder(stateWater, world, pos, rand, level));

    // Decay dirt
    when(world.getBlock(pos.down())).thenReturn(Blocks.DIRT);
    when(world.getFlowVelocity(any(BlockState.class), any(EBlockPos.class))).thenReturn(new EVec3d(0.0, 0, 1)); // south
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
    final IWorld world = mock(IWorld.class);
    final EBlockPos pos = new EBlockPos(0, 0, 0);
    final Random rand = new Random(); // unused, in tests

    // Found water
    when(world.getBlock(any(EBlockPos.class))).thenReturn(Blocks.WATER);
    Assertions.assertFalse(tasks.maybeAddMoss(world, pos, rand));

    // Found cobble, which is DECAY_ALWAYS_ODDS, adds moss
    when(world.getBlock(any(EBlockPos.class))).thenReturn(Blocks.COBBLESTONE);
    Assertions.assertTrue(tasks.maybeAddMoss(world, pos, rand));
    verify(world).setBlockState(any(EBlockPos.class), eq(Blocks.MOSSY_COBBLESTONE.getDefaultState()), anyInt());
  }
}
