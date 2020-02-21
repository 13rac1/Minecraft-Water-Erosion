package com._13rac1.erosion.forge;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = WaterErosionMod.MODID, name = WaterErosionMod.NAME, version = WaterErosionMod.VERSION)
public class WaterErosionMod {
  public static final String MODID = "water-erosion";
  public static final String NAME = "Water Erosion";
  public static final String VERSION = "1.1.0-alpha1";

  public static Logger logger;

  @EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    logger = event.getModLog();
  }

  @EventHandler
  public void init(FMLInitializationEvent event) {
    logger.info("Water Erosion Enabled");
  }
}