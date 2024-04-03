package com._13rac1.erosion.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com._13rac1.erosion.WaterErosionMod;
import com._13rac1.erosion.worldgen.ModBiomeModifiers;
import com._13rac1.erosion.worldgen.ModConfiguredFeatures;
import com._13rac1.erosion.worldgen.ModPlacedFeatures;

// tutorial: orge Modding Tutorial - Minecraft 1.20.1: Ore Generation | #38
// https://www.youtube.com/watch?v=GRJyJWtSnvQ

public class ModWorldGenProvider extends DatapackBuiltinEntriesProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, ModConfiguredFeatures::bootstrap)
            .add(Registries.PLACED_FEATURE, ModPlacedFeatures::bootstrap)
            .add(ForgeRegistries.Keys.BIOME_MODIFIERS, ModBiomeModifiers::bootstrap);

    public ModWorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(WaterErosionMod.MOD_ID));
    }
}
