package com._13rac1.erosion.minecraft;

import net.minecraft.block.Block;
import net.minecraft.tags.ITag;
import net.minecraft.tags.BlockTags;

// Sort of extends net.minecraft.tags.BlockTags, but the original BlockTags is
// final and access transformers are not working within vscode. They work fine
// during compile, but just not during development.
public class EBlockTags {
  public static final ITag.INamedTag<Block> LOGS = BlockTags.LOGS;
  public static final ITag.INamedTag<Block> LEAVES = BlockTags.LEAVES;
  public static final ITag.INamedTag<Block> SAND = BlockTags.SAND;
  public static final ITag.INamedTag<Block> WOOL = BlockTags.WOOL;
}
