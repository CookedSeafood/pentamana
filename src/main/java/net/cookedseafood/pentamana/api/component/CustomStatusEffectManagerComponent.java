package net.cookedseafood.pentamana.api.component;

import net.cookedseafood.pentamana.effect.CustomStatusEffectManager;
import org.ladysnake.cca.api.v3.component.Component;

public interface CustomStatusEffectManagerComponent extends Component {
	CustomStatusEffectManager getStatusEffectManager();

	void setStatusEffectManager(CustomStatusEffectManager statusEffectManager);
}
