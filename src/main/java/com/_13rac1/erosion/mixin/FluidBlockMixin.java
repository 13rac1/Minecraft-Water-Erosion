package com._13rac1.erosion.mixin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com._13rac1.erosion.ErodableBlocks;
import com._13rac1.erosion.FluidLevel;

// Reference:
// https://github.com/vktec/butterfly/blob/e8411285/src/main/java/uk/org/vktec/butterfly/mixin/FluidBlockMixin.java

// TODO: Turn upper edge blocks of pools to sand or add a mud block?

// TODO: Flowing water should have an extremely low chance of eroding cobblestone.

// TODO: Add Menu to allow disable of some features:
// https://www.curseforge.com/minecraft/mc-mods/modmenu

// TODO: Change Lake generation to create more water sources on hills
// https://fabricmc.net/wiki/tutorial:fluids

// TODO: Avoid carving channels in sand since it is affected by gravity.

@Mixin(FluidBlock.class)
public class FluidBlockMixin extends Block {
	public FluidBlockMixin(Settings settings) {
		super(settings);
	}

	@Override
	public boolean hasRandomTicks(BlockState state) {
		// Water only, not Lava.
		return state.getBlock() == Blocks.WATER;
	}

	// TODO: Use Inject or Override? @Override
	@Inject(method = "randomTick", at = @At("HEAD"), require = 1)
	private void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random rand, CallbackInfo info) {

		Integer level = state.get(FluidBlock.LEVEL);

		maybeSourceBreak(state, world, pos, rand, level);

		// Skip source blocks, only flowing water.
		if (level == FluidLevel.SOURCE) {
			return;
		}

		if (maybeFlowingWall(state, world, pos, rand, level)) {
			// Return if the flow breaks a wall.
			return;
		}

		maybeErodeEdge(state, world, pos, rand, level);
	}

	private void maybeErodeEdge(BlockState state, ServerWorld world, BlockPos pos, Random rand, Integer level) {
		// Get the block under us.
		BlockPos underPos = pos.down();
		BlockState underState = world.getBlockState(underPos);
		Block underBlock = underState.getBlock();

		// Return if the block below us is not erodable.
		Integer underResistance = ErodableBlocks.getErosionResistance(underBlock);
		if (underResistance == ErodableBlocks.MAX_RESISTANCE) {
			// System.out.println(underBlock.getName().asFormattedString());
			return;
		}
		// underResistance into percent chance of removal.
		if (rand.nextInt(ErodableBlocks.MAX_RESISTANCE) >= ErodableBlocks.MAX_RESISTANCE - underResistance) {
			return;
		}

		// Return if we are not a water edge block and not level 7. Level 7, the
		// last one, is allowed to dig down to extend the water flow.
		if (!this.isEdge(world, pos) && level != FluidLevel.FLOW7) {
			return;
		}

		// System.out.println("pos x:" + pos.getX() + " y:" + pos.getY() + " z:" +
		// pos.getZ() + " water level: " + level);
		System.out.println("Removing block: " + underBlock.getName().asFormattedString());
		// TODO: What is the ideal integer flag value?
		Integer underBlocklevel = level < FluidLevel.FALLING7 ? level + 1 : FluidLevel.FALLING7;
		world.setBlockState(underPos, Blocks.WATER.getDefaultState().with(FluidBlock.LEVEL, underBlocklevel), 3);

		// Don't delete source blocks
		if (state.get(FluidBlock.LEVEL) == FluidLevel.SOURCE) {
			return;
		}

		// Delete the water block

		// TODO: Maybe the water block itself shouldn't be deleted?
		world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);

		// Delete upwards until there's no water or a water source is found. The
		// goal is to lower the water level naturally without having to reflow the
		// entire stream/creek/river.
		BlockPos posUp = pos.up();
		while (world.getBlockState(posUp).getBlock() instanceof FluidBlock) {
			if (world.getBlockState(posUp).get(FluidBlock.LEVEL) == FluidLevel.SOURCE) {
				break;
			}
			// TODO: Go up until finding the last water block and delete that. The
			// rest are fine.
			world.setBlockState(posUp, Blocks.AIR.getDefaultState(), 3);
			posUp = posUp.up();
		}
		// TODO: Anything further to do since we are deleting ourself?
	}

	private boolean isEdge(ServerWorld world, BlockPos pos) {
		List<BlockPos> listSidePos = Arrays.asList(pos.north(), pos.south(), pos.east(), pos.west());

		for (BlockPos sidePos : listSidePos) {
			BlockPos underPos = sidePos.down();
			BlockState underState = world.getBlockState(underPos);
			Block underBlock = underState.getBlock();

			if (!(underBlock instanceof FluidBlock)) {
				// System.out.println("Did not find side under water block: " +
				// underBlock.getClass().getName());
				continue;
			}
			// System.out.println("Found side under water block");

			return true;
		}
		return false;
	}

	private boolean maybeFlowingWall(BlockState state, ServerWorld world, BlockPos pos, Random rand, Integer level) {
		if (level == FluidLevel.SOURCE || level > FluidLevel.FLOW6) {
			// level Flow7 goes down, never to the side.
			return false;
		}

		FluidBlock block = (FluidBlock) state.getBlock();
		FluidState fluidState = block.getFluidState(state);
		// Note: fluidState.method_20785() -> float getLevel().

		// Find flow direction: Velocity is a 3D vector normalized to 1 pointing the
		// direction the water is flowing.
		Vec3d velocity = fluidState.getVelocity(world, pos);

		if (Math.abs(velocity.x) < 1 && Math.abs(velocity.z) < 1) {
			// Skip 45 degree flows.
			//
			// The velocity vector is normalized, therefore 45 degree flows are
			// represented by two floats of +/- 0.707.
			return false;
		}

		// Find the position of the block in the flow direction.
		BlockPos flowPos = pos.add(new Vec3i(velocity.x, velocity.y, velocity.z));

		// Block above cannot be wood, keep trees standing on dirt.
		// TODO: Look more than one block up for wood.
		BlockPos aboveFlowPos = flowPos.up();
		BlockState aboveFlowState = world.getBlockState(aboveFlowPos);
		Block aboveFlowBlock = aboveFlowState.getBlock();
		if (BlockTags.LOGS.contains(aboveFlowBlock)) {
			return false;
		}
		// TODO: Do not remove an erodable block if the stack above is unsupported.
		// Search around the stack to confirm a block other than air and water is
		// touching it.

		BlockState flowState = world.getBlockState(flowPos);
		Integer flowResistance = ErodableBlocks.getErosionResistance(flowState.getBlock());
		if (flowResistance == ErodableBlocks.MAX_RESISTANCE) {
			// Skip unbreakable blocks
			return false;
		}

		// TODO: The block behind must have the same flow direction.

		// flowResistance into percent chance of removal.
		if (rand.nextInt(ErodableBlocks.MAX_RESISTANCE) >= ErodableBlocks.MAX_RESISTANCE - flowResistance) {
			return false;
		}

		System.out.println("Removing block to side:" + flowState.getBlock().getName().asFormattedString());
		world.setBlockState(flowPos, Blocks.AIR.getDefaultState(), 3);
		return true;
	}

	private static final int SOURCE_BREAKS_ABOVE_SEA_LEVEL = 2;

	private void maybeSourceBreak(BlockState state, ServerWorld world, BlockPos pos, Random rand, Integer level) {
		// Source blocks only.
		if (level != FluidLevel.SOURCE) {
			return;
		}
		// TODO: Break when there's less than three blocks to air

		// Skip blocks less than sea level+.
		if (pos.getY() < world.getSeaLevel() + SOURCE_BREAKS_ABOVE_SEA_LEVEL) {
			// System.out.println("Too Low" + pos.getY() + "sea:" +
			// world.getSeaLevel());
			return;
		}

		// Skip blocks without air above.
		Block upBlock = world.getBlockState(pos.up()).getBlock();
		if (upBlock != Blocks.AIR && upBlock != Blocks.CAVE_AIR) {
			// System.out.println("Not Surface Water:" +
			// world.getBlockState(pos.up()).getBlock().getName().asFormattedString());
			// System.out.println("Y:" + pos.getY() + "sea:" + world.getSeaLevel());
			return;
		}

		// Skip blocks already flowing
		FluidState posFluidState = state.getFluidState();
		Vec3d velocity = posFluidState.getVelocity(world, pos);
		if (velocity.length() > 0) {
			return;
		}
		// System.out.println("length:" + velocity.length());

		// Blocks near an erodable surface only.
		List<Vec3i> listDirection = Arrays.asList(new Vec3i(1, 0, 0), new Vec3i(-1, 0, 0), new Vec3i(0, 0, 1),
				new Vec3i(0, 0, -1));
		// Randomize the list each run.
		Collections.shuffle(listDirection);

		for (Vec3i dir : listDirection) {
			BlockPos sidePos = pos.add(dir);

			Block sideBlock = world.getBlockState(sidePos).getBlock();
			if (sideBlock == Blocks.WATER) {
				// Short circuit water blocks. Should save CPU as this will be the most
				// common result.
				continue;
			}

			Integer sideResistance = ErodableBlocks.getErosionResistance(sideBlock);
			if (sideResistance == ErodableBlocks.MAX_RESISTANCE) {
				// Skip unbreakable.
				continue;
			}
			// Found a breakable block in the direction.

			// Check forward 7, 14, etc for air. Check a level lower each seventh
			// block, to be sure the flow can make it to the selected block, rather
			// than just make a 7 block creek.
			boolean foundAir = true;
			int yDeeper = 0;
			// TODO: Should the odds of breakage increase when block 21 is clear?
			// TODO: Should all blocks in the potential route be checked?
			for (int airMultipler : Arrays.asList(7, 14)) {
				Vec3i airDirection = new Vec3i(dir.getX() * airMultipler, dir.getY() - yDeeper, dir.getZ() * airMultipler);
				// System.out.println("maybeairdir:" + airDirection);
				yDeeper++;
				BlockPos maybeAirPos = pos.add(airDirection);
				BlockState maybeAirState = world.getBlockState(maybeAirPos);
				// if (!maybeAirState.isAir()) {
				Block maybeAirBlock = maybeAirState.getBlock();
				if (maybeAirBlock != Blocks.AIR && maybeAirBlock != Blocks.CAVE_AIR) {
					foundAir = false;
					break;
				}
			}
			if (!foundAir) {
				// Skip if air was not found.
				continue;
			}

			// Check behind. Is there enough "pressure" to break a wall? More blocks
			// increases the odds, but there must be at least two in a row to avoid
			// breaking generated farms.
			int waterFound = 0;
			for (int waterMultipler : Arrays.asList(1, 2, 3)) {
				Vec3i waterDirection = new Vec3i(-dir.getX() * waterMultipler, dir.getY(), -dir.getZ() * waterMultipler);
				// System.out.println("maybewaterdir:" + waterDirection);
				BlockPos maybeWaterPos = pos.add(waterDirection);
				BlockState maybeWaterState = world.getBlockState(maybeWaterPos);
				// System.out.println("maybe water:" +
				// maybeWaterState.getBlock().getName().asFormattedString());
				if (maybeWaterState.getBlock() != Blocks.WATER) {
					// TODO: Must be source water block
					break;
				}
				waterFound++;
			}
			if (waterFound < 1) {
				// Skip if not enough sequential water blocks found behind.
				// System.out.println("not enough water");
				continue;
			}
			// TODO: Better odds for waterFound > 1.
			// BUG: waterFound must be greater than 3 for cobblestone to avoid breaking
			// stock village wells.
			// TODO: Make cobblestone breaking a specific option.

			// TODO: Check depth. Greater depth increases odds of a wall breakthrough.

			// flowResistance into percent chance of removal.
			if (rand.nextInt(ErodableBlocks.MAX_RESISTANCE) >= ErodableBlocks.MAX_RESISTANCE - sideResistance) {
				// Stop looking completely if flow fails.

				// TODO: Should this chance check occur earlier?
				return;
			}
			System.out.println(
					"Removing block to source side:" + world.getBlockState(sidePos).getBlock().getName().asFormattedString());
			world.setBlockState(sidePos, Blocks.AIR.getDefaultState(), 3);

			// Only process the first erodable side found.
			return;
		}
	}
}
