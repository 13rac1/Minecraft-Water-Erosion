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
    final BlockPos pos7 = pos.add(airDirection7);
    final BlockPos pos14 = pos.add(airDirection14);

    final BlockState stateAir = new BlockState(Blocks.AIR, ImmutableMap.of());
    final BlockState stateNotAir = new BlockState(Blocks.DIRT, ImmutableMap.of());

    when(world.getBlockState(pos7)).thenReturn(stateAir);
    Assertions.assertTrue(Tasks.airInFlowPath(world, pos, dir));
    verify(world).getBlockState(pos7);

    when(world.getBlockState(pos7)).thenReturn(stateNotAir);
    when(world.getBlockState(pos14)).thenReturn(stateAir);
    Assertions.assertTrue(Tasks.airInFlowPath(world, pos, dir));
    verify(world, times(2)).getBlockState(pos7); // Total calls since world create.
    verify(world).getBlockState(pos14);

    when(world.getBlockState(pos7)).thenReturn(stateNotAir);
    when(world.getBlockState(pos14)).thenReturn(stateNotAir);
    Assertions.assertFalse(Tasks.airInFlowPath(world, pos, dir));
    verify(world, times(3)).getBlockState(pos7);
    verify(world, times(2)).getBlockState(pos14);
  }

  @Test
  void testIsBlockType() {
    Assertions.assertTrue(Tasks.isCobbleStone(Blocks.COBBLESTONE));
    Assertions.assertFalse(Tasks.isCobbleStone(Blocks.DIRT));

    Assertions.assertTrue(Tasks.isStoneBricks(Blocks.STONE_BRICKS));
    Assertions.assertFalse(Tasks.isStoneBricks(Blocks.DIRT));
  }

  @Test
  void testMaybeAddMoss() {
    final ErosionWorld world = mock(ErosionWorld.class);
    final BlockPos pos = new BlockPos(0, 0, 0);
    final BlockState stateWater = Blocks.WATER.getDefaultState();
    final BlockState stateCobble = Blocks.COBBLESTONE.getDefaultState();
    final Random rand = new Random(); // unused

    // Found water
    when(world.getBlockState(any(BlockPos.class))).thenReturn(stateWater);
    Assertions.assertFalse(Tasks.maybeAddMoss(stateWater, world, pos, rand));

    // Found cobble, which is DECAY_ALWAYS_ODDS, adds moss
    when(world.getBlockState(any(BlockPos.class))).thenReturn(stateCobble);
    Assertions.assertTrue(Tasks.maybeAddMoss(stateWater, world, pos, rand));
    verify(world).setBlockState(any(BlockPos.class), eq(Blocks.MOSSY_COBBLESTONE.getDefaultState()), anyInt());
  }
}
