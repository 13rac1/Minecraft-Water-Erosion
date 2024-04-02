package com._13rac1.erosion;

import java.nio.file.Path;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
  public static final String CATEGORY_GENERAL = "general";
  public static ForgeConfigSpec COMMON_CONFIG;

  public static ForgeConfigSpec.BooleanValue ERODE_FARMLAND;

  static {
    ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();

    COMMON_BUILDER.comment("General settings").push(CATEGORY_GENERAL);

    ERODE_FARMLAND = COMMON_BUILDER.comment("Enable erode and decay of Farmland").define("erodeFarmland", true);

    COMMON_BUILDER.pop();

    COMMON_CONFIG = COMMON_BUILDER.build();
  }

  public static void loadConfig(ForgeConfigSpec spec, Path path) {
    final CommentedFileConfig configData = CommentedFileConfig.builder(path).sync().autosave()
        .writingMode(WritingMode.REPLACE).build();

    configData.load();
    spec.setConfig(configData);
  }
}
