package com._13rac1.erosion.common;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;

public class TestMaybeFlowingWall extends TestTasksCommon {

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
    void testOnlyWallBreakers() {
        final Level world = mock(Level.class, levelSettings);
        final BlockPos pos = new BlockPos(10, 0, 0);
        final RandomSource rand = mock(RandomSource.class);
        final BlockState bsWater = getWaterState(FluidLevel.FALLING7);

        Assertions.assertFalse(tasks.maybeFlowingWall(world, bsWater, pos, rand, FluidLevel.FALLING7));
    }

    @Test
    void testNot45DegreeFlows() {
        final Level world = mock(Level.class, levelSettings);
        final BlockPos pos = new BlockPos(10, 0, 0);
        final RandomSource rand = mock(RandomSource.class);
        final BlockState bsWater = getWaterState(FluidLevel.FLOW1);
        final Tasks spyTask = spy(tasks);

        Vec3 degree45 = new Vec3(0.707, 0, 0.707);
        doReturn(degree45).when(spyTask).getFlowVelocity(any(Level.class), any(BlockPos.class), any(BlockState.class));
        Assertions.assertFalse(spyTask.maybeFlowingWall(world, bsWater, pos, rand, FluidLevel.FLOW1));
    }

    @Test
    void testShortestDistanceRequireSolidBlock() {
        final Level world = mock(Level.class, levelSettings);
        final BlockPos pos = new BlockPos(10, 0, 0);

        Vec3i dirForward = Direction.WEST.getNormal();
        whenBlock(world, pos.offset(dirForward), Blocks.AIR);
        Assertions.assertEquals(null,
                tasks.findShortestDirectionToAirOrWater(world, pos, FluidLevel.FALLING7, dirForward));
        whenBlock(world, pos.offset(dirForward), Blocks.WATER);
        Assertions.assertEquals(null,
                tasks.findShortestDirectionToAirOrWater(world, pos, FluidLevel.FALLING7, dirForward));
    }

    @Test
    void testShortestDistanceCannotErode() {
        final Level world = mock(Level.class, levelSettings);
        final BlockPos pos = new BlockPos(10, 0, 0);

        Vec3i dirForward = Direction.WEST.getNormal();
        // Forward
        whenBlock(world, pos.west(), Blocks.GOLD_BLOCK);
        // Right
        whenBlock(world, pos.north(), Blocks.GOLD_BLOCK);
        // Left
        whenBlock(world, pos.south(), Blocks.GOLD_BLOCK);
        Assertions.assertEquals(null,
                tasks.findShortestDirectionToAirOrWater(world, pos, FluidLevel.FLOW7, dirForward));
    }

    @Test
    void testShortestDistanceBasic() {
        final Level world = mock(Level.class, levelSettings);
        final BlockPos pos = new BlockPos(10, 0, 0);

        Vec3i dirForward = Direction.WEST.getNormal();
        // Forward
        whenBlock(world, pos.west(), Blocks.DIRT);
        // Stop treeInColumn check
        whenBlock(world, pos.west().above(), Blocks.AIR);
        // FluidLevel.FLOW6 means max flow of one and FLOW7 goes down.
        whenBlock(world, pos.west().below(), Blocks.AIR);
        // Right
        whenBlock(world, pos.north(), Blocks.GOLD_BLOCK);
        // Left
        whenBlock(world, pos.south(), Blocks.GOLD_BLOCK);

        Assertions.assertEquals(dirForward,
                tasks.findShortestDirectionToAirOrWater(world, pos, FluidLevel.FLOW6, dirForward));
    }

    @Test
    void testShortestDistanceAllThreeDirections() {
        final Level world = mock(Level.class, levelSettings);
        final BlockPos pos = new BlockPos(10, 0, 0);

        Vec3i dirForward = Direction.WEST.getNormal();
        // Forward
        whenBlock(world, pos.west(), Blocks.DIRT);
        // Stop treeInColumn check
        whenBlock(world, pos.west().above(), Blocks.AIR);
        whenBlock(world, pos.west().west(), Blocks.DIRT);
        whenBlock(world, pos.west().west().west(), Blocks.AIR);
        // Right
        whenBlock(world, pos.north(), Blocks.DIRT);
        whenBlock(world, pos.north().above(), Blocks.AIR);
        whenBlock(world, pos.north().north(), Blocks.DIRT);
        whenBlock(world, pos.north().north().north(), Blocks.AIR);
        // Left - Will be selected
        whenBlock(world, pos.south(), Blocks.DIRT);
        whenBlock(world, pos.south().above(), Blocks.AIR);
        whenBlock(world, pos.south().south(), Blocks.AIR);

        Assertions.assertEquals(tasks.dirTurnLeft(dirForward),
                tasks.findShortestDirectionToAirOrWater(world, pos, FluidLevel.FLOW1, dirForward));
    }

    @Test
    void testBreakWall() {
        final Level world = mock(Level.class, levelSettings);
        final BlockPos pos = new BlockPos(10, 0, 0);
        final RandomSource rand = mock(RandomSource.class);
        final BlockState bsWater = getWaterState(FluidLevel.FLOW1);
        final Tasks spyTask = spy(tasks);

        Vec3 west = Vec3.atLowerCornerOf(Direction.WEST.getNormal());
        doReturn(west).when(spyTask).getFlowVelocity(any(Level.class), any(BlockPos.class),
                any(BlockState.class));
        // Forward
        whenBlock(world, pos.west(), Blocks.DIRT);
        // Stop treeInColumn check
        whenBlock(world, pos.west().above(), Blocks.AIR);
        // Freedom
        whenBlock(world, pos.west().west(), Blocks.AIR);
        // Right
        whenBlock(world, pos.north(), Blocks.GOLD_BLOCK);
        // Left
        whenBlock(world, pos.south(), Blocks.GOLD_BLOCK);

        when(rand.nextInt(anyInt())).thenReturn(0);
        Assertions.assertTrue(spyTask.maybeFlowingWall(world, bsWater, pos, rand, FluidLevel.FLOW1));
    }
}
