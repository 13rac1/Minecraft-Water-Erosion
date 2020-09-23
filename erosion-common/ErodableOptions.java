package com._13rac1.erosion.common;

import java.util.ArrayList;

import net.minecraft.block.Block;

/**
 * ErodableOptions describes block metadata which controls the odds of erosion
 * and what block it may decay to.
 */
public class ErodableOptions {
  Integer resistanceOdds;
  Block decayToBlock;
  ArrayList<Block> decayList;

  ErodableOptions(Integer resistanceOdds, Block decayToBlock) {
    this.resistanceOdds = resistanceOdds;
    this.decayToBlock = decayToBlock;
    this.decayList = new ArrayList<>();
  }

}
