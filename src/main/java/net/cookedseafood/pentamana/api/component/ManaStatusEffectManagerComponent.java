package net.cookedseafood.pentamana.api.component;

import net.cookedseafood.pentamana.mana.ManaStatusEffectManager;
import org.ladysnake.cca.api.v3.component.Component;

public interface ManaStatusEffectManagerComponent extends Component {
    ManaStatusEffectManager getStatusEffectManager();

    void setStatusEffectManager(ManaStatusEffectManager statusEffectManager);
}
