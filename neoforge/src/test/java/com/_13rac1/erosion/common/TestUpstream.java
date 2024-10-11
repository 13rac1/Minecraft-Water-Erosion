package com._13rac1.erosion.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com._13rac1.erosion.common.FluidLevel;
import com.mojang.serialization.MapCodec;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.world.level.block.state.properties.Property;

// Testing upstream code to be sure it works as expected.
public class TestUpstream {
    // Simple test to confirm BlockState is working as expected.
    @Test
    void testBlockState() {
        // NOTE 2024-10-09: Wow, this test broke in the 1.20.4 to 1.20.6 upgrade.

        // Create an empty Reference2ObjectArrayMap for properties
        Reference2ObjectArrayMap<Property<?>, Comparable<?>> properties = new Reference2ObjectArrayMap<>();

        // Create a MapCodec for BlockState
        MapCodec<BlockState> codec = MapCodec.unit(() -> null);

        final BlockState state = new BlockState(Blocks.AIR, properties, codec);

        Assertions.assertEquals(Blocks.AIR, state.getBlock());

        final Reference2ObjectArrayMap<Property<?>, Comparable<?>> propertiesWater = new Reference2ObjectArrayMap<>();
        propertiesWater.put(LiquidBlock.LEVEL, FluidLevel.SOURCE);
        final BlockState stateWater = new BlockState(Blocks.WATER, propertiesWater, codec);

        Assertions.assertEquals(Blocks.WATER, stateWater.getBlock());
        Assertions.assertEquals(FluidLevel.SOURCE, stateWater.getValue(LiquidBlock.LEVEL));
    }
}
