package com._13rac1.erosion.minecraft;

import net.minecraft.util.math.vector.Vector3d;

public class EVec3d {
  Vector3d vec;

  public EVec3d(Vector3d vec) {
    this.vec = vec;
  }

  public EVec3d(int x, int y, int z) {
    this.vec = new Vector3d(x, y, z);
  }

  public EVec3d(double x, double y, double z) {
    this.vec = new Vector3d(x, y, z);
  }

  public double getX() {
    return this.vec.getX();
  }

  public double getY() {
    return this.vec.getY();
  }

  public double getZ() {
    return this.vec.getZ();
  }

  public double length() {
    return this.vec.length();
  }
}
