package com._13rac1.erosion.common;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
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
        assertNotNull(pos);
        doReturn(bs).when(world).getBlockState(pos);
    }

    @BeforeAll
    static void beforeAll() throws Exception {
        // Bootstrap the whole world.
        FakeWorldVersion.init();
    }

    @Test
    void testCannotErodeGold() {
        final Level world = mock(Level.class, levelSettings);
        final BlockPos pos = new BlockPos(10, 0, 0);
        final RandomSource rand = mock(RandomSource.class);
        final BlockState bsWater = getWaterState(FluidLevel.FLOW1);

        whenBlock(world, pos, Blocks.WATER);
        whenBlock(world, pos.below(), Blocks.GOLD_BLOCK);
        Assertions.assertFalse(tasks.maybeErodeEdge(world, bsWater, pos, rand, FluidLevel.FLOW1));
    }

    @Test
    void testOnlyEdgesOrLevel7() {
        final Level world = mock(Level.class, levelSettings);
        final BlockPos pos = new BlockPos(10, 0, 0);
        final RandomSource rand = mock(RandomSource.class);
        final BlockState bsWater = getWaterState(FluidLevel.FLOW1);

        // Returning 0 forces canErode() to pass
        when(rand.nextInt(anyInt())).thenReturn(0);
        whenBlock(world, pos, Blocks.WATER);
        // Dirt is erodable
        whenBlock(world, pos.below(), Blocks.DIRT);
        // DIRT means it is NOT edge, WATER would mean it is an edge.
        // isEdge() checks all four directions
        whenBlock(world, pos.below().north(), Blocks.DIRT);
        whenBlock(world, pos.below().south(), Blocks.DIRT);
        whenBlock(world, pos.below().east(), Blocks.DIRT);
        whenBlock(world, pos.below().west(), Blocks.DIRT);
        // Tasks.java:126 if (!isEdge(world, pos) && level != FluidLevel.FLOW7) {
        // This evaluates to (true && true)
        Assertions.assertFalse(tasks.maybeErodeEdge(world, bsWater, pos, rand, FluidLevel.FLOW1));
    }

    @Test
    void testOnlyEdgesNotAir() {
        final Level world = mock(Level.class, levelSettings);
        final BlockPos pos = new BlockPos(10, 0, 0);
        final RandomSource rand = mock(RandomSource.class);
        final BlockState bsWater = getWaterState(FluidLevel.FLOW1);

        when(rand.nextInt(anyInt())).thenReturn(0);
        whenBlock(world, pos, Blocks.WATER);
        // Dirt will not becomeair, when nextInt == 0
        whenBlock(world, pos.below(), Blocks.DIRT);
        // isEdge() checks north first
        whenBlock(world, pos.below().north(), Blocks.WATER);
        Assertions.assertTrue(tasks.maybeErodeEdge(world, bsWater, pos, rand, FluidLevel.FLOW1));
    }

    @Test
    void testToAirWithAirAbove() {
        final Level world = mock(Level.class, levelSettings);
        final BlockPos pos = new BlockPos(10, 0, 0);
        final RandomSource rand = mock(RandomSource.class);
        final BlockState bsWater = getWaterState(FluidLevel.FLOW1);

        when(rand.nextInt(anyInt())).thenReturn(0);
        whenBlock(world, pos, Blocks.WATER);
        whenBlock(world, pos.below(), Blocks.CLAY); // Always to air
        whenBlock(world, pos.below().north(), Blocks.WATER);
        whenBlock(world, pos.above(), Blocks.AIR);
        Assertions.assertTrue(tasks.maybeErodeEdge(world, bsWater, pos, rand, FluidLevel.FLOW1));
    }

    @Test
    @SuppressWarnings("null")
    void testToAirWithWater() {
        final Level world = mock(Level.class, levelSettings);
        final BlockPos pos = new BlockPos(10, 0, 0);
        final RandomSource rand = mock(RandomSource.class);
        final BlockState bsWater = getWaterState(FluidLevel.FLOW1);

        when(rand.nextInt(anyInt())).thenReturn(0);
        whenBlock(world, pos, Blocks.WATER);
        whenBlock(world, pos.below(), Blocks.CLAY); // Always to air
        whenBlock(world, pos.below().north(), Blocks.WATER);
        when(world.getBlockState(pos.above())).thenReturn(getWaterState(FluidLevel.FLOW1));
        whenBlock(world, pos.above().above(), Blocks.AIR);
        Assertions.assertTrue(tasks.maybeErodeEdge(world, bsWater, pos, rand, FluidLevel.FLOW1));
    }

}
