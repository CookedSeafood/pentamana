package net.cookedseafood.pentamana.mana;

import java.util.Objects;
import java.util.UUID;
import net.cookedseafood.pentamana.Pentamana;
import net.cookedseafood.pentamana.api.event.ConsumManaCallback;
import net.cookedseafood.pentamana.api.event.RegenManaCallback;
import net.cookedseafood.pentamana.api.event.TickManaCallback;
import net.cookedseafood.pentamana.attribute.PentamanaAttributeIdentifiers;
import net.cookedseafood.pentamana.component.CustomStatusEffectManagerComponentInstance;
import net.cookedseafood.pentamana.effect.PentamanaStatusEffectIdentifiers;
import net.cookedseafood.pentamana.effect.CustomStatusEffectManager;
import net.cookedseafood.pentamana.enchantment.PentamanaEnchantmentIdentifiers;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.BossBarManager;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * A tickable manabar, with owner player.
 * 
 * <p>Contains an extra manabar which represents the manabar in client
 * to help reduce network usage.
 */
public final class ServerManaBar extends ManaBar {
    private MinecraftServer server;
    private ServerPlayerEntity player;
    private UUID uuid;
    private String name;
    private Identifier id;
    private ManaBar clientManaBar;
    private byte life;

    public ServerManaBar(MinecraftServer server, ServerPlayerEntity player, UUID uuid, String name, Identifier id, float capacity, float supply, ManaBar.Position position, ManaTextual textual, boolean isVisible, BossBar.Color color, BossBar.Style style) {
        super(capacity, supply, position, textual, isVisible, color, style);
        this.server = server;
        this.player = player;
        this.uuid = uuid;
        this.name = name;
        this.id = id;
        this.clientManaBar = new ManaBar(capacity, supply, position, textual, isVisible, color, style);
    }

    /**
     * Shadow copy {@code manaBar}'s value.
     * 
     * @param manaBar
     * @return a new ServerManaBar
     */
    public static ServerManaBar of(ManaBar manaBar) {
        return new ServerManaBar(null, null, null, null, null, manaBar.getCapacity(), manaBar.getSupply(), manaBar.getPosition(), manaBar.getTextual(), manaBar.isVisible(), manaBar.getColor(), manaBar.getStyle());
    }

    public void tick(ServerPlayerEntity player) {
        if (this.player == null) {
            this.player = player;
            this.uuid = this.player.getUuid();
            this.name = Pentamana.MANA_BAR_NAME_PREFIX + this.uuid.toString();
            this.id = Identifier.of(Pentamana.MOD_ID, this.name);
        }

        CustomStatusEffectManager statusEffectManager = CustomStatusEffectManagerComponentInstance.CUSTOM_STATUS_EFFECT_MANAGER.get(this.player).getStatusEffectManager();

        float capacity = (float)this.player.getCustomModifiedValue(PentamanaAttributeIdentifiers.MANA_CAPACITY, Pentamana.manaCapacityBase);
        capacity += Pentamana.enchantmentCapacityBase * this.player.getWeaponStack().getEnchantments().getLevel(PentamanaEnchantmentIdentifiers.CAPACITY);
        capacity += statusEffectManager.has(PentamanaStatusEffectIdentifiers.MANA_BOOST) ? Pentamana.statusEffectManaBoostBase * (statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_BOOST) + 1) : 0;
        capacity -= statusEffectManager.has(PentamanaStatusEffectIdentifiers.MANA_REDUCTION) ? Pentamana.statusEffectManaReductionBase * (statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_REDUCTION) + 1) : 0;
        capacity += Pentamana.isConversionExperienceLevel ? Pentamana.conversionExperienceLevelBase * this.player.experienceLevel : 0;
        capacity = Math.max(capacity, 0.0f);

        TickManaCallback.EVENT.invoker().interact(this.player);

        this.capacity = capacity;

        if (this.supply == this.capacity) {
        } else if (this.supply < this.capacity && this.supply >= 0.0f) {
            this.regen();
        } else if (this.supply > this.capacity) {
            this.supply = this.capacity;
        } else if (this.supply < 0) {
            this.supply = 0.0f;
        };

        this.tickClient();
    }

    /**
     * Consume the {@link Pentamana#manaRegenerationBase} amount of mana after the custom modifiers
     * and enchantments are applied.
     * 
     * <p>Target supply is capped at capacity and 0.0f.
     * 
     * @return {@link #isFull()}.
     * 
     * @see #regen(float)
     */
    public boolean regen() {
        CustomStatusEffectManager statusEffectManager = CustomStatusEffectManagerComponentInstance.CUSTOM_STATUS_EFFECT_MANAGER.get(this.player).getStatusEffectManager();

        float regen = (float)this.player.getCustomModifiedValue(PentamanaAttributeIdentifiers.MANA_REGENERATION, Pentamana.manaRegenerationBase);
        regen += Pentamana.enchantmentStreamBase * this.player.getWeaponStack().getEnchantments().getLevel(PentamanaEnchantmentIdentifiers.STREAM);
        regen += statusEffectManager.has(PentamanaStatusEffectIdentifiers.INSTANT_MANA) ? Pentamana.statusEffectInstantManaBase * Math.pow(2, statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.INSTANT_MANA)) : 0;
        regen -= statusEffectManager.has(PentamanaStatusEffectIdentifiers.INSTANT_DEPLETE) ? Pentamana.statusEffectInstantDepleteBase * Math.pow(2, statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.INSTANT_DEPLETE)) : 0;
        regen += statusEffectManager.has(PentamanaStatusEffectIdentifiers.MANA_REGENERATION) ? Pentamana.manaPerPoint / (float)Math.max(1, Pentamana.statusEffectManaRegenerationBase >> statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_REGENERATION)) : 0;
        regen -= statusEffectManager.has(PentamanaStatusEffectIdentifiers.MANA_INHIBITION) ? Pentamana.manaPerPoint / (float)Math.max(1, Pentamana.statusEffectManaInhibitionBase >> statusEffectManager.getActiveAmplifier(PentamanaStatusEffectIdentifiers.MANA_INHIBITION)) : 0;

        RegenManaCallback.EVENT.invoker().interact(this.player);

        return this.regen(regen);
    }

    /**
     * Regen the {@code regen} amount of mana.
     * 
     * <p>Target supply is capped at capacity and 0.0f.
     * 
     * @param regen the amount to regen.
     * @return {@link #isFull()}.
     * 
     * @see #regen()
     */
    public boolean regen(float regen) {
        float targetSupply = this.supply + regen;
        targetSupply = Math.min(targetSupply, capacity);
        targetSupply = Math.max(targetSupply, 0.0f);
        return (this.supply = targetSupply) == this.capacity;
    }

    /**
     * Consume the {@code consum} amount of mana after the custom modifiers and enchantments
     * are applied if the player has at least {@code consum} amount of mana.
     * 
     * @param consum the amount to consume.
     * @return true if successful, otherwise false.
     */
    public boolean consum(float consum) {
        float targetConsum = (float)player.getCustomModifiedValue(PentamanaAttributeIdentifiers.MANA_CONSUMPTION, consum);
        targetConsum *= 1 - Pentamana.enchantmentUtilizationBase * player.getWeaponStack().getEnchantments().getLevel(PentamanaEnchantmentIdentifiers.UTILIZATION);

        ConsumManaCallback.EVENT.invoker().interact(this.player);

        float targetSupply = this.supply - targetConsum;
        if (targetSupply >= 0.0f) {
            this.supply = targetSupply;
            return true;
        }

        return false;
    }

    public void tickClient() {
        if (this.isVisible) {
            if ((!this.equalsClient() || (this.isEndOfLife() && this.position == ManaBar.Position.ACTIONBAR)) && !this.isSuppressed()) {
                if (this.position != this.clientManaBar.position) {
                    this.finishOnScreen();
                }

                this.sendOnScreen();
                this.updateClient();
                this.life = (byte)-Pentamana.displayIdleInterval;
            }

            this.incrementLife();
        } else if (this.clientManaBar.isVisible) {
            this.finishOnScreen();
            this.updateClient();
            this.life = (byte)0;
        }
    }

    public void sendOnScreen() {
        if (this.position == ManaBar.Position.ACTIONBAR) {
            this.player.sendMessage(this.textual.toText(capacity, supply), true);
        } else if (this.position == ManaBar.Position.BOSSBAR) {
            CommandBossBar bossbar = this.server.getBossBarManager().getOrAdd(this.id, Text.empty());
            if (this.clientManaBar.capacity != this.capacity) {
                bossbar.setMaxValue((int)(this.capacity / Pentamana.manaPerPoint));
            }

            if (this.clientManaBar.supply != this.supply) {
                bossbar.setValue((int)(this.supply / Pentamana.manaPerPoint));
            }

            if (this.clientManaBar.supply != this.supply
            || this.clientManaBar.capacity != this.capacity
            || Objects.equals(this.clientManaBar.textual, this.textual)) {
                bossbar.setName(this.textual.toText(capacity, supply));
            }

            if (this.clientManaBar.color != this.color) {
                bossbar.setColor(this.color);
            }

            if (this.clientManaBar.style != this.style) {
                bossbar.setStyle(this.style);
            }

            if (this.clientManaBar.position != this.position) {
                bossbar.setMaxValue((int)(this.capacity / Pentamana.manaPerPoint));
                bossbar.setValue((int)(this.supply / Pentamana.manaPerPoint));
                bossbar.setName(this.textual.toText(capacity, supply));
                bossbar.setColor(this.color);
                bossbar.setStyle(this.style);
                bossbar.addPlayer(this.player);
            }
        } else if (this.position == ManaBar.Position.SIDERBAR) {
            ScoreboardObjective objective = this.server.getScoreboard().getOrAddObjective(this.name, ScoreboardCriterion.DUMMY, Text.empty(), ScoreboardCriterion.RenderType.INTEGER, true, null);
            if (this.clientManaBar.position != this.position) {
                this.player.networkHandler.sendPacket(new ScoreboardObjectiveUpdateS2CPacket(objective, ScoreboardObjectiveUpdateS2CPacket.ADD_MODE));
                this.player.networkHandler.sendPacket(new ScoreboardDisplayS2CPacket(ScoreboardDisplaySlot.SIDEBAR, objective));
            }

            if (this.clientManaBar.supply != this.supply
            || this.clientManaBar.capacity != this.capacity
            || Objects.equals(this.clientManaBar.textual, this.textual)) {
                objective.setDisplayName(this.textual.toText(capacity, supply));
                this.player.networkHandler.sendPacket(new ScoreboardObjectiveUpdateS2CPacket(objective, ScoreboardObjectiveUpdateS2CPacket.UPDATE_MODE));
            }
        }
    }

    public void finishOnScreen() {
        if (this.clientManaBar.position == ServerManaBar.Position.ACTIONBAR) {
            this.player.sendMessage(Text.literal(""), true);
        } else if (this.clientManaBar.position == ServerManaBar.Position.BOSSBAR) {
            BossBarManager bossbarManager = this.server.getBossBarManager();
            if (bossbarManager.contains(this.id)) {
                bossbarManager.get(this.id).clearPlayers();
                bossbarManager.remove(this.id);
            }
        } else if (this.clientManaBar.position == ManaBar.Position.SIDERBAR) {
            this.player.networkHandler.sendPacket(new ScoreboardObjectiveUpdateS2CPacket(this.server.getScoreboard().getNullableObjective(this.name), ScoreboardObjectiveUpdateS2CPacket.REMOVE_MODE));
        }
    }

    public boolean isSuppressed() {
        return this.life > (byte)0 && this.life < Pentamana.displaySuppressionInterval;
    }

    public boolean isEndOfLife() {
        return this.life >= 0;
    }

    public boolean equalsClient() {
        return this.capacity == this.clientManaBar.capacity
        && this.supply == this.clientManaBar.supply
        && this.position == this.clientManaBar.position
        && Objects.equals(this.textual, this.clientManaBar.textual)
        && this.isVisible == this.clientManaBar.isVisible
        && this.color == this.clientManaBar.color
        && this.style == this.clientManaBar.style;
    }

    public void updateClient() {
        this.clientManaBar.capacity =
            this.clientManaBar.capacity == this.capacity ?
            this.clientManaBar.capacity :
            this.capacity;
        this.clientManaBar.supply =
            this.clientManaBar.supply == this.supply ?
            this.clientManaBar.supply :
            this.supply;
        this.clientManaBar.position =
            this.clientManaBar.position == this.position ?
            this.clientManaBar.position :
            this.position;
        this.clientManaBar.textual =
            Objects.equals(this.clientManaBar.textual, this.textual) ?
            this.clientManaBar.textual :
            this.textual.deepCopy();
        this.clientManaBar.isVisible =
            this.clientManaBar.isVisible == this.isVisible ?
            this.clientManaBar.isVisible :
            this.isVisible;
        this.clientManaBar.color =
            this.clientManaBar.color == this.color ?
            this.clientManaBar.color :
            this.color;
        this.clientManaBar.style =
            this.clientManaBar.style == this.style ?
            this.clientManaBar.style :
            this.style;
    }

    public MinecraftServer getServer() {
        return this.server;
    }

    public void setServer(MinecraftServer server) {
        this.server = server;
    }

    public ServerManaBar withServer(MinecraftServer server) {
        this.server = server;
        return this;
    }

    public ServerPlayerEntity getPlayer() {
        return this.player;
    }

    public void setPlayer(ServerPlayerEntity player) {
        this.player = player;
    }

    public ServerManaBar withPlayer(ServerPlayerEntity player) {
        this.player = player;
        return this;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public ServerManaBar withUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ServerManaBar withName(String name) {
        this.name = name;
        return this;
    }

    public Identifier getId() {
        return this.id;
    }

    public void setId(Identifier id) {
        this.id = id;
    }

    public ServerManaBar withId(Identifier id) {
        this.id = id;
        return this;
    }

    public ManaBar getClientManaBar() {
        return this.clientManaBar;
    }

    public void setClientManaBar(ManaBar clientManaBar) {
        this.clientManaBar = clientManaBar;
    }

    public ServerManaBar withClientManaBar(ManaBar clientManaBar) {
        this.clientManaBar = clientManaBar;
        return this;
    }

    public byte getLife() {
        return this.life;
    }

    public void setLife(byte life) {
        this.life = life;
    }

    public byte incrementLife() {
        return ++this.life;
    }

    public byte incrementLife(byte value) {
        this.setLife((byte)(this.life + value));
        return this.life;
    }

    /**
     * @param nbtCompound
     * @param registryLookup
     * @return ServerManaBar with no server, player, uuid, name and id.
     */
    public static ServerManaBar fromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
        return ServerManaBar.of(ManaBar.fromNbt(nbtCompound, registryLookup))
            .withClientManaBar(ManaBar.fromNbt(nbtCompound.getCompound("clientManaBar"), registryLookup));
    }

    @Override
    public NbtCompound toNbt(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound nbtCompound = super.toNbt(registryLookup);
        nbtCompound.put("clientManaBar", this.clientManaBar.toNbt(registryLookup));
        return nbtCompound;
    }
}
