package com._13rac1.erosion.minecraft;

import net.minecraft.block.Block;
import net.minecraft.tag.Tag;
import net.minecraft.tag.BlockTags;

// Sort of extends net.minecraft.tags.BlockTags, but the original BlockTags is
// final and access transformers are not working within vscode. They work fine
// during compile, but just not during development.
public class EBlockTags {
  public static final Tag<Block> LOGS = BlockTags.LOGS;
  public static final Tag<Block> LEAVES = BlockTags.LEAVES;
  public static final Tag<Block> SAND = BlockTags.SAND;
  public static final Tag<Block> WOOL = BlockTags.WOOL;
}
