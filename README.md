# Pentamana

Pentamana is a very customizable mana library for storing, modifying, and displaying mana that runs server-side.

If you'd like to add a feature, feel free to open an issue [here](https://github.com/CookedSeafood/pentamana/issues).

## Feature

- 1 manabar.
- 4 attribute modifiers.
- 4 enchantments.
- 8 status effects.
- Datapack interactable.
- Very configurable.
- Nothing presented unless turning on or generating by yourself.

## Installation

`gradle.properties`:

```properties
pentamana_version=0.6.9
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

## Configuration

Here is a template configuration file `config/pentamana.json` filled with default values. You may only need to write the lines you would like to modify.

```json
{
  "manaPerPoint": 1,
  "manaCapacityBase": 2.0,
  "manaRegenerationBase": 0.0625,
  "enchantmentCapacityBase": 2.0,
  "enchantmentStreamBase": 0.015625,
  "enchantmentUtilizationBase": 0.1,
  "enchantmentPotencyBase": 0.5,
  "statusEffectManaBoostBase": 4.0,
  "statusEffectManaReductionBase": 4.0,
  "statusEffectInstantManaBase": 4.0,
  "statusEffectInstantDepleteBase": 6.0,
  "statusEffectManaPowerBase": 3.0,
  "statusEffectManaSicknessBase": 4.0,
  "statusEffectManaRegenerationBase": 50,
  "statusEffectManaInhibitionBase": 40,
  "isConversionExperienceLevel": false,
  "conversionExperienceLevel": 0.5,
  "displayIdleInterval": 40,
  "displaySuppressionInterval": 40,
  "isForceEnabled": false,
  "isEnabled": true,
  "isVisible": true,
  "isCompression": false,
  "compressionSize": 20,
  "manaPattern": [{"text":"$"}],
  "manaRenderType": "character",
  "manaBarPosition": "actionbar",
  "manaBarColor": "blue",
  "manaBarStyle": "progress",
  "pointsPerCharacter" : 2,
  "manaCharset": [[{"text":"★","color":"aqua"}],[{"text":"⯪","color":"aqua"}],[{"text":"☆","color":"black"}]],
}
```

- `manaPerPoint` Amount of mana to be considered as 1 mana point.
- `manaCapacityBase` Initial mana capacity.
- `manaRegenerationBase` Initial mana regen amount per tick.
- `enchantmentCapacityBase` Level multiplier, the result will be added to mana capacity.
- `enchantmentStreamBase` Level multiplier, the result will be added to mana regeneration.
- `enchantmentUtilizationBase` Level multiplier, the result in 100% will be saved.
- `enchantmentPotencyBase` Level multiplier, the result will be added to casting damage.
- `statusEffectManaBoostBase` Level multiplier, the result will be added to mana capacity.
- `statusEffectManaReductionBase` Level multiplier, the result will be substracted from mana capacity.
- `statusEffectInstantManaBase` Level multiplier, the result will be added to mana regeneration.
- `statusEffectInstantDepleteBase` Level multiplier, the result will be substracted from mana regeneration.
- `statusEffectManaRegenerationBase` Mana point divisor, the result will be added to regeneration.
- `statusEffectManaInhibitionBase` Mana point divisor, the result will be substracted from regeneration.
- `statusEffectManaPowerBase` Amplifier multiplier, the result will be added to casting damage.
- `statusEffectManaSicknessBase` Amplifier multiplier, the result will be added to casting damage.
- `isConversionExperienceLevel` Increase mana capacity by experience level.
- `conversionExperienceLevel` Level multiplier, the result will be added to mana capacity.
- `displayIdleInterval` Ticks actionbar not updating if idle.
- `displaySuppressionInterval` Ticks actionbar not updating if interrupted.
- `isForceEnabled` Make the mod enabled for every player when setting to ture, do not modify their own preference.
- `isEnabled` Default preference.
- `isVisible` Default preference.
- `isCompression` Default preference.
- `compressionSize` Default preference, in character.
- `manaPattern` Default preference. Use `$` to represent mana render text.
- `manaRenderType` Default preference. Can be character, numeric, percentage and none.
- `manaBarPosition` Default preference. Can be actionbar, bossbar and siderbar.
- `manaBarColor` Default preference.
- `manaBarStyle` Default preference.
- `pointsPerCharacter` Default preference. Amount of mana points to be considered as 1 mana character.
- `manaCharset` Default preference. from 100% to 0% point character.

Enchantments are written in json and registered using datapack. It can be directly modified.

## Commands

- `/mana enbale` Enable this mod for yourself.
- `/mana disable` Disable this mod for yourself completely.
- `/manabar set visibility <false|true>` Set the manabar visibility for yourself.
- `/manabar set pattern <text>` Set the manabar pattern for yourself. Use `$` to represent mana render text. Text which not in `extra` is ignored.
- `/manabar set type <character|numeric|percentage|none>` Set the mana render text type for yourself.
- `/manabar set position <actionbar|bossbar|siderbar>` Set the manabar position for yourself.
- `/manabar set color <pink|blue|red|green|yellow|purple|white>` Set the manabar color in bossbar for yourself.
- `/manabar set style <progress|notched_6|notched_10|notched_12|notched_20>` Set the manabar style in bossbar for yourself.
- `/manabar set points_per_character <value>` Set the amount of mana points to be considered as 1 mana character for yourself.
- `/manabar set character <text> [<character_type_index>] [<character_index>]` Set the #`[<character_index>]` `[<character_type_index>]` point mana character for yourself.
- `/manabar reset [<visibility|pattern|type|position|color|style|points_per_character|character>]` Reset manabar options for yourself.
- `/pentamana debug config` Print config file. (from disk, _NOT_ from loaded config)
- `/pentamana debug manabar server [<player>]` Print server manabar info of the player.
- `/pentamana debug manabar client [<player>]` Print client manabar info of the player.
- `/pentamana debug effect <effect> [<player>]` Print the `<effect>` status effect info of the player.

### Commands Require Premission Level 2

- `/mana reload` Reload config file.
- `/mana get` Get mana supply. Returns mana supply in point.
- `/mana set` Set mana supply. Returns modified mana supply in point.
- `/mana add` Add mana supply. Returns modified mana supply in point.
- `/mana subtract` Subtract mana supply. Returns modified mana supply in point.
- `/custom effect give <players> <effect> [<duration|infinite>] [<amplifier>]` Give status effect. `<effect>` can be `pentamana.mana_boost`, `pentamana.mana_reduction`, `pentamana.instant_mana`, `pentamana.instant_deplete`, `pentamana.mana_regeneration`, `pentamana.mana_inhibition`, `pentamana.mana_power`, `pentamana.mana_sick`.
- `/custom effect clear <players> [<effect>]` Clear status effect.

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

Below is an example modifier which increase mana capacity by 120(![2PointManaChar.png](https://cdn.modrinth.com/data/UgFKzdOy/images/a26007574007d784e65c79cb957c3e0d3e94be6f.png)×60) when held in offhand.

```component
[
  minecraft:custom_data={
    modifiers: [
      {
        attribute: "pentamana:mana_capacity",
        base: 120.0d,
        operation: "add_value",
        slot: "offhand"
      }
    ]
  }
]
```

## Status Effects

Status effects can be added or removed from items using custom data components. They are applied when the item is consumed.

```txt
[List] status_effects
|- [Compound]
   |- [String] id: Can be `pentamana:mana_boost`, `pentamana:mana_reduction`, `pentamana:instant_mana`, `pentamana:instant_deplete`, `pentamana:mana_regeneration`, `pentamana:mana_inhibition`, `pentamana:mana_power` and `pentamana:mana_sickness`.
   |- [int] duration: value.
   \- [int] amplifier: value.
```

Below is an example status effect which increase the mana regeneration by 16(![2PointManaChar.png](https://cdn.modrinth.com/data/UgFKzdOy/images/a26007574007d784e65c79cb957c3e0d3e94be6f.png)×8) when the item is consumed.

```component
[
  minecraft:custom_data={
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

- Mana Boost: Increase mana capacity by `level * statusEffectManaBoostBase`.
- Mana Reduction: Decrease mana capacity by `level * statusEffectManaReductionBase`.
- Instant Mana: Increase mana regeneration by `2 ^ level * statusEffectInstantManaBase`.
- Instant Deplete: Decrease mana regeneration by `2 ^ level * statusEffectInstantDepleteBase`.
- Mana Regeneration: Increase mana regeneration by `manaPerPoint / statusEffectManaRegenerationBase >> level`
- Mana Inhibition: Decrease mana regeneration by `manaPerPoint / statusEffectManaInhibitionBase >> level`
- Mana Power: Increase casting damage by `level * statusEffectManaPowerBase`.
- Mana Sickness: Decrease casting damage by `level * statusEffectManaSicknessBase`.

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

## Tutorial: Create your very own magic weapon

Codes in this tutorial are licenced under CC-0.

Let's say we want a right-click weapon named `Magik Wand` that consumes 1(![1PointManaChar.png](https://cdn.modrinth.com/data/UgFKzdOy/images/d943f1772f350c1645aef349b1c0dcd86a90296c.png)) mana per use when held in mainhand.

Here is a callback which will be called everytime a player uses items:

```java
UseItemCallback.EVENT.register((player, world, hand) -> {
  
})
```

Check if the hand is mainhand:

```java
if (Hand.OFF_HAND.equals(hand)) {
  return ActionResult.PASS;
}
```

Check if the item has a `Magik Wand` name:

```java
ItemStack stack = player.getMainHandStack();
if (!"Magik Wand".equals(stack.getItemName().getString())) {
  return ActionResult.PASS;
}
```

Check if the player has mana enabled:

```java
if (!ManaPreferenceComponentInstance.MANA_PREFERENCE.get(player).isEnabled()) {
  return ActionResult.PASS;
}
```

Consume the mana and fire your weapon if the consumption is successful. Consumption will succeed if the player has enough mana supply.

```java
if (!ServerManaBarComponentInstance.SERVER_MANA_BAR.get(player).getServerManaBar().consum(1.0f)) {
  return ActionResult.PASS;
}

// Your code here
```

## Formulas

### Mana Capacity

```java
float capacity = (float)this.player.getCustomModifiedValue("pentamana:mana_capacity", Pentamana.manaCapacityBase);
capacity += Pentamana.enchantmentCapacityBase * this.player.getWeaponStack().getEnchantments().getLevel("pentamana:capacity");
capacity += statusEffectManager.has("pentamana:mana_boost") ? Pentamana.statusEffectManaBoostBase * (statusEffectManager.getActiveStatusEffectAmplifier("pentamana:mana_boost") + 1) : 0;
capacity -= statusEffectManager.has("pentamana:mana_reduction") ? Pentamana.statusEffectManaReductionBase * (statusEffectManager.getActiveStatusEffectAmplifier("pentamana:mana_reduction") + 1) : 0;
capacity = Math.max(capacity, 0.0f);
```

### Mana Regeneration

```java
float regen = (float)this.player.getCustomModifiedValue("pentamana:mana_regeneration", Pentamana.manaRegenerationBase);
regen += Pentamana.enchantmentStreamBase * this.player.getWeaponStack().getEnchantments().getLevel("pentamana:stream");
regen += statusEffectManager.has("pentamana:instant_mana") ? Pentamana.statusEffectInstantManaBase * Math.pow(2, statusEffectManager.getActiveStatusEffectAmplifier("pentamana:instant_mana")) : 0;
regen -= statusEffectManager.has("pentamana:instant_deplete") ? Pentamana.statusEffectInstantDepleteBase * Math.pow(2, statusEffectManager.getActiveStatusEffectAmplifier("pentamana:instant_deplete")) : 0;
regen += statusEffectManager.has("pentamana:mana_regeneration") ? Pentamana.manaPerPoint / (float)Math.max(1, Pentamana.statusEffectManaRegenerationBase >> statusEffectManager.getActiveStatusEffectAmplifier("pentamana:mana_regeneration")) : 0;
regen -= statusEffectManager.has("pentamana:mana_inhibition") ? Pentamana.manaPerPoint / (float)Math.max(1, Pentamana.statusEffectManaInhibitionBase >> statusEffectManager.getActiveStatusEffectAmplifier("pentamana:mana_inhibition")) : 0;
```

### Mana Consumption

```java
float targetConsum = (float)player.getCustomModifiedValue("pentamana:mana_consumption", consum);
targetConsum *= 1 - Pentamana.enchantmentUtilizationBase * player.getWeaponStack().getEnchantments().getLevel("pentamana:utilization");
```

### Casting Damage

```java
float castingDamage = manaCapacity;
castingDamage /= Pentamana.manaCapacityBase;
castingDamage *= (float)player.getCustomModifiedValue("pentamana:casting_damage", baseDamage);
castingDamage += potencyLevel != 0 ? ++potencyLevel * Pentamana.enchantmentPotencyBase / Integer.MAX_VALUE : 0;
castingDamage += statusEffectManager.has("pentamana:mana_power") ? (statusEffectManager.getActiveStatusEffectAmplifier("pentamana:mana_power") + 1) * Pentamana.statusEffectManaPowerBase : 0;
castingDamage -= statusEffectManager.has("pentamana:mana_sickness") ? (statusEffectManager.getActiveStatusEffectAmplifier("pentamana:mana_sickness") + 1) * Pentamana.statusEffectManaSicknessBase : 0;
castingDamage = Math.max(castingDamage, 0.0f);
castingDamage *= entity instanceof WitchEntity ? 0.15f : 1;
```

## Events

- `TickManaCallback` Called after the mana capacity calculation, before everything else.
- `RegenManaCallback` Called when a player is regenerating mana. After the mana regeneration calculation, before regenerating mana.
- `ConsumeManaCallback` Called when a player is consuming mana. After the mana consumption calculation, before consuming mana.

## FAQ

### Containing in mod packs

Yes, As long as the download source is modrinth.

### Back porting / porting to other mod loaders

No. You can port it yourself as long as you give credit to the original work.
