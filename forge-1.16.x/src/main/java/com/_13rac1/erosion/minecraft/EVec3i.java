package com._13rac1.erosion.minecraft;

import net.minecraft.util.math.vector.Vector3i;

public class EVec3i {
  Vector3i vec;

  public EVec3i() {
    this.vec = new Vector3i(0, 0, 0);
  }

  public EVec3i(Vector3i vec) {
    this.vec = vec;
  }

  public EVec3i(int x, int y, int z) {
    this.vec = new Vector3i(x, y, z);
  }

  public EVec3i(double x, double y, double z) {
    this.vec = new Vector3i(x, y, z);
  }

  private Vector3i getVec() {
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
    return new EVec3i(this.vec.crossProduct(vec.getVec()));
  };
}
