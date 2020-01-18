# Water Erosion for Minecraft

Erosion brings water to life in Minecraft.

Worlds evolve with creeks, streams, and waterfalls as time progresses. Water
flows may change direction or even stop as the water erodes to stone.

## Features

* Water flowing over edges erode blocks below.
* Water flowing into a block wall will dissolve the wall.
* Water flow ends (level 7) dissolve the block below them to dig holes deeper
  and potentially continue the flow.
* Water Sources dissolve blocks to the sides in the direction of air spaces.
* Erodable blocks include sand, gravel, clay, farmland, dirt, grass, and,
  rarely, cobblestone.

## Videos

* [Taiga Forest Creeks](https://www.youtube.com/watch?v=N29mWO8NTOU)
* [Three Savanna Waterfalls](https://www.youtube.com/watch?v=Gi73OZ0hbqE)

## Installation

### Fabric versions

Download the current release `water-erosion-x.x.x-fabric-x.x.x.jar` and add to
your installation.

### Forge versions

The Forge version requires [MixinBootstrap][MixinBootstrap]. Download the
current release `water-erosion-x.x.x-forge-x.x.x.jar`, then download the
[`MixinBootstrap-1.0.0.jar`][MixinBootstrapJar]. Put both files in your
MinecraftForge Mod folder.

[MixinBootstrap]: https://github.com/LXGaming/MixinBootstrap
[MixinBootstrapJar]: https://github.com/LXGaming/MixinBootstrap/releases/download/v1.0.0/MixinBootstrap-1.0.0.jar

## Open Questions

* Should eroded/dissolved blocks drop items?
* Should blocks sometimes erode to lesser blocks before disappearing such as
  Dirt->Sand, Cobblestone->Gravel, Sand->Clay?

## TODO features

* Configurable Erosion odds

## Technical Notes

This "core mod" adds code to the Water block `randomTick()` functions using the
[Mixin framework][MixinFramework]. The code and functionality is structured to
use the least possible system CPU, but a CPU increase may still be noticed.

`randomTick()` is only called by the Minecraft runtime for a 128 block radius
around the player, so the odds of erosion occuring are set fairly high. The odds
should be reduced if random tick distance or speed is increased.

Source water blocks at or below the world sea level are ignored to reduce CPU use.

[MixinFramework]: https://github.com/SpongePowered/Mixin

## Test Areas

Create each world, teleport to the specified location, and, optionally, change
the `randomTickSpeed` setting to speed up the process.

```script
/teleport <X><Y><Z>
/gamerule randomTickSpeed <INT> # Suggest 100
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
* `4470 75 2071` - A large Mountain arch waterfall digs a pond. BUG: Shouldn't remove the entire water column, just the top.
* `3908 79 3276` - A Forest lake creates a 40 block creek to a river. BUG: May get blocked when a level7 block doesn't dig down in front.
* `4300 77 3511` - Three Plains lakes create a short river of rapids.

### Seed -103432684796306269

Tested on Minecraft 1.14.4

* `178 63 -22` - Creeks run through a dense Dark Forest

### Seed -4513252684046391402

Tested on Minecraft 1.15.1

* `1243 96 -520` - A naturally generated Savanna/Desert village spread across a sand dune and the top of a mountain full of monsters is surrounded by waterfalls

## Build

```bash
make # Builds all supported versions.
```

## License

### Water Erosion Mod for Minecraft

Copyright 2019 by Brad Erickson. Licensed GPL.

### Fabric Template

Copyright 2019 by the Fabric Minecraft Modding Toolchain authors. Licensed CC0.
