package com._13rac1.erosion.common;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

public class TestMaybeErodeEdge extends TestTasksCommon {
    private void whenBlock(Level world, BlockPos pos, Block block) {
        final BlockState bs = block.defaultBlockState();
        bs.initCache();
        doReturn(bs).when(world).getBlockState(pos);
    }

    @BeforeAll
    static void beforeAll() throws Exception {
        // Bootstrap the whole world.
        FakeWorldVersion.init();
    }

    @Test
    void testOnlyAllowLevel7() {
        final Level world = mock(Level.class, levelSettings);
        final BlockPos pos = new BlockPos(10, 0, 0);
        final RandomSource rand = mock(RandomSource.class);
        final BlockState bsWater = getWaterState(FluidLevel.FLOW1);

        whenBlock(world, pos, Blocks.WATER);
        Assertions.assertFalse(tasks.maybeErodeEdge(world, bsWater, pos, rand, FluidLevel.FLOW1));
    }

    @Test
    void testCannotErodeGold() {
        final Level world = mock(Level.class, levelSettings);
        final BlockPos pos = new BlockPos(10, 0, 0);
        final RandomSource rand = mock(RandomSource.class);
        final BlockState bsWater = getWaterState(FluidLevel.FLOW7);

        whenBlock(world, pos, Blocks.WATER);
        whenBlock(world, pos.below(), Blocks.GOLD_BLOCK);
        Assertions.assertFalse(tasks.maybeErodeEdge(world, bsWater, pos, rand, FluidLevel.FLOW7));
    }

    @Test
    void testOnlyEdgesNotAir() {
        final Level world = mock(Level.class, levelSettings);
        final BlockPos pos = new BlockPos(10, 0, 0);
        final RandomSource rand = mock(RandomSource.class);
        final BlockState bsWater = getWaterState(FluidLevel.FLOW7);

        when(rand.nextInt(anyInt())).thenReturn(0);
        whenBlock(world, pos, Blocks.WATER);
        whenBlock(world, pos.below(), Blocks.DIRT); // Not to air, when nextInt == 0
        whenBlock(world, pos.below().north(), Blocks.WATER);
        Assertions.assertTrue(tasks.maybeErodeEdge(world, bsWater, pos, rand, FluidLevel.FLOW7));
    }

    @Test
    void testToAirWithAirAbove() {
        final Level world = mock(Level.class, levelSettings);
        final BlockPos pos = new BlockPos(10, 0, 0);
        final RandomSource rand = mock(RandomSource.class);
        final BlockState bsWater = getWaterState(FluidLevel.FLOW7);

        when(rand.nextInt(anyInt())).thenReturn(0);
        whenBlock(world, pos, Blocks.WATER);
        whenBlock(world, pos.below(), Blocks.CLAY); // Always to air
        whenBlock(world, pos.below().north(), Blocks.WATER);
        whenBlock(world, pos.above(), Blocks.AIR);
        Assertions.assertTrue(tasks.maybeErodeEdge(world, bsWater, pos, rand, FluidLevel.FLOW7));
    }

    @Test
    void testToAirWithWater() {
        final Level world = mock(Level.class, levelSettings);
        final BlockPos pos = new BlockPos(10, 0, 0);
        final RandomSource rand = mock(RandomSource.class);
        final BlockState bsWater = getWaterState(FluidLevel.FLOW7);

        when(rand.nextInt(anyInt())).thenReturn(0);
        whenBlock(world, pos, Blocks.WATER);
        whenBlock(world, pos.below(), Blocks.CLAY); // Always to air
        whenBlock(world, pos.below().north(), Blocks.WATER);
        when(world.getBlockState(pos.above())).thenReturn(getWaterState(FluidLevel.FLOW7));
        whenBlock(world, pos.above().above(), Blocks.AIR);
        Assertions.assertTrue(tasks.maybeErodeEdge(world, bsWater, pos, rand, FluidLevel.FLOW7));
    }

}
