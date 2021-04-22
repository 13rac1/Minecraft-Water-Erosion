package com._13rac1.erosion.common;

// https://minecraft.gamepedia.com/Water#Block_states
public class FluidLevel {
  // Water source block.
  public static final Integer SOURCE = 0;

  // The distance from a water source or falling water block.
  public static final Integer FLOW1 = 1;
  public static final Integer FLOW2 = 2;
  public static final Integer FLOW3 = 3;
  public static final Integer FLOW4 = 4;
  public static final Integer FLOW5 = 5;
  public static final Integer FLOW6 = 6;
  public static final Integer FLOW7 = 7;

  // Falling water.
  // This level is equal to the falling water above, and if it's non-falling,
  // equal to 8 plus the level of the non-falling water above it.
  public static final Integer FALLING0 = 8;
  public static final Integer FALLING1 = 9;
  public static final Integer FALLING2 = 10;
  public static final Integer FALLING3 = 11;
  public static final Integer FALLING4 = 12;
  public static final Integer FALLING5 = 13;
  public static final Integer FALLING6 = 14;
  public static final Integer FALLING7 = 15;
}
