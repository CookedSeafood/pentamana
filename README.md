# Pentamana

Pentamana is a scoreboard-based mana mod that runs server-side providing mana modification and damage calculation hooks.

Pentamana acts as a mana system, handling tasks such as mana regeneration and mana consumption. It display manabar in actionbar.

## Mana Mechanic

Each player starts with 33,554,431 mana capacity (1 star in the manabar). Mana capacity is maxed out at 2,147,483,647 mana, or 64 stars total. The mana capacity would be calculated by the formula below:

``` txt
Mana Capacity = AttributeModify(33554431) + CapacityEnchantmentLevel * 33554432
```

A player basicly regenerate 1,048,576 mana every tick (32 ticks per star). The mana regeneration amount per tick would be calculated by the formula below:

``` txt
Mana Regen = AttributeModify(1048576) + StreamEnchantmentLevel * 65536
```

The output damage from casting would be calculated by the formula below: (Can be got via `(ServerPlayerEntity)player.getCastingDamageAgainst(Entity entity, float baseDamage)`)

``` txt
Magic Damage = baseDamage * (ManaCapacity / ManaScale) + PotencyEnchantmentLevel > 0 ? ++PotencyEnchantmentLevel * 0.5 : 0
```

## Commands

`/mana enbale` Enable this mod for yourself.

`/mana disable` Disable this mod for yourself completely.

`/mana set character full <value>` Set the mana character of 2 points mana for yourself.

`/mana set character full <value>` Set the mana character of 1 point mana for yourself.

`/mana set character full <value>` Set the mana character of 0 point mana for yourself.

`/mana set color full <value>` Set mana the color of 2 points mana for yourself.

`/mana set color half <value>` Set mana the color of 1 point mana for yourself.

`/mana set color zero <value>` Set mana the color of 0 point mana for yourself.

`/mana reset` Reset mana options for yourself.

`/mana reload` Reload config file. (Require premission level 2)

`/mana version` Print mod version.

This mod is disbled for every player by default.

## Configuration

The config file is not shipped along with the mod. Below is a template config file `config/pentamana.json` filled with default values. You may only need to write the lines you would like to modify. (and braces)

```json
{
  // Amount of mana be considered as 1 point mana.
  "manaScale": 16777216,
  // Initial mana capacity, should be odd.
  "manaCapacityBase": 33554431,
  // Used in capacity enchantment, should be even.
  "manaCapacityIncrementBase": 33554432,
  // Initial mana regen amount per tick.
  "manaRegenBase": 1048576,
  // Used in stream enchantment
  "manaRegenIncrementBase": 65536,
  // Ticks of actionbar updating suppression when interrupted
  "maxManabarLife": 40,
  // Default mana character of 2 points mana.
  "manaCharFull": "★",
  // Default mana character of 1 point mana.
  "manaCharHalf": "⯪",
  // Default mana character of 0 point mana.
  "manaCharZero": "☆",
  // Default mana color of 2 points mana.
  "manaColorFull": "aqua",
  // Default mana color of 1 point mana.
  "manaColorHalf": "aqua",
  // Default mana color of 0 point mana.
  "manaColorZero": "black",
  // Make the mod enabled for every player when setting to ture, do not modify their own preference.
  "forceEnabled": false
}
```

Enchantments are registed using datapack. You can open mod jar and edit it.

## Modifiers

Modifiers can be added or removed from items using custom data components. They are active while equipped in the weapon slot.

```txt
[List] modifiers
|- [Compound]
   |- [String] attribute: Can be `pentamana:mana_capacity`, `pentamana:mana_regeneration` and `pentamana:mana_consumption`.
   |- [Double] base: value.
   \- [String] operation: Can be `add_value`, `add_multiplied_base` and `add_multiplied_total`.
```

Below is an example modifier which increase mana capacity by 301,989,888(18 points mana).

```component
[
  "minecraft:custom_data":{
    modifiers: [
      {
        attribute: "pentamana:mana_capacity",
        base: 301989888.0d,
        operation: "add_value"
      }
    ]
  }
]
```

## Objectives

`pentamana.mana` Mana supply at last tick

`pentamana.mana_capacity` Mana capacity at last tick

`pentamana.mana_regeneration` Amount of mana regened at last tick

`pentamana.mana_consumption` Amount of mana to consume

`pentamana.manabar_life` Ticks left till next display update if idle

`pentamana.mana_char_full` The complement of mana character of 2 points mana.

`pentamana.mana_char_half` The complement of mana character of 1 point mana.

`pentamana.mana_char_zero` The complement of mana character of 0 point mana.

`pentamana.mana_color_full` The mana color which is used in manabar.

`pentamana.mana_color_half` The mana color which is used in manabar.

`pentamana.mana_color_zero` The mana color which is used in manabar.

`pentamana.enabled` 1 if enabled, otherwise not.

## Events

- `TickManaCallback` Called after the mana capacity calculation, before everything else.

- `RegenManaCallback` Called when a player is regenerating mana. After the mana regeneration calculation, before regenerating mana.

- `ConsumeManaCallback` Called when a player is consuming mana. After the mana consumption calculation, before consuming mana.

## Tutorial: Create your very own magic weapon

This tutorial assumes that you already have a method that will be called when the weapon is used.

First, set the amount of mana the weapon consumes per use. For example, 16,777,216(1 point mana).

```java
ServerCommandSource source = player.getServerCommandSource();
ManaCommand.executeSetManaConsume(source, 16777216)
```

Second, consume the mana and fire your weapon if the consumption is successful. Consumption will succeed if the player has enough mana.

```java
if (ManaCommand.executeConsume(source) == 0) {
  return;
}

// Your code here
```

The result code will look like this:

```java
public void useExampleWeapon(ServerPlayerEntity player) {
  ServerCommandSource source = player.getServerCommandSource();
  ManaCommand.executeSetManaConsume(source, 2000000)

  if (ManaCommand.executeConsume(source) == 0) {
    return;
  }

  // Your code here
}
```

## Enchantments

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

## License

You are free to port it to any mod loader other than fabric as long as you credit the origin version.
