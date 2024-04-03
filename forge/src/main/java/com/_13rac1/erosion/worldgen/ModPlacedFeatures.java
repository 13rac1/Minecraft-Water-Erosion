package com._13rac1.erosion.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.heightproviders.TrapezoidHeight;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.EnvironmentScanPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.placement.SurfaceRelativeThresholdFilter;

import java.util.List;

import com._13rac1.erosion.WaterErosionMod;

public class ModPlacedFeatures {
    public static final ResourceKey<PlacedFeature> WATER_ORE_PLACED_KEY = registerKey("water_ore_placed");

    public static void bootstrap(BootstapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        // Attempt generation 10 times per chunk between sea level (62) and max iron ore
        // generation height (384).

        // register(context, WATER_ORE_PLACED_KEY,
        // configuredFeatures.getOrThrow(
        // ModConfiguredFeatures.OVERWORLD_WATER_ORE_KEY),
        // ModOrePlacement.commonOrePlacement(10,
        // HeightRangePlacement.uniform(VerticalAnchor.absolute(62),
        // VerticalAnchor.absolute(384))));

        // As Lake???
        PlacementUtils.register(context, WATER_ORE_PLACED_KEY,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.OVERWORLD_WATER_ORE_KEY),
                RarityFilter.onAverageOnceEvery(1),
                // InSquarePlacement.spread(),
                HeightRangePlacement.of(TrapezoidHeight.of(VerticalAnchor.absolute(63),
                        VerticalAnchor.absolute(123)))
        // EnvironmentScanPlacement.scanningFor(Direction.UP,
        // BlockPredicate.allOf(BlockPredicate.ONLY_IN_AIR_PREDICATE,
        // BlockPredicate.insideWorld(new BlockPos(0, -5, 0))),
        // 10)
        // SurfaceRelativeThresholdFilter.of(Heightmap.Types.WORLD_SURFACE_WG, -10, -2)
        );
    }

    private static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(WaterErosionMod.MOD_ID, name));
    }

    private static void register(BootstapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key,
            Holder<ConfiguredFeature<?, ?>> configuration,
            List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }
}
