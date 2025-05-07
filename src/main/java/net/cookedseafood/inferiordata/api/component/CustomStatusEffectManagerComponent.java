package net.cookedseafood.inferiordata.api.component;

import net.cookedseafood.inferiordata.effect.CustomStatusEffectManager;
import org.ladysnake.cca.api.v3.component.Component;

public interface CustomStatusEffectManagerComponent extends Component {
    CustomStatusEffectManager getStatusEffectManager();

    void setStatusEffectManager(CustomStatusEffectManager statusEffectManager);
}
