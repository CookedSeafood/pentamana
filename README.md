# Pentamana

Pentamana is an extraordinary customizable mana library for storing and modifying mana that runs server-side.

![manabar.png](https://cdn.modrinth.com/data/UgFKzdOy/images/ef535fac56d849195a46117f9f21b6f5eaa7f5b0.png)

## Configuration

Here is a template configuration file `config/pentamana.json` filled with default values. You may only need to write the lines you would like to modify.

```json
{
  "manaPerPoint": 1,
  "pointsPerCharacter": 2,
  "manaRegenBase": 2.0,
  "enchantmentCapacityBase": 2.0,
  "enchantmentStreamBase": 0.0625,
  "enchantmentUtilizationBase": 0.1,
  "enchantmentPotencyBase": 0.5,
  "statusEffectManaBoostBase": 4.0,
  "statusEffectManaReductionBase": 4.0,
  "statusEffectInstantManaBase": 4.0,
  "statusEffectInstantDepleteBase": 6.0,
  "statusEffectManaPowerBase": 3.0,
  "statusEffectManaSicknessBase": 4.0,
  "statusEffectManaRegenBase": 50,
  "statusEffectManaInhibitionBase": 40,
  "displayIdleInterval": 40,
  "displaySuppressionInterval": 40,
  "forceManaEnabled": false,
  "manaEnabled": true,
  "manaDisplay": true,
  "manaRenderType": 0,
  "manaFixedSize": 20,
  "manaCharacters": [["{\"text\":\"★\",\"color\":\"aqua\"}"],["{\"text\":\"⯪\",\"color\":\"aqua\"}"],["{\"text\":\"☆\",\"color\":\"black\"}"]]
}
```

- `manaPerPoint` Amount of mana to be considered as 1 mana point.
- `pointsPerCharacter` Amount of mana points to be considered as 1 mana character.
- `manaCapacityBase` Initial mana capacity. Should be odd.
- `manaRegenBase` Initial mana regen amount per tick.
- `enchantmentCapacityBase` Level multiplier, the result will be added to mana capacity. Sould be even.
- `enchantmentStreamBase` Level multiplier, the result will be added to mana regeneration.
- `enchantmentUtilizationBase` Level multiplier, the result in 100% will be saved.
- `enchantmentPotencyBase` Level multiplier, the result will be added to casting damage.
- `statusEffectManaBoostBase` Level multiplier, the result will be added to mana capacity.
- `statusEffectManaReductionBase` Level multiplier, the result will be substracted from mana capacity.
- `statusEffectInstantManaBase` Level multiplier, the result will be added to mana regeneration.
- `statusEffectInstantDepleteBase` Level multiplier, the result will be substracted from mana regeneration.
- `statusEffectManaRegenBase` Mana point divisor, the result will be added to regeneration.
- `statusEffectManaInhibitionBase` Mana point divisor, the result will be substracted from regeneration.
- `statusEffectManaPowerBase` Amplifier multiplier, the result will be added to casting damage.
- `statusEffectManaSicknessBase` Amplifier multiplier, the result will be added to casting damage.
- `displayIdleInterval` Ticks actionbar not updating if idle.
- `displaySuppressionInterval` Ticks actionbar not updating if interrupted.
- `maxManaCharIndexForDisplay` Literally, in characters, for perfermence sack.
- `forceManaEnabled` Make the mod enabled for every player when setting to ture, do not modify their own preference.
- `manaEnabled` Default preference.
- `manaDisplay` Default preference.
- `manaRenderType` Default preference. 0 if flex_size, 1 if fixed_size, 2 if numberic, 3 if percentage.
- `manaFixedSize` Default preference, in characters.
- `manaCharacters` Default preference. from 0% to 100% point character.

Enchantments are written in json and registered using datapack. It can be directly modified.

## Commands

- `/mana enbale` Enable this mod for yourself.
- `/mana disable` Disable this mod for yourself completely.
- `/mana set display <false|true>` Set the manabar visibility for yourself.
- `/mana set render_type <fixed_size|flex_size|numberic|percentage> [<size>]` Set the manabar render type for yourself.
- `/mana set points_per_character <value>` Set the amount of mana points to be considered as 1 mana character for yourself.
- `/mana set character <text> [<type_index>] [<character_index>]` Set the #`character_index` `type_index` point mana character for yourself.
- `/mana reset [<display|render_type|points_per_character|character>]` Reset mana options for yourself.
- `/mana reload` Reload config file. (Require premission level 2)

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

Below is an example modifier which increase mana capacity by 2,490,368(![2PointManaChar.png](https://cdn.modrinth.com/data/UgFKzdOy/images/a26007574007d784e65c79cb957c3e0d3e94be6f.png)×19) when held in offhand.

```component
[
  custom_data={
    modifiers: [
      {
        attribute: "pentamana:mana_capacity",
        base: 2490368.0d,
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

Below is an example status effect which increase the mana by 1,048,576(![2PointManaChar.png](https://cdn.modrinth.com/data/UgFKzdOy/images/a26007574007d784e65c79cb957c3e0d3e94be6f.png)×8) when the item is consumed.

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

- Mana Boost: Increase mana capacity by `level * statusEffectManaBoostBase`.
- Mana Reduction: Decrease mana capacity by `level * statusEffectManaReductionBase`.
- Instant Mana: Increase mana regeneration by `2 ^ level * statusEffectInstantManaBase`.
- Instant Deplete: Decrease mana regeneration by `2 ^ level * statusEffectInstantDepleteBase`.
- Mana Regeneration: Increase mana regeneration by `manaPerPoint / statusEffectManaRegenBase >> level`
- Mana Inhibition: Decrease mana regeneration by `manaPerPoint / statusEffectManaInhibitionBase >> level`
- Mana Power: Increase casting damage by `level * statusEffectManaPowerBase`.
- Mana Sickness: Decrease casting damage by `level * statusEffectManaSicknessBase`.

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
if (!ManaPreference.MANA_PREFERENCE.get(player).getManaEnabled()) {
  return ActionResult.PASS;
}
```

Consume the mana and fire your weapon if the consumption is successful. Consumption will succeed if the player has enough mana supply.

```java
if (ManaStatus.MANA_STATUS.get(player).consum(player, 1) == 0.0f) {
  return ActionResult.PASS;
}

// Your code here
```

## Formulas

### Mana Capacity

```java
float manaCapacity = (float)player.getCustomModifiedValue("pentamana:mana_capacity", Pentamana.manaCapacityBase);
manaCapacity += Pentamana.enchantmentCapacityBase * player.getWeaponStack().getEnchantments().getLevel("pentamana:capacity");
manaCapacity += manaStatusEffect.hasStatusEffect("pentamana:mana_boost") ?
    Pentamana.statusEffectManaBoostBase * (manaStatusEffect.getActiveStatusEffectAmplifier("pentamana:mana_boost") + 1) :
    0;
manaCapacity -= manaStatusEffect.hasStatusEffect("pentamana:mana_reduction") ?
    Pentamana.statusEffectManaReductionBase * (manaStatusEffect.getActiveStatusEffectAmplifier("pentamana:mana_reduction") + 1) :
    0;
manaCapacity = Math.max(manaCapacity, 0.0f);
```

### Mana Regeneration

```java
float manaRegen = (float)player.getCustomModifiedValue("pentamana:mana_regeneration", Pentamana.manaRegenBase);
manaRegen += Pentamana.enchantmentStreamBase * player.getWeaponStack().getEnchantments().getLevel("pentamana:stream");
manaRegen += manaStatusEffect.hasStatusEffect("pentamana:instant_mana") ? Pentamana.statusEffectInstantManaBase * Math.pow(2, manaStatusEffect.getActiveStatusEffectAmplifier("pentamana:instant_mana")) : 0;
manaRegen -= manaStatusEffect.hasStatusEffect("pentamana:instant_deplete") ? Pentamana.statusEffectInstantDepleteBase * Math.pow(2, manaStatusEffect.getActiveStatusEffectAmplifier("pentamana:instant_deplete")) : 0;
manaRegen += manaStatusEffect.hasStatusEffect("pentamana:mana_regeneration") ? Pentamana.manaPerPoint / (float)Math.max(1, Pentamana.statusEffectManaRegenBase >> manaStatusEffect.getActiveStatusEffectAmplifier("pentamana:mana_regeneration")) : 0;
manaRegen -= manaStatusEffect.hasStatusEffect("pentamana:mana_inhibition") ? Pentamana.manaPerPoint / (float)Math.max(1, Pentamana.statusEffectManaInhibitionBase >> manaStatusEffect.getActiveStatusEffectAmplifier("pentamana:mana_inhibition")) : 0;
```

### Mana Consumption

```java
float targetManaConsume = (float)player.getCustomModifiedValue("pentamana:mana_consumption", manaConsume);
targetManaConsume *= 1 - Pentamana.enchantmentUtilizationBase * player.getWeaponStack().getEnchantments().getLevel("pentamana:utilization");
```

### Casting Damage

```java
float castingDamage = manaCapacity;
castingDamage /= Pentamana.manaCapacityBase;
castingDamage *= (float)player.getCustomModifiedValue("pentamana:casting_damage", baseDamage);
castingDamage += potencyLevel != 0 ?
  ++potencyLevel * Pentamana.enchantmentPotencyBase / Integer.MAX_VALUE :
  0;
castingDamage += manaStatusEffect.hasStatusEffect("pentamana:mana_power") ?
  (manaStatusEffect.getActiveStatusEffectAmplifier("pentamana:mana_power") + 1) * Pentamana.statusEffectManaPowerBase :
  0;
castingDamage -= manaStatusEffect.hasStatusEffect("pentamana:mana_sickness") ?
  (manaStatusEffect.getActiveStatusEffectAmplifier("pentamana:mana_sickness") + 1) * Pentamana.statusEffectManaSicknessBase :
  0;
castingDamage = Math.max(castingDamage, 0.0f);
castingDamage *= entity instanceof WitchEntity ? 0.15f : 1;
```

## Events

- `TickManaCallback` Called after the mana capacity calculation, before everything else.
- `RegenManaCallback` Called when a player is regenerating mana. After the mana regeneration calculation, before regenerating mana.
- `ConsumeManaCallback` Called when a player is consuming mana. After the mana consumption calculation, before consuming mana.

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
