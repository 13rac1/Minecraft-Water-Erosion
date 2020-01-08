package com._13rac1.erosion.common;

// https://github.com/FabricMC/yarn/blob/8f3f55e2/mappings/net/minecraft/world/ModifiableWorld.mapping
public class BlockFlag {
  // Propagates a change event to surrounding blocks.
  public static final Integer PROPAGATE_CHANGE = 1;
  // Notifies listeners and clients who need to react when the block changes.
  public static final Integer NOTIFY_LISTENERS = 2;
  // Used in conjunction with NOTIFY_LISTENERS to suppress the render pass on
  // clients.
  public static final Integer NO_REDRAW = 4;
  // Forces a synchronous redraw on clients.
  public static final Integer REDRAW_ON_MAIN_THREAD = 8;
  // Bypass virtual blockstate changes and forces the passed state to be stored
  // as-is.
  public static final Integer FORCE_STATE = 16;
  // Prevents the previous block (container) from dropping items when destroyed.
  public static final Integer SKIP_DROPS = 32;
  // Signals that this is a mechanical update, usually caused by pistons moving
  // blocks.
  public static final Integer MECHANICAL_UPDATE = 64;
}
