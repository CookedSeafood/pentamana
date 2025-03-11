package net.cookedseafood.pentamana.api.component;

import java.util.List;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.ladysnake.cca.api.v3.component.Component;

public interface ManaDisplayComponent extends Component {
    void tick(ServerPlayerEntity player);

    boolean isSuppressed();

    void updateManabar(ServerPlayerEntity player, Text manabar, byte manabarPosition, BossBar.Color bossbarColor, BossBar.Style bossbarStyle);

    void updateManabarInActionbar(ServerPlayerEntity player, Text manabar);

    void updateManabarInBossbar(ServerPlayerEntity player, Text manabar, BossBar.Color bossbarColor, BossBar.Style bossbarStyle);

    void finishManabar(ServerPlayerEntity player, byte manabarPosition);

    void finishManabarInActionbar(ServerPlayerEntity player);

    void finishManabarInBossBar(ServerPlayerEntity player);

    Text toText(byte manabarType, int pointsPerCharacter, List<List<Text>> manaCharacters);

    Text toCharacterText(int pointsPerCharacter, List<List<Text>> manaCharacters);

    Text toNumericText();

    Text toPercentageText();

    Text toNoneText();

    byte getManabarLife();

    byte setManabarLife(byte manabarLife);

    default byte incrementManabarLife() {
        return incrementManabarLife((byte)1);
    };

    default byte incrementManabarLife(byte value) {
        return setManabarLife((byte)(this.getManabarLife() + value));
    };

    byte getLastManabarPosition();

    byte setLastManabarPosition(byte lastManabarPosition);

    default byte incrementLastManabarPosition() {
        return incrementLastManabarPosition((byte)1);
    };

    default byte incrementLastManabarPosition(byte value) {
        return setLastManabarPosition((byte)(this.getLastManabarPosition() + value));
    };

    int getLastManaSupplyPoint();

    int setLastManaSupplyPoint(int lastManaSupplyPoint);

    default int incrementLastManaSupplyPoint() {
        return incrementLastManaSupplyPoint(1);
    };

    default int incrementLastManaSupplyPoint(int value) {
        return setLastManaSupplyPoint(this.getLastManaSupplyPoint() + value);
    };

    int getLastManaCapacityPoint();

    int setLastManaCapacityPoint(int lastManaCapacityPoint);

    default int incrementLastManaCapacityPoint() {
        return incrementLastManaCapacityPoint(1);
    };

    default int incrementLastManaCapacityPoint(int value) {
        return setLastManaCapacityPoint(this.getLastManaCapacityPoint() + value);
    };

    boolean getLastIsVisible();

    void setLastIsVisible(boolean lastIsVisible);
}
