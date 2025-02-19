# Pentamana

Pentamana is a scoreboard-based and most customizable mana system that runs server-side providing mana modification and damage calculation hooks.

![manabar.png](https://cdn.modrinth.com/data/UgFKzdOy/images/ef535fac56d849195a46117f9f21b6f5eaa7f5b0.png)

## Configuration

Below is a template config file `config/pentamana.json` filled with default values. You may only need to write the lines you would like to modify.

```json
{
  "manaPerPoint": 16777216,
  "manaCapacityBase": 33554431,
  "manaRegenBase": 1048576,
  "enchantmentCapacityBase": 33554432,
  "enchantmentStreamBase": 65536,
  "enchantmentUtilizationBase": 214748364,
  "enchantmentPotencyBase": 1073741823,
  "statusEffectInstantManaBase": 4,
  "statusEffectInstantDepleteBase": 6,
  "statusEffectManaRegenBase": 50,
  "statusEffectManaInhibitionBase": 40,
  "statusEffectManaPowerBase": 3,
  "statusEffectManaSicknessBase": 4,
  "maxManabarLife": 40,
  "defaultManabarSize": 20,
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
- `manaRegenBase`: Initial mana regen amount per tick.
- `enchantmentCapacityBase`: Mana capacity increase amount per level.
- `enchantmentStreamBase`: Mana regeneration increase amount per level.
- `enchantmentUtilizationBase`: Mana consumption decrease percent per level. 100% is 2147483647.
- `enchantmentPotencyBase`: Level multiplier, the result will be added to casting damage. 100% is 2147483647.
- `statusEffectInstantManaBase`: Mana gained multiplier.
- `statusEffectInstantDepleteBase`: Mana losed multiplier.
- `statusEffectManaRegenBase`: Mana point divisor, the result will be regenerated.
- `statusEffectManaInhibitionBase`: Mana point divisor, the result will be regenerated.
- `statusEffectManaPowerBase` Amplifier multiplier, the result will be added to casting damage.
- `statusEffectManaSicknessBase` Amplifier multiplier, the result will be added to casting damage.
- `maxManabarLife`: Ticks actionbar updating will be suppressed if interrupted.
- `defaultManabarSize`: Default manabar size in characters.
- `manaChars`: Default mana characters, in code point. from 0% to 100% character. The count of its elements determines the amount of mana points to be considered as 1 mana character.
- `manaColors`: Deafult color of characters, in RGB value. from 0% to 100% character.
- `manaBolds`: Default bold of characters. from 0% to 100% character.
- `manaItalics`: Default italic of characters. from 0% to 100% character.
- `manaUnderlineds`: Default underlined of characters. from 0% to 100% character.
- `manaStrikethroughs`: Default strikethrough of characters. from 0% to 100% character.
- `manaObfuscateds`: Default obfuscated of characters. from 0% to 100% character.
- `forceEnabled`: Make the mod enabled for every player when setting to ture, do not modify their own preference.

Enchantments are written in json and registered using datapack. It can be directly modified.

## Commands

- `/mana enbale` Enable this mod for yourself.
- `/mana disable` Disable this mod for yourself completely.
- `/mana set display <false|true>` Set the manabar visibility for yourself.
- `/mana set render_type <flex_size|fixed_size|numberic>` Set the manabar render type for yourself.
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
  custom_data={
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

## Status Effects

Status effects compound can be added or removed from items using custom data components. They are applied when the item is consumed.

```txt
[List] status_effects
|- [Compound]
   |- [String] id: Can be `pentamana:instant_mana`, `pentamana:instant_deplete`, `pentamana:mana_regeneration`, `pentamana:mana_inhibition`, `pentamana:mana_power` and `pentamana:mana_sickness`.
   |- [int] duration: value.
   \- [int] amplifier: value.
```

Below is an example status effect which increase the mana by 268,435,456(![manaCharFull.png](https://cdn.modrinth.com/data/UgFKzdOy/images/a26007574007d784e65c79cb957c3e0d3e94be6f.png)×8) when the item is consumed.

```component
[
  custom_data={
    status_effects: [
      {
        id: "pentamana:instant_mana",
        duration: 1,
        amplifier: 2
      }
    ]
  }
]
```

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

## Formulas

### Mana Capacity

```java
int manaCapacity = (int)player.getCustomModifiedValue("pentamana:mana_capacity", Pentamana.manaCapacityBase);
manaCapacity += Pentamana.enchantmentCapacityBase * player.getWeaponStack().getEnchantments().getLevel("pentamana:capacity");
```

### Mana Regeneration

```java
int manaRegen = (int)player.getCustomModifiedValue("pentamana:mana_regeneration", Pentamana.manaRegenBase);
manaRegen += Pentamana.enchantmentStreamBase * player.getWeaponStack().getEnchantments().getLevel("pentamana:stream");
manaRegen += player.hasCustomStatusEffect("pentamana:instant_mana") ? Pentamana.manaPerPoint * Pentamana.statusEffectInstantManaBase * Math.pow(2, player.getActiveCustomStatusEffect("pentamana:instant_mana").getInt("amplifier")) : 0;
manaRegen -= player.hasCustomStatusEffect("pentamana:instant_deplete") ? Pentamana.manaPerPoint * Pentamana.statusEffectInstantDepleteBase * Math.pow(2, player.getActiveCustomStatusEffect("pentamana:instant_deplete").getInt("amplifier")) : 0;
manaRegen += player.hasCustomStatusEffect("pentamana:mana_regeneration") ? Pentamana.manaPerPoint / Math.max(1, Pentamana.statusEffectManaRegenBase >> player.getActiveCustomStatusEffect("pentamana:mana_regeneration").getInt("amplifier")) : 0;
manaRegen -= player.hasCustomStatusEffect("pentamana:mana_inhibition") ? Pentamana.manaPerPoint / Math.max(1, Pentamana.statusEffectManaInhibitionBase >> player.getActiveCustomStatusEffect("pentamana:mana_inhibition").getInt("amplifier")) : 0;
```

### Mana Consumption

```java
int manaConsume = (int)player.getCustomModifiedValue("pentamana:mana_consumption", executeGetManaConsum(source));
manaConsume *= (Integer.MAX_VALUE - Pentamana.enchantmentUtilizationBase * player.getWeaponStack().getEnchantments().getLevel("pentamana:utilization")) / (float)Integer.MAX_VALUE;
```

### Casting Damage

```java
float castingDamage = manaCapacity;
castingDamage /= Pentamana.manaCapacityBase;
castingDamage *= (float)((ServerPlayerEntity)(Object)this).getCustomModifiedValue("pentamana:casting_damage", baseDamage);
castingDamage += potencyLevel != 0 ? ++potencyLevel * (float)Pentamana.enchantmentPotencyBase / Integer.MAX_VALUE : 0;
castingDamage += ((ServerPlayerEntity)(Object)this).hasCustomStatusEffect("pentamana:mana_power") ? (((ServerPlayerEntity)(Object)this).getActiveCustomStatusEffect("pentamana:mana_power").getInt("amplifier") + 1) * Pentamana.statusEffectManaPowerBase : 0;
castingDamage -= ((ServerPlayerEntity)(Object)this).hasCustomStatusEffect("pentamana:mana_sickness") ? (((ServerPlayerEntity)(Object)this).getActiveCustomStatusEffect("pentamana:mana_sickness").getInt("amplifier") + 1) * Pentamana.statusEffectManaSicknessBase : 0;
castingDamage = Math.max(0, castingDamage);
castingDamage *= entity instanceof WitchEntity ? (float)0.15 : 1;
```

## Events

- `TickManaCallback` Called after the mana capacity calculation, before everything else.
- `RegenManaCallback` Called when a player is regenerating mana. After the mana regeneration calculation, before regenerating mana.
- `ConsumeManaCallback` Called when a player is consuming mana. After the mana consumption calculation, before consuming mana.

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
- `status_effect.pentamana.<id>_<amplifier>` The duration of `id` status effect of `amplifier` + 1 level.

## Enchantments

### Capacity

- Maximum level: II
- Primary items: Stick
- Secondary items: Axe, Hoe, Mace, Pickaxe, Shovel, Sword, Trident
- Enchantment weight: 2

Capacity adds extra mana capacity `level * enchantmentCapacityBase`.

### Stream

- Maximum level: III
- Primary items: Stick
- Secondary items: Axe, Hoe, Mace, Pickaxe, Shovel, Sword, Trident
- Enchantment weight: 5

Stream adds extra mana regeneration by `level * enchantmentStreamBase`.

### Potency

- Maximum level: V
- Primary items: Stick
- Secondary items: Axe, Hoe, Mace, Pickaxe, Shovel, Sword, Trident
- Enchantment weight: 10

Potency adds the casting damage by `(level + 1) * enchantmentPotencyBase`.

### Utilization

- Maximum level: V
- Primary items: Stick
- Secondary items: Axe, Hoe, Mace, Pickaxe, Shovel, Sword, Trident
- Enchantment weight: 5

Utilization reduces the mana cost of casting by `level * enchantmentUtilizationBase` percent.

## License

You are free to back port this mod, or port it to any mod loader other than fabric, as long as you credit the origin version and respect the license applied to this version, which is GPL-3.0-or-later.
