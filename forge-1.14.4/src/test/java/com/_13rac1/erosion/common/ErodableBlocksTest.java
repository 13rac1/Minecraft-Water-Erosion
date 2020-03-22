package com._13rac1.erosion.common;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public class ErodableBlocksTest {

  @BeforeAll
  static void beforeAll() throws Exception {
    FakeBlock.setupFakeBlocks();
  }

  @Test
  void testGetResistance() {
    Assertions.assertEquals(0, ErodableBlocks.getResistance(Blocks.AIR));
    Assertions.assertEquals(20, ErodableBlocks.getResistance(Blocks.COBBLESTONE));
  }

  @Test
  void testCanErode() {
    Assertions.assertTrue(ErodableBlocks.canErode(Blocks.DIRT));
    Assertions.assertFalse(ErodableBlocks.canErode(Blocks.AIR));
  }

  @Test
  void testMaybeDecay() {
    Random rand = new Random();
    Assertions.assertEquals(Blocks.AIR, ErodableBlocks.maybeDecay(rand, Blocks.CLAY));
  }

  @Test
  void testDecayTo() {
    // Test an important decay hierarchy
    Assertions.assertEquals(Blocks.DIRT, ErodableBlocks.decayTo(Blocks.GRASS));
    Assertions.assertEquals(Blocks.COARSE_DIRT, ErodableBlocks.decayTo(Blocks.DIRT));
    Assertions.assertEquals(Blocks.GRAVEL, ErodableBlocks.decayTo(Blocks.COARSE_DIRT));
    Assertions.assertEquals(Blocks.SAND, ErodableBlocks.decayTo(Blocks.GRAVEL));
    Assertions.assertEquals(Blocks.CLAY, ErodableBlocks.decayTo(Blocks.SAND));
    Assertions.assertEquals(Blocks.AIR, ErodableBlocks.decayTo(Blocks.CLAY));
  }

  @Test
  void testCanSourceBreak() {
    // Test depends on current setting of SOURCE_BREAK_RESIST_ODDS
    Assertions.assertTrue(ErodableBlocks.canSourceBreak(Blocks.GRASS));
    Assertions.assertFalse(ErodableBlocks.canSourceBreak(Blocks.COBBLESTONE));
  }

  @Test
  void testGetDecayList() {
    // Check the decayList for Grass is returned in the expected order.
    List<Block> expected = Arrays.asList(Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.GRAVEL, Blocks.SAND, Blocks.CLAY);
    List<Block> actual = ErodableBlocks.getDecayList(Blocks.GRASS);

    System.out.println(expected);
    System.out.println(actual);

    Assertions.assertArrayEquals(expected.toArray(), actual.toArray());
  }
}
