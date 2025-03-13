package net.cookedseafood.pentamana.api.component;

import java.util.List;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.text.Text;
import org.ladysnake.cca.api.v3.component.Component;

public interface ManaPreferenceComponent extends Component {
    boolean isEnabled();

    void setIsEnabled(boolean isEnabled);

    boolean isVisible();

    void setIsVisible(boolean isVisible);

    boolean isCompression();

    void setIsCompression(boolean isCompression);

    byte getCompressionSize();

    void setCompressionSize(byte compressionSize);

    Text getManabarPattern();

    void setManabarPattern(Text manabarPattern);

    byte getManabarType();

    void setManabarType(byte manabarType);

    byte getManabarPosition();

    void setManabarPosition(byte manabarPosition);

    BossBar.Color getManabarColor();

    void setManabarColor(BossBar.Color manabarColor);

    BossBar.Style getManabarStyle();

    void setManabarStyle(BossBar.Style manabarStyle);

    int getPointsPerCharacter();

    void setPointsPerCharacter(int pointsPerCharacter);

    List<List<Text>> getManaCharacter();

    void setManaCharacter(List<List<Text>> manaCharacter);
}
