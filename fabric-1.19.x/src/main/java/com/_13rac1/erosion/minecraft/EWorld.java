package com._13rac1.erosion.minecraft;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.Level;

import com._13rac1.erosion.common.IWorld;

public class EWorld implements IWorld {
  private Level world;

  public EWorld(Level world) {
    this.world = world;
  }

  public BlockState getBlockState(EBlockPos pos) {
    return this.world.getBlockState(pos.getPos());
  }

  public Block getBlock(EBlockPos pos) {
    return this.world.getBlockState(pos.getPos()).getBlock();
  }

  public Boolean setBlockAndUpdate(EBlockPos pos, BlockState newState) {
    return this.world.setBlockAndUpdate(pos.getPos(), newState);
  }

  public int getSeaLevel() {
    return this.world.getSeaLevel();
  }

  public EVec3 getFlowVelocity(BlockState state, EBlockPos pos) {
    FluidState fluidState = state.getFluidState();
    return new EVec3(fluidState.getFlow(this.world, pos.getPos()));
  }

  public Boolean isFluidBlock(Block block) {
    return block instanceof LiquidBlock;
  }

}
