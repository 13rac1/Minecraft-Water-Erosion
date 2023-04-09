package com._13rac1.erosion.common;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;

public class EWorld {
  private Level world;

  public EWorld(Level world) {
    this.world = world;
  }

  public BlockState getBlockState(BlockPos pos) {
    return this.world.getBlockState(pos);
  }

  public Boolean setBlockAndUpdate(BlockPos pos, BlockState newState) {
    return this.world.setBlockAndUpdate(pos, newState);
  }

  public int getSeaLevel() {
    return this.world.getSeaLevel();
  }

}
