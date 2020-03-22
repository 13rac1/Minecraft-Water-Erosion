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
  void testIsBlockType() {
    Assertions.assertTrue(Tasks.isCobbleStone(Blocks.COBBLESTONE));
    Assertions.assertFalse(Tasks.isCobbleStone(Blocks.DIRT));

    Assertions.assertTrue(Tasks.isStoneBricks(Blocks.STONE_BRICKS));
    Assertions.assertFalse(Tasks.isStoneBricks(Blocks.DIRT));
  }
}
