package com._13rac1.erosion.common;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;

public class FakeBlock {
  // src: https://stackoverflow.com/a/3301720
  static void setFinalStatic(Field field, Object newValue) throws Exception {
    field.setAccessible(true);

    Field modifiersField = Field.class.getDeclaredField("modifiers");
    modifiersField.setAccessible(true);
    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    if (field.get(null) == null) {
      // Only set if the field is null, setupFakeBlocks() is called multiple times.
      field.set(null, newValue);
    }
  }

  // Create a fake block so the Block.class field is not null for that entry.
  public static void make(String blockName, Material material) throws Exception {
    Block.Properties properties = Block.Properties.create(material);
    Block block = new Block(properties);

    setFinalStatic(Blocks.class.getField(blockName), block); // Uppercase block name.
  }

  // Setup all blocks.NAME values in used in tests, because they are normally set
  // during Minecraft startup and therefore are null values, which means using
  // them in ErodableBlocks.getErodables() will cause a null exception.
  public static void setupFakeBlocks() throws Exception {
    make("AIR", Material.AIR);
    make("WATER", Material.WATER);

    make("CLAY", Material.CLAY);
    make("RED_SAND", Material.SAND);
    make("SAND", Material.SAND);
    make("GRAVEL", Material.MISCELLANEOUS);
    make("COARSE_DIRT", Material.MISCELLANEOUS);
    make("FARMLAND", Material.MISCELLANEOUS);
    make("DIRT", Material.MISCELLANEOUS);
    make("PODZOL", Material.MISCELLANEOUS);
    make("GRASS_BLOCK", Material.MISCELLANEOUS);
    make("GRASS", Material.MISCELLANEOUS);
    make("MOSSY_COBBLESTONE", Material.MISCELLANEOUS);
    make("MOSSY_COBBLESTONE_SLAB", Material.MISCELLANEOUS);
    make("MOSSY_COBBLESTONE_STAIRS", Material.MISCELLANEOUS);
    make("MOSSY_COBBLESTONE_WALL", Material.MISCELLANEOUS);
    make("COBBLESTONE", Material.MISCELLANEOUS);
    make("COBBLESTONE_SLAB", Material.MISCELLANEOUS);
    make("COBBLESTONE_STAIRS", Material.MISCELLANEOUS);
    make("COBBLESTONE_WALL", Material.MISCELLANEOUS);
    make("MOSSY_STONE_BRICKS", Material.MISCELLANEOUS);
    make("MOSSY_STONE_BRICK_SLAB", Material.MISCELLANEOUS);
    make("MOSSY_STONE_BRICK_STAIRS", Material.MISCELLANEOUS);
    make("MOSSY_STONE_BRICK_WALL", Material.MISCELLANEOUS);
    make("STONE_BRICKS", Material.MISCELLANEOUS);
    make("STONE_BRICK_SLAB", Material.MISCELLANEOUS);
    make("STONE_BRICK_STAIRS", Material.MISCELLANEOUS);
    make("STONE_BRICK_WALL", Material.MISCELLANEOUS);
  }
}
