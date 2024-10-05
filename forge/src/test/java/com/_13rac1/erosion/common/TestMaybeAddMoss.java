package com._13rac1.erosion.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

public class TestMaybeAddMoss extends TestTasksCommon {
    @BeforeAll
    static void beforeAll() throws Exception {
        // Bootstrap the whole world.
        FakeWorldVersion.init();
    }

    @Test
    @SuppressWarnings("null")
    void testBasic() {
        final Level world = mock(Level.class, levelSettings);
        final BlockPos pos = new BlockPos(0, 0, 0);
        final RandomSource rand = RandomSource.create(); // unused, in tests but required
        final BlockState water = getWaterState(FluidLevel.FLOW1);
        final BlockState cobblestone = Blocks.COBBLESTONE.defaultBlockState();

        // Found water
        when(world.getBlockState(any(BlockPos.class))).thenReturn(water);
        Assertions.assertFalse(tasks.maybeAddMoss(world, pos, rand));

        // Found cobble, which is DECAY_ALWAYS_ODDS, adds moss
        when(world.getBlockState(any(BlockPos.class))).thenReturn(cobblestone);
        Assertions.assertTrue(tasks.maybeAddMoss(world, pos, rand));
        verify(world).setBlockAndUpdate(any(BlockPos.class), eq(Blocks.MOSSY_COBBLESTONE.defaultBlockState()));
    }

    @Test
    @SuppressWarnings("null")
    void testCopyProperties() {
        final BlockState water = getWaterState(FluidLevel.FLOW1);
        final BlockState cobblestone = Blocks.COBBLESTONE_STAIRS.defaultBlockState();

        // Cobblestone does not have the property 'level' so assert exception
        // Disabled for compatibility with Immersive Weathering
        // assertThrows(UnsupportedOperationException.class, () ->
        // tasks.copyProperties(water, cobblestone));

        BlockState water2 = getWaterState(FluidLevel.FLOW2);
        assertEquals(FluidLevel.FLOW2, water2.getValue(LiquidBlock.LEVEL));

        water2 = tasks.copyProperties(water, water2);
        assertEquals(FluidLevel.FLOW1, water2.getValue(LiquidBlock.LEVEL));
    }
}
