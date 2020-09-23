package com._13rac1.erosion.minecraft;

import java.util.function.Supplier;

import net.minecraft.fluid.FlowingFluid;

// DO NOT USE instaceof with this class. A Block will never be an instance of this class.

public class EFluidBlock extends net.minecraft.block.FlowingFluidBlock {
  public EFluidBlock(Supplier<? extends FlowingFluid> supplier, Properties properties) {
    super(supplier, properties);
  }
}
