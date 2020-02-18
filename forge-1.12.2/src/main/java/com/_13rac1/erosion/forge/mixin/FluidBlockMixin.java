package com._13rac1.erosion.forge.mixin;

import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.PublicBlockLiquid;
//import net.minecraft.block.BlockStaticLiquid;
//import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.Block;

import net.minecraft.block.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.Vec3d;

import org.spongepowered.asm.mixin.Mixin;

import com._13rac1.erosion.common.ErosionWorld;
import com._13rac1.erosion.common.Tasks;

@Mixin(BlockLiquid.class)
public class FluidBlockMixin extends Block {
  private Tasks tasks = new Tasks();

  protected FluidBlockMixin(Material materialIn) {
    super(materialIn);
    super.needsRandomTick = true;
  }

  // Implement the ErosionWorld interface for the Forge API.
  class ForgeWorld implements ErosionWorld {
    private World world;

    public ForgeWorld(World world) {
      this.world = world;
    }

    public BlockState getBlockState(BlockPos pos) {
      return new BlockState(this.world.getBlockState(pos));
    }

    public Boolean setBlockState(BlockPos pos, BlockState newState, Integer flags) {
      // Unwrap the IBlockState from within the local implementation of BlockState
      IBlockState iState = newState.getIBlockState();
      return this.world.setBlockState(pos, iState, flags);
    }

    public Block getBlock(BlockPos pos) {
      return this.world.getBlockState(pos).getBlock();
    }

    public int getSeaLevel() {
      return this.world.getSeaLevel();
    }

    public Vec3d getFlowVelocity(BlockState state, BlockPos pos) {
      IBlockState iState = state.getIBlockState();
      return PublicBlockLiquid.getFlow(world, pos, iState);
    }

    public Boolean isFluidBlock(Block block) {
      return block instanceof BlockLiquid;
    }
  }

  @Override
  public boolean getTickRandomly() {
    // Default in Block returns the value of needsRandomTick, which is set true
    // during in the BlockLiquid for both Water and Lava, but BlockStaticLiquid
    // constructor for Lava only.
    return true;
  }

  // Must use @Override because randomTick() does not exist in BlockLiquid. I'm
  // surprised I haven't run into this using mixins with 1.14.4+ on Forge or
  // Fabric, but the code structure is different.
  // @Inject(method = "randomTick", at = @At("HEAD"))
  @Override
  public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random) {
    super.randomTick(worldIn, pos, state, random);

    if (this.material != Material.WATER) {
      return;
    }
    // System.out.println(state.getBlock().getLocalizedName());
    // System.out.println(pos);

    ForgeWorld forgeWorld = new ForgeWorld(worldIn);
    BlockState blockState = new BlockState(state);
    tasks.run(blockState, forgeWorld, pos, random);
  }
}
