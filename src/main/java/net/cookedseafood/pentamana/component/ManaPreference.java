package net.cookedseafood.pentamana.component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.cookedseafood.pentamana.Pentamana;
import net.cookedseafood.pentamana.api.component.ManaPreferenceComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
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
    private boolean isEnabled;
    private boolean isVisible;
    private byte manaRenderType;
    private int manaFixedSize;
    private int pointsPerCharacter;
    private List<List<Text>> manaCharacters;

    public ManaPreference() {
        if (Pentamana.isLoaded) {
            this.isEnabled = Pentamana.isEnabled;
            this.isVisible = Pentamana.isVisible;
            this.manaRenderType = Pentamana.manaRenderType;
            this.pointsPerCharacter = Pentamana.pointsPerCharacter;
            this.manaFixedSize = Pentamana.manaFixedSize;
            this.manaCharacters = new ArrayList<>(Pentamana.manaCharacters);
        }
    }

    @Override
    public boolean getEnabled() {
        return this.isEnabled;
    }

    @Override
    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    @Override
    public boolean getVisibility() {
        return this.isVisible;
    }

    @Override
    public void setVisibility(boolean isVisible) {
        this.isVisible = isVisible;
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
        this.isEnabled = nbtCompound.contains("isEnabled") ?
            nbtCompound.getBoolean("isEnabled") :
            Pentamana.isEnabled;
        this.isVisible = nbtCompound.contains("isVisible") ?
            nbtCompound.getBoolean("isVisible") :
            Pentamana.isVisible;
        this.manaRenderType = nbtCompound.contains("manaRenderType", NbtElement.STRING_TYPE) ?
            Pentamana.ManaRenderType.getIndex(nbtCompound.getString("manaRenderType")) :
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
                    .map(manaCharacter -> Text.Serialization.fromJson(manaCharacter, registryLookup))
                    .map(Text.class::cast)
                    .collect(Collectors.toList())
                )
                .collect(Collectors.toList()) :
            new ArrayList<>(Pentamana.manaCharacters);
    }

    @Override
    public void writeToNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
        nbtCompound.putBoolean("isEnabled", isEnabled);
        nbtCompound.putBoolean("isVisible", isVisible);
        nbtCompound.putString("manaRenderType", Pentamana.ManaRenderType.getName(manaRenderType));
        nbtCompound.putInt("manaFixedSize", manaFixedSize);
        nbtCompound.putInt("pointsPerCharacter", pointsPerCharacter);
        nbtCompound.put("manaCharacters", manaCharacters.stream()
            .map(manaCharacterType -> manaCharacterType.stream()
                .map(manaCharacter -> Text.Serialization.toJsonString(manaCharacter, registryLookup))
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
