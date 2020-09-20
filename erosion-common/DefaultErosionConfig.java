package com._13rac1.erosion.common;

public class DefaultErosionConfig implements IErosionConfig {

  @Override
  public Boolean GetErodeFarmLand() {
    // Default to enabled to erode farmland
    return true;
  }
}
