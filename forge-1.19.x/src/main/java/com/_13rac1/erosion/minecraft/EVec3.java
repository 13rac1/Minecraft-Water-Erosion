package com._13rac1.erosion.minecraft;

import net.minecraft.world.phys.Vec3;

public class EVec3 {
  Vec3 vec;

  public EVec3(Vec3 vec) {
    this.vec = vec;
  }

  public EVec3(int x, int y, int z) {
    this.vec = new Vec3(x, y, z);
  }

  public EVec3(double x, double y, double z) {
    this.vec = new Vec3(x, y, z);
  }

  public double getX() {
    return this.vec.x;
  }

  public double getY() {
    return this.vec.y;
  }

  public double getZ() {
    return this.vec.z;
  }

  public double length() {
    return this.vec.length();
  }
}
