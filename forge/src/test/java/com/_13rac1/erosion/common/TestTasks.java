package com._13rac1.erosion.common;

import static org.mockito.Mockito.*;

import java.util.ArrayList;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

@SuppressWarnings("null")
public class TestTasks extends TestTasksCommon {
  // Helper to reduce clutter while describing blocks in a mock world.
  private void whenBlock(Level world, int x, int y, int z, Block block) {
    final BlockPos pos = new BlockPos(x, y, z);
    final BlockState bs = block.defaultBlockState();
    bs.initCache();
    doReturn(bs).when(world).getBlockState(pos);
  }

  private void whenBlock(Level world, BlockPos pos, Block block) {
    final BlockState bs = block.defaultBlockState();
    bs.initCache();
    doReturn(bs).when(world).getBlockState(pos);
  }

  // Helper to reduce clutter to confirm access of blocks in a mock world.
  private void verifyBlock(Level world, int x, int y, int z) {
    final BlockPos pos = new BlockPos(x, y, z);
    verify(world).getBlockState(pos);
  }

  @BeforeAll
  static void beforeAll() throws Exception {
    // Bootstrap the whole world.
    FakeWorldVersion.init();
  }

  @Test
  void testDirLeftRight() {
    // Verbosely test both the Minecraft libraries and the Task methods work as
    // expected.
    BlockPos posStart = new BlockPos(0, 0, 0);
    Vec3i dirForward = Direction.NORTH.getNormal();
    BlockPos posForward = posStart.offset(dirForward);

    Assertions.assertNotEquals(posStart.south(), posForward);
    Assertions.assertEquals(posStart.north(), posForward);

    Vec3i dirLeft = dirForward.cross(Direction.DOWN.getNormal());
    BlockPos posLeft = posStart.offset(dirLeft);
    Assertions.assertEquals(posStart.west(), posLeft);
    Assertions.assertEquals(dirLeft, tasks.dirTurnLeft(dirForward));

    Vec3i dirRight = dirForward.cross(Direction.UP.getNormal());
    BlockPos posRight = posStart.offset(dirRight);
    Assertions.assertEquals(posStart.east(), posRight);
    Assertions.assertEquals(dirRight, tasks.dirTurnRight(dirForward));
  }

  @Test
  void testIsEdgeNorth() {
    // isEdge() checks north first, so this is just a test of the first return.
    final Level world = mock(Level.class, levelSettings);
    final BlockPos pos = new BlockPos(0, 0, 0);
    final BlockState water = Blocks.WATER.defaultBlockState();

    when(world.getBlockState(pos.north().below())).thenReturn(water);
    Assertions.assertTrue(tasks.isEdge(world, pos));
  }

  @Test
  void testDistanceToAirWaterInFlowPathSource() {
    final Level world = mock(Level.class, levelSettings);
    final BlockPos startPos = new BlockPos(0, 0, 0);
    final Vec3i flowDir = new Vec3i(1, 0, 0); // South is positive

    // Check one block away in flow
    whenBlock(world, 0, 0, 0, Blocks.WATER);
    whenBlock(world, 1, 0, 0, Blocks.AIR);
    Assertions.assertEquals(1, tasks.distanceToAirWaterInFlowPath(world, startPos, flowDir, FluidLevel.SOURCE));
    verifyBlock(world, 1, 0, 0);

    // Check to the end of the flow
    whenBlock(world, 0, 0, 0, Blocks.WATER);
    whenBlock(world, 1, 0, 0, Blocks.DIRT);
    whenBlock(world, 2, 0, 0, Blocks.DIRT);
    whenBlock(world, 3, 0, 0, Blocks.DIRT);
    whenBlock(world, 4, 0, 0, Blocks.DIRT);
    whenBlock(world, 5, 0, 0, Blocks.DIRT);
    whenBlock(world, 6, 0, 0, Blocks.DIRT);
    whenBlock(world, 7, 0, 0, Blocks.AIR);
    Assertions.assertEquals(7, tasks.distanceToAirWaterInFlowPath(world, startPos, flowDir, FluidLevel.SOURCE));
    verifyBlock(world, 7, 0, 0);

    // Check to the end of the flow and down one
    whenBlock(world, 7, 0, 0, Blocks.DIRT);
    whenBlock(world, 7, -1, 0, Blocks.AIR);
    Assertions.assertEquals(7, tasks.distanceToAirWaterInFlowPath(world, startPos, flowDir, FluidLevel.SOURCE));
    verifyBlock(world, 7, -1, 0);

    // Check to the end of the flow and down one
    whenBlock(world, 7, -1, 0, Blocks.DIRT);
    whenBlock(world, 8, -1, 0, Blocks.DIRT);
    whenBlock(world, 9, -1, 0, Blocks.DIRT);
    whenBlock(world, 10, -1, 0, Blocks.DIRT);
    whenBlock(world, 11, -1, 0, Blocks.DIRT);
    whenBlock(world, 12, -1, 0, Blocks.DIRT);
    whenBlock(world, 13, -1, 0, Blocks.DIRT);
    whenBlock(world, 14, -1, 0, Blocks.AIR);
    Assertions.assertEquals(14, tasks.distanceToAirWaterInFlowPath(world, startPos, flowDir, FluidLevel.SOURCE));
    verifyBlock(world, 14, -1, 0);

    // Check for fail when flow path is all dirt
    whenBlock(world, 14, -1, 0, Blocks.DIRT);
    Assertions.assertEquals(tasks.AIR_WATER_NOT_FOUND,
        tasks.distanceToAirWaterInFlowPath(world, startPos, flowDir, FluidLevel.SOURCE));

    // Check for fail when lower flow path is blocked by unerodable
    whenBlock(world, 13, -1, 0, Blocks.GOLD_BLOCK);
    Assertions.assertEquals(tasks.AIR_WATER_NOT_FOUND,
        tasks.distanceToAirWaterInFlowPath(world, startPos, flowDir, FluidLevel.SOURCE));

    // Check for fail when upper flow path is blocked by unerodable
    whenBlock(world, 1, 0, 0, Blocks.GOLD_BLOCK);
    Assertions.assertEquals(tasks.AIR_WATER_NOT_FOUND,
        tasks.distanceToAirWaterInFlowPath(world, startPos, flowDir, FluidLevel.SOURCE));

  }

  @Test
  void testDistanceToAirWaterInFlowPathFlow6() {
    final Level world = mock(Level.class, levelSettings);
    final BlockPos startPos = new BlockPos(0, 0, 0);
    final Vec3i flowDir = new Vec3i(1, 0, 0); // South is positive

    // Check one block away in flow
    whenBlock(world, 0, 0, 0, Blocks.WATER);
    whenBlock(world, 1, 0, 0, Blocks.DIRT);
    whenBlock(world, 1, -1, 0, Blocks.AIR);
    Assertions.assertEquals(1, tasks.distanceToAirWaterInFlowPath(world, startPos, flowDir, FluidLevel.FLOW6));
    verifyBlock(world, 1, 0, 0);
    verifyBlock(world, 1, -1, 0);
  }

  // Validate assumptions, does upstream FluidState.getFlow works as expected?
  @Test
  void testUpstreamGetFlow() {
    // Note: We cannot use verify() on world.getFluidState because getFlow() (Line
    // 55 in Forge 1.19.4) uses a mutating object, mockito stores the object
    // reference rather than a copy so the four direction calls to
    // level.getFluidState() are stored with the same Vec3 values, in this case West
    // because it is last.

    // Verified arguments can be mutated before they are verified:
    // https://github.com/Ravisbatta/mockito/issues/185

    final Level world = mock(Level.class, levelSettings);
    final FlowingFluid water = Fluids.FLOWING_WATER;
    final BlockPos pos = new BlockPos(10, 0, 0);
    BlockPos.MutableBlockPos bpNorth = newMutableBP(pos.north());
    BlockPos.MutableBlockPos bpSouth = newMutableBP(pos.south());
    BlockPos.MutableBlockPos bpEast = newMutableBP(pos.east());
    BlockPos.MutableBlockPos bpWest = newMutableBP(pos.west());

    // The following describes five water blocks, 3 source surrounding a FLOW1, with
    // a FLOW2 to the north. This should flow north.
    FluidState fsCurrent = water.getFlowing(FluidLevel.FLOW1, false);
    FluidState fsFlow2 = water.getFlowing(FluidLevel.FLOW2, false);
    FluidState fsSource = water.getSource(false);
    when(world.getFluidState(bpNorth)).thenReturn(fsFlow2);
    when(world.getFluidState(bpSouth)).thenReturn(fsSource);
    when(world.getFluidState(bpEast)).thenReturn(fsSource);
    when(world.getFluidState(bpWest)).thenReturn(fsSource);

    // Sure, we could just write the vector (0.0, 0.0, -1.0), but let's be sure.
    Vec3i iNormalNorth = Direction.NORTH.getNormal();
    Vec3 normalNorth = new Vec3(iNormalNorth.getX(), iNormalNorth.getY(), iNormalNorth.getZ());

    Assertions.assertEquals(normalNorth, fsCurrent.getFlow(world, pos));
    // We cannot verify calls to getFluidState, see note above.
  }

  // A near-redundant test compared to testUpstreamGetFlow(), but important
  // enough to test separtely. Confirms BlockState.initCache() works as expected.
  @Test
  void testGetFlowVelocity() {
    final Level world = mock(Level.class, levelSettings);
    final FlowingFluid water = Fluids.FLOWING_WATER;
    final BlockPos pos = new BlockPos(10, 0, 0);
    BlockPos.MutableBlockPos bpNorth = newMutableBP(pos.north());
    BlockPos.MutableBlockPos bpSouth = newMutableBP(pos.south());
    BlockPos.MutableBlockPos bpEast = newMutableBP(pos.east());
    BlockPos.MutableBlockPos bpWest = newMutableBP(pos.west());

    // The following describes five water blocks, 3 source surrounding a FLOW1, with
    // a FLOW2 to the east. This should flow east.

    final BlockState bsCurrent = getWaterState(FluidLevel.FLOW1);
    FluidState fsFlow2 = water.getFlowing(FluidLevel.FLOW2, false);
    FluidState fsSource = water.getSource(false);
    when(world.getFluidState(bpNorth)).thenReturn(fsSource);
    when(world.getFluidState(bpSouth)).thenReturn(fsSource);
    when(world.getFluidState(bpEast)).thenReturn(fsFlow2);
    when(world.getFluidState(bpWest)).thenReturn(fsSource);

    // Sure, we could just write the vector (1.0, 0.0, 0.0), but let's be sure.
    Vec3i iNormalEast = Direction.EAST.getNormal();
    Vec3 normalEast = new Vec3(iNormalEast.getX(), iNormalEast.getY(), iNormalEast.getZ());

    Assertions.assertEquals(normalEast, tasks.getFlowVelocity(world, pos, bsCurrent));
    // We cannot verify calls to getFluidState, see note above in
    // testUpstreamGetFlow().
  }

  @SuppressWarnings("deprecation")
  @Test
  void testTreeInColumn() {
    final Level world = mock(Level.class, levelSettings);
    final BlockPos pos = new BlockPos(10, 0, 0);

    // Test assumption: Are Oak logs tagged as logs?
    // No... because the registries aren't initialized.
    // Testing assumptions in case registries are enabled later
    Assertions.assertFalse(Blocks.OAK_LOG.defaultBlockState().is(BlockTags.LOGS));
    // So add it for the tests
    ArrayList<TagKey<Block>> tags = new ArrayList<TagKey<Block>>();
    tags.add(BlockTags.LOGS);
    // TODO: builtInRegistryHolder() is deprecated, what's the new method?
    Blocks.OAK_LOG.builtInRegistryHolder().bindTags(tags);
    // Now this works
    Assertions.assertTrue(Blocks.OAK_LOG.defaultBlockState().is(BlockTags.LOGS));
    // Blocks.OAK_LOG.defaultBlockState().getTags().toList() =>
    // [TagKey[minecraft:block / minecraft:logs]]

    whenBlock(world, pos, Blocks.DIRT);
    whenBlock(world, 10, 1, 0, Blocks.OAK_LOG);
    Assertions.assertTrue(tasks.treeInColumn(world, pos));

    whenBlock(world, 10, 1, 0, Blocks.AIR);
    Assertions.assertFalse(tasks.treeInColumn(world, pos));

    whenBlock(world, 10, 1, 0, Blocks.DIRT);
    whenBlock(world, 10, 2, 0, Blocks.DIRT);
    whenBlock(world, 10, 3, 0, Blocks.DIRT);
    whenBlock(world, 10, 4, 0, Blocks.DIRT);
    whenBlock(world, 10, 5, 0, Blocks.DIRT);
    Assertions.assertFalse(tasks.treeInColumn(world, pos));
  }

}
