package com._13rac1.erosion.common;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Assertions;
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

  @Test
  void testDecayTo() throws Exception {
    Block.Properties properties = Block.Properties.create(Material.AIR);
    Block block = new Block(properties);
    setFinalStatic(Blocks.class.getField("AIR"), block);

    Block.Properties properties2 = Block.Properties.create(Material.CLAY);
    Block block2 = new Block(properties2);
    setFinalStatic(Blocks.class.getField("CLAY"), block2);

    Assertions.assertEquals(Blocks.AIR, ErodableBlocks.decayTo(Blocks.CLAY));

  }
}
