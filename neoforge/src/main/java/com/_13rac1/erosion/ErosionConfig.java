package com._13rac1.erosion;

import com._13rac1.erosion.common.IErosionConfig;

// Implements IErosionConfig to translate from the Forge API to the Water Erosion Config
public class ErosionConfig implements IErosionConfig {
  @Override
  public Boolean GetErodeFarmLand() {
    return Config.ERODE_FARMLAND.get();
  }
}
