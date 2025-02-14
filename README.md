# Pentamana

Pentamana is a scoreboard-based and most customizable mana system that runs server-side providing mana modification and damage calculation hooks.

![manabar.png](https://cdn.modrinth.com/data/UgFKzdOy/images/ef535fac56d849195a46117f9f21b6f5eaa7f5b0.png)

## Mana Mechanic

Each player starts with 33,554,431(![manaCharFull.png](https://cdn.modrinth.com/data/UgFKzdOy/images/a26007574007d784e65c79cb957c3e0d3e94be6f.png)) mana capacity. Mana capacity is maxed out at 2,147,483,647(![manaCharFull.png](https://cdn.modrinth.com/data/UgFKzdOy/images/a26007574007d784e65c79cb957c3e0d3e94be6f.png)×64) mana. The mana capacity would be calculated by the formula below:

``` txt
Mana Capacity = AttributeModify(33554431) + CapacityEnchantmentLevel * 33554432
```

A player basicly regenerate 1,048,576 mana every tick (32 ticks per ![manaCharFull.png](https://cdn.modrinth.com/data/UgFKzdOy/images/a26007574007d784e65c79cb957c3e0d3e94be6f.png)). The mana regeneration amount per tick would be calculated by the formula below:

``` txt
Mana Regen = AttributeModify(1048576) + StreamEnchantmentLevel * 65536
```

The output damage from casting would be calculated by the formula below: (Can be got via `(ServerPlayerEntity)player.getCastingDamageAgainst(Entity entity, float baseDamage)`)

``` txt
Magic Damage = baseDamage * (ManaCapacity / manaPerPoint) + PotencyEnchantmentLevel > 0 ? ++PotencyEnchantmentLevel * 0.5 : 0
```

## Commands

`/mana enbale` Enable this mod for yourself.

`/mana disable` Disable this mod for yourself completely.

`/mana set display <false|true>` Set the manabar visibility for yourself.

`/mana set render_type <graphic|numberic>` Set the manabar render type for yourself.

`/mana set character <text> [<type_index>] [<character_index>]` Set the #`character_index` `type_index` point mana character for yourself.

`/mana reset` Reset mana character for yourself.

`/mana reload` Reload config file. (Require premission level 2)

This mod is disbled for every player by default.

The visibility is false for every player by default.

## Configuration

The config file is not shipped along with the mod. Below is a template config file `config/pentamana.json` filled with default values. You may only need to write the lines you would like to modify. (and braces)

```json
{
  // Amount of mana to be considered as 1 mana point.
  "manaPerPoint": 0x100_0000,
  // Initial mana capacity, should be odd.
  "manaCapacityBase": 0x1ff_ffff,
  // Used in capacity enchantment, should be even.
  "manaCapacityIncrementBase": 0x200_0000,
  // Initial mana regen amount per tick.
  "manaRegenBase": 0x10_0000,
  // Used in stream enchantment
  "manaRegenIncrementBase": 0x1_0000,
  // Ticks of actionbar updating suppression when interrupted
  "maxManabarLife": 40,
  // Default mana characters, from 0% to 100% point. The count of its elements determines the amount of mana points to be considered as 1 mana character.
  "manaChars": [0x2605, 0x2bea, 0x2606],
  // Deafult color of characters, from 0% to 100% point.
  "manaColors": [0x55ffff, 0x55ffff, 0x0],
  // Default bold of characters, from 0% to 100% point.
  "manaBolds": [false, false, false],
  // Default italic of characters, from 0% to 100% point.
  "manaItalics": [false, false, false],
  // Default underlined of characters, from 0% to 100% point.
  "manaUnderlineds": [false, false, false],
  // Default strikethrough of characters, from 0% to 100% point.
  "manaStrikethroughs": [false, false, false],
  // Default obfuscated of characters, from 0% to 100% point.
  "manaObfuscateds": [false, false, false],
  // Make the mod enabled for every player when setting to ture, do not modify their own preference.
  "forceEnabled": false
}
```

Enchantments are registed using datapack. You can open mod jar and edit it.

## Modifiers

Modifiers can be added or removed from items using custom data components. They are active when equipped in the written slot.

```txt
[List] modifiers
|- [Compound]
   |- [String] attribute: Can be `pentamana:mana_capacity`, `pentamana:mana_regeneration` and `pentamana:mana_consumption`.
   |- [Double] base: Any.
   |- [String] id: Any. Used to distinguish the same.
   |- [String] operation: Can be `add_value`, `add_multiplied_base` and `add_multiplied_total`.
   \- [String] slot: Can be `mainhand`, `offhand`, `feet`, `legs`, `chest` and `head`.
```

Below is an example modifier which increase mana capacity by 1,275,068,416(![manaCharFull.png](https://cdn.modrinth.com/data/UgFKzdOy/images/a26007574007d784e65c79cb957c3e0d3e94be6f.png)×19) when held in offhand.

```component
[
  "minecraft:custom_data":{
    modifiers: [
      {
        attribute: "pentamana:mana_capacity",
        base: 1275068416.0d,
        operation: "add_value",
        slot: "offhand"
      }
    ]
  }
]
```

## Objectives

`pentamana.mana` Mana supply at last tick.

`pentamana.mana_capacity` Mana capacity at last tick.

`pentamana.mana_regeneration` Amount of mana regenerated at last tick.

`pentamana.mana_consumption` Amount of mana to consume.

`pentamana.manabar_life` Ticks left till next display update if idle.

`pentamana.mana_char_<type_index>_<character_index>` The code point of #`character_index` `type_index` point mana character.

`pentamana.mana_color_<type_index>_<character_index>` The index of #`character_index` `type_index` point mana character's color + 1.

`pentamana.mana_bold_<type_index>_<character_index>` The boolean of #`character_index` `type_index` point mana character's bold.

`pentamana.mana_italic_<type_index>_<character_index>` The boolean of #`character_index` `type_index` point mana character's italic.

`pentamana.mana_underlined_<type_index>_<character_index>` The boolean of #`character_index` `type_index` point mana character's underlined.

`pentamana.mana_strikethrough_<type_index>_<character_index>` The boolean of #`character_index` `type_index` point mana character's strikethrough.

`pentamana.mana_obfuscated_<type_index>_<character_index>` The boolean of #`character_index` `type_index` point mana character's obfuscated.

`pentamana.enabled` 1 if enabled, otherwise not.

`pentamana.display` 1 if visible, otherwise not.

`pentamana.render_type` 1 if numberic, otherwise graphic.

`pentamana.mana_point` Mana supply in point at last tick. Used only in display.

`pentamana.mana_capacity_point` Mana capacity in point at last tick. Used only in display.

## Events

- `TickManaCallback` Called after the mana capacity calculation, before everything else.

- `RegenManaCallback` Called when a player is regenerating mana. After the mana regeneration calculation, before regenerating mana.

- `ConsumeManaCallback` Called when a player is consuming mana. After the mana consumption calculation, before consuming mana.

## Tutorial: Create your very own magic weapon

This tutorial assumes that you already have a method that will be called when the weapon is used.

First, set the amount of mana the weapon consumes per use. For example, 16,777,216(![manaCharHalf](https://cdn.modrinth.com/data/UgFKzdOy/images/d943f1772f350c1645aef349b1c0dcd86a90296c.png)).

```java
ServerCommandSource source = player.getServerCommandSource();
ManaCommand.executeSetManaConsum(source, 16777216)
```

Second, consume the mana and fire your weapon if the consumption is successful. Consumption will succeed if the player has enough mana.

```java
if (ManaCommand.executeConsum(source) == 0) {
  return;
}

// Your code here
```

The result code will look like this:

```java
public void useExampleWeapon(ServerPlayerEntity player) {
  ServerCommandSource source = player.getServerCommandSource();
  ManaCommand.executeSetManaConsum(source, 16777216)

  if (ManaCommand.executeConsum(source) == 0) {
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

You are free to back port this mod, or port it to any mod loader other than fabric, as long as you credit the origin version.
