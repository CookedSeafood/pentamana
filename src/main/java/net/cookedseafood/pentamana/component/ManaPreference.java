package net.cookedseafood.pentamana.component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.cookedseafood.pentamana.Pentamana;
import net.cookedseafood.pentamana.api.component.ManaPreferenceComponent;
import net.cookedseafood.pentamana.render.ManabarPositions;
import net.cookedseafood.pentamana.render.ManabarTypes;
import net.minecraft.entity.boss.BossBar;
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
        ComponentRegistry.getOrCreate(Identifier.of(Pentamana.MOD_ID, "mana_preference"), ManaPreference.class);
    private boolean isEnabled;
    private boolean isVisible;
    private boolean isCompression;
    private byte compressionSize;
    private byte manabarType;
    private byte manabarPosition;
    private int pointsPerCharacter;
    private List<List<Text>> manaCharacters;
    private BossBar.Color bossbarColor;
    private BossBar.Style bossbarStyle;

    public ManaPreference() {
        if (Pentamana.isLoaded) {
            this.isEnabled = Pentamana.isEnabled;
            this.isVisible = Pentamana.isVisible;
            this.isCompression = Pentamana.isCompression;
            this.compressionSize = Pentamana.compressionSize;
            this.manabarType = Pentamana.manabarType;
            this.pointsPerCharacter = Pentamana.pointsPerCharacter;
            this.manaCharacters = new ArrayList<>(Pentamana.manaCharacters);
        }
    }

    @Override
    public boolean isEnabled() {
        return this.isEnabled;
    }

    @Override
    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    @Override
    public boolean isVisible() {
        return this.isVisible;
    }

    @Override
    public void setIsVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    @Override
    public boolean isCompression() {
        return this.isCompression;
    }

    @Override
    public void setIsCompression(boolean isCompression) {
        this.isCompression = isCompression;
    }

    @Override
    public byte getCompressionSize() {
        return this.compressionSize;
    }

    @Override
    public void setCompressionSize(byte compressionSize) {
        this.compressionSize = compressionSize;
    }

    @Override
    public byte getManabarType() {
        return this.manabarType;
    }

    @Override
    public void setManabarType(byte manabarType) {
        this.manabarType = manabarType;
    }

    @Override
    public byte getManabarPosition() {
        return this.manabarPosition;
    }

    @Override
    public void setManabarPosition(byte manabarPosition) {
        this.manabarPosition = manabarPosition;
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
    public BossBar.Color getBossBarColor() {
        return this.bossbarColor;
    }

    @Override
    public void setBossBarColor(BossBar.Color bossbarColor) {
        this.bossbarColor = bossbarColor;
    }

    @Override
    public BossBar.Style getBossBarStyle() {
        return this.bossbarStyle;
    }

    @Override
    public void setBossBarStyle(BossBar.Style bossbarStyle) {
        this.bossbarStyle = bossbarStyle;
    }

    @Override
    public void readFromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
        this.isEnabled = nbtCompound.contains("isEnabled") ?
            nbtCompound.getBoolean("isEnabled") :
            Pentamana.isEnabled;
        this.isVisible = nbtCompound.contains("isVisible") ?
            nbtCompound.getBoolean("isVisible") :
            Pentamana.isVisible;
        this.isCompression = nbtCompound.contains("isCompression") ?
            nbtCompound.getBoolean("isCompression") :
            Pentamana.isCompression;
        this.compressionSize = nbtCompound.contains("compressionSize", NbtElement.BYTE_TYPE) ?
            nbtCompound.getByte("compressionSize") :
            Pentamana.compressionSize;
        this.manabarType = nbtCompound.contains("manabarType", NbtElement.STRING_TYPE) ?
            ManabarTypes.getIndex(nbtCompound.getString("manabarType")) :
            Pentamana.manabarType;
        this.manabarPosition = nbtCompound.contains("manabarPosition", NbtElement.STRING_TYPE) ?
            ManabarPositions.getIndex(nbtCompound.getString("manabarPosition")) :
            Pentamana.manabarPosition;
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
        this.bossbarColor = nbtCompound.contains("bossbarColor", NbtElement.STRING_TYPE) ?
            BossBar.Color.byName(nbtCompound.getString("bossbarColor")) :
            Pentamana.bossbarColor;
        this.bossbarStyle = nbtCompound.contains("bossbarStyle", NbtElement.STRING_TYPE) ?
            BossBar.Style.byName(nbtCompound.getString("bossbarStyle")) :
            Pentamana.bossbarStyle;
    }

    @Override
    public void writeToNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
        nbtCompound.putBoolean("isEnabled", isEnabled);
        nbtCompound.putBoolean("isVisible", isVisible);
        nbtCompound.putBoolean("isCompression", isCompression);
        nbtCompound.putByte("compressionSize", compressionSize);
        nbtCompound.putString("manabarType", ManabarTypes.getName(manabarType));
        nbtCompound.putString("manabarPosition", ManabarPositions.getName(manabarPosition));
        nbtCompound.putInt("pointsPerCharacter", pointsPerCharacter);
        nbtCompound.put("manaCharacters", manaCharacters.stream()
            .map(manaCharacterType -> manaCharacterType.stream()
                .map(manaCharacter -> Text.Serialization.toJsonString(manaCharacter, registryLookup))
                .map(NbtString::of)
                .collect(NbtList::new, NbtList::add, (left, right) -> left.addAll(right))
            )
            .collect(NbtList::new, NbtList::add, (left, right) -> left.addAll(right))
        );
        nbtCompound.putString("bossbarColor", bossbarColor.getName());
        nbtCompound.putString("bossbarStyle", bossbarStyle.getName());
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(MANA_PREFERENCE, player -> new ManaPreference(), RespawnCopyStrategy.ALWAYS_COPY);
    }
}
