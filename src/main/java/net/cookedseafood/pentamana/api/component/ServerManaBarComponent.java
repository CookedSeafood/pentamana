package net.cookedseafood.pentamana.api.component;

import net.cookedseafood.pentamana.mana.ServerManaBar;
import org.ladysnake.cca.api.v3.component.Component;

public interface ServerManaBarComponent extends Component {
	ServerManaBar getServerManaBar();

	void setServerManaBar(ServerManaBar serverManaBar);
}
