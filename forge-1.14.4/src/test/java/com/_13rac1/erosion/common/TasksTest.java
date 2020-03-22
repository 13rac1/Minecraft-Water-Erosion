package com._13rac1.erosion.common;

import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.state.IProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class TasksTest {

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
  void testIsEdgeNorth() {
    // isEdge() checks north first, so this is just a test of the first return.
    // TODO: Check failure cases.
    final ErosionWorld world = mock(ErosionWorld.class);
    final BlockPos pos = new BlockPos(0, 0, 0);
    final BlockState state = new BlockState(Blocks.WATER, ImmutableMap.of());

    when(world.getBlockState(pos.north().down())).thenReturn(state);
    when(world.isFluidBlock(Blocks.WATER)).thenReturn(true);
    Assertions.assertTrue(Tasks.isEdge(world, pos));
  }

  @Test
  void testAirFlowInPath() {
    final ErosionWorld world = mock(ErosionWorld.class);
    final BlockPos pos = new BlockPos(0, 0, 0);
    final Vec3i dir = new Vec3i(1, 0, 0); // South is positive

    final Vec3i airDirection7 = new Vec3i(7, 0, 0);
    final Vec3i airDirection14 = new Vec3i(14, -1, 0);

    final BlockState stateAir = new BlockState(Blocks.AIR, ImmutableMap.of());
    final BlockState stateNotAir = new BlockState(Blocks.DIRT, ImmutableMap.of());
    when(world.getBlockState(pos.add(airDirection7))).thenReturn(stateAir);

    Assertions.assertTrue(Tasks.airInFlowPath(world, pos, dir));

    when(world.getBlockState(pos.add(airDirection7))).thenReturn(stateNotAir);
    when(world.getBlockState(pos.add(airDirection14))).thenReturn(stateAir);
    Assertions.assertTrue(Tasks.airInFlowPath(world, pos, dir));

    when(world.getBlockState(pos.add(airDirection7))).thenReturn(stateNotAir);
    when(world.getBlockState(pos.add(airDirection14))).thenReturn(stateNotAir);
    Assertions.assertFalse(Tasks.airInFlowPath(world, pos, dir));
  }

  @Test
  void testIsBlockType() {
    Assertions.assertTrue(Tasks.isCobbleStone(Blocks.COBBLESTONE));
    Assertions.assertFalse(Tasks.isCobbleStone(Blocks.DIRT));

    Assertions.assertTrue(Tasks.isStoneBricks(Blocks.STONE_BRICKS));
    Assertions.assertFalse(Tasks.isStoneBricks(Blocks.DIRT));
  }
}
