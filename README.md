# Water Erosion Mod for Minecraft Java Edition

**Water Erosion makes the world evolve with streams and waterfalls.**

Find Forest brooks and Mountain creeks during your explorations. Dirt and sand
wash away to deepen the stream bed and cut banks into the environment. The
landscapes become more diverse and unique. Pass through a dry Desert valley one
day and find it flooded the next. A stream may even change direction to dry up
the original flow.

## Goal

Implement Erosion as though it were a regular Minecraft feature making the world
more dynamic. It should not break existing gameplay or enable cheating.

## Features

* Water flowing over edges erode blocks below.
* Water flowing into a block wall will dissolve the wall.
* Water flow ends (level 7) dissolve the block below them to dig holes deeper
  and potentially continue the flow.
* Water Sources dissolve blocks to the sides in the direction of air spaces.
* Erodable blocks include sand, gravel, clay, farmland, dirt, grass, mud, mud bricks, and,
  rarely, cobblestone and stone bricks.
* Erodable blocks have differing resistances to erosion depending on their
  density, but all erode over time.
* Blocks decay during erosion to weaker blocks before disappearing.
* Blocks decay upstream in stream beds.
* Cobblestone and Stone Bricks grow moss near water.

Decay hierarchy:

* Cobblestone -> Mossy Cobblestone -> Gravel
* Stone Bricks -> Mossy Stone Bricks -> Gravel
* Grass Path -> Grass -> Dirt -> Coarse Dirt -> Mud -> Gravel
* Gravel -> Sand -> Clay(rarely) -> Air/Water

## Media

[![Water Erosion Mod Playlist](https://i.imgur.com/azPKFFY.png)](https://www.youtube.com/watch?v=ZQbcPGYTRvA&list=PLFgkjwcnVWJXh0zbaWK0F6OyW_pZOFS9d)

* **[Screenshots](https://www.curseforge.com/minecraft/mc-mods/water-erosion/screenshots)**
* [Real Time Waterfall Erosion - High Speed Video](https://www.youtube.com/watch?v=ZQbcPGYTRvA)
* [Taiga Forest Creeks - High Speed Erosion](https://www.youtube.com/watch?v=N29mWO8NTOU)
* [Three Savanna Waterfalls - Erosion Complete](https://www.youtube.com/watch?v=Gi73OZ0hbqE)

## Install

Download from: https://www.curseforge.com/minecraft/mc-mods/water-erosion

Supported versions:

* Forge 1.20.x
* NeoForge 1.20.x
* Fabric 1.20.x

Previously Supported Versions:

* Fabric 1.14.4
* Fabric 1.15.2
* Fabric 1.16.x
* Fabric 1.18.x
* Fabric 1.19.x
* Forge 1.12.2
* Forge 1.14.4
* Forge 1.15.2
* Forge 1.16.x
* Forge 1.18.x
* Forge 1.19.x

## TODO features

* [1.17+ world gen removed hill/mountain aquifers, add them back](https://github.com/13rac1/Minecraft-Water-Erosion/issues/28)
* [Optionally drop items of eroded blocks](https://github.com/13rac1/Minecraft-Water-Erosion/issues/3)
* [Configurable Erosion odds and types - Slow, Medium(default), Fast, Custom](https://github.com/13rac1/Minecraft-Water-Erosion/issues/1)

## Test Areas

Create each world, teleport to the specified location, and, optionally, change
the `randomTickSpeed` setting to speed up the process.

```script
/teleport X Y Z
/gamerule randomTickSpeed NUMBER # Suggest 150
```

### Seed 4465334863609190468

Tested on Minecraft 1.20.1

* `-388 73 258` - A massive waterfall covers the side of a mountain peak in a swamp
* `-538 85 1133` - A waterfall fills the floor of a narrow forest crevice 
* `-638 72 1459` - A stream finds multiple routes through the trees in a forest

## Inspiration

* Reality
* Aquarela - Russian documentary about water

## Development

### Code structure

Common erosion code is in the `erosion-common` directory and symlinked into the Forge, NeoForge, and Fabric codebases.

### Build

The `Makefile` will build Forge, NeoForge, and Fabric versions

```bash
make
```

### Unit tests

Erosion functionality is unit tested within the Forge codebase, run the unit tests with:

```bash
make test
```

### Forge Dependencies

Forge dependency versions are specified in:

* `build.gradle`
* `src/main/resources/META-INF/mods.toml`

Check versions at: https://files.minecraftforge.net/net/minecraftforge/forge/promotions_slim.json

### Fabric Dependencies

Fabric dependency versions are specified in:

* `gradle.properties`
* `src/main/resources/fabric.mod.json`

Check versions at: https://fabricmc.net/develop/

### Technical Notes

This "core mod" adds code to the Water block `randomTick()` functions using the
[Mixin framework][MixinFramework]. The code and functionality is structured to
reduce system CPU use, but a CPU increase may still be noticed.

The Minecraft in-game UI shows the opposite water level value than the true block
metadata. Data `level==1` is displayed as Targeted Fluid level:7. Data `level==7`
is displayed as Targeted Fluid level:1.

`randomTick()` is only called by the Minecraft runtime for a 128 block radius
around the player, so the odds of erosion occuring are set fairly high. The odds
should be reduced if random tick distance or speed is increased.

Source water blocks at or below the world sea level are ignored to reduce CPU use.

[MixinFramework]: https://github.com/SpongePowered/Mixin

## License

Copyright 2020+ by Brad Erickson. Licensed GPLv3.
