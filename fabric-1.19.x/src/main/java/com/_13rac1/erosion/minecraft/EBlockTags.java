package com._13rac1.erosion.minecraft;

import net.minecraft.world.level.block.Block;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;

// Sort of extends net.minecraft.tags.BlockTags, but the original BlockTags is
// final and access transformers are not working within vscode. They work fine
// during compile, but just not during development.

public class EBlockTags {
  public static final TagKey<Block> LOGS = BlockTags.LOGS;
  public static final TagKey<Block> LEAVES = BlockTags.LEAVES;
  public static final TagKey<Block> SAND = BlockTags.SAND;
  public static final TagKey<Block> WOOL = BlockTags.WOOL;
}
