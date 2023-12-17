package com._13rac1.erosion.common;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class TestErodableBlocks {

  @BeforeAll
  static void beforeAll() throws Exception {
    FakeWorldVersion.init();
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
    RandomSource rand = RandomSource.create();
    Assertions.assertEquals(Blocks.AIR, ErodableBlocks.maybeDecay(rand, Blocks.CLAY));
  }

  @Test
  void testDecayTo() {
    // Test an important decay hierarchy
    Assertions.assertEquals(Blocks.DIRT, ErodableBlocks.decayTo(Blocks.SHORT_GRASS));
    Assertions.assertEquals(Blocks.COARSE_DIRT, ErodableBlocks.decayTo(Blocks.DIRT));
    Assertions.assertEquals(Blocks.GRAVEL, ErodableBlocks.decayTo(Blocks.COARSE_DIRT));
    Assertions.assertEquals(Blocks.SAND, ErodableBlocks.decayTo(Blocks.GRAVEL));
    Assertions.assertEquals(Blocks.CLAY, ErodableBlocks.decayTo(Blocks.SAND));
    Assertions.assertEquals(Blocks.AIR, ErodableBlocks.decayTo(Blocks.CLAY));
  }

  @Test
  void testCanSourceBreak() {
    // Test depends on current setting of SOURCE_BREAK_RESIST_ODDS
    Assertions.assertTrue(ErodableBlocks.canSourceBreak(Blocks.SHORT_GRASS));
    Assertions.assertFalse(ErodableBlocks.canSourceBreak(Blocks.COBBLESTONE));
  }

  @Test
  void testGetDecayList() {
    // Check the decayList for Grass is returned in the expected order.
    List<Block> expected = Arrays.asList(Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.GRAVEL, Blocks.SAND, Blocks.CLAY);
    List<Block> actual = ErodableBlocks.getDecayList(Blocks.SHORT_GRASS);

    System.out.println(expected);
    System.out.println(actual);

    Assertions.assertArrayEquals(expected.toArray(), actual.toArray());
  }
}
