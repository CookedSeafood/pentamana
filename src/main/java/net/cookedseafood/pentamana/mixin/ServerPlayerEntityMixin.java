package net.cookedseafood.pentamana.mixin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.cookedseafood.candywrapper.util.BossBars;
import net.cookedseafood.pentamana.Pentamana;
import net.cookedseafood.pentamana.api.ServerPlayerEntityApi;
import net.cookedseafood.pentamana.data.PentamanaConfig;
import net.cookedseafood.pentamana.data.PentamanaPreference;
import net.cookedseafood.pentamana.render.ManaBar;
import net.cookedseafood.pentamana.render.ManaCharset;
import net.cookedseafood.pentamana.render.ManaPattern;
import net.cookedseafood.pentamana.render.ManaRender;
import net.cookedseafood.pentamana.render.ManaTextual;
import net.cookedseafood.pentamana.render.ServerManaBar;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements ServerPlayerEntityApi {
    @Override
    public void putManaBarDisplay(ManaBar.Position position) {
        this.getManaBar().putDisplay(position);
    }

    @Override
    public void putManaBarDisplay() {
        this.getManaBar().putDisplay();
    }

    @Override
    public void removeManaBarDisplay(ManaBar.Position position) {
        this.getManaBar().removeDisplay(position);
    }

    @Override
    public void removeManaBarDisplay() {
        this.getManaBar().removeDisplay();
    }

    @Override
    public boolean isManaBarDisplayOutdate(boolean isValueChanged) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        PentamanaPreference preference = player.getPentamanaPreference();
        return (preference.position == ManaBar.Position.ACTIONBAR && player.getServer().getTicks() % 40 == 0 || isValueChanged) && preference.isVisible && !preference.isSuppressed;
    }

    @Override
    public ServerManaBar getManaBar() {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        UUID uuid = player.getUuid();
        String name = "manabar." + uuid.toString();

        return new ServerManaBar(player, uuid, name, Identifier.of(Pentamana.MOD_NAMESPACE, name));
    }

    @Override
    public ManaTextual getManaTextual() {
        return ManaTextual.fromPreference(this.getPentamanaPreference());
    }

    @Override
    public ManaRender getManaRender() {
        return ManaRender.fromPreference(this.getPentamanaPreference());
    }

    @Override
    public PentamanaPreference getPentamanaPreference() {
        return PentamanaPreference.fromNbt(
            ((ServerPlayerEntity)(Object)this).getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getCompoundOrEmpty("pentamana_preference"),
            ((ServerPlayerEntity)(Object)this).getRegistryManager()
        );
    }

    @Override
    public void setPentamanaPreference(PentamanaPreference preference) {
        ((ServerPlayerEntity)(Object)this).setComponent(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(
            ((ServerPlayerEntity)(Object)this).getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().copyFrom(preference.toNbt(((ServerPlayerEntity)(Object)this).getRegistryManager()))
        ));
    }

    @Override
    public boolean isManaBarVisible() {
        return ((ServerPlayerEntity)(Object)this).getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getCompoundOrEmpty("pentamana_preference").getBoolean("visibility", PentamanaConfig.DefaultPreference.isVisible);
    }

    @Override
    public void setManaBarVisibility(boolean value) {
        ((ServerPlayerEntity)(Object)this).setComponent(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(
            ((ServerPlayerEntity)(Object)this).getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().copyFrom(
                new NbtCompound(
                    new HashMap<>(
                        Map.<String, NbtElement>of(
                            "pentamana_preference",
                            new NbtCompound(
                                new HashMap<>(
                                    Map.<String, NbtElement>of(
                                        "visibility",
                                        NbtByte.of(value)
                                    )
                                )
                            )
                        )
                    )
                )
            )
        ));
    }

    @Override
    public boolean isManaBarSuppressed() {
        return ((ServerPlayerEntity)(Object)this).getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getCompoundOrEmpty("pentamana_preference").getBoolean("suppression", PentamanaConfig.DefaultPreference.isSuppressed);
    }

    @Override
    public void setManaBarSuppression(boolean value) {
        ((ServerPlayerEntity)(Object)this).setComponent(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(
            ((ServerPlayerEntity)(Object)this).getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().copyFrom(
                new NbtCompound(
                    new HashMap<>(
                        Map.<String, NbtElement>of(
                            "pentamana_preference",
                            new NbtCompound(
                                new HashMap<>(
                                    Map.<String, NbtElement>of(
                                        "suppression",
                                        NbtByte.of(value)
                                    )
                                )
                            )
                        )
                    )
                )
            )
        ));
    }

    @Override
    public ManaBar.Position getManaBarPosition() {
        return ManaBar.Position.byName(((ServerPlayerEntity)(Object)this).getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getCompoundOrEmpty("pentamana_preference").getString("position", PentamanaConfig.DefaultPreference.position.getName()));
    }

    @Override
    public void setManaBarPosition(ManaBar.Position position) {
        ((ServerPlayerEntity)(Object)this).setComponent(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(
            ((ServerPlayerEntity)(Object)this).getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().copyFrom(
                new NbtCompound(
                    new HashMap<>(
                        Map.<String, NbtElement>of(
                            "pentamana_preference",
                            new NbtCompound(
                                new HashMap<>(
                                    Map.<String, NbtElement>of(
                                        "position",
                                        NbtString.of(position.getName())
                                    )
                                )
                            )
                        )
                    )
                )
            )
        ));
    }

    @Override
    public ManaRender.Type getManaRenderType() {
        return ManaRender.Type.byName(((ServerPlayerEntity)(Object)this).getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getCompoundOrEmpty("pentamana_preference").getString("type", PentamanaConfig.DefaultPreference.type.getName()));
    }

    @Override
    public void setManaRenderType(ManaRender.Type type) {
        ((ServerPlayerEntity)(Object)this).setComponent(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(
            ((ServerPlayerEntity)(Object)this).getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().copyFrom(
                new NbtCompound(
                    new HashMap<>(
                        Map.<String, NbtElement>of(
                            "pentamana_preference",
                            new NbtCompound(
                                new HashMap<>(
                                    Map.<String, NbtElement>of(
                                        "type",
                                        NbtString.of(type.getName())
                                    )
                                )
                            )
                        )
                    )
                )
            )
        ));
    }

    @Override
    public ManaPattern getManaPattern() {
        return ManaPattern.fromNbt(
            ((ServerPlayerEntity)(Object)this).getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getCompoundOrEmpty("pentamana_preference").getListOrEmpty("pattern"),
            ((ServerPlayerEntity)(Object)this).getRegistryManager()
        );
    }

    @Override
    public void setManaPattern(ManaPattern pattern) {
        ((ServerPlayerEntity)(Object)this).setComponent(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(
            ((ServerPlayerEntity)(Object)this).getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().copyFrom(
                new NbtCompound(
                    new HashMap<>(
                        Map.<String, NbtElement>of(
                            "pentamana_preference",
                            new NbtCompound(
                                new HashMap<>(
                                    Map.<String, NbtElement>of(
                                        "pattern",
                                        pattern.toNbt(((ServerPlayerEntity)(Object)this).getRegistryManager())
                                    )
                                )
                            )
                        )
                    )
                )
            )
        ));
    }

    @Override
    public ManaCharset getManaCharset() {
        return ManaCharset.fromNbt(
            ((ServerPlayerEntity)(Object)this).getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getCompoundOrEmpty("pentamana_preference").getListOrEmpty("charset"),
            ((ServerPlayerEntity)(Object)this).getRegistryManager()
        );
    }

    @Override
    public void setManaCharset(ManaCharset charset) {
        ((ServerPlayerEntity)(Object)this).setComponent(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(
            ((ServerPlayerEntity)(Object)this).getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().copyFrom(
                new NbtCompound(
                    new HashMap<>(
                        Map.<String, NbtElement>of(
                            "pentamana_preference",
                            new NbtCompound(
                                new HashMap<>(
                                    Map.<String, NbtElement>of(
                                        "charset",
                                        charset.toNbt(((ServerPlayerEntity)(Object)this).getRegistryManager())
                                    )
                                )
                            )
                        )
                    )
                )
            )
        ));
    }

    @Override
    public int getManaPointsPerCharacter() {
        return ((ServerPlayerEntity)(Object)this).getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getCompoundOrEmpty("pentamana_preference").getInt("points_per_character", PentamanaConfig.DefaultPreference.pointsPerCharacter);
    }

    @Override
    public void setManaPointsPerCharacter(int value) {
        ((ServerPlayerEntity)(Object)this).setComponent(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(
            ((ServerPlayerEntity)(Object)this).getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().copyFrom(
                new NbtCompound(
                    new HashMap<>(
                        Map.<String, NbtElement>of(
                            "pentamana_preference",
                            new NbtCompound(
                                new HashMap<>(
                                    Map.<String, NbtElement>of(
                                        "points_per_character",
                                        NbtInt.of(value)
                                    )
                                )
                            )
                        )
                    )
                )
            )
        ));
    }

    @Override
    public boolean isManaRenderCompressed() {
        return ((ServerPlayerEntity)(Object)this).getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getCompoundOrEmpty("pentamana_preference").getBoolean("compression", PentamanaConfig.DefaultPreference.isCompressed);
    }

    @Override
    public void setManaRenderCompression(boolean value) {
        ((ServerPlayerEntity)(Object)this).setComponent(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(
            ((ServerPlayerEntity)(Object)this).getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().copyFrom(
                new NbtCompound(
                    new HashMap<>(
                        Map.<String, NbtElement>of(
                            "pentamana_preference",
                            new NbtCompound(
                                new HashMap<>(
                                    Map.<String, NbtElement>of(
                                        "compression",
                                        NbtByte.of(value)
                                    )
                                )
                            )
                        )
                    )
                )
            )
        ));
    }

    @Override
    public byte getManaRenderCompressionSize() {
        return ((ServerPlayerEntity)(Object)this).getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getCompoundOrEmpty("pentamana_preference").getByte("compression_size", PentamanaConfig.DefaultPreference.compressionSize);
    }

    @Override
    public void setManaRenderCompressionSize(byte value) {
        ((ServerPlayerEntity)(Object)this).setComponent(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(
            ((ServerPlayerEntity)(Object)this).getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().copyFrom(
                new NbtCompound(
                    new HashMap<>(
                        Map.<String, NbtElement>of(
                            "pentamana_preference",
                            new NbtCompound(
                                new HashMap<>(
                                    Map.<String, NbtElement>of(
                                        "compression_size",
                                        NbtByte.of(value)
                                    )
                                )
                            )
                        )
                    )
                )
            )
        ));
    }

    @Override
    public BossBar.Color getManaBarColor() {
        return BossBars.Colors.byName(((ServerPlayerEntity)(Object)this).getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getCompoundOrEmpty("pentamana_preference").getString("color", PentamanaConfig.DefaultPreference.color.getName()));
    }

    @Override
    public void setManaBarColor(BossBar.Color color) {
        ((ServerPlayerEntity)(Object)this).setComponent(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(
            ((ServerPlayerEntity)(Object)this).getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().copyFrom(
                new NbtCompound(
                    new HashMap<>(
                        Map.<String, NbtElement>of(
                            "pentamana_preference",
                            new NbtCompound(
                                new HashMap<>(
                                    Map.<String, NbtElement>of(
                                        "color",
                                        NbtString.of(color.getName())
                                    )
                                )
                            )
                        )
                    )
                )
            )
        ));
    }

    @Override
    public BossBar.Style getManaBarStyle() {
        return BossBars.Styles.byName(((ServerPlayerEntity)(Object)this).getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getCompoundOrEmpty("pentamana_preference").getString("style", PentamanaConfig.DefaultPreference.style.getName()));
    }

    @Override
    public void setManaBarStyle(BossBar.Style style) {
        ((ServerPlayerEntity)(Object)this).setComponent(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(
            ((ServerPlayerEntity)(Object)this).getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().copyFrom(
                new NbtCompound(
                    new HashMap<>(
                        Map.<String, NbtElement>of(
                            "pentamana_preference",
                            new NbtCompound(
                                new HashMap<>(
                                    Map.<String, NbtElement>of(
                                        "style",
                                        NbtString.of(style.getName())
                                    )
                                )
                            )
                        )
                    )
                )
            )
        ));
    }

    @Shadow
    public abstract ServerCommandSource getCommandSource();
}
