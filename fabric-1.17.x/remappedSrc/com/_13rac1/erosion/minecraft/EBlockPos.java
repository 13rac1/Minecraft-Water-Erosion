package com._13rac1.erosion.minecraft;

import net.minecraft.util.math.BlockPos;

public class EBlockPos {
  private BlockPos pos;

  public EBlockPos(BlockPos pos) {
    this.pos = pos;
  }

  public EBlockPos(int x, int y, int z) {
    this.pos = new BlockPos(x, y, z);
  }

  public BlockPos getPos() {
    // TODO: Make private?
    return this.pos;
  }

  public EBlockPos up() {
    return new EBlockPos(this.pos.up());
  }

  public EBlockPos down() {
    return new EBlockPos(this.pos.down());
  }

  public EBlockPos north() {
    return new EBlockPos(this.pos.north());
  }

  public EBlockPos south() {
    return new EBlockPos(this.pos.south());
  }

  public EBlockPos east() {
    return new EBlockPos(this.pos.east());
  }

  public EBlockPos west() {
    return new EBlockPos(this.pos.west());
  }

  public EBlockPos add(EVec3i value) {
    return new EBlockPos(this.pos.add(value.vec));
  }

  public int getX() {
    return pos.getX();
  }

  public int getY() {
    return pos.getY();
  }

  public int getZ() {
    return pos.getZ();
  }

  // Overriding equals() to compare two EBlockPos objects
  // ref: https://www.geeksforgeeks.org/overriding-equals-method-in-java/
  // Required for the unit tests which currently only appear in Forge-1.14.4
  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }

    if (!(o instanceof EBlockPos)) {
      return false;
    }

    EBlockPos bp = (EBlockPos) o;

    // Compare wrapped BlockPos objects instead.
    return pos.equals(bp.pos);
  }

}
