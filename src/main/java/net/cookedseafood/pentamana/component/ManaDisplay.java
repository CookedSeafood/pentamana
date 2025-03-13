package net.cookedseafood.pentamana.component;

import java.util.List;
import net.cookedseafood.pentamana.Pentamana;
import net.cookedseafood.pentamana.api.component.ManaDisplayComponent;
import net.cookedseafood.pentamana.render.ManabarPositions;
import net.cookedseafood.pentamana.render.ManabarTypes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.BossBarManager;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnableComponent;

public class ManaDisplay implements ManaDisplayComponent, EntityComponentInitializer, RespawnableComponent<ManaDisplay> {
    public static final ComponentKey<ManaDisplay> MANA_DISPLAY =
        ComponentRegistry.getOrCreate(Identifier.of(Pentamana.MOD_ID, "mana_display"), ManaDisplay.class);
    private byte manabarLife;
    private boolean lastIsVisible;
    private int lastManabarPattern;
    private byte lastManabarType;
    private byte lastManabarPosition;
    private int lastManaSupplyPoint;
    private int lastManaCapacityPoint;

    public ManaDisplay() {
    }

    @Override
    public void tick(ServerPlayerEntity player) {
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        if (manaPreference.isVisible()) {
            tickVisible(player, manaPreference);
            ++this.manabarLife;
            return;
        }

        tickInvisible(player, manaPreference);
    }

    private void tickVisible(ServerPlayerEntity player, ManaPreference manaPreference) {
        this.lastIsVisible = true;
        boolean isCompression = manaPreference.isCompression();
        byte compressionSize = manaPreference.getCompressionSize();
        Text manabarPattern = manaPreference.getManabarPattern();
        byte manabarType = manaPreference.getManabarType();
        byte manabarPosition = manaPreference.getManabarPosition();
        int pointsPerCharacter = manaPreference.getPointsPerCharacter();
        if (this.isSuppressed() || !this.isOutdate(player, manabarPattern, manabarType, manabarPosition, pointsPerCharacter, isCompression, compressionSize)) {
            return;
        }

        this.manabarLife = (byte)-Pentamana.displayIdleInterval;
        this.updateManabar(player, this.toPattern(manabarType, manabarPattern, pointsPerCharacter, manaPreference.getManaCharacter()), manabarPosition, manaPreference.getManabarColor(), manaPreference.getManabarStyle());
    }

    private void tickInvisible(ServerPlayerEntity player, ManaPreference manaPreference) {
        if (!this.lastIsVisible) {
            return;
        }

        this.lastIsVisible = false;
        this.manabarLife = 0;
        this.finishManabar(player, manaPreference.getManabarPosition());
    }

    @Override
    public boolean isSuppressed() {
        return this.manabarLife > (byte)0 && this.manabarLife < Pentamana.displaySuppressionInterval;
    }

    private boolean isOutdate(ServerPlayerEntity player, Text manabarPattern, byte manabarType, byte manabarPosition, int pointsPerCharacter, boolean isCompression, byte compressionSize) {
        ManaStatus manaStatus = ManaStatus.MANA_STATUS.get(player);
        return this.tryUpdateStatus(manabarType, manaStatus.getManaCapacity(), manaStatus.getManaSupply(), pointsPerCharacter, isCompression, compressionSize)
        || this.manabarLife >= (byte)0
        || this.tryUpdateManabarPattern(player, manabarPattern)
        || this.tryUpdateManabarType(player, manabarType)
        || this.tryUpdateManabarPosition(player, manabarPosition);
    }

    private boolean tryUpdateStatus(byte manabarType, float manaCapacity, float manaSupply, int pointsPerCharacter, boolean isCompression, byte compressionSize) {
        return manabarType == ManabarTypes.CHARACTER.getIndex() ?
            this.tryUpdateStatusInCharacter(manaCapacity, manaSupply, pointsPerCharacter, isCompression, compressionSize) :
            manabarType == ManabarTypes.NUMERIC.getIndex() ?
            this.tryUpdateStatusInNumeric(manaCapacity, manaSupply) :
            manabarType == ManabarTypes.PERCENTAGE.getIndex() ?
            this.tryUpdateStatusInPercentage(manaCapacity, manaSupply) :
            manabarType == ManabarTypes.NONE.getIndex() ?
            this.tryUpdateStatusInNone(manaCapacity, manaSupply) :
            false;
    }

    private boolean tryUpdateStatusInCharacter(float manaCapacity, float manaSupply, int pointsPerCharacter, boolean isCompression, byte compressionSize) {
        int manaCapacityPoint;
        int manaSupplyPoint;
        if (!isCompression) {
            manaCapacityPoint = (int)(manaCapacity / Pentamana.manaPerPoint);
            manaSupplyPoint = (int)(manaSupply / Pentamana.manaPerPoint);
        } else {
            manaCapacityPoint = pointsPerCharacter * compressionSize;
            manaSupplyPoint = (int)(manaSupply / manaCapacity * manaCapacityPoint);
        }

        if (manaCapacityPoint == this.lastManaCapacityPoint && manaSupplyPoint == this.lastManaSupplyPoint) {
            return false;
        }

        this.lastManaCapacityPoint = manaCapacityPoint;
        this.lastManaSupplyPoint = manaSupplyPoint;
        return true;
    }

    private boolean tryUpdateStatusInNumeric(float manaCapacity, float manaSupply) {
        int manaCapacityPoint = (int)(manaCapacity / Pentamana.manaPerPoint);
        int manaSupplyPoint = (int)(manaSupply / Pentamana.manaPerPoint);

        if (manaCapacityPoint == this.lastManaCapacityPoint && manaSupplyPoint == this.lastManaSupplyPoint) {
            return false;
        }

        this.lastManaCapacityPoint = manaCapacityPoint;
        this.lastManaSupplyPoint = manaSupplyPoint;
        return true;
    }

    private boolean tryUpdateStatusInPercentage(float manaCapacity, float manaSupply) {
        byte manaSupplyPoint = (byte)(manaSupply / manaCapacity * 100);
        if (manaSupplyPoint == this.lastManaSupplyPoint) {
            return false;
        }

        this.lastManaSupplyPoint = manaSupplyPoint;
        return true;
    }

    private boolean tryUpdateStatusInNone(float manaCapacity, float manaSupply) {
        int manaCapacityPoint = (int)(manaCapacity / Pentamana.manaPerPoint);
        int manaSupplyPoint = (int)(manaSupply / Pentamana.manaPerPoint);

        if (manaCapacityPoint == this.lastManaCapacityPoint && manaSupplyPoint == this.lastManaSupplyPoint) {
            return false;
        }

        this.lastManaCapacityPoint = manaCapacityPoint;
        this.lastManaSupplyPoint = manaSupplyPoint;
        return true;
    }

    private boolean tryUpdateManabarPattern(ServerPlayerEntity player, Text manabarPattern) {
        int hashCode = manabarPattern.hashCode();
        if (this.lastManabarPattern == hashCode) {
            return false;
        }

        this.lastManabarPattern = hashCode;
        return true;
    }

    private boolean tryUpdateManabarType(ServerPlayerEntity player, byte manabarType) {
        if (manabarType == this.lastManabarType) {
            return false;
        }

        this.lastManabarType = manabarType;
        return true;
    }

    private boolean tryUpdateManabarPosition(ServerPlayerEntity player, byte manabarPosition) {
        if (manabarPosition == this.lastManabarPosition) {
            return false;
        }

        this.finishManabar(player, this.lastManabarPosition);
        this.lastManabarPosition = manabarPosition;
        return true;
    }

    @Override
    public void updateManabar(ServerPlayerEntity player, Text manabar, byte manabarPosition, BossBar.Color manabarColor, BossBar.Style manabarStyle) {
        if (manabarPosition == ManabarPositions.ACTIONBAR.getIndex()) {
            this.updateManabarInActionbar(player, manabar);
        } else if (manabarPosition == ManabarPositions.BOSSBAR.getIndex()) {
            this.updateManabarInBossbar(player, manabar, manabarColor, manabarStyle);
        }
    }

    @Override
    public void updateManabarInActionbar(ServerPlayerEntity player, Text manabar) {
        player.sendMessage(manabar, true);
    }

    @Override
    public void updateManabarInBossbar(ServerPlayerEntity player, Text manabar, BossBar.Color manabarColor, BossBar.Style manabarStyle) {
        CommandBossBar bossbar = player.getServer().getBossBarManager().getOrAdd(Identifier.of(Pentamana.MOD_ID, "manabar." + player.getUuidAsString()), manabar);
        bossbar.setMaxValue(this.lastManaCapacityPoint);
        bossbar.setValue(this.lastManaSupplyPoint);
        bossbar.setName(manabar);
        bossbar.setColor(manabarColor);
        bossbar.setStyle(manabarStyle);
        bossbar.addPlayer(player);
    }

    @Override
    public void finishManabar(ServerPlayerEntity player, byte manabarPosition) {
        if (manabarPosition == ManabarPositions.ACTIONBAR.getIndex()) {
            this.finishManabarInActionbar(player);
        } else if (manabarPosition == ManabarPositions.BOSSBAR.getIndex()) {
            this.finishManabarInBossBar(player);
        }
    }

    @Override
    public void finishManabarInActionbar(ServerPlayerEntity player) {
        player.sendMessage(Text.literal(""), true);
    }

    @Override
    public void finishManabarInBossBar(ServerPlayerEntity player) {
        BossBarManager bossbarManager = player.getServer().getBossBarManager();
        Identifier id = Identifier.of(Pentamana.MOD_ID, "manabar." + player.getUuidAsString());
        if (bossbarManager.contains(id)) {
            bossbarManager.get(id).clearPlayers();
            bossbarManager.remove(id);
        }
    }

    @Override
    public Text toPattern(byte manabarType, Text manabarPattern, int pointsPerCharacter, List<List<Text>> manaCharacter) {
        List<Text> siblings = manabarPattern.getSiblings();
        if (!siblings.stream().anyMatch(sibling -> "$".equals(sibling.getString()))) {
            return manabarPattern;
        }

        MutableText manabarText = Text.empty().setStyle(manabarPattern.getStyle());
        siblings.stream()
            .map(sibling -> "$".equals(sibling.getString()) ? this.toText(manabarType, pointsPerCharacter, manaCharacter) : sibling)
            .forEach(sibling -> manabarText.append(sibling));
        return manabarText;
    }

    @Override
    public Text toText(byte manabarType, int pointsPerCharacter, List<List<Text>> manaCharacter) {
        return manabarType == ManabarTypes.CHARACTER.getIndex() ?
            this.toCharacterText(pointsPerCharacter, manaCharacter) :
            manabarType == ManabarTypes.NUMERIC.getIndex() ?
            this.toNumericText() :
            manabarType == ManabarTypes.PERCENTAGE.getIndex() ?
            this.toPercentageText() :
            manabarType == ManabarTypes.NONE.getIndex() ?
            this.toNoneText() :
            Text.empty();
    }

    @Override
    public Text toCharacterText(int pointsPerCharacter, List<List<Text>> manaCharacter) {
        int manaCapacityPointTrimmed = this.lastManaCapacityPoint - this.lastManaCapacityPoint % pointsPerCharacter;
        int manaPointTrimmed = this.lastManaSupplyPoint - this.lastManaSupplyPoint % pointsPerCharacter;

        manaCapacityPointTrimmed = Math.min(manaCapacityPointTrimmed, Pentamana.manaPointLimit);

        MutableText manabar = Text.empty();
        for (int manaPointIndex = 0; manaPointIndex < manaCapacityPointTrimmed; manaPointIndex += pointsPerCharacter) {
            int manaCharacterTypeIndex =
                manaPointIndex < manaPointTrimmed ?
                0 : manaPointIndex < lastManaSupplyPoint ?
                lastManaSupplyPoint - manaPointIndex : pointsPerCharacter;
            int manaCharacterIndex = manaPointIndex / pointsPerCharacter;

            Text SelectedManaCharacter = manaCharacter.get(manaCharacterTypeIndex).get(manaCharacterIndex);
            manabar.append(SelectedManaCharacter);
        }

        return manabar;
    }

    @Override
    public Text toNumericText() {
        return Text.literal(this.lastManaSupplyPoint + "/" + this.lastManaCapacityPoint).setStyle(Style.EMPTY.withColor(Formatting.AQUA));
    }

    @Override
    public Text toPercentageText() {
        return Text.literal(this.lastManaSupplyPoint + "%").setStyle(Style.EMPTY.withColor(Formatting.AQUA));
    }

    @Override
    public Text toFixedText(Text fixedText) {
        return fixedText;
    }

    @Override
    public Text toNoneText() {
        return Text.empty();
    }

    @Override
    public byte getManabarLife() {
        return manabarLife;
    }

    @Override
    public byte setManabarLife(byte manabarLife) {
        return this.manabarLife = manabarLife;
    }

    @Override
    public boolean getLastIsVisible() {
        return this.lastIsVisible;
    }

    @Override
    public void setLastIsVisible(boolean lastIsVisible) {
        this.lastIsVisible = lastIsVisible;
    }

    @Override
    public int getLastManabarPattern() {
        return this.lastManabarPattern;
    }

    @Override
    public void setLastManabarPattern(int lastManabarPattern) {
        this.lastManabarPattern = lastManabarPattern;
    }

    @Override
    public byte getLastManabarType() {
        return this.lastManabarType;
    }

    @Override
    public void setLastManabarType(byte lastManabarType) {
        this.lastManabarType = lastManabarType;
    }

    @Override
    public byte getLastManabarPosition() {
        return lastManabarPosition;
    }

    @Override
    public byte setLastManabarPosition(byte lastManabarPosition) {
        return this.lastManabarPosition = lastManabarPosition;
    }

    @Override
    public int getLastManaSupplyPoint() {
        return lastManaSupplyPoint;
    }

    @Override
    public int setLastManaSupplyPoint(int lastManaSupplyPoint) {
        return this.lastManaSupplyPoint = lastManaSupplyPoint;
    }

    @Override
    public int getLastManaCapacityPoint() {
        return lastManaCapacityPoint;
    }

    @Override
    public int setLastManaCapacityPoint(int lastManaCapacityPoint) {
        return this.lastManaCapacityPoint = lastManaCapacityPoint;
    }

    @Override
    public void readFromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
    }

    @Override
    public void writeToNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(MANA_DISPLAY, player -> new ManaDisplay());
    }
}
