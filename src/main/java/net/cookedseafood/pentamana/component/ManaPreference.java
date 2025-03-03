package net.cookedseafood.pentamana.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.cookedseafood.pentamana.Pentamana;
import net.cookedseafood.pentamana.api.component.ManaPreferenceComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import org.ladysnake.cca.api.v3.entity.RespawnableComponent;

public class ManaPreference implements ManaPreferenceComponent, EntityComponentInitializer, RespawnableComponent<ManaPreferenceComponent> {
    public static final ComponentKey<ManaPreference> MANA_PREFERENCE =
        ComponentRegistry.getOrCreate(Identifier.of("pentamana", "mana_preference"), ManaPreference.class);
    private boolean manaEnabled;
    private boolean manaDisplay;
    private byte manaRenderType;
    private int manaFixedSize;
    private int pointsPerCharacter;
    private List<List<Text>> manaCharacters;

    public ManaPreference() {
        this.manaEnabled = true;
        this.manaDisplay = true;
        this.manaRenderType = (byte)1;
        this.pointsPerCharacter = 2;
        this.manaFixedSize = 20;
        this.manaCharacters = Stream.concat(
            Stream.of(
                Collections.nCopies(256, (Text)Text.literal("\u2605").setStyle(
                    Style.EMPTY.withColor(TextColor.fromRgb(0x55ffff))
                )),
                Collections.nCopies(256, (Text)Text.literal("\u2bea").setStyle(
                    Style.EMPTY.withColor(TextColor.fromRgb(0x55ffff))
                )),
                Collections.nCopies(256, (Text)Text.literal("\u2606").setStyle(
                    Style.EMPTY.withColor(TextColor.fromRgb(0x000000))
                ))
            ),
            Collections.nCopies(125, Collections.nCopies(256, ScreenTexts.EMPTY)).stream()
        ).collect(Collectors.toList());
    }

    @Override
    public boolean getEnabled() {
        return this.manaEnabled;
    }

    @Override
    public void setEnabled(boolean manaEnabled) {
        this.manaEnabled = manaEnabled;
    }

    @Override
    public boolean getDisplay() {
        return this.manaDisplay;
    }

    @Override
    public void setDisplay(boolean manaDisplay) {
        this.manaDisplay = manaDisplay;
    }

    @Override
    public byte getManaRenderType() {
        return this.manaRenderType;
    }

    @Override
    public void setManaRenderType(byte manaRenderType) {
        this.manaRenderType = manaRenderType;
    }

    @Override
    public int getManaFixedSize() {
        return this.manaFixedSize;
    }

    @Override
    public void setManaFixedSize(int manaFixedSize) {
        this.manaFixedSize = manaFixedSize;
    }

    @Override
    public int getPointsPerCharacter() {
        return this.pointsPerCharacter;
    }

    @Override
    public void setPointsPerCharacter(int pointsPerCharacter) {
        this.pointsPerCharacter = pointsPerCharacter;
    }

    @Override
    public List<List<Text>> getManaCharacters() {
        return this.manaCharacters;
    }

    @Override
    public void setManaCharacters(List<List<Text>> manaCharacters) {
        this.manaCharacters = manaCharacters;
    }

    @Override
    public void readFromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
        this.manaEnabled = nbtCompound.contains("manaEnabled") ?
            nbtCompound.getBoolean("manaEnabled") :
            Pentamana.manaEnabled;
        this.manaDisplay = nbtCompound.contains("manaDisplay") ?
            nbtCompound.getBoolean("manaDisplay") :
            Pentamana.manaDisplay;
        this.manaRenderType = nbtCompound.contains("manaRenderType", NbtElement.BYTE_TYPE) ?
            nbtCompound.getByte("manaRenderType") :
            Pentamana.manaRenderType;
        this.manaFixedSize = nbtCompound.contains("manaFixedSize", NbtElement.INT_TYPE) ?
            nbtCompound.getInt("manaFixedSize") :
            Pentamana.manaFixedSize;
        this.pointsPerCharacter = nbtCompound.contains("pointsPerCharacter", NbtElement.INT_TYPE) ?
            nbtCompound.getInt("pointsPerCharacter") :
            Pentamana.pointsPerCharacter;
        this.manaCharacters = nbtCompound.contains("manaCharacters", NbtElement.LIST_TYPE) ?
            nbtCompound.getList("manaCharacters", NbtList.LIST_TYPE).stream()
                .map(manaCharacterType -> ((NbtList)manaCharacterType).stream()
                    .map(NbtString.class::cast)
                    .map(NbtString::asString)
                    .map(Text::literal)
                    .map(Text.class::cast)
                    .collect(Collectors.toList())
                )
                .collect(Collectors.toList()) :
            new ArrayList<>(Pentamana.manaCharacters);
    }

    @Override
    public void writeToNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
        nbtCompound.putBoolean("manaEnabled", manaEnabled);
        nbtCompound.putBoolean("manaDisplay", manaDisplay);
        nbtCompound.putByte("manaRenderType", manaRenderType);
        nbtCompound.putInt("manaFixedSize", manaFixedSize);
        nbtCompound.putInt("pointsPerCharacter", pointsPerCharacter);
        nbtCompound.put("manaCharacters", manaCharacters.stream()
            .map(manaCharacterType -> manaCharacterType.stream()
                .map(Text::getString)
                .map(NbtString::of)
                .collect(NbtList::new, NbtList::add, (left, right) -> left.addAll(right))
            )
            .collect(NbtList::new, NbtList::add, (left, right) -> left.addAll(right))
        );
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(MANA_PREFERENCE, player -> new ManaPreference(), RespawnCopyStrategy.ALWAYS_COPY);
    }
}
