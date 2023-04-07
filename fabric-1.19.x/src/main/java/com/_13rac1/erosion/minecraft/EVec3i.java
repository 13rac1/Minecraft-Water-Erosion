package com._13rac1.erosion.minecraft;

import net.minecraft.core.Vec3i;

public class EVec3i {
  Vec3i vec;

  public EVec3i() {
    this.vec = new Vec3i(0, 0, 0);
  }

  public EVec3i(Vec3i vec) {
    this.vec = vec;
  }

  public EVec3i(int x, int y, int z) {
    this.vec = new Vec3i(x, y, z);
  }

  public EVec3i(double x, double y, double z) {
    this.vec = new Vec3i((int) x, (int) y, (int) z);
  }

  private Vec3i getVec() {
    return this.vec;
  }

  public int getX() {
    return this.vec.getX();
  }

  public int getY() {
    return this.vec.getY();
  }

  public int getZ() {
    return this.vec.getZ();
  }

  public EVec3i crossProduct(EVec3i vec) {
    return new EVec3i(this.vec.cross(this.vec));
  };
}
