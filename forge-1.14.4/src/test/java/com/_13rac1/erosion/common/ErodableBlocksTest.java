package com._13rac1.erosion.common;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;

public class ErodableBlocksTest {
  // src: https://stackoverflow.com/a/3301720
  static void setFinalStatic(Field field, Object newValue) throws Exception {
    field.setAccessible(true);

    Field modifiersField = Field.class.getDeclaredField("modifiers");
    modifiersField.setAccessible(true);
    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

    field.set(null, newValue);
  }

  // Create a fake block so the Block.class field is not null for that entry.
  static void fakeBlock(String blockName, Material material) throws Exception {
    Block.Properties properties = Block.Properties.create(material);
    Block block = new Block(properties);

    setFinalStatic(Blocks.class.getField(blockName), block); // Uppercase block name.
  }

  // Setup all blocks.NAME values in used in tests, because they are normally set
  // during Minecraft startup and therefore are null values, which means using
  // them in ErodableBlocks.getErodables() will cause a null exception.
  @BeforeAll
  static void beforeAll() throws Exception {
    fakeBlock("AIR", Material.AIR);
    fakeBlock("WATER", Material.WATER);

    fakeBlock("CLAY", Material.CLAY);
    fakeBlock("RED_SAND", Material.SAND);
    fakeBlock("SAND", Material.SAND);
    fakeBlock("GRAVEL", Material.MISCELLANEOUS);
    fakeBlock("COARSE_DIRT", Material.MISCELLANEOUS);
    fakeBlock("FARMLAND", Material.MISCELLANEOUS);
    fakeBlock("DIRT", Material.MISCELLANEOUS);
    fakeBlock("PODZOL", Material.MISCELLANEOUS);
    fakeBlock("GRASS_BLOCK", Material.MISCELLANEOUS);
    fakeBlock("GRASS", Material.MISCELLANEOUS);
    fakeBlock("MOSSY_COBBLESTONE", Material.MISCELLANEOUS);
    fakeBlock("MOSSY_COBBLESTONE_SLAB", Material.MISCELLANEOUS);
    fakeBlock("MOSSY_COBBLESTONE_STAIRS", Material.MISCELLANEOUS);
    fakeBlock("MOSSY_COBBLESTONE_WALL", Material.MISCELLANEOUS);
    fakeBlock("COBBLESTONE", Material.MISCELLANEOUS);
    fakeBlock("COBBLESTONE_SLAB", Material.MISCELLANEOUS);
    fakeBlock("COBBLESTONE_STAIRS", Material.MISCELLANEOUS);
    fakeBlock("COBBLESTONE_WALL", Material.MISCELLANEOUS);
    fakeBlock("MOSSY_STONE_BRICKS", Material.MISCELLANEOUS);
    fakeBlock("MOSSY_STONE_BRICK_SLAB", Material.MISCELLANEOUS);
    fakeBlock("MOSSY_STONE_BRICK_STAIRS", Material.MISCELLANEOUS);
    fakeBlock("MOSSY_STONE_BRICK_WALL", Material.MISCELLANEOUS);
    fakeBlock("STONE_BRICKS", Material.MISCELLANEOUS);
    fakeBlock("STONE_BRICK_SLAB", Material.MISCELLANEOUS);
    fakeBlock("STONE_BRICK_STAIRS", Material.MISCELLANEOUS);
    fakeBlock("STONE_BRICK_WALL", Material.MISCELLANEOUS);
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
