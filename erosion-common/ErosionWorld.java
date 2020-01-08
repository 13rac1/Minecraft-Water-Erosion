package com._13rac1.erosion.common;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;

public interface ErosionWorld {
  BlockState getBlockState(BlockPos pos);

  Boolean setBlockState(BlockPos pos, BlockState newState, Integer flags);

  int getSeaLevel();

  Vec3d getFlowVelocity(BlockState state, BlockPos pos);
}
