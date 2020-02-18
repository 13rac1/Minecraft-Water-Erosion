package net.minecraft.block;

import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;

public class BlockState {
  private IBlockState state;

  // Wrap the IBlockState in a MC 1.14.4+ Compatibility Class
  public BlockState(IBlockState stateIn) {
    state = stateIn;
  }

  // Unwrap the IBlockState out of a MC 1.14.4+ Compatibility Class
  public IBlockState getIBlockState() {
    return state;
  }

  public Integer get(PropertyInteger Key) {
    return (Integer) state.getProperties().get(FluidBlock.LEVEL);
  }

  public Block getBlock() {
    return state.getBlock();
  }
}
