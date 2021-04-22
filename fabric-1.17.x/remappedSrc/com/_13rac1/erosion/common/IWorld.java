package com._13rac1.erosion.common;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import com._13rac1.erosion.minecraft.EBlockPos;
import com._13rac1.erosion.minecraft.EVec3d;

public interface IWorld {
  BlockState getBlockState(EBlockPos pos);

  Block getBlock(EBlockPos pos);

  Boolean setBlockState(EBlockPos pos, BlockState newState, Integer flags);

  int getSeaLevel();

  /**
   * Find flow direction: Velocity is a 3D vector normalized to 1 pointing the 2D
   * direction the water is flowing, ignoring up and down.
   */
  // Range of possible values include:
  // (-1.0, 0.0, 0.0)
  // (0.0, 0.0, -1.0)
  // (0.8944271606898969, 0.0, -0.44721361033295565)
  // (0.7071067613036184, 0.0, -0.7071067613036184)
  // (0.9999999664723898, 0.0, 0.0)
  // (0.3162277853996985, 0.0, -0.9486833137896449) - Rare
  EVec3d getFlowVelocity(BlockState state, EBlockPos pos);

  Boolean isFluidBlock(Block block);
}
