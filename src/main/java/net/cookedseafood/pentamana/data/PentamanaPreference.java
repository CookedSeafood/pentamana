package net.cookedseafood.pentamana.data;

import java.util.HashMap;
import java.util.Map;
import net.cookedseafood.candywrapper.util.BossBars;
import net.cookedseafood.pentamana.render.ManaBar;
import net.cookedseafood.pentamana.render.ManaCharset;
import net.cookedseafood.pentamana.render.ManaPattern;
import net.cookedseafood.pentamana.render.ManaRender;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;

public class PentamanaPreference {
    public boolean isVisible;
    public boolean isSuppressed;
    public ManaBar.Position position;
    public ManaRender.Type type;
    public ManaPattern pattern;
    public int pointsPerCharacter;
    public boolean isCompressed;
    public byte compressionSize;
    public ManaCharset charset;
    public BossBar.Color color;
    public BossBar.Style style;

    public PentamanaPreference(
        boolean isVisible,
        boolean isSuppressed,
        ManaBar.Position position,
        ManaRender.Type type,
        ManaPattern pattern,
        int pointsPerCharacter,
        boolean isCompressed,
        byte compressionSize,
        ManaCharset charset,
        BossBar.Color color,
        BossBar.Style style
    ) {
        this.isVisible = isVisible;
        this.position = position;
        this.type = type;
        this.pattern = pattern;
        this.pointsPerCharacter = pointsPerCharacter;
        this.isCompressed = isCompressed;
        this.compressionSize = compressionSize;
        this.charset = charset;
        this.color = color;
        this.style = style;
    }

    public PentamanaPreference() {
        this(
            PentamanaConfig.DefaultPreference.isVisible,
            PentamanaConfig.DefaultPreference.isSuppressed,
            PentamanaConfig.DefaultPreference.position,
            PentamanaConfig.DefaultPreference.type,
            PentamanaConfig.DefaultPreference.pattern,
            PentamanaConfig.DefaultPreference.pointsPerCharacter,
            PentamanaConfig.DefaultPreference.isCompressed,
            PentamanaConfig.DefaultPreference.compressionSize,
            PentamanaConfig.DefaultPreference.charset,
            PentamanaConfig.DefaultPreference.color,
            PentamanaConfig.DefaultPreference.style
        );
    }

    public static PentamanaPreference fromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
        return new PentamanaPreference(
            nbtCompound.getBoolean("visibility", PentamanaConfig.DefaultPreference.isVisible),
            nbtCompound.getBoolean("suppression", PentamanaConfig.DefaultPreference.isSuppressed),
            nbtCompound.contains("position") ? ManaBar.Position.byName(nbtCompound.getString("position").get()) : PentamanaConfig.DefaultPreference.position,
            nbtCompound.contains("type") ? ManaRender.Type.byName(nbtCompound.getString("type").get()) : PentamanaConfig.DefaultPreference.type,
            nbtCompound.contains("pattern") ? ManaPattern.fromNbt(nbtCompound.getListOrEmpty("pattern"), wrapperLookup) : PentamanaConfig.DefaultPreference.pattern,
            nbtCompound.getInt("points_per_character", PentamanaConfig.DefaultPreference.pointsPerCharacter),
            nbtCompound.getBoolean("compression", PentamanaConfig.DefaultPreference.isCompressed),
            nbtCompound.getByte("compression_size", PentamanaConfig.DefaultPreference.compressionSize),
            nbtCompound.contains("charset") ? ManaCharset.fromNbt(nbtCompound.getListOrEmpty("charset"), wrapperLookup) : PentamanaConfig.DefaultPreference.charset,
            nbtCompound.contains("color") ? BossBars.Colors.byName(nbtCompound.getString("color").get()) : PentamanaConfig.DefaultPreference.color,
            nbtCompound.contains("style") ? BossBars.Styles.byName(nbtCompound.getString("style").get()) : PentamanaConfig.DefaultPreference.style
            );
    }

    public NbtCompound toNbt(RegistryWrapper.WrapperLookup wrapperLookup) {
        return new NbtCompound(
            new HashMap<>(
                Map.<String,NbtElement>ofEntries(
                    Map.entry("visibility", NbtByte.of(this.isVisible)),
                    Map.entry("suppression", NbtByte.of(this.isSuppressed)),
                    Map.entry("position", NbtString.of(this.position.getName())),
                    Map.entry("type", NbtString.of(this.type.getName())),
                    Map.entry("pattern", this.pattern.toNbt(wrapperLookup)),
                    Map.entry("points_per_character", NbtInt.of(this.pointsPerCharacter)),
                    Map.entry("compression", NbtByte.of(this.isCompressed)),
                    Map.entry("compression_size", NbtByte.of(this.compressionSize)),
                    Map.entry("charset", this.charset.toNbt(wrapperLookup)),
                    Map.entry("color", NbtString.of(this.color.getName())),
                    Map.entry("style", NbtString.of(this.style.getName()))
                )
            )
        );
    }
}
