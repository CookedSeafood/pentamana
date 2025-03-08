package net.cookedseafood.pentamana.component;

import java.util.List;
import net.cookedseafood.pentamana.Pentamana;
import net.cookedseafood.pentamana.api.component.ManaDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
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
        ComponentRegistry.getOrCreate(Identifier.of("pentamana", "mana_display"), ManaDisplay.class);
    private byte manabarLife;
    private int manaSupplyPoint;
    private int manaCapacityPoint;
    private byte manaSupplyPercent;

    public ManaDisplay() {
    }

    @Override
    public void tick(PlayerEntity player) {
        ManaPreference manaPreference = ManaPreference.MANA_PREFERENCE.get(player);

        if (manaPreference.getVisibility() == false) {
            return;
        }

        if (this.manabarLife > (byte)0 && this.manabarLife < Pentamana.displaySuppressionInterval) {
            return;
        }

        ManaStatus manaStatus = ManaStatus.MANA_STATUS.get(player);

        float manaCapacity = manaStatus.getManaCapacity();
		float manaSupply = manaStatus.getManaSupply();

        byte manaRenderType = manaPreference.getManaRenderType();
        if (manaRenderType == Pentamana.ManaRenderType.FLEX_SIZE.getIndex()) {
            int currentManaCapacityPoint = (int)(manaCapacity / Pentamana.manaPerPoint);
            int currentManaSupplyPoint = (int)(manaSupply / Pentamana.manaPerPoint);
            if (this.manaCapacityPoint == currentManaCapacityPoint && this.manaSupplyPoint == currentManaSupplyPoint && manabarLife < (byte)0) {
                return;
            }

            this.manaCapacityPoint = currentManaCapacityPoint;
            this.manaSupplyPoint = currentManaSupplyPoint;
            this.manabarLife = (byte)-Pentamana.displayIdleInterval;
            player.sendMessage(getManabarGraphical(currentManaCapacityPoint, currentManaSupplyPoint, manaPreference.getPointsPerCharacter(), manaPreference.getManaCharacters()), true);
            return;
        }

        if (manaRenderType == Pentamana.ManaRenderType.FIXED_SIZE.getIndex()) {
            int pointsPerCharacter = manaPreference.getPointsPerCharacter();

            int currentManaCapacityPoint = pointsPerCharacter * manaPreference.getManaFixedSize();
            int currentManaSupplyPoint = (int)(manaSupply / manaCapacity * currentManaCapacityPoint);
            if (this.manaCapacityPoint == currentManaCapacityPoint && this.manaSupplyPoint == currentManaSupplyPoint && manabarLife < (byte)0) {
                return;
            }

            this.manaCapacityPoint = currentManaCapacityPoint;
            this.manaSupplyPoint = currentManaSupplyPoint;
            this.manabarLife = (byte)-Pentamana.displayIdleInterval;
            player.sendMessage(getManabarGraphical(currentManaCapacityPoint, currentManaSupplyPoint, pointsPerCharacter, manaPreference.getManaCharacters()), true);
            return;
        }

        if (manaRenderType == Pentamana.ManaRenderType.NUMBERIC.getIndex()) {
            int currentManaCapacityPoint = (int)(manaCapacity / Pentamana.manaPerPoint);
            int currentManaSupplyPoint = (int)(manaSupply / Pentamana.manaPerPoint);
            if (this.manaCapacityPoint == currentManaCapacityPoint && this.manaSupplyPoint == currentManaSupplyPoint && manabarLife < (byte)0) {
                return;
            }

            this.manaCapacityPoint = currentManaCapacityPoint;
            this.manaSupplyPoint = currentManaSupplyPoint;
            this.manabarLife = (byte)-Pentamana.displayIdleInterval;
            player.sendMessage(Text.literal(currentManaSupplyPoint + "/" + currentManaCapacityPoint).setStyle(Style.EMPTY.withColor(Formatting.AQUA)), true);
            return;
        }

        if (manaRenderType == Pentamana.ManaRenderType.PERCENTAGE.getIndex()) {
            byte currentManaSupplyPercent = (byte)(manaSupply / manaCapacity * 100);
            if (this.manaSupplyPercent == currentManaSupplyPercent && manabarLife < (byte)0) {
                return;
            }

            this.manaSupplyPercent = currentManaSupplyPercent;
            this.manabarLife = (byte)-Pentamana.displayIdleInterval;
            player.sendMessage(Text.literal(currentManaSupplyPercent + "%").setStyle(Style.EMPTY.withColor(Formatting.AQUA)), true);
            return;
        }
    }

    private static Text getManabarGraphical(int manaCapacityPoint, int manaSupplyPoint, int pointsPerCharacter, List<List<Text>> manaCharacters) {
        int manaCapacityPointTrimmed = manaCapacityPoint - manaCapacityPoint % pointsPerCharacter;
        int manaPointTrimmed = manaSupplyPoint - manaSupplyPoint % pointsPerCharacter;

        manaCapacityPointTrimmed = Math.min(manaCapacityPointTrimmed, Pentamana.manaPointLimit);

        MutableText manabar = MutableText.of(PlainTextContent.EMPTY);
        for (int manaPointIndex = 0; manaPointIndex < manaCapacityPointTrimmed; manaPointIndex += pointsPerCharacter) {
            int manaCharacterTypeIndex =
                manaPointIndex < manaPointTrimmed ?
                0 : manaPointIndex < manaSupplyPoint ?
                manaSupplyPoint - manaPointIndex : pointsPerCharacter;
            int manaCharacterIndex = manaPointIndex / pointsPerCharacter;

            Text manaCharacter = manaCharacters.get(manaCharacterTypeIndex).get(manaCharacterIndex);
            manabar.append(manaCharacter);
        }

        return manabar;
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
    public int getManaSupplyPoint() {
        return manaSupplyPoint;
    }

    @Override
    public int setManaSupplyPoint(int manaSupplyPoint) {
        return this.manaSupplyPoint = manaSupplyPoint;
    }

    @Override
    public int getManaCapacityPoint() {
        return manaCapacityPoint;
    }

    @Override
    public int setManaCapacityPoint(int manaCapacityPoint) {
        return this.manaCapacityPoint = manaCapacityPoint;
    }

    @Override
    public byte getManaSupplyPercent() {
        return manaSupplyPercent;
    }

    @Override
    public byte setManaSupplyPercent(byte manaSupplyPercent) {
        return this.manaSupplyPercent = manaSupplyPercent;
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
