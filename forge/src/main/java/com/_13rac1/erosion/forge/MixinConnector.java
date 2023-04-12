package com._13rac1.erosion.forge;

import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

public class MixinConnector implements IMixinConnector {
  @Override
  public void connect() {
    System.out.println("WaterErosion: Invoking Mixin Connector");
    Mixins.addConfiguration("erosion.mixins.json");
    // if (FMLEnvironment.dist == Dist.CLIENT) {
    // Mixins.addConfiguration("erosion.mixins_client.json");
    // }
  }
}
