package com._13rac1.erosion.minecraft;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;

// DO NOT USE instaceof with this class. A Block will never be an instance of this class.

public class EFluidBlock extends net.minecraft.world.level.block.LiquidBlock {
  public EFluidBlock(FlowingFluid p_54694_, BlockBehaviour.Properties p_54695_) {
    super(p_54694_, p_54695_);
  }

}
