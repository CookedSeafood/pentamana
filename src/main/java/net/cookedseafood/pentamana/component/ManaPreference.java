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
    private Text manabarPattern;
    private byte manabarType;
    private byte manabarPosition;
    private BossBar.Color manabarColor;
    private BossBar.Style manabarStyle;
    private int pointsPerCharacter;
    private List<List<Text>> manaCharacter;

    public ManaPreference() {
        if (Pentamana.isLoaded) {
            this.isEnabled = Pentamana.isEnabled;
            this.isVisible = Pentamana.isVisible;
            this.isCompression = Pentamana.isCompression;
            this.compressionSize = Pentamana.compressionSize;
            this.manabarPattern = Pentamana.manabarPattern;
            this.manabarType = Pentamana.manabarType;
            this.manabarPosition = Pentamana.manabarPosition;
            this.manabarColor = Pentamana.manabarColor;
            this.manabarStyle = Pentamana.manabarStyle;
            this.pointsPerCharacter = Pentamana.pointsPerCharacter;
            this.manaCharacter = new ArrayList<>(Pentamana.manaCharacter);
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
    public Text getManabarPattern() {
        return this.manabarPattern;
    }

    @Override
    public void setManabarPattern(Text manabarPattern) {
        this.manabarPattern = manabarPattern;
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
    public List<List<Text>> getManaCharacter() {
        return this.manaCharacter;
    }

    @Override
    public void setManaCharacter(List<List<Text>> manaCharacter) {
        this.manaCharacter = manaCharacter;
    }

    @Override
    public BossBar.Color getManabarColor() {
        return this.manabarColor;
    }

    @Override
    public void setManabarColor(BossBar.Color manabarColor) {
        this.manabarColor = manabarColor;
    }

    @Override
    public BossBar.Style getManabarStyle() {
        return this.manabarStyle;
    }

    @Override
    public void setManabarStyle(BossBar.Style manabarStyle) {
        this.manabarStyle = manabarStyle;
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
        this.manabarPattern = nbtCompound.contains("manabarPattern") ?
            Text.Serialization.fromJson(nbtCompound.getString("manabarPattern"), registryLookup) :
            Pentamana.manabarPattern;
        this.manabarType = nbtCompound.contains("manabarType", NbtElement.STRING_TYPE) ?
            ManabarTypes.getIndex(nbtCompound.getString("manabarType")) :
            Pentamana.manabarType;
        this.manabarPosition = nbtCompound.contains("manabarPosition", NbtElement.STRING_TYPE) ?
            ManabarPositions.getIndex(nbtCompound.getString("manabarPosition")) :
            Pentamana.manabarPosition;
        this.manabarColor = nbtCompound.contains("manabarColor", NbtElement.STRING_TYPE) ?
            BossBar.Color.byName(nbtCompound.getString("manabarColor")) :
            Pentamana.manabarColor;
        this.manabarStyle = nbtCompound.contains("manabarStyle", NbtElement.STRING_TYPE) ?
            BossBar.Style.byName(nbtCompound.getString("manabarStyle")) :
            Pentamana.manabarStyle;
        this.pointsPerCharacter = nbtCompound.contains("pointsPerCharacter", NbtElement.INT_TYPE) ?
            nbtCompound.getInt("pointsPerCharacter") :
            Pentamana.pointsPerCharacter;
        this.manaCharacter = nbtCompound.contains("manaCharacter", NbtElement.LIST_TYPE) ?
            nbtCompound.getList("manaCharacter", NbtList.LIST_TYPE).stream()
                .map(manaCharacterType -> ((NbtList)manaCharacterType).stream()
                    .map(NbtString.class::cast)
                    .map(NbtString::asString)
                    .map(manaCharacter -> Text.Serialization.fromJson(manaCharacter, registryLookup))
                    .map(Text.class::cast)
                    .collect(Collectors.toList())
                )
                .collect(Collectors.toList()) :
            new ArrayList<>(Pentamana.manaCharacter);
    }

    @Override
    public void writeToNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
        nbtCompound.putBoolean("isEnabled", isEnabled);
        nbtCompound.putBoolean("isVisible", isVisible);
        nbtCompound.putBoolean("isCompression", isCompression);
        nbtCompound.putByte("compressionSize", compressionSize);
        nbtCompound.putString("manabarPattern", Text.Serialization.toJsonString(manabarPattern, registryLookup));
        nbtCompound.putString("manabarType", ManabarTypes.getName(manabarType));
        nbtCompound.putString("manabarPosition", ManabarPositions.getName(manabarPosition));
        nbtCompound.putString("manabarColor", manabarColor.getName());
        nbtCompound.putString("manabarStyle", manabarStyle.getName());
        nbtCompound.putInt("pointsPerCharacter", pointsPerCharacter);
        nbtCompound.put("manaCharacter", manaCharacter.stream()
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
