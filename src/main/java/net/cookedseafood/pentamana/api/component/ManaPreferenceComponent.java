package net.cookedseafood.pentamana.api.component;

import org.ladysnake.cca.api.v3.component.Component;

public interface ManaPreferenceComponent extends Component {
    boolean isEnabled();

    void setIsEnabled(boolean isEnabled);
}
