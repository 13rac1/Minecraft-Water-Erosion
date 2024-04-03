package com._13rac1.erosion.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.List;
import net.minecraft.data.worldgen.features.CaveFeatures;
import com._13rac1.erosion.WaterErosionMod;

@SuppressWarnings({ "deprecation", "null" })
public class ModConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_WATER_ORE_KEY = registerKey("water_ore");

    public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> context) {
        // RuleTest stoneReplaceable = new
        // TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);

        // List<OreConfiguration.TargetBlockState> overworldWaterOres = List
        // .of(OreConfiguration.target(stoneReplaceable,
        // Blocks.WATER.defaultBlockState()));

        // register(context, OVERWORLD_WATER_ORE_KEY, Feature.ORE,
        // new OreConfiguration(overworldWaterOres, 9));

        // As a lake???
        register(context,
                OVERWORLD_WATER_ORE_KEY,
                Feature.LAKE,
                new LakeFeature.Configuration(
                        BlockStateProvider.simple(Blocks.WATER.defaultBlockState()),
                        BlockStateProvider.simple(Blocks.SAND.defaultBlockState())));
    }

    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(WaterErosionMod.MOD_ID, name));
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(
            BootstapContext<ConfiguredFeature<?, ?>> context,
            ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}
