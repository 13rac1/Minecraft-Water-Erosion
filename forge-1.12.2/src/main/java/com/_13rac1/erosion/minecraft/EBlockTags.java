package com._13rac1.erosion.minecraft;

import java.util.Collections;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

// Sort of extends net.minecraft.tags.BlockTags, but the original BlockTags is
// final and access transformers are not working within vscode. They work fine
// during compile, but just not during development.
public class EBlockTags {
  public static final Tag<Block> LOGS = new Tag<Block>(Blocks.AIR);
  public static final Tag<Block> LEAVES = new Tag<Block>(Blocks.LEAVES);
  public static final Tag<Block> SAND = new Tag<Block>(Blocks.SAND);
  public static final Tag<Block> WOOL = new Tag<Block>(Blocks.AIR);

  public static class Tag<T> {
    private final Set<T> taggedItems;

    // https://docs.oracle.com/javase/specs/jls/se7/html/jls-9.html#jls-9.6.3.7
    @SafeVarargs
    public Tag(T... inItems) {
      this.taggedItems = Collections.emptySet();
      for (T item : inItems) {

        // TODO: Complete blocktags implementation.
        // this.taggedItems.add(item);
      }
    }

    public boolean contains(T itemIn) {
      return this.taggedItems.contains(itemIn);
    }
  }
}
