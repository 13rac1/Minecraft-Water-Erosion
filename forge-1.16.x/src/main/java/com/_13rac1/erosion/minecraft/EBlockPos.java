package com._13rac1.erosion.minecraft;

import net.minecraft.util.math.BlockPos;

public class EBlockPos {
  private BlockPos pos;

  public EBlockPos(BlockPos pos) {
    this.pos = pos;
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

}
