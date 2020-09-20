package com._13rac1.erosion.common;

// The Water Erosion Config interface means the configuration can be provided by
// multiple Minecraft SDKs and versions without any SDK specific imports.

public interface IErosionConfig {
  // GetErodeFarmLand determines if Farm Land blocks can be eroded.
  public Boolean GetErodeFarmLand();
}
