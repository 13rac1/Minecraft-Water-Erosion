package com._13rac1.erosion.common;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TestMaybeDecayUnder extends TestTasksCommon {

    @BeforeAll
    static void beforeAll() throws Exception {
        // Bootstrap the whole world.
        FakeWorldVersion.init();
    }

    @Test
    @SuppressWarnings("null")
    void testMaybeDecayUnder() {
        final Level world = mock(Level.class, levelSettings);
        final BlockPos pos = new BlockPos(0, 0, 0);
        final BlockState stateWater = getWaterState(FluidLevel.FLOW1);
        final RandomSource rand = RandomSource.create(); // unused, in tests but required

        final BlockState water = Blocks.WATER.defaultBlockState();
        final BlockState dirt = Blocks.DIRT.defaultBlockState();
        final BlockState clay = Blocks.DIRT.defaultBlockState();
        final BlockState cobblestone = Blocks.COBBLESTONE.defaultBlockState();
        final BlockState sand = Blocks.SAND.defaultBlockState();

        // No decay under source blocks
        Integer level = FluidLevel.SOURCE;
        Assertions.assertFalse(tasks.maybeDecayUnder(world, stateWater, pos, rand, level));

        // No decay of water on top of water.
        level = FluidLevel.FLOW1;
        when(world.getBlockState(pos.below())).thenReturn(water);
        Assertions.assertFalse(tasks.maybeDecayUnder(world, stateWater, pos, rand, level));

        // Need a spyTask to mock out getFlowVelocity() returns
        // https://site.mockito.org/javadoc/current/org/mockito/Spy.html
        Tasks spyTask = spy(tasks);
        Vec3 degree0 = new Vec3(0, 0, 0);
        Vec3 degree45 = new Vec3(0.707, 0, 0.707);

        // Note, see docs: Must use doReturn() as when() calls the method.
        doReturn(degree45).when(spyTask).getFlowVelocity(any(Level.class), any(BlockPos.class), any(BlockState.class));
        // Confirm the spy works...
        Assertions.assertEquals(degree45, spyTask.getFlowVelocity(world, pos, stateWater));

        doReturn(degree0).when(spyTask).getFlowVelocity(any(Level.class), any(BlockPos.class), any(BlockState.class));
        // No decay if block will become air.
        level = FluidLevel.FLOW1;
        when(world.getBlockState(pos.below())).thenReturn(clay);
        Assertions.assertFalse(spyTask.maybeDecayUnder(world, stateWater, pos, rand, level));

        // No decay for 45 degree angles.
        when(world.getBlockState(pos.below())).thenReturn(cobblestone);
        doReturn(degree45).when(spyTask).getFlowVelocity(any(Level.class), any(BlockPos.class), any(BlockState.class));
        Assertions.assertFalse(spyTask.maybeDecayUnder(world, stateWater, pos, rand,
                FluidLevel.FLOW1));

        // Decay dirt
        when(world.getBlockState(pos.below())).thenReturn(dirt);
        Vec3 south = new Vec3(0.0, 0, 1);
        doReturn(south).when(spyTask).getFlowVelocity(any(Level.class), any(BlockPos.class), any(BlockState.class));
        when(world.getBlockState(pos.below().south())).thenReturn(sand);
        Assertions.assertTrue(spyTask.maybeDecayUnder(world, stateWater, pos, rand,
                FluidLevel.FLOW1));

    }
}
