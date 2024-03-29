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
* Erodable blocks include sand, gravel, clay, farmland, dirt, grass, and,
  rarely, cobblestone.
* Erodable blocks have differing resistances to erosion depending on their
  density, but all erode over time.
* Blocks decay during erosion to weaker blocks before disappearing.
* Blocks decay upstream in stream beds.
* Cobblestone and Stone Bricks grow moss near water.

Decay hierarchy:

* Cobblestone -> Mossy Cobblestone -> Gravel
* Stone Bricks -> Mossy Stone Bricks -> Gravel
* Grass Path -> Grass -> Dirt -> Coarse Dirt -> Gravel
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

Note: All test areas were found in pre-1.17 before the major world gen updates in
Caves & Cliffs, so new ones need to be located.

Create each world, teleport to the specified location, and, optionally, change
the `randomTickSpeed` setting to speed up the process.

```script
/teleport X Y Z
/gamerule randomTickSpeed NUMBER # Suggest 100
```

### Seed -1988839586448825536

Tested on Minecraft 1.15.1

* `1052 79 229` - A Savanna stream near a town flows into a crevasse.
* `1103 102 467` - A Savanna hilltop lake creates multiple waterfalls, which may
  change direction or dry up.
* `1303 80 579` - A Savanna underground lake breaks through the hillside to
  create a waterfall.
* `1309 89 753` - A Savanna source block flows through tunnel, then creates a
  small steam through trees to a pond.

### Seed 837828468367153798

Tested on Minecraft 1.15.1

* `683 92 163` - A Giant Tree Tiaga hilltop lake creates multiple creeks
  around a Pillager Outpost.
* `1424 89 7` - A Giant Tree Tiaga hilltop lake flows into an underground lake and to seaside wetlands.
* `1973 93 634` - A Mountain lake creates a 30 block tall set of waterfalls into a cave and a forest.
* `4378 70 2779` - A Tiaga underground lake flows out of both sides of a hill creating three waterfalls.
* `4406 75 2105` - Multiple Mountain arch waterfalls flow to the river.
* `3908 79 3276` - A Forest lake creates a 40 block creek to a river. BUG: May get blocked when a level7 block doesn't dig down in front.
* `4300 77 3511` - Three Plains lakes create a short river of rapids.

### Seed -103432684796306269

Tested on Minecraft 1.14.4

* `178 63 -22` - Creeks run through a dense Dark Forest

### Seed -4513252684046391402

Tested on Minecraft 1.15.1

* `1243 96 -520` - A naturally generated Savanna/Desert village spread across a
  sand dune and the top of a mountain full of monsters is surrounded by
  waterfalls

## Inspiration

* Reality
* Aquarela - Russian documentary about water

## Development

### Code structure

Common erosion code is in the `erosion-common` directory and symlinked into the Forge and Fabric codebases.

### Build

The `Makefile` will build both Forge and Fabric versions

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

Copyright 2020-2023 by Brad Erickson. Licensed GPL.
