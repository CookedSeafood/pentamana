# Pentamana

Pentamana is a very customizable mana library handles storing, ticking, and displaying mana that runs server-side.

If you'd like to add a feature, feel free to open an issue on [github issues](https://github.com/CookedSeafood/pentamana/issues).

## Concept

In pentamana, mana are stored and ticked on each living entity.

![Compound](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/compound.png) **data**: Parent tag.  
&ensp;|- ![Float](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/float.png) **mana**: Any  
&ensp;\\- ![Float](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/float.png) **mana_capacity**: Any

While we have `mana` and `mana_capacity` stored, `mana_capacity` is stored for the displaying purpose solely. Value of `mana_capacity` and `mana_regeneration` are calculated before `mana` for various condition changings(enchantments, status effects, modifiers, etc).

Mana can be measured in 3 ways:

- ![Float](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/float.png) Mana: For storing and calculating.
- ![Int](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/int.png) Mana Point: For displaying as number. Converted from `mana / manaPerPoint`.
- ![Int](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/int.png) Mana Character: For displaying as character status bar. Converted from `manaPoint / pointsPerCharacter`.

Manabar preferences are stored and manabars are generated and ticked on each player entity.

![Compound](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/compound.png) **data**: Parent tag.  
&ensp;\\- ![Compound](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/compound.png) **pentamana_preference**  
&emsp;&emsp;|- ![Boolean](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/boolean.png) **visibility**: True if manabar should be visible.  
&emsp;&emsp;|- ![Boolean](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/boolean.png) **suppression**: True if manabar display update should be suppressed.  
&emsp;&emsp;|- ![String](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/string.png) **position**: Manabar position. Can be `actionbar`, `bossbar` and `siderbar`.  
&emsp;&emsp;|- ![String](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/string.png) **type**: Manabar render type. Can be `character`, `numeric`, `percentage` and `none`.  
&emsp;&emsp;|- ![Compound](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/compound.png) **pattern**: Manabar text pattern. Manabar render text is respresented by `$`.  
&emsp;&emsp;|- ![Int](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/int.png) **pointsPerCharacter**: Amount of mana points to be considered as 1 mana character.  
&emsp;&emsp;|- ![Boolean](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/boolean.png) **compression**: True if fixed size should be used if `type` is `character`.  
&emsp;&emsp;|- ![Byte](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/byte.png) **compression_size**: The size in characters of compression.  
&emsp;&emsp;|- ![Compound](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/compound.png) **charset**: The 2-dim charset used if `type` is `character`. Can define mana-character with 128 index and 128 states. From 100% to 0% state, then from first index to last index.  
&emsp;&emsp;|- ![String](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/string.png) **color**: The color of bossbar which act as manabar.  
&emsp;&emsp;\\- ![String](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/string.png) **style**: The style of bossbar which act as manabar.

## Configuration

Here is a template configuration file `config/pentamana.json` filled with default values. You may only need to write the lines you would like to modify.

```json
{
  "manaPerPoint": 1,
  "manaCapacityBase": 2.0,
  "manaRegenerationBase": 0.0625,
  "enchantmentCapacityBase": 2.0,
  "enchantmentStreamBase": 0.015625,
  "enchantmentManaEfficiencyBase": 0.1,
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
    "pointsPerCharacter": 2,
    "isCompressed": false,
    "compressionSize": 20,
    "charset": [[{"text":"★","color":"aqua"}],[{"text":"⯪","color":"aqua"}],[{"text":"☆","color":"black"}]],
    "color": "blue",
    "style": "progress"
  }
}
```

`manaCapacityBase`, `enchantmentCapacityBase`, `statusEffectManaBoostBase`, `statusEffectManaReductionBase`, `shouldConvertExperienceLevel`, `experienceLevelConversionBase` are used by the formula below:

```java
MutableFloat capacity = new MutableFloat((float)livingEntity.getCustomModifiedValue(PentamanaAttributeIdentifiers.MANA_CAPACITY, base));
livingEntity.getEnchantments(Enchantments.CAPACITY).forEach(entry -> capacity.setValue(capacity.floatValue() + PentamanaConfig.HANDLER.instance().enchantmentCapacityBase * (entry.getIntValue() + 1)));
return Math.max(
  capacity.floatValue()
    + (statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.MANA_BOOST) ? PentamanaConfig.HANDLER.instance().statusEffectManaBoostBase * (statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_BOOST) + 1) : 0)
    - (statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.MANA_REDUCTION) ? PentamanaConfig.HANDLER.instance().statusEffectManaReductionBase * (statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_REDUCTION) + 1) : 0)
    + (PentamanaConfig.HANDLER.instance().shouldConvertExperienceLevel && livingEntity instanceof ServerPlayerEntity ? PentamanaConfig.HANDLER.instance().experienceLevelConversionBase * ((ServerPlayerEntity)livingEntity).experienceLevel : 0),
  0.0f
);
```

`manaRegenerationBase`, `enchantmentStreamBase`, `statusEffectInstantManaBase`, `statusEffectInstantDepleteBase`, `statusEffectManaRegenerationBase`, `statusEffectManaInhibitionBase` are used by the formula below:

```java
MutableFloat regen = new MutableFloat((float)livingEntity.getCustomModifiedValue(PentamanaAttributeIdentifiers.MANA_REGENERATION, base));
livingEntity.getEnchantments(Enchantments.STREAM).forEach(entry -> regen.setValue(regen.floatValue() + PentamanaConfig.HANDLER.instance().enchantmentStreamBase * (entry.getIntValue() + 1)));
return regen.floatValue()
  + (statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.INSTANT_MANA) ? PentamanaConfig.HANDLER.instance().statusEffectInstantManaBase * (float)Math.pow(2.0, statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.INSTANT_MANA)) : 0)
  - (statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.INSTANT_DEPLETE) ? PentamanaConfig.HANDLER.instance().statusEffectInstantDepleteBase * (float)Math.pow(2.0, statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.INSTANT_DEPLETE)) : 0)
  + (statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.MANA_REGENERATION) ? PentamanaConfig.HANDLER.instance().manaPerPoint / Math.max(1, PentamanaConfig.HANDLER.instance().statusEffectManaRegenerationBase >> statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_REGENERATION)) : 0)
  - (statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.MANA_INHIBITION) ? PentamanaConfig.HANDLER.instance().manaPerPoint / Math.max(1, PentamanaConfig.HANDLER.instance().statusEffectManaInhibitionBase >> statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_INHIBITION)) : 0);
```

`enchantmentManaEfficiencyBase` is used by the formula below:

```java
MutableFloat consum = new MutableFloat((float)livingEntity.getCustomModifiedValue(PentamanaAttributeIdentifiers.MANA_CONSUMPTION, amount));
livingEntity.getEnchantments(Enchantments.MANA_EFFICIENCY).forEach(entry -> consum.setValue(consum.floatValue() * (1 - PentamanaConfig.HANDLER.instance().enchantmentManaEfficiencyBase * (entry.getIntValue() + 1))));
```

`enchantmentPotencyBase`, `statusEffectManaPowerBase`, `statusEffectManaSicknessBase` are used by the formula below:

```java
MutableFloat damage = new MutableFloat(this.getManaCapacity() / PentamanaConfig.HANDLER.instance().manaCapacityBase * (float)livingEntity.getCustomModifiedValue(PentamanaAttributeIdentifiers.CASTING_DAMAGE, baseDamage));
livingEntity.getEnchantments(Enchantments.POTENCY).forEach(entry -> damage.setValue(damage.floatValue() + PentamanaConfig.HANDLER.instance().enchantmentPotencyBase * (entry.getIntValue() + 1)));
return Math.max(
  (
    damage.floatValue()
      + (statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.MANA_POWER) ? (statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_POWER) + 1) * PentamanaConfig.HANDLER.instance().statusEffectManaPowerBase : 0)
      - (statusEffectManager.containsKey(PentamanaStatusEffectIdentifiers.MANA_SICKNESS) ? (statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_SICKNESS) + 1) * PentamanaConfig.HANDLER.instance().statusEffectManaSicknessBase : 0)
  )
  * (entity instanceof WitchEntity ? 0.15f : 1),
  0.0f
);
```

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

![Compound](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/compound.png) **custom_data**: Parent tag.  
&ensp;\\- ![List](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/list.png) **modifiers**  
&emsp;&emsp;\\- ![Compound](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/compound.png)  
&emsp;&emsp;&emsp;&ensp;|- ![String](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/string.png) **attribute**: `namespace:path`. Can be `pentamana:mana_capacity`, `pentamana:mana_regeneration`, `pentamana:mana_consumption` and `pentamana:casting_damage`.  
&emsp;&emsp;&emsp;&ensp;|- ![Double](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/double.png) **base**: Any.  
&emsp;&emsp;&emsp;&ensp;|- ![String](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/string.png) **id**: Any.  
&emsp;&emsp;&emsp;&ensp;|- ![String](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/string.png) **operation**: Can be `add_value`, `add_multiplied_base` and `add_multiplied_total`.  
&emsp;&emsp;&emsp;&ensp;\\- ![String](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/string.png) **slot**: Can be `mainhand`, `offhand`, `feet`, `legs`, `chest` and `head`.

Below is an example modifier which increase mana capacity by 120(![2_point_mana_char.png](https://cdn.modrinth.com/data/UgFKzdOy/images/a26007574007d784e65c79cb957c3e0d3e94be6f.png)×60) when held in offhand.

```component
[
  custom_data={
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

![Compound](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/compound.png) **custom_data**: Parent tag.  
&ensp;\\- ![List](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/list.png) **status_effects**  
&emsp;&emsp;\\- ![Compound](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/compound.png)  
&emsp;&emsp;&emsp;&ensp;|- ![String](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/string.png) **id**: `namespace:path`. Can be `pentamana:mana_boost`, `pentamana:mana_reduction`, `pentamana:instant_mana`, `pentamana:instant_deplete`, `pentamana:mana_regeneration`, `pentamana:mana_inhibition`, `pentamana:mana_power` and `pentamana:mana_sickness`.  
&emsp;&emsp;&emsp;&ensp;|- ![Int](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/int.png) **duration**: Any.  
&emsp;&emsp;&emsp;&ensp;\\- ![Int](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/int.png) **amplifier**: Any.

Below is an example status effect which increase the mana regeneration by 16(![2_point_mana_char.png](https://cdn.modrinth.com/data/UgFKzdOy/images/a26007574007d784e65c79cb957c3e0d3e94be6f.png)×8) when the item is consumed.

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

### Mana Efficiency

- Maximum level: V
- Primary items: Stick
- Secondary items: Axe, Hoe, Mace, Pickaxe, Shovel, Sword, Trident
- Enchantment weight: 5

Mana Efficiency reduces the casting mana cost by `level * enchantmentManaEfficiencyBase` percent.

### Potency

- Maximum level: V
- Primary items: Stick
- Secondary items: Axe, Hoe, Mace, Pickaxe, Shovel, Sword, Trident
- Enchantment weight: 10

Potency adds the casting damage by `(level + 1) * enchantmentPotencyBase`.

## Tutorial

Codes in this tutorial are licenced under CC-0.

### Installation

`gradle.properties`:

```properties
pentamana_version=1.0.0
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

### Create an item that consumes mana

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

### Back porting / porting to other mod loaders

No. You can port it yourself as long as you give credit to the original work.
