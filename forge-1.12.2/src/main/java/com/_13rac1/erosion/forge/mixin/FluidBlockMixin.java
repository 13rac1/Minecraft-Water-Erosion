package com._13rac1.erosion.forge.mixin;

import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.Block;

import net.minecraft.block.BlockState;
import net.minecraft.block.state.IBlockState;

import org.spongepowered.asm.mixin.Mixin;

import com._13rac1.erosion.common.Tasks;
import com._13rac1.erosion.minecraft.EBlockPos;
import com._13rac1.erosion.minecraft.EWorld;

@Mixin(BlockLiquid.class)
public class FluidBlockMixin extends Block {
  private Tasks tasks = new Tasks();

  protected FluidBlockMixin(Material materialIn) {
    super(materialIn);
    super.needsRandomTick = true;
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

    EWorld forgeWorld = new EWorld(worldIn);
    BlockState blockState = new BlockState(state);
    tasks.run(blockState, forgeWorld, new EBlockPos(pos), random);
  }
}
