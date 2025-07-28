# Pentamana

Pentamana is a very customizable mana library for storing, modifying, and displaying mana that runs server-side.

If you'd like to add a feature, feel free to open an issue on [github issues](https://github.com/CookedSeafood/pentamana/issues).

## Feature

- 1 manabar (1 text pattern, 3 display position, 4 render type).
- 4 attribute modifiers.
- 4 enchantments.
- 8 status effects.
- Datapack interactable.
- Very configurable.
- Nothing presented unless turning on or generating by yourself.

## Installation

`gradle.properties`:

```properties
pentamana_version=0.8.2
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
  "shouldConvertExperienceLevel": false,
  "experienceLevelConversionBase": 0.5,
  "default_preference": {
    "isVisible": true,
    "isSuppressed": false,
    "position": "actionbar",
    "type": "character",
    "pattern": [{"text":"$"}],
    "charset": [[{"text":"★","color":"aqua"}],[{"text":"⯪","color":"aqua"}],[{"text":"☆","color":"black"}]],
    "pointsPerCharacter": 2,
    "isCompressed": false,
    "compressionSize": 20,
    "color": "blue",
    "style": "progress"
  }
}
```

### `manaPerPoint`

Amount of mana to be considered as 1 mana point.

### `manaCapacityBase`, `enchantmentCapacityBase`, `statusEffectManaBoostBase`, `statusEffectManaReductionBase`, `shouldConvertExperienceLevel`, `experienceLevelConversionBase`

Are used by the formula below:

```java
float capacity = (float)entity.getCustomModifiedValue(PentamanaAttributeIdentifiers.MANA_CAPACITY, PentamanaConfig.manaCapacityBase);
capacity += PentamanaConfig.enchantmentCapacityBase * entity.getWeaponStack().getEnchantments().getLevel(PentamanaEnchantmentIdentifiers.CAPACITY);
capacity += statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.MANA_BOOST) ? PentamanaConfig.statusEffectManaBoostBase * (statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_BOOST) + 1) : 0;
capacity -= statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.MANA_REDUCTION) ? PentamanaConfig.statusEffectManaReductionBase * (statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_REDUCTION) + 1) : 0;
capacity += PentamanaConfig.shouldConvertExperienceLevel && entity instanceof ServerPlayerEntity ? PentamanaConfig.experienceLevelConversionBase * ((ServerPlayerEntity)entity).experienceLevel : 0;
capacity = Math.max(capacity, 0.0f);
```

### `manaRegenerationBase`, `enchantmentStreamBase`, `statusEffectInstantManaBase`, `statusEffectInstantDepleteBase`, `statusEffectManaRegenerationBase`, `statusEffectManaInhibitionBase`

Are used by the formula below:

```java
float regen = (float)entity.getCustomModifiedValue(PentamanaAttributeIdentifiers.MANA_REGENERATION, PentamanaConfig.manaRegenerationBase);
regen += PentamanaConfig.enchantmentStreamBase * entity.getWeaponStack().getEnchantments().getLevel(PentamanaEnchantmentIdentifiers.STREAM);
regen += statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.INSTANT_MANA) ? PentamanaConfig.statusEffectInstantManaBase * Math.pow(2, statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.INSTANT_MANA)) : 0;
regen -= statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.INSTANT_DEPLETE) ? PentamanaConfig.statusEffectInstantDepleteBase * Math.pow(2, statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.INSTANT_DEPLETE)) : 0;
regen += statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.MANA_REGENERATION) ? PentamanaConfig.manaPerPoint / (float)Math.max(1, PentamanaConfig.statusEffectManaRegenerationBase >> statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_REGENERATION)) : 0;
regen -= statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.MANA_INHIBITION) ? PentamanaConfig.manaPerPoint / (float)Math.max(1, PentamanaConfig.statusEffectManaInhibitionBase >> statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_INHIBITION)) : 0;
```

### `enchantmentUtilizationBase`

Is used by the formula below:

```java
float targetConsum = (float)entity.getCustomModifiedValue(PentamanaAttributeIdentifiers.MANA_CONSUMPTION, amount);
targetConsum *= 1 - PentamanaConfig.enchantmentUtilizationBase * entity.getWeaponStack().getEnchantments().getLevel(PentamanaEnchantmentIdentifiers.UTILIZATION);
```

### `enchantmentPotencyBase`, `statusEffectManaPowerBase`, `statusEffectManaSicknessBase`

Are used by the formula below:

```java
float castingDamage = manaCapacity;
castingDamage /= PentamanaConfig.manaCapacityBase;
castingDamage *= (float)livingEntity.getCustomModifiedValue(PentamanaAttributeIdentifiers.CASTING_DAMAGE, baseDamage);
castingDamage += potencyLevel != 0 ? ++potencyLevel * PentamanaConfig.enchantmentPotencyBase / Integer.MAX_VALUE : 0;
castingDamage += statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.MANA_POWER) ? (statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_POWER) + 1) * PentamanaConfig.statusEffectManaPowerBase : 0;
castingDamage -= statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.MANA_SICKNESS) ? (statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_SICKNESS) + 1) * PentamanaConfig.statusEffectManaSicknessBase : 0;
castingDamage = Math.max(castingDamage, 0.0f);
castingDamage *= entity instanceof WitchEntity ? 0.15f : 1;
```

### `isVisible`

True if manabar should be visible.

### `isSuppressed`

True if manabar display update should be suppressed.

### `position`

Manabar position. Can be `actionbar`, `bossbar` and `siderbar`.

### `type`

Manabar render type. Can be `character`, `numeric`, `percentage` and `none`.

### `pattern`

Manabar text pattern. Manabar render text is respresented by `$`.

### `charset`

The 2-dim charset used if `type` is `character`. Can define mana-character with 128 index and 128 states. From 100% to 0% state, then from first index to last index.

### `pointsPerCharacter`

Amount of mana points to be considered as 1 mana character.

### `isCompressed`

True if fixed size should be used if `type` is `character`.

### `compressionSize`

The size in characters of compression.

### `color`

The color of bossbar which act as manabar.

### `style`

The style of bossbar which act as manabar.

## Command

- `/manabar set visibility <false|true>` Set the manabar visibility for player preference.
- `/manabar set suppression <false|true>` Set the manabar suppression for player preference.
- `/manabar set pattern <text>` Set the manabar text pattern for player preference. Manabar render text is respresented by `$`. Text which not in `extra` is ignored.
- `/manabar set type <character|numeric|percentage|none>` Set the manabar render type for player preference.
- `/manabar set position <actionbar|bossbar|siderbar>` Set the manabar position for player preference.
- `/manabar set color <pink|blue|red|green|yellow|purple|white>` Set the color of bossbar which act as manabar for player preference.
- `/manabar set style <progress|notched_6|notched_10|notched_12|notched_20>` Set style of bossbar which act as manabar for player preference.
- `/manabar set points_per_character <value>` Set the character-point ratio for player preference.
- `/manabar set character <text> [<character_type_index>] [<character_index>]` Set the `character_index` index `character_type_index` state character for player preference.
- `/manabar reset [<visibility|pattern|type|position|color|style|points_per_character|character>]` Reset player preference.
- `/pentamana debug preference [<player>]` Print player preference.

The commands below require premission level 2 to execute.

- `/mana reload` Reload config file.
- `/mana get` Print mana supply. Returns mana supply in point.
- `/mana set` Set mana supply. Returns modified mana supply in point.
- `/mana add` Add mana supply. Returns modified mana supply in point.
- `/mana subtract` Subtract mana supply. Returns modified mana supply in point.
- `/custom effect give <entities> <effect> [<duration|infinite>] [<amplifier>]` Give custom status effect. `effect` can be `pentamana.mana_boost`, `pentamana.mana_reduction`, `pentamana.instant_mana`, `pentamana.instant_deplete`, `pentamana.mana_regeneration`, `pentamana.mana_inhibition`, `pentamana.mana_power` and `pentamana.mana_sick`.

## Modifier

Modifiers can be added to or removed from items using custom data components. They are active when equipped in the written slot.

```txt
[List] modifiers
|- [Compound]
   |- [String] attribute: `namespace:path`. Can be `pentamana:mana_capacity`, `pentamana:mana_regeneration`, `pentamana:mana_consumption` and `pentamana:casting_damage`.
   |- [Double] base: Any.
   |- [String] id: Any.
   |- [String] operation: Can be `add_value`, `add_multiplied_base` and `add_multiplied_total`.
   \- [String] slot: Can be `mainhand`, `offhand`, `feet`, `legs`, `chest` and `head`.
```

Below is an example modifier which increase mana capacity by 120(![2_point_mana_char.png](https://cdn.modrinth.com/data/UgFKzdOy/images/a26007574007d784e65c79cb957c3e0d3e94be6f.png)×60) when held in offhand.

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

## Status Effect

Status effects can be added to or removed from items using custom data components. They are applied when the item is consumed.

```txt
[List] status_effects
|- [Compound]
   |- [String] id: `namespace:path`. Can be `pentamana:mana_boost`, `pentamana:mana_reduction`, `pentamana:instant_mana`, `pentamana:instant_deplete`, `pentamana:mana_regeneration`, `pentamana:mana_inhibition`, `pentamana:mana_power` and `pentamana:mana_sickness`.
   |- [int] duration: Any.
   \- [int] amplifier: Any.
```

Below is an example status effect which increase the mana regeneration by 16(![2_point_mana_char.png](https://cdn.modrinth.com/data/UgFKzdOy/images/a26007574007d784e65c79cb957c3e0d3e94be6f.png)×8) when the item is consumed.

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

### Mana Boost

Increase mana capacity by `level * statusEffectManaBoostBase`.

### Mana Reduction

Decrease mana capacity by `level * statusEffectManaReductionBase`.

### Instant Mana

Increase mana regeneration by `2 ^ level * statusEffectInstantManaBase`.

### Instant Deplete

Decrease mana regeneration by `2 ^ level * statusEffectInstantDepleteBase`.

### Mana Regeneration

Increase mana regeneration by `manaPerPoint / statusEffectManaRegenerationBase >> level`

### Mana Inhibition

Decrease mana regeneration by `manaPerPoint / statusEffectManaInhibitionBase >> level`

### Mana Power

Increase casting damage by `level * statusEffectManaPowerBase`.

### Mana Sickness

Decrease casting damage by `level * statusEffectManaSicknessBase`.

## Enchantment

### Capacity

- Maximum level: II
- Primary items: Stick
- Secondary items: Axe, Hoe, Mace, Pickaxe, Shovel, Sword, Trident
- Enchantment weight: 2

Capacity adds extra mana capacity `level * enchantmentCapacityBase`.

### Stream

- Maximum level: II
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

### Mana Efficiency

- Maximum level: V
- Primary items: Stick
- Secondary items: Axe, Hoe, Mace, Pickaxe, Shovel, Sword, Trident
- Enchantment weight: 5

Mana Efficiency reduces the casting mana cost by `level * enchantmentUtilizationBase` percent.

## Tutorial: Create your very own magic weapon

Codes in this tutorial are licenced under CC-0.

Let's say we want a right-click weapon named `Magik Wand` that consumes 1(![1_point_mana_char.png](https://cdn.modrinth.com/data/UgFKzdOy/images/d943f1772f350c1645aef349b1c0dcd86a90296c.png)) mana per use when held in mainhand.

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

Consume the mana and fire your weapon if the consumption is successful. Consumption will succeed if the player has enough mana supply.

```java
if (!player.consumMana(1.0f)) {
  return ActionResult.PASS;
}

// Your code here
```

## Event

- `TickManaCallback` Called at the head of mana ticking.
- `RegenManaCallback` Called at the head of mana regeneration.
- `ConsumeManaCallback` Called at the head of mana consumption.

## FAQ

### Containing in mod packs

Yes, as long as the download source is modrinth.

### Back porting / porting to other mod loaders

No. You can port it yourself as long as you give credit to the original work.
