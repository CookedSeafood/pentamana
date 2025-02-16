# Pentamana

Pentamana is a scoreboard-based and most customizable mana system that runs server-side providing mana modification and damage calculation hooks.

![manabar.png](https://cdn.modrinth.com/data/UgFKzdOy/images/ef535fac56d849195a46117f9f21b6f5eaa7f5b0.png)

## Formulas

These formulas are calculated using int except for the `AttributeModify()`, which using double.

![formula.png](https://cdn.modrinth.com/data/UgFKzdOy/images/79b37c549ca65479b4ec7a41505487f5dfe33b64.png)

![fomula.png](https://cdn.modrinth.com/data/UgFKzdOy/images/e631bc048b9c619ffa08a0158b7b9509dc2fe68a.png)

![formula.png](https://cdn.modrinth.com/data/UgFKzdOy/images/df41d9afd3ef70ff131bced2a45b4ec67a98f9fc.png)

This formula is calculated using float except for the `AttributeModify()`, which using double.

![formula.png](https://cdn.modrinth.com/data/UgFKzdOy/images/3fcb505cc7f4014836a6434bfde665379678a9e1.png)

```txt
ManaCapacity = AttributeModify(manaCapacityBase) + CapacityEnchantmentLevel * ManaCapacityIncrementBase
ManaRegen = AttributeModify(ManaRegenBase) + StreamEnchantmentLevel * ManaRegenIncrementBase
ManaConsumption = AttributeModify(ManaConsumptionBase) * (10 - UtilizationEnchantmentLevel) / 10
CastingDamage = AttributeModify(BaseDamage) * (ManaCapacity / ManaPerPoint) + PotencyEnchantmentLevel > 0 ? ++PotencyEnchantmentLevel * 0.5 : 0
```

## Configuration

Below is a template config file `config/pentamana.json` filled with default values. You may only need to write the lines you would like to modify.

```json
{
  "manaPerPoint": 16777216,
  "manaCapacityBase": 33554431,
  "manaCapacityIncrementBase": 33554432,
  "manaRegenBase": 1048576,
  "manaRegenIncrementBase": 65536,
  "maxManabarLife": 40,
  "manaChars": [9733, 11242, 9734],
  "manaColors": [5636095, 5636095, 0],
  "manaBolds": [false, false, false],
  "manaItalics": [false, false, false],
  "manaUnderlineds": [false, false, false],
  "manaStrikethroughs": [false, false, false],
  "manaObfuscateds": [false, false, false],
  "forceEnabled": false
}
```

- `manaPerPoint`: Amount of mana to be considered as 1 mana point.
- `manaCapacityBase`: Initial mana capacity, should be odd.
- `manaCapacityIncrementBase`: Used in capacity enchantment, should be even.
- `manaRegenBase`: Initial mana regen amount per tick.
- `manaRegenIncrementBase`: Used in stream enchantment.
- `maxManabarLife`: Ticks actionbar updating will be suppressed if interrupted.
- `manaChars`: Default mana characters in code point, from 0% to 100% character. The count of its elements determines the amount of mana points to be considered as 1 mana character.
- `manaColors`: Deafult color in RGB value of characters, from 0% to 100% character.
- `manaBolds`: Default bold of characters, from 0% to 100% character.
- `manaItalics`: Default italic of characters, from 0% to 100% character.
- `manaUnderlineds`: Default underlined of characters, from 0% to 100% character.
- `manaStrikethroughs`: Default strikethrough of characters, from 0% to 100% character.
- `manaObfuscateds`: Default obfuscated of characters, from 0% to 100% character.
- `forceEnabled`: Make the mod enabled for every player when setting to ture, do not modify their own preference.

Enchantments are registed using datapack. You can open mod jar and edit it.

## Commands

- `/mana enbale` Enable this mod for yourself.
- `/mana disable` Disable this mod for yourself completely.
- `/mana set display <false|true>` Set the manabar visibility for yourself.
- `/mana set render_type <graphic|numberic>` Set the manabar render type for yourself.
- `/mana set character <text> [<type_index>] [<character_index>]` Set the #`character_index` `type_index` point mana character for yourself.
- `/mana reset` Reset mana character for yourself.
- `/mana reload` Reload config file. (Require premission level 2)

This mod is disbled for every player by default.

The visibility is false for every player by default.

## Modifiers

Modifiers can be added or removed from items using custom data components. They are active when equipped in the written slot.

```txt
[List] modifiers
|- [Compound]
   |- [String] attribute: Can be `pentamana:mana_capacity`, `pentamana:mana_regeneration`, `pentamana:mana_consumption` and `pentamana:casting_damage`.
   |- [Double] base: Any.
   |- [String] id: Any.
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

- `pentamana.mana` Mana supply at last tick.
- `pentamana.mana_capacity` Mana capacity at last tick.
- `pentamana.mana_regeneration` Amount of mana regenerated at last tick.
- `pentamana.mana_consumption` Amount of mana to consume.
- `pentamana.manabar_life` Ticks left till next display update if idle.
- `pentamana.mana_char_<type_index>_<character_index>` The code point of #`character_index` `type_index` point mana character.
- `pentamana.mana_color_<type_index>_<character_index>` The RGB value of #`character_index` `type_index` point mana character's color + 1.
- `pentamana.mana_bold_<type_index>_<character_index>` The boolean of #`character_index` `type_index` point mana character's bold.
- `pentamana.mana_italic_<type_index>_<character_index>` The boolean of #`character_index` `type_index` point mana character's italic.
- `pentamana.mana_underlined_<type_index>_<character_index>` The boolean of #`character_index` `type_index` point mana character's underlined.
- `pentamana.mana_strikethrough_<type_index>_<character_index>` The boolean of #`character_index` `type_index` point mana character's strikethrough.
- `pentamana.mana_obfuscated_<type_index>_<character_index>` The boolean of #`character_index` `type_index` point mana character's obfuscated.
- `pentamana.enabled` 1 if enabled, otherwise not.
- `pentamana.display` 1 if visible, otherwise not.
- `pentamana.render_type` 1 if numberic, otherwise graphic.
- `pentamana.mana_point` Mana supply in point at last tick. Used only in display.
- `pentamana.mana_capacity_point` Mana capacity in point at last tick. Used only in display.

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

Capacity adds extra mana capacity `ManaCapacityIncrementBase` per level.

### Stream

- Maximum level: III
- Primary items: Stick
- Secondary items: Axe, Hoe, Mace, Pickaxe, Shovel, Sword, Trident
- Enchantment weight: 5

Stream adds extra mana regeneration `ManaRegenIncrementBase` per level.

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
