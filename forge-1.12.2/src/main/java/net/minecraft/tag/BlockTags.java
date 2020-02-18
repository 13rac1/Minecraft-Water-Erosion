package net.minecraft.tag;

import java.util.Collections;
import java.util.Set;

import net.minecraft.init.Blocks;
import net.minecraft.block.Block;

public class BlockTags {
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
