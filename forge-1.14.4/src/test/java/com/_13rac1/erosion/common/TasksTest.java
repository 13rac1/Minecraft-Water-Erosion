package com._13rac1.erosion.common;

import com.google.common.collect.ImmutableMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.state.IProperty;

public class TasksTest {

  @BeforeAll
  static void beforeAll() throws Exception {
    FakeBlock.setupFakeBlocks();
  }

  // Simple test to confirm BlockState is working as expected.
  @Test
  void testBlockState() {
    ImmutableMap<IProperty<?>, Comparable<?>> properties = ImmutableMap.of();
    BlockState state = new BlockState(Blocks.AIR, properties);

    Assertions.assertEquals(Blocks.AIR, state.getBlock());

    ImmutableMap<IProperty<?>, Comparable<?>> propertiesWater = ImmutableMap.of(FluidBlock.LEVEL, FluidLevel.SOURCE);
    BlockState stateWater = new BlockState(Blocks.WATER, propertiesWater);

    Assertions.assertEquals(Blocks.WATER, stateWater.getBlock());
    Assertions.assertEquals(FluidLevel.SOURCE, stateWater.get(FluidBlock.LEVEL));

  }
}
