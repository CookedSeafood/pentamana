package net.cookedseafood.pentamana.api.component;

import net.minecraft.entity.player.PlayerEntity;
import org.ladysnake.cca.api.v3.component.Component;

public interface ManaStatusComponent extends Component {
    float tick(PlayerEntity player);

    float regen(PlayerEntity player);

    float regen(PlayerEntity player, float manaRegen);

    float consum(PlayerEntity player, float manaConsume);

    float getManaSupply();

    float setManaSupply(float manaSupply);

    default float incrementManaSupply() {
        return incrementManaSupply(1.0f);
    };

    default float incrementManaSupply(float manaSupply) {
        return setManaSupply(this.getManaSupply() + manaSupply);
    };

    float getManaCapacity();

    float setManaCapacity(float manaCapacity);

    default float incrementManaCapacity() {
        return incrementManaCapacity(1.0f);
    };

    default float incrementManaCapacity(float manaCapacity) {
        return setManaCapacity(this.getManaCapacity() + manaCapacity);
    };
}
