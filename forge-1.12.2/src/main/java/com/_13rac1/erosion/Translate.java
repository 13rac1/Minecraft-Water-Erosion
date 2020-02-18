package com._13rac1.erosion;

import net.minecraft.block.BlockState;
import net.minecraft.block.state.IBlockState;

public class Translate {
  // Translate a Minecraft 1.12.2 IBlockState into a Minecraft 1.14.4+ code
  // compatible class.
  public static BlockState State(IBlockState iState) {
    return new BlockState(iState);
  }
}
