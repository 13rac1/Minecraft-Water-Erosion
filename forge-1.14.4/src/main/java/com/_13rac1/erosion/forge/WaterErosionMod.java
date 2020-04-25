package com._13rac1.erosion.forge;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("water-erosion")
public class WaterErosionMod {
  private static final Logger LOGGER = LogManager.getLogger();

  public WaterErosionMod() {
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

    ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);

    MinecraftForge.EVENT_BUS.register(this);

    Config.loadConfig(Config.COMMON_CONFIG, FMLPaths.CONFIGDIR.get().resolve("water-erosion.toml"));
  }

  private void setup(final FMLCommonSetupEvent event) {
    LOGGER.info("Water Erosion Enabled");
  }
}
