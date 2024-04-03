package com._13rac1.erosion;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com._13rac1.erosion.common.ErodableBlocks;

@Mod(WaterErosionMod.MOD_ID)
public class WaterErosionMod {
  public static final String MOD_ID = "watererosion";
  public static final Logger LOGGER = LogManager.getFormatterLogger();

  public WaterErosionMod() {
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

    ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);

    MinecraftForge.EVENT_BUS.register(this);

    Config.loadConfig(Config.COMMON_CONFIG, FMLPaths.CONFIGDIR.get().resolve("water-erosion.toml"));

    // Set the ErodableBlocks Config static to a new instance of the ErosionConfig.
    ErodableBlocks.Config = new ErosionConfig();
  }

  private void setup(final FMLCommonSetupEvent event) {
    LOGGER.info("Water Erosion Enabled");
  }
}
