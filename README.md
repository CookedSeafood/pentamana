# Pentamana

Pentamana is a scoreboard-based mana mod that runs server-side providing mana modification and damage calculation hooks.

## What is this?

Pentamana acts as a mana system, handling tasks such as mana regeneration and mana consumption. The goal is to reduce the time and effort required to add a server-side mana-consuming weapon. It use actionbar to display manabar.

## Enable this Mod for Yourself

`/mana enbale` to enable this mod for YOURSELF.

`/mana disable` to disable THIS mod for YOURSELF completely.

This mod is disbled for every player by default.

## Mana Mechanic

Each player starts with 33,554,431 mana capacity (1 star in the manabar). Mana capacity is maxed out at 2,147,483,647 mana, or 64 stars total. The mana capacity would be calculated by the formula below:

``` txt
Mana Capacity = 33554431 + Capacity Enchantment Level * 33554432
```

A player basicly regenerate 1,048,576 mana every tick (32 ticks per star). The mana regeneration amount per tick would be calculated by the formula below:

``` txt
Mana Regen = 1048576 + Stream Enchantment Level * 65536
```

The output magic damage from casting would be calculated by the formula below: (Needs self-implementation)

``` txt
Magic Damage = amount * (Mana Capacity / Mana Scale) + [0.5 + Potency Enchantment Level * 0.5](if potency presented)
```

## Enchantments

Enchantment damage for custom weapons would need additional apply.

### Capacity

- Maximum level: II
- Primary items: Stick
- Secondary items: Axe, Hoe, Mace, Pickaxe, Shovel, Sword, Trident
- Enchantment weight: 2

Capacity adds extra mana capacity 33,554,432 per level.

### Stream

- Maximum level: III
- Primary items: Stick
- Secondary items: Axe, Hoe, Mace, Pickaxe, Shovel, Sword, Trident
- Enchantment weight: 5

Stream adds extra mana regeneration 65,536 per level.

### Potency

- Maximum level: V
- Primary items: Stick
- Secondary items: Axe, Hoe, Mace, Pickaxe, Shovel, Sword, Trident
- Enchantment weight: 10

Potency adds 1 extra casting magic damage for the first level and 0.5 for all subsequent levels.

### Utilization

- Maximum level: V
- Primary items: Stick
- Secondary items: Axe, Hoe, Mace, Pickaxe, Shovel, Sword, Trident
- Enchantment weight: 5

Utilization reduces the mana cost of casting by 10% per level.

## Configuration

Below is a template config file `config/pentamana.json` filled with default values. Create the file yourself if you need config.

```json
{
  "manaScale": 16777216 // Amount of mana be considered as 1 point mana.
  "manaCapacityBase": 33554431 // Initial mana capacity, should be odd.
  "manaCapacityIncrementBase": 33554432 // eg. using for capacity enchantment, should be even.
  "manaRegenBase": 1048576 // Initial mana regen amount per tick.
  "manaRegenIncrementBase": 65536 // eg. using for stream enchantment
  "maxManabarLife": 40 // Ticks of actionbar updating suppression when interrupted
  "manaCharFull": "★" // 2 point mana
  "manaCharHalf": "⯪" // 1 point mana
  "manaCharZero": "☆" // 0 point mana
  "manaColor": "aqua" // The color of mana in actionbar
}
```

Enchantments are registed using datapack. You can open mod jar and edit it.

## Objectives

`pentamana.mana` Mana supply at last tick

`pentamana.mana_capacity` Mana capacity at last tick

`pentamana.mana_regen` Amount of mana regened at last tick

`pentamana.mana_consume` Amount of mana to consume

`pentamana.manabar_life` Ticks left till next update if idle

`pentamana.enabled` 1 if enabled.

## Events

- `TickManaCallback` Called after the mana capacity calculation, before everything else.

- `RegenManaCallback` Called when a player is regenerating mana. After the mana regeneration calculation, before regenerating mana.

- `ConsumeManaCallback` Called when a player is consuming mana. After the mana consumption calculation, before consuming mana.

## Tutorial: Create your very own magic weapon

This tutorial assumes that you already have a method that will be called when the weapon is used.

First, set the mana the weapon consumes. For example, 2000.

```java
ServerCommandSource source = player.getServerCommandSource();
ManaCommand.executeSetManaConsume(source, 2000)
```

Second, consume the mana and fire your weapon if successful consumed.

```java
if (ManaCommand.executeConsume(source) == 0) {
  return;
}

// Your code here
```

The result code will look like this:

```java
public void UseExampleWeapon(ServerPlayerEntity player) {
  ServerCommandSource source = player.getServerCommandSource();
  ManaCommand.executeSetManaConsume(source, 2000)

  if (ManaCommand.executeConsume(source) == 0) {
    return;
  }

  // Your code here
}
```

## License

You are free to port it to any mod loader other than fabric.
