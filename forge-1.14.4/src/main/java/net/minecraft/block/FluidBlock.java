package net.minecraft.block;

import java.util.function.Supplier;

import net.minecraft.fluid.FlowingFluid;

// DO NOT USE instanceof with this class. A Block will never be an instance of this class.

public class FluidBlock extends net.minecraft.block.FlowingFluidBlock {
  public FluidBlock(Supplier<? extends FlowingFluid> supplier, Properties properties) {
    super(supplier, properties);
  }
}
