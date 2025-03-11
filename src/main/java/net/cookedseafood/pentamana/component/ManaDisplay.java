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
    private byte lastManabarPosition;
    private int lastManaSupplyPoint;
    private int lastManaCapacityPoint;
    private boolean lastIsVisible;

    public ManaDisplay() {
    }

    @Override
    public void tick(ServerPlayerEntity player) {
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);
        if (manaPreference.isVisible()) {
            tickVisible(player, manaPreference.getManabarType(), manaPreference.getManabarPosition(), manaPreference.getPointsPerCharacter(), manaPreference.isCompression(), manaPreference.getCompressionSize(), manaPreference.getManaCharacters(), manaPreference.getBossBarColor(), manaPreference.getBossBarStyle());
            ++this.manabarLife;
            return;
        }

        tickInvisible(player, manaPreference.getManabarPosition());
    }

    private void tickVisible(ServerPlayerEntity player, byte manabarType, byte manabarPosition, int pointsPerCharacter, boolean isCompression, byte compressionSize, List<List<Text>> manaCharacters, BossBar.Color bossbarColor, BossBar.Style bossbarStyle) {
        this.lastIsVisible = true;
        if (this.isSuppressed() || !this.isOutdate(player, manabarType, manabarPosition, pointsPerCharacter, isCompression, compressionSize)) {
            return;
        }

        this.manabarLife = (byte)-Pentamana.displayIdleInterval;
        this.updateManabar(player, this.toText(manabarType, pointsPerCharacter, manaCharacters), manabarPosition, bossbarColor, bossbarStyle);
    }

    private void tickInvisible(ServerPlayerEntity player, byte manabarPosition) {
        if (!this.lastIsVisible) {
            return;
        }

        this.lastIsVisible = false;
        this.manabarLife = 0;
        this.finishManabar(player, manabarPosition);
    }

    @Override
    public boolean isSuppressed() {
        return this.manabarLife > (byte)0 && this.manabarLife < Pentamana.displaySuppressionInterval;
    }

    private boolean isOutdate(ServerPlayerEntity player, byte manabarType, byte manabarPosition, int pointsPerCharacter, boolean isCompression, byte compressionSize) {
        ManaStatus manaStatus = ManaStatus.MANA_STATUS.get(player);
        return this.tryUpdateStatus(manabarType, manaStatus.getManaCapacity(), manaStatus.getManaSupply(), pointsPerCharacter, isCompression, compressionSize)
        || this.manabarLife >= (byte)0
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

        if (this.lastManaCapacityPoint == manaCapacityPoint && this.lastManaSupplyPoint == manaSupplyPoint) {
            return false;
        }

        this.lastManaCapacityPoint = manaCapacityPoint;
        this.lastManaSupplyPoint = manaSupplyPoint;
        return true;
    }

    private boolean tryUpdateStatusInNumeric(float manaCapacity, float manaSupply) {
        int manaCapacityPoint = (int)(manaCapacity / Pentamana.manaPerPoint);
        int manaSupplyPoint = (int)(manaSupply / Pentamana.manaPerPoint);

        if (this.lastManaCapacityPoint == manaCapacityPoint && this.lastManaSupplyPoint == manaSupplyPoint) {
            return false;
        }

        this.lastManaCapacityPoint = manaCapacityPoint;
        this.lastManaSupplyPoint = manaSupplyPoint;
        return true;
    }

    private boolean tryUpdateStatusInPercentage(float manaCapacity, float manaSupply) {
        byte manaSupplyPoint = (byte)(manaSupply / manaCapacity * 100);
        if (this.lastManaSupplyPoint == manaSupplyPoint) {
            return false;
        }

        this.lastManaSupplyPoint = manaSupplyPoint;
        return true;
    }

    private boolean tryUpdateStatusInNone(float manaCapacity, float manaSupply) {
        int manaCapacityPoint = (int)(manaCapacity / Pentamana.manaPerPoint);
        int manaSupplyPoint = (int)(manaSupply / Pentamana.manaPerPoint);

        if (this.lastManaCapacityPoint == manaCapacityPoint && this.lastManaSupplyPoint == manaSupplyPoint) {
            return false;
        }

        this.lastManaCapacityPoint = manaCapacityPoint;
        this.lastManaSupplyPoint = manaSupplyPoint;
        return true;
    }

    private boolean tryUpdateManabarPosition(ServerPlayerEntity player, byte manabarPosition) {
        if (this.lastManabarPosition == manabarPosition) {
            return false;
        }

        this.finishManabar(player, this.lastManabarPosition);
        this.lastManabarPosition = manabarPosition;
        return true;
    }

    @Override
    public void updateManabar(ServerPlayerEntity player, Text manabar, byte manabarPosition, BossBar.Color bossbarColor, BossBar.Style bossbarStyle) {
        if (manabarPosition == ManabarPositions.ACTIONBAR.getIndex()) {
            this.updateManabarInActionbar(player, manabar);
        } else if (manabarPosition == ManabarPositions.BOSSBAR.getIndex()) {
            this.updateManabarInBossbar(player, manabar, bossbarColor, bossbarStyle);
        }
    }

    @Override
    public void updateManabarInActionbar(ServerPlayerEntity player, Text manabar) {
        player.sendMessage(manabar, true);
    }

    @Override
    public void updateManabarInBossbar(ServerPlayerEntity player, Text manabar, BossBar.Color bossbarColor, BossBar.Style bossbarStyle) {
        CommandBossBar bossbar = player.getServer().getBossBarManager().getOrAdd(Identifier.of(Pentamana.MOD_ID, "manabar" + player.getUuidAsString()), manabar);
        bossbar.setMaxValue(this.lastManaCapacityPoint);
        bossbar.setValue(this.lastManaSupplyPoint);
        bossbar.setName(manabar);
        bossbar.setColor(bossbarColor);
        bossbar.setStyle(bossbarStyle);
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
        Identifier id = Identifier.of(Pentamana.MOD_ID, "manabar" + player.getUuidAsString());
        if (bossbarManager.contains(id)) {
            bossbarManager.get(id).clearPlayers();
            bossbarManager.remove(id);
        }
    }

    @Override
    public Text toText(byte manabarType, int pointsPerCharacter, List<List<Text>> manaCharacters) {
        return manabarType == ManabarTypes.CHARACTER.getIndex() ?
            this.toCharacterText(pointsPerCharacter, manaCharacters) :
            manabarType == ManabarTypes.NUMERIC.getIndex() ?
            this.toNumericText() :
            manabarType == ManabarTypes.PERCENTAGE.getIndex() ?
            this.toPercentageText() :
            manabarType == ManabarTypes.NONE.getIndex() ?
            this.toNoneText() :
            Text.empty();
    }

    @Override
    public Text toCharacterText(int pointsPerCharacter, List<List<Text>> manaCharacters) {
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

            Text manaCharacter = manaCharacters.get(manaCharacterTypeIndex).get(manaCharacterIndex);
            manabar.append(manaCharacter);
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
    public boolean getLastIsVisible() {
        return this.lastIsVisible;
    }

    @Override
    public void setLastIsVisible(boolean lastIsVisible) {
        this.lastIsVisible = lastIsVisible;
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
