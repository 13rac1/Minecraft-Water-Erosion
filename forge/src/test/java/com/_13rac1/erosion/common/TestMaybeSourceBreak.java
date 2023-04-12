package com._13rac1.erosion.common;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com._13rac1.erosion.common.Tasks.msb;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

public class TestMaybeSourceBreak extends TestTasksCommon {
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
    void testSourceBlocksOnly() {
        Assertions.assertEquals(msb.NOT_SOURCE, tasks.maybeSourceBreak(null, null, null, null, FluidLevel.FLOW1));
    }

    @Test
    void testBelowSeaLevel() {
        final Level world = mock(Level.class, levelSettings);
        final BlockPos pos = new BlockPos(0, 0, 0);
        doReturn(63).when(world).getSeaLevel();

        Assertions.assertEquals(msb.BELOW_SEA_LEVEL, tasks.maybeSourceBreak(world, null, pos, null, FluidLevel.SOURCE));
    }

    @Test
    void testNotSurfaceWater() {
        final Level world = mock(Level.class, levelSettings);
        final BlockPos pos = new BlockPos(0, 64, 0);
        doReturn(63).when(world).getSeaLevel();
        whenBlock(world, new BlockPos(0, 65, 0), Blocks.WATER);

        Assertions.assertEquals(msb.NOT_SURFACE_WATER,
                tasks.maybeSourceBreak(world, null, pos, null, FluidLevel.SOURCE));
    }

    @Test
    void testAlreadyFlowing() {
        final Level world = mock(Level.class, levelSettings);
        final BlockPos pos = new BlockPos(0, 64, 0);
        final BlockState bsWater = getWaterState(FluidLevel.SOURCE);
        doReturn(63).when(world).getSeaLevel();
        whenBlock(world, new BlockPos(0, 65, 0), Blocks.AIR);
        final Tasks spyTask = spy(tasks);

        Vec3 degreeFlowing = new Vec3(1, 0, 0);
        doReturn(degreeFlowing).when(spyTask).getFlowVelocity(any(Level.class), any(BlockPos.class),
                any(BlockState.class));
        Assertions.assertEquals(msb.ALREADY_FLOWING,
                spyTask.maybeSourceBreak(world, bsWater, pos, null, FluidLevel.SOURCE));
    }

    @Test
    void testSourceBreakSuccess() {
        final Level world = mock(Level.class, levelSettings);
        final RandomSource rand = mock(RandomSource.class);
        final BlockPos pos = new BlockPos(0, 64, 0);
        final BlockState bsWater = getWaterState(FluidLevel.SOURCE);
        doReturn(63).when(world).getSeaLevel();
        whenBlock(world, new BlockPos(0, 65, 0), Blocks.AIR);
        final Tasks spyTask = spy(tasks);

        // Not flowing
        Vec3 degreeFlowing = new Vec3(0, 0, 0);
        doReturn(degreeFlowing).when(spyTask).getFlowVelocity(any(Level.class), any(BlockPos.class),
                any(BlockState.class));

        // ErodableBlocks.maybeErode => True
        when(rand.nextInt(anyInt())).thenReturn(0);
        // Dirt in front, then air so
        whenBlock(world, pos.north(), Blocks.DIRT);
        whenBlock(world, pos.north().north(), Blocks.AIR);
        // Water for "pressure" then dirt behind to stop checks
        whenBlock(world, pos.south(), Blocks.WATER);
        whenBlock(world, pos.south().south(), Blocks.DIRT);
        Assertions.assertEquals(msb.SUCCESS,
                spyTask.maybeSourceBreak(world, bsWater, pos, rand, FluidLevel.SOURCE));
    }

}
