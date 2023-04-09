package com._13rac1.erosion.common;

import static org.mockito.Mockito.*;

import java.util.ArrayList;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockSettings;
import org.mockito.listeners.InvocationListener;
import org.mockito.listeners.MethodInvocationReport;
import org.mockito.quality.Strictness;

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
import net.minecraft.util.RandomSource;

public class TestTasks {
  private static Tasks tasks = new Tasks();

  // levelMockListener reports when returned values will be null
  class levelMockListener implements InvocationListener {
    @Override
    public void reportInvocation(MethodInvocationReport methodInvocationReport) {
      if (methodInvocationReport.getInvocation().getLocation().getSourceFile() == "TestTasks.java") {
        // Ignore self invocations such as using when()
        return;
      }

      if (methodInvocationReport.getReturnedValue() == null) {
        throw new UnsupportedOperationException("Unstubbed access: " +
            methodInvocationReport.getInvocation());
      }
    }
  }

  private MockSettings levelSettings = withSettings()
      .verboseLogging()
      .strictness(Strictness.STRICT_STUBS)
      .invocationListeners(new levelMockListener());

  // Helper to reduce code clutter while creating MutableBlockPos
  private BlockPos.MutableBlockPos newMutableBP(BlockPos pos) {
    return new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
  }

  // Helper to reduce clutter while describing blocks in a mock world.
  void whenBlock(Level world, int x, int y, int z, Block block) {
    final BlockPos pos = new BlockPos(x, y, z);
    when(world.getBlockState(pos)).thenReturn(block.defaultBlockState());
  }

  void whenBlock(Level world, BlockPos pos, Block block) {
    when(world.getBlockState(pos)).thenReturn(block.defaultBlockState());
  }

  // Helper to reduce clutter to confirm access of blocks in a mock world.
  void verifyBlock(Level world, int x, int y, int z) {
    final BlockPos pos = new BlockPos(x, y, z);
    verify(world).getBlockState(pos);
  }

  @BeforeAll
  static void beforeAll() throws Exception {
    // Bootstrap the whole world.
    FakeWorldVersion.init();
  }

  // Simple test to confirm BlockState is working as expected.
  @Test
  void testBlockState() {
    final MapCodec<BlockState> map = null;
    final BlockState state = new BlockState(Blocks.AIR, ImmutableMap.of(), map);

    Assertions.assertEquals(Blocks.AIR, state.getBlock());

    final ImmutableMap<Property<?>, Comparable<?>> propertiesWater = ImmutableMap.of(LiquidBlock.LEVEL,
        FluidLevel.SOURCE);
    final BlockState stateWater = new BlockState(Blocks.WATER, propertiesWater, map);

    Assertions.assertEquals(Blocks.WATER, stateWater.getBlock());
    Assertions.assertEquals(FluidLevel.SOURCE, stateWater.getValue(LiquidBlock.LEVEL));
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
  void testDistanceToAirWaterInFlowPath() {
    final Level world = mock(Level.class, levelSettings);
    final BlockPos startPos = new BlockPos(0, 0, 0);
    final Vec3i flowDir = new Vec3i(1, 0, 0); // South is positive

    // Check one block away in flow
    whenBlock(world, 0, 0, 0, Blocks.WATER);
    whenBlock(world, 1, 0, 0, Blocks.AIR);
    Assertions.assertEquals(1, tasks.distanceToAirWaterInFlowPath(world, startPos, flowDir, FluidLevel.SOURCE), 1);
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

  private BlockState getWaterState(int level) {
    final BlockState bs = Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, level);
    // IMPORTANT: BlockStates must have initCache() run to correctly set the private
    // fluidstate after the LEVEL value has been set.
    bs.initCache();
    return bs;
  }

  @Test
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
    Assertions.assertFalse(tasks.maybeDecayUnder(stateWater, world, pos, rand, level));

    // No decay of water on top of water.
    level = FluidLevel.FLOW1;
    when(world.getBlockState(pos.below())).thenReturn(water);
    Assertions.assertFalse(tasks.maybeDecayUnder(stateWater, world, pos, rand, level));

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
    Assertions.assertFalse(spyTask.maybeDecayUnder(stateWater, world, pos, rand, level));

    // No decay for 45 degree angles.
    when(world.getBlockState(pos.below())).thenReturn(cobblestone);
    doReturn(degree45).when(spyTask).getFlowVelocity(any(Level.class), any(BlockPos.class), any(BlockState.class));
    Assertions.assertFalse(spyTask.maybeDecayUnder(stateWater, world, pos, rand,
        FluidLevel.FLOW1));

    // Decay dirt
    when(world.getBlockState(pos.below())).thenReturn(dirt);
    Vec3 south = new Vec3(0.0, 0, 1);
    doReturn(south).when(spyTask).getFlowVelocity(any(Level.class), any(BlockPos.class), any(BlockState.class));
    when(world.getBlockState(pos.below().south())).thenReturn(sand);
    Assertions.assertTrue(spyTask.maybeDecayUnder(stateWater, world, pos, rand,
        FluidLevel.FLOW1));

  }

  @Test
  void testMaybeAddMoss() {
    final Level world = mock(Level.class, levelSettings);
    final BlockPos pos = new BlockPos(0, 0, 0);
    final RandomSource rand = RandomSource.create(); // unused, in tests but required
    final BlockState water = Blocks.WATER.defaultBlockState();
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
