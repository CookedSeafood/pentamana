package net.cookedseafood.pentamana.api.component;

import java.util.List;
import net.minecraft.text.Text;
import org.ladysnake.cca.api.v3.component.Component;

public interface ManaPreferenceComponent extends Component {
    boolean getEnabled();

    void setEnabled(boolean isEnabled);

    boolean getVisibility();

    void setVisibility(boolean isVisible);

    byte getManaRenderType();

    void setManaRenderType(byte manaRenderType);

    int getManaFixedSize();

    void setManaFixedSize(int manaFixedSize);

    int getPointsPerCharacter();

    void setPointsPerCharacter(int pointsPerCharacter);

    List<List<Text>> getManaCharacters();

    void setManaCharacters(List<List<Text>> manaCharacters);
}
