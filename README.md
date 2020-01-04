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

## Open Questions

* Should eroded/dissolved blocks drop items?
* Should odds be adjusted for balancing?

## Limitations

This is currently only a Fabric mod, because SpongeForge [does not support
1.14.x or newer][SpongeForge2019-12] yet. Forge support will be added when
possible, but functionality will not be backported to Minecraft 1.12.x.

Water blocks at or below the world sea level are ignored to reduce CPU use.

## Technical Notes

This "core mod" adds code to the Water block `randomTick()` functions using the
[Mixin framework][MixinFramework]. The code and functionality is structured to
use the least possible system CPU, but a CPU increase may still be noticed.

[MixinFramework]:(https://github.com/SpongePowered/Mixin)
[SpongeForge2019-12]:(https://forums.spongepowered.org/t/sponge-status-update-12th-december-2019/34368)

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

## Build

```bash
./gradlew build
```

## Notes

Forge build (not yet supported) will require Java 8:

```bash
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
```

## License

### Water Erosion Mod for Minecraft

Copyright 2019 by Brad Erickson. Licensed GPL.

### Fabric Template

Copyright 2019 by the Fabric Minecraft Modding Toolchain authors. Licensed CC0.
