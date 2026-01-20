# Pentamana

Pentamana is a very customizable mana library handles storing, ticking, and displaying mana that runs server-side.

If you'd like to add a feature, feel free to open an issue on [github issues](https://github.com/CookedSeafood/pentamana/issues).

## Concept

In pentamana, mana are stored and ticked on each living entity.

![Compound](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/compound.png) **data**: Parent tag.  
&ensp;|- ![Float](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/float.png) **mana**: Any  
&ensp;\\- ![Float](https://github.com/CookedSeafood/nbtsheet/raw/0cfc19cc5644a82c921d39f9c40729aca3dea33d/float.png) **mana_capacity**: Any

`mana_capacity` is stored for the displaying purpose solely. Value of `mana_capacity` and `mana_regeneration` are calculated before `mana` for various condition changings(enchantments, status effects, modifiers, etc).

## Configuration

`config/pentamana/server.json`:

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
  "experienceLevelConversionBase": 0.5
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
- `/pentamana set ... <...>` Set server config and save to file.
- `/pentamana reset [...]` Set server config to default and save to file.
- `/custom effect give <entities> <effect> [<duration|infinite>] [<amplifier>]` Give custom status effect. `effect` can be `pentamana.mana_boost`, `pentamana.mana_reduction`, `pentamana.instant_mana`, `pentamana.instant_deplete`, `pentamana.mana_regeneration`, `pentamana.mana_inhibition`, `pentamana.mana_power` and `pentamana.mana_sick`.

### Client Command

- `/manabar set ... <...>` Set manabar config and save to file.
- `/manabar reset [...]` Set manabar config to default and save to file.

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
pentamana_version=2.0.0-alpha.1
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
