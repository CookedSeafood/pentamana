# Pentamana

Pentamana is a library adds non-registered mana status for living entities.

![Compound](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/compound.png) **data**: Parent tag.  
&ensp;|- ![Float](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/float.png) **mana**: Any  
&ensp;\\- ![Float](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/float.png) **mana_capacity**: Any

![Manabar.png](https://cdn.modrinth.com/data/UgFKzdOy/images/f3cd7c3f727aa816cea812bd654485bc743dda71.png)

## Configuration

`config/pentamana/server.json`:

```json
{
  "manaCapacityBase": 20.0,
  "manaRegenerationBase": 0.0625
}
```

`config/pentamana/client.json`:

```json
{
  "manabarMaxStars": 20
}
```

## Command

### Server Command

The commands below require premission level 2 to execute.

- `/mana get` Print mana supply. Returns mana supply in point.
- `/mana set` Set mana supply. Returns modified mana supply in point.
- `/mana add` Add mana supply. Returns modified mana supply in point.
- `/mana subtract` Subtract mana supply. Returns modified mana supply in point.
- `/pentamana reload` Reload server config file.
- `/pentamana set ... <...>` Set server config and save to config file.
- `/pentamana reset [...]` Set server config to default and save to config file.

### Client Command

- `/manabar set ... <...>` Set manabar config and save to config file.
- `/manabar reset [...]` Set manabar config to default and save to config file.

## Tutorial

Codes in this tutorial are licenced under CC-0.

### Installation

`gradle.properties`:

```properties
pentamana_version=2.0.0-alpha.9
```

`build.gradle`:

```gradle
repositories {
    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = "https://api.modrinth.com/maven"
            }
        }
        filter {
            includeGroup "maven.modrinth"
        }
    }
}

dependencies {
    modImplementation "maven.modrinth:pentamana:${project.pentamana_version}"
}
```

### Consume mana

```java
livingEntity.consumeMana(1.234f) // Return true if successful.
```

### Change how mana capacity is calculated

```java
ManaEvents.CALCULATE_CAPACITY.register((livingEntity, capacity) -> {
    float f = capacity.floatValue() // Base value.
    // TO-DO: Do your modifies to f.
    capacity.setValue(f);
    return InteractionResult.PASS;
})
```

All events:

- `ManaEvents.CALCULATE_CAPACITY`
- `ManaEvents.CALCULATE_REGENERATION`
- `ManaEvents.CALCULATE_CONSUMPTION`
- `ManaEvents.CALCULATE_DAMAGE`
