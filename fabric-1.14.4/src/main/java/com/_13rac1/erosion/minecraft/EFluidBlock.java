package com._13rac1.erosion.minecraft;

import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.BaseFluid;

// DO NOT USE instaceof with this class. A Block will never be an instance of this class.

public class EFluidBlock extends FluidBlock {
  protected EFluidBlock(BaseFluid fluid, Settings settings) {
    super(fluid, settings);
  }
}
