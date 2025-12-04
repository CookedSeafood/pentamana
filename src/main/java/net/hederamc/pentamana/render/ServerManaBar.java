package net.hederamc.pentamana.render;

import java.util.UUID;
import net.hederamc.pentamana.data.PentamanaConfig;
import net.hederamc.pentamana.data.PentamanaPreference;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.BossBarManager;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class ServerManaBar extends ManaBar {
    private MinecraftServer server;
    private ServerScoreboard scoreboard;
    private BossBarManager bossBarManager;
    private ServerPlayerEntity player;
    private UUID uuid;
    private String name;
    private Identifier id;

    public ServerManaBar(ServerPlayerEntity player, UUID uuid, String name, Identifier id, float capacity, float supply, ManaBar.Position position, ManaTextual textual, boolean isVisible, boolean isSuppressed, BossBar.Color color, BossBar.Style style) {
        super(capacity, supply, position, textual, isVisible, isSuppressed, color, style);
        this.server = player.getServer();
        this.scoreboard = player.getServer().getScoreboard();
        this.bossBarManager = player.getServer().getBossBarManager();
        this.player = player;
        this.uuid = uuid;
        this.name = name;
        this.id = id;
    }

    public ServerManaBar(ServerPlayerEntity player, UUID uuid, String name, Identifier id, ManaBar manabar) {
        this(player, uuid, name, id, player.getManaCapacity(), player.getMana(), manabar.position, manabar.textual, manabar.isVisible, manabar.isSuppressed, manabar.color, manabar.style);
    }

    public ServerManaBar(ServerPlayerEntity player, UUID uuid, String name, Identifier id, PentamanaPreference preference) {
        this(player, uuid, name, id, ManaBar.fromPreference(preference));
    }

    public ServerManaBar(ServerPlayerEntity player, UUID uuid, String name, Identifier id) {
        this(player, uuid, name, id, player.getPentamanaPreference());
    }

    public void putDisplay(ManaBar.Position position) {
        if (position == ManaBar.Position.ACTIONBAR) {
            this.player.sendMessage(this.textual.toText(capacity, supply), true);
        } else if (position == ManaBar.Position.BOSSBAR) {
            CommandBossBar bossbar = this.bossBarManager.getOrAdd(this.id, Text.empty());

            bossbar.setMaxValue((int)(this.capacity / PentamanaConfig.manaPerPoint));
            bossbar.setValue((int)(this.supply / PentamanaConfig.manaPerPoint));
            bossbar.setName(this.textual.toText(capacity, supply));
            bossbar.setColor(this.color);
            bossbar.setStyle(this.style);
            bossbar.addPlayer(this.player);
        } else if (position == ManaBar.Position.SIDERBAR) {
            ScoreboardObjective objective = this.scoreboard.getNullableObjective(this.name);

            if (objective == null) {
                objective = this.scoreboard.addObjective(this.name, ScoreboardCriterion.DUMMY, Text.empty(), ScoreboardCriterion.RenderType.INTEGER, true, null);
                this.player.networkHandler.sendPacket(new ScoreboardObjectiveUpdateS2CPacket(objective, ScoreboardObjectiveUpdateS2CPacket.ADD_MODE));
                this.player.networkHandler.sendPacket(new ScoreboardDisplayS2CPacket(ScoreboardDisplaySlot.SIDEBAR, objective));
            }

            objective.setDisplayName(this.textual.toText(capacity, supply));
            this.player.networkHandler.sendPacket(new ScoreboardObjectiveUpdateS2CPacket(objective, ScoreboardObjectiveUpdateS2CPacket.UPDATE_MODE));
        }
    }

    public void putDisplay() {
        this.putDisplay(this.position);
    }

    public void removeDisplay(ManaBar.Position position) {
        if (position == ServerManaBar.Position.ACTIONBAR) {
            this.player.sendMessage(Text.literal(""), true);
        } else if (position == ServerManaBar.Position.BOSSBAR) {
            if (this.bossBarManager.containsKey(this.id)) {
                this.bossBarManager.get(this.id).clearPlayers();
                this.bossBarManager.remove(this.id);
            }
        } else if (position == ManaBar.Position.SIDERBAR) {
            ScoreboardObjective objective = this.scoreboard.getNullableObjective(this.name);

            if (objective != null) {
                this.scoreboard.removeObjective(objective);
                this.player.networkHandler.sendPacket(new ScoreboardObjectiveUpdateS2CPacket(objective, ScoreboardObjectiveUpdateS2CPacket.REMOVE_MODE));
            }
        }
    }

    public void removeDisplay() {
        this.removeDisplay(this.position);
    }

    public MinecraftServer getServer() {
        return this.server;
    }

    public void setServer(MinecraftServer server) {
        this.server = server;
    }

    public ServerManaBar withServer(MinecraftServer server) {
        this.setServer(server);
        return this;
    }

    public ServerScoreboard getScoreboard() {
        return this.scoreboard;
    }

    public void setScoreboard(ServerScoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    public ServerManaBar withScoreboard(ServerScoreboard scoreboard) {
        this.setScoreboard(scoreboard);
        return this;
    }

    public BossBarManager getBossBarManager() {
        return this.bossBarManager;
    }

    public void setBossBarManager(BossBarManager bossBarManager) {
        this.bossBarManager = bossBarManager;
    }

    public ServerManaBar withBossBarManager(BossBarManager bossBarManager) {
        this.setBossBarManager(bossBarManager);
        return this;
    }

    public ServerPlayerEntity getPlayer() {
        return this.player;
    }

    public void setPlayer(ServerPlayerEntity player) {
        this.player = player;
    }

    public ServerManaBar withPlayer(ServerPlayerEntity player) {
        this.setPlayer(player);
        return this;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public ServerManaBar withUuid(UUID uuid) {
        this.setUuid(uuid);
        return this;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ServerManaBar withName(String name) {
        this.setName(name);
        return this;
    }

    public Identifier getId() {
        return this.id;
    }

    public void setId(Identifier id) {
        this.id = id;
    }

    public ServerManaBar withId(Identifier id) {
        this.setId(id);
        return this;
    }

    @Override
    public void setCapacity(float capacity) {
        if (this.capacity == capacity) {
            return;
        }

        this.player.setManaCapacity(capacity);
        super.setCapacity(capacity);
    }

    public ServerManaBar withCapacity(float capacity) {
        this.setCapacity(capacity);
        return this;
    }

    @Override
    public void setSupply(float supply) {
        if (this.supply == supply) {
            return;
        }

        this.player.setMana(supply);
        super.setSupply(supply);
    }

    public ServerManaBar withSupply(float supply) {
        this.setSupply(supply);
        return this;
    }

    @Override
    public void setPosition(ManaBar.Position position) {
        if (this.position == position) {
            return;
        }

        this.player.setManaBarPosition(position);
        super.setPosition(position);
    }

    public ServerManaBar withPosition(ManaBar.Position position) {
        this.setPosition(position);
        return this;
    }

    @Override
    public void setVisibility(boolean isVisible) {
        if (this.isVisible == isVisible) {
            return;
        }

        this.player.setManaBarVisibility(isVisible);
        super.setVisibility(isVisible);
    }

    public ServerManaBar withVisibility(boolean isVisible) {
        this.setVisibility(isVisible);
        return this;
    }

    @Override
    public void setSuppression(boolean isSuppressed) {
        if (this.isSuppressed == isSuppressed) {
            return;
        }

        this.player.setManaBarSuppression(isSuppressed);
        super.setSuppression(isSuppressed);
    }

    public ServerManaBar withSuppression(boolean isSuppressed) {
        this.setSuppression(isSuppressed);
        return this;
    }

    @Override
    public void setColor(BossBar.Color color) {
        if (this.color == color) {
            return;
        }

        this.player.setManaBarColor(color);
        super.setColor(color);
    }

    public ServerManaBar withColor(BossBar.Color color) {
        this.setColor(color);
        return this;
    }

    @Override
    public void setStyle(BossBar.Style style) {
        if (this.style == style) {
            return;
        }

        this.player.setManaBarStyle(style);
        super.setStyle(style);
    }

    public ServerManaBar withStyle(BossBar.Style style) {
        this.setStyle(style);
        return this;
    }

    /**
     * A shadow copy.
     * 
     * @return a new ServerManaBar
     * 
     * @see #deepCopy()
     */
    @Override
    public ServerManaBar copy() {
        return new ServerManaBar(this.player, this.uuid, this.name, this.id, super.copy());
    }

    /**
     * A deep copy.
     * 
     * @return a new ServerManaBar
     * 
     * @see #copy()
     */
    @Override
    public ServerManaBar deepCopy() {
        return new ServerManaBar(this.player, this.uuid, this.name, Identifier.of(this.id.toString()), super.deepCopy());
    }
}
