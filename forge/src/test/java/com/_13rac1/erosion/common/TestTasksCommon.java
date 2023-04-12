package com._13rac1.erosion.common;

import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.mockito.MockSettings;
import org.mockito.listeners.InvocationListener;
import org.mockito.listeners.MethodInvocationReport;
import org.mockito.quality.Strictness;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.LiquidBlock;

public class TestTasksCommon {
    protected static Tasks tasks = new Tasks();

    // levelMockListener reports when returned values will be null
    class levelMockListener implements InvocationListener {
        private List<String> allowedCallers = Arrays.asList(
                "TestTasks.java",
                "TestMaybeErodeEdge.java",
                "TestMaybeFlowingWall.java",
                "TestMaybeSourceBreak.java");

        @Override
        public void reportInvocation(MethodInvocationReport methodInvocationReport) {
            final String caller = methodInvocationReport.getInvocation().getLocation().getSourceFile();
            if (allowedCallers.contains(caller)) {
                // Ignore self invocations such as using when()
                return;
            }

            if (methodInvocationReport.getReturnedValue() == null) {
                throw new UnsupportedOperationException(" Unstubbed access by " + caller + ": " +
                        methodInvocationReport.getInvocation());
            }
        }
    }

    protected MockSettings levelSettings = withSettings()
            .verboseLogging()
            .strictness(Strictness.STRICT_STUBS)
            .invocationListeners(new levelMockListener());

    // Helper to reduce code clutter while creating MutableBlockPos
    protected BlockPos.MutableBlockPos newMutableBP(BlockPos pos) {
        return new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
    }

    protected BlockState getWaterState(int level) {
        final BlockState bs = Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, level);
        // IMPORTANT: BlockStates must have initCache() run to correctly set the private
        // fluidstate after the LEVEL value has been set.
        bs.initCache();
        return bs;
    }
}
