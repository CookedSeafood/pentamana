package net.cookedseafood.pentamana.api.component;

import java.util.List;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.ladysnake.cca.api.v3.component.Component;

public interface ManaDisplayComponent extends Component {
    void tick(ServerPlayerEntity player);

    boolean isSuppressed();

    void updateManabar(ServerPlayerEntity player, Text manabar, byte manabarPosition, BossBar.Color manabarColor, BossBar.Style manabarStyle);

    void updateManabarInActionbar(ServerPlayerEntity player, Text manabar);

    void updateManabarInBossbar(ServerPlayerEntity player, Text manabar, BossBar.Color manabarColor, BossBar.Style manabarStyle);

    void finishManabar(ServerPlayerEntity player, byte manabarPosition);

    void finishManabarInActionbar(ServerPlayerEntity player);

    void finishManabarInBossBar(ServerPlayerEntity player);

    Text toPattern(byte manabarType, Text manabarPattern, int pointsPerCharacter, List<List<Text>> manaCharacter);

    Text toText(byte manabarType, int pointsPerCharacter, List<List<Text>> manaCharacter);

    Text toCharacterText(int pointsPerCharacter, List<List<Text>> manaCharacter);

    Text toNumericText();

    Text toPercentageText();

    Text toFixedText(Text fixedText);

    Text toNoneText();

    byte getManabarLife();

    byte setManabarLife(byte manabarLife);

    default byte incrementManabarLife() {
        return incrementManabarLife((byte)1);
    };

    default byte incrementManabarLife(byte value) {
        return setManabarLife((byte)(this.getManabarLife() + value));
    };

    boolean getLastIsVisible();

    void setLastIsVisible(boolean lastIsVisible);

    int getLastManabarPattern();

    void setLastManabarPattern(int lastManabarPattern);

    byte getLastManabarType();

    void setLastManabarType(byte lastManabarType);

    byte getLastManabarPosition();

    byte setLastManabarPosition(byte lastManabarPosition);

    int getLastManaSupplyPoint();

    int setLastManaSupplyPoint(int lastManaSupplyPoint);

    int getLastManaCapacityPoint();

    int setLastManaCapacityPoint(int lastManaCapacityPoint);
}
