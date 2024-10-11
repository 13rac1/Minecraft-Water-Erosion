package com._13rac1.erosion;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;

import com.mojang.logging.LogUtils;

import com._13rac1.erosion.common.ErodableBlocks;

import org.slf4j.Logger;

/*
 * Reference: https://github.com/neoforged/MDK/blob/be6dc16a/src/main/java/com/example/examplemod/ExampleMod.java
 */

@Mod(WaterErosionMod.MODID)
public class WaterErosionMod {
  // Define mod id in a common place for everything to reference
  public static final String MODID = "watererosion";
  // Directly reference a slf4j logger
  private static final Logger LOGGER = LogUtils.getLogger();

  public WaterErosionMod(IEventBus modEventBus, ModContainer modContainer) {

    modEventBus.addListener(this::setup);

    modContainer.registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);

    Config.loadConfig(Config.COMMON_CONFIG, FMLPaths.CONFIGDIR.get().resolve("water-erosion.toml"));

    // Set the ErodableBlocks Config static to a new instance of the ErosionConfig.
    ErodableBlocks.Config = new ErosionConfig();
  }

  private void setup(final FMLCommonSetupEvent event) {
    LOGGER.info("Water Erosion Enabled");
  }
}
