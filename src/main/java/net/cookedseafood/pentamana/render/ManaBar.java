package net.cookedseafood.pentamana.render;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.cookedseafood.candywrapper.util.BossBars;
import net.cookedseafood.pentamana.data.PentamanaConfig;
import net.cookedseafood.pentamana.data.PentamanaPreference;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;

public class ManaBar {
    protected float capacity;
    protected float supply;
    protected ManaBar.Position position;
    protected ManaTextual textual;
    protected boolean isVisible;
    protected boolean isSuppressed;
    protected BossBar.Color color;
    protected BossBar.Style style;

    public ManaBar(float capacity, float supply, ManaBar.Position position, ManaTextual textual, boolean isVisible, boolean isSuppressed, BossBar.Color color, BossBar.Style style) {
        this.capacity = capacity;
        this.supply = supply;
        this.position = position;
        this.textual = textual;
        this.isVisible = isVisible;
        this.isSuppressed = isSuppressed;
        this.color = color;
        this.style = style;
    }

    public boolean isFull() {
        return this.supply == capacity;
    }

    public boolean isEmpty() {
        return this.supply == 0.0f;
    }

    public float getCapacity() {
        return this.capacity;
    }

    public void setCapacity(float capacity) {
        this.capacity = capacity;
    }

    public ManaBar withCapacity(float capacity) {
        this.setCapacity(capacity);
        return this;
    }

    public float getSupply() {
        return this.supply;
    }

    public void setSupply(float supply) {
        this.supply = supply;
    }

    public ManaBar withSupply(float supply) {
        this.setSupply(supply);
        return this;
    }

    public ManaBar.Position getPosition() {
        return this.position;
    }

    public void setPosition(ManaBar.Position position) {
        this.position = position;
    }

    public ManaBar withPosition(ManaBar.Position position) {
        this.setPosition(position);
        return this;
    }

    public ManaTextual getTextual() {
        return this.textual;
    }

    public void setTextual(ManaTextual textual) {
        this.textual = textual;
    }

    public ManaBar withTextual(ManaTextual textual) {
        this.setTextual(textual);
        return this;
    }

    public boolean isVisible() {
        return this.isVisible;
    }

    public void setVisibility(boolean isVisible) {
        this.isVisible = isVisible;
    }

    public ManaBar withVisibility(boolean isVisible) {
        this.setVisibility(isVisible);
        return this;
    }

    public boolean isSuppressed() {
        return this.isSuppressed;
    }

    public void setSuppression(boolean isSuppressed) {
        this.isSuppressed = isSuppressed;
    }

    public ManaBar withSuppression(boolean isSuppressed) {
        this.setSuppression(isSuppressed);
        return this;
    }

    public BossBar.Color getColor() {
        return this.color;
    }

    public void setColor(BossBar.Color color) {
        this.color = color;
    }

    public ManaBar withColor(BossBar.Color color) {
        this.setColor(color);
        return this;
    }

    public BossBar.Style getStyle() {
        return this.style;
    }

    public void setStyle(BossBar.Style style) {
        this.style = style;
    }

    public ManaBar withStyle(BossBar.Style style) {
        this.setStyle(style);
        return this;
    }

    /**
     * A shadow copy.
     * 
     * @return a new ManaBar
     * 
     * @see #deepCopy()
     */
    public ManaBar copy() {
        return new ManaBar(this.capacity, this.supply, this.position, this.textual, this.isVisible, this.isSuppressed, this.color, this.style);
    }

    /**
     * A deep copy.
     * 
     * @return a new ManaBar
     * 
     * @see #copy()
     */
    public ManaBar deepCopy() {
        return new ManaBar(this.capacity, this.supply, this.position, this.textual.deepCopy(), this.isVisible, this.isSuppressed, this.color, this.style);
    }

    public static ManaBar fromPreference(PentamanaPreference preference) {
        return new ManaBar(
            0,
            0,
            preference.position,
            ManaTextual.fromPreference(preference),
            preference.isVisible,
            preference.isSuppressed,
            preference.color,
            preference.style
        );
    }

    public static ManaBar fromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
        return new ManaBar(
            nbtCompound.getFloat("capacity", 0f),
            nbtCompound.getFloat("supply", 0f),
            nbtCompound.contains("position") ? ManaBar.Position.byName(nbtCompound.getString("position").get()) : PentamanaConfig.DefaultPreference.position,
            ManaTextual.fromNbt(nbtCompound.getCompoundOrEmpty("textual"), wrapperLookup),
            nbtCompound.getBoolean("isVisible", PentamanaConfig.DefaultPreference.isVisible),
            nbtCompound.getBoolean("isSuppressed", PentamanaConfig.DefaultPreference.isSuppressed),
            nbtCompound.contains("color") ? BossBars.Colors.byName(nbtCompound.getString("color").get()) : PentamanaConfig.DefaultPreference.color,
            nbtCompound.contains("style") ? BossBars.Styles.byName(nbtCompound.getString("style").get()) : PentamanaConfig.DefaultPreference.style
        );
    }

    public NbtCompound toNbt(RegistryWrapper.WrapperLookup wrapperLookup) {
        return new NbtCompound(
            new HashMap<>(
                Map.<String,NbtElement>ofEntries(
                    Map.entry("capacity", NbtFloat.of(this.capacity)),
                    Map.entry("supply", NbtFloat.of(this.supply)),
                    Map.entry("position", NbtString.of(this.position.name)),
                    Map.entry("textual", this.textual.toNbt(wrapperLookup)),
                    Map.entry("isVisible", NbtByte.of(this.isVisible)),
                    Map.entry("isSuppressed", NbtByte.of(this.isSuppressed)),
                    Map.entry("color", NbtString.of(this.color.getName())),
                    Map.entry("style", NbtString.of(this.style.getName()))
                )
            )
        );
    }

    public static enum Position {
        ACTIONBAR((byte)0, "actionbar"),
        BOSSBAR((byte)1, "bossbar"),
        SIDERBAR((byte)2, "siderbar");

        private byte index;
        private String name;

        Position(byte index, String name) {
            this.index = index;
            this.name = name;
        }

        public byte getIndex() {
            return this.index;
        }

        public String getName() {
            return this.name;
        }

        public static byte getIndex(String name) {
            return Arrays.stream(Position.values())
                .filter(position -> position.name.equals(name))
                .map(position -> position.index)
                .findAny()
                .orElse((byte)0);
        }

        public static String getName(byte index) {
            return Arrays.stream(Position.values())
                .filter(position -> position.index == index)
                .map(position -> position.name)
                .findAny()
                .orElse("");
        }

        public static Position byIndex(byte index) {
            return Arrays.stream(Position.values())
                .filter(position -> position.index == index)
                .findAny()
                .get();
        }

        public static Position byName(String name) {
            return Arrays.stream(Position.values())
                .filter(position -> position.name.equals(name))
                .findAny()
                .get();
        }
    }
}
