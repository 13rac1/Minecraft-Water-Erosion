package com._13rac1.erosion.minecraft;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockState;
import net.minecraft.block.PublicBlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;

import com._13rac1.erosion.Translate;
import com._13rac1.erosion.common.IWorld;

public class EWorld implements IWorld {
  private World world;

  public EWorld(World world) {
    this.world = world;
  }

  public BlockState getBlockState(EBlockPos pos) {
    return Translate.State(this.world.getBlockState(pos.getPos()));
  }

  public Block getBlock(EBlockPos pos) {
    return this.world.getBlockState(pos.getPos()).getBlock();
  }

  public Boolean setBlockState(EBlockPos pos, BlockState newState, Integer flags) {
    return this.world.setBlockState(pos.getPos(), newState.getIBlockState(), flags);
  }

  public int getSeaLevel() {
    return this.world.getSeaLevel();
  }

  public EVec3d getFlowVelocity(BlockState state, EBlockPos pos) {
    IBlockState iState = state.getIBlockState();
    return new EVec3d(PublicBlockLiquid.getFlow(world, pos.getPos(), iState));
  }

  public Boolean isFluidBlock(Block block) {
    return block instanceof BlockLiquid;
  }

}
