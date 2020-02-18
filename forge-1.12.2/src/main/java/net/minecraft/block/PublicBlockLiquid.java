package net.minecraft.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;

// Static method in the same package allows access to BlockLiquid.getFlow()
public class PublicBlockLiquid {
  public static Vec3d getFlow(IBlockAccess worldIn, BlockPos pos, IBlockState state) {
    BlockLiquid block = (BlockLiquid) state.getBlock();
    // getFlow() is protected, make it public.
    return block.getFlow(worldIn, pos, state);
  }

}
