package com._13rac1.erosion.minecraft;

import net.minecraft.util.math.Vec3d;

public class EVec3d {
  Vec3d vec;

  public EVec3d(Vec3d vec) {
    this.vec = vec;
  }

  public EVec3d(int x, int y, int z) {
    this.vec = new Vec3d(x, y, z);
  }

  public EVec3d(double x, double y, double z) {
    this.vec = new Vec3d(x, y, z);
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
