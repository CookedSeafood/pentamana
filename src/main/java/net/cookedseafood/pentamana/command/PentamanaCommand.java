package net.cookedseafood.pentamana.command;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import net.cookedseafood.pentamana.Pentamana;
import net.cookedseafood.pentamana.component.ManaPreferenceComponentImpl;
import net.cookedseafood.pentamana.component.ManaStatusEffectManagerComponentImpl;
import net.cookedseafood.pentamana.component.ServerManaBarComponentImpl;
import net.cookedseafood.pentamana.mana.ManaBar;
import net.cookedseafood.pentamana.mana.ManaCharset;
import net.cookedseafood.pentamana.mana.ManaPattern;
import net.cookedseafood.pentamana.mana.ManaRender;
import net.cookedseafood.pentamana.mana.ManaStatusEffectManager;
import net.cookedseafood.pentamana.mana.ManaTextual;
import net.cookedseafood.pentamana.mana.ServerManaBar;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.mutable.MutableInt;

public class PentamanaCommand {
    public PentamanaCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(
            CommandManager.literal(Pentamana.MOD_ID)
            .then(
                CommandManager.literal("debug")
                .then(
                    CommandManager.literal("server")
                    .executes(context -> executeDebugServer(context.getSource()))
                )
                .then(
                    CommandManager.literal("client")
                    .executes(context -> executeDebugClient(context.getSource()))
                )
                .then(
                    CommandManager.literal("config")
                    .executes(context -> executeDebugConfig(context.getSource()))
                )
            )
            .then(
                CommandManager.literal("version")
                .executes(context -> executeVersion(context.getSource()))
            )
        );
    }

    public static int executeDebugServer(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreferenceComponentImpl manaPreference = ManaPreferenceComponentImpl.MANA_PREFERENCE.get(player);
        ManaStatusEffectManager statusEffectManager = ManaStatusEffectManagerComponentImpl.MANA_STATUS_EFFECT.get(player).getStatusEffectManager();
        ServerManaBar serverManaBar = ServerManaBarComponentImpl.SERVER_MANA_BAR.get(player).getServerManaBar();
        ManaTextual textual = serverManaBar.getTextual();
        ManaPattern pattern = textual.getPattern();
        ManaRender render = textual.getRender();
        ManaCharset charset = render.getCharset();
        BossBar.Color color = serverManaBar.getColor();
        BossBar.Style style = serverManaBar.getStyle();

        MutableText profile = MutableText.of(PlainTextContent.EMPTY);
        profile.append(Text.literal("\n" + serverManaBar.getSupply() + "/" + serverManaBar.getCapacity()).formatted(Formatting.AQUA));
        profile.append(Text.literal(" " + serverManaBar.getLife()).formatted(Formatting.GRAY));
        statusEffectManager.forEach(statusEffect -> {
            profile.append(Text.literal("\n" + statusEffect.getId().toString()).formatted(Formatting.YELLOW));
            profile.append(Text.literal(" " + statusEffect.getDuration()).formatted(Formatting.GREEN));
            profile.append(Text.literal(" " + statusEffect.getAmplifier()).formatted(Formatting.LIGHT_PURPLE));
        });
        profile.append(Text.literal("\n- isEnabled "));
        profile.append(manaPreference.isEnabled() ? Text.literal("true").formatted(Formatting.GREEN) : Text.literal("false").formatted(Formatting.RED));
        profile.append(Text.literal("\n- manaBarPosition "));
        profile.append(Text.literal(serverManaBar.getPosition().getName()).formatted(Formatting.YELLOW));
        profile.append(Text.literal("\n- manaPattern "));
        profile.append(Text.literal(pattern.toText().toString()).formatted(Formatting.YELLOW));
        profile.append(Text.literal("\n- manaRenderType "));
        profile.append(Text.literal(render.getType().getName()).formatted(Formatting.YELLOW));
        profile.append(Text.literal("\n- manaCharset "));
        for (int i = 0; i < 10; i++) {
            profile.append(charset.get(i).get(i));
        };
        profile.append(Text.literal("\n- pointsPerCharacter "));
        profile.append(Text.literal("" + render.getPointsPerCharacter()).formatted(Formatting.GREEN));
        profile.append(Text.literal("\n- isCompression "));
        profile.append(render.isCompression() ? Text.literal("true").formatted(Formatting.GREEN) : Text.literal("false").formatted(Formatting.RED));
        profile.append(Text.literal("\n- compressionSize "));
        profile.append(Text.literal("" + render.getCompressionSize()).formatted(Formatting.GREEN));
        profile.append(Text.literal("\n- isVisible "));
        profile.append(serverManaBar.isVisible() ? Text.literal("true").formatted(Formatting.GREEN) : Text.literal("false").formatted(Formatting.RED));
        profile.append(Text.literal("\n- manaBarColor "));
        profile.append(Text.literal("" + color.getName()).formatted(Formatting.YELLOW));
        profile.append(Text.literal("\n- manaBarStyle "));
        profile.append(Text.literal("" + style.getName()).formatted(Formatting.YELLOW));

        source.sendFeedback(() -> profile, false);
        return 0;
    }

    public static int executeDebugClient(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ManaPreferenceComponentImpl manaPreference = ManaPreferenceComponentImpl.MANA_PREFERENCE.get(player);
        ManaStatusEffectManager statusEffectManager = ManaStatusEffectManagerComponentImpl.MANA_STATUS_EFFECT.get(player).getStatusEffectManager();
        ManaBar clientManaBar = ServerManaBarComponentImpl.SERVER_MANA_BAR.get(player).getServerManaBar().getClientManaBar();
        ManaTextual textual = clientManaBar.getTextual();
        ManaPattern pattern = textual.getPattern();
        ManaRender render = textual.getRender();
        ManaCharset charset = render.getCharset();
        BossBar.Color color = clientManaBar.getColor();
        BossBar.Style style = clientManaBar.getStyle();

        MutableText profile = MutableText.of(PlainTextContent.EMPTY);
        profile.append(Text.literal("\n" + clientManaBar.getSupply() + "/" + clientManaBar.getCapacity()).formatted(Formatting.AQUA));
        statusEffectManager.forEach(statusEffect -> {
            profile.append(Text.literal("\n" + statusEffect.getId().toString()).formatted(Formatting.YELLOW));
            profile.append(Text.literal(" " + statusEffect.getDuration()).formatted(Formatting.GREEN));
            profile.append(Text.literal(" " + statusEffect.getAmplifier()).formatted(Formatting.LIGHT_PURPLE));
        });
        profile.append(Text.literal("\n- isEnabled "));
        profile.append(manaPreference.isEnabled() ? Text.literal("true").formatted(Formatting.GREEN) : Text.literal("false").formatted(Formatting.RED));
        profile.append(Text.literal("\n- manaBarPosition "));
        profile.append(Text.literal(clientManaBar.getPosition().getName()).formatted(Formatting.YELLOW));
        profile.append(Text.literal("\n- manaPattern "));
        profile.append(Text.literal(pattern.toText().toString()).formatted(Formatting.YELLOW));
        profile.append(Text.literal("\n- manaRenderType "));
        profile.append(Text.literal(render.getType().getName()).formatted(Formatting.YELLOW));
        profile.append(Text.literal("\n- manaCharset "));
        for (int i = 0; i < 10; i++) {
            profile.append(charset.get(i).get(i));
        };
        profile.append(Text.literal("\n- pointsPerCharacter "));
        profile.append(Text.literal("" + render.getPointsPerCharacter()).formatted(Formatting.GREEN));
        profile.append(Text.literal("\n- isCompression "));
        profile.append(render.isCompression() ? Text.literal("true").formatted(Formatting.GREEN) : Text.literal("false").formatted(Formatting.RED));
        profile.append(Text.literal("\n- compressionSize "));
        profile.append(Text.literal("" + render.getCompressionSize()).formatted(Formatting.GREEN));
        profile.append(Text.literal("\n- isVisible "));
        profile.append(clientManaBar.isVisible() ? Text.literal("true").formatted(Formatting.GREEN) : Text.literal("false").formatted(Formatting.RED));
        profile.append(Text.literal("\n- manaBarColor "));
        profile.append(Text.literal("" + color.getName()).formatted(Formatting.YELLOW));
        profile.append(Text.literal("\n- manaBarStyle "));
        profile.append(Text.literal("" + style.getName()).formatted(Formatting.YELLOW));

        source.sendFeedback(() -> profile, false);
        return 0;
    }

    public static int executeDebugConfig(ServerCommandSource source) {
        String configString;
        try {
            configString = FileUtils.readFileToString(new File("./config/pentamana.json"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            source.sendFeedback(() -> Text.literal("\nPentamana config:\nThere is no config file."), false);
            return 0;
        }

        JsonObject config = new Gson().fromJson(configString, JsonObject.class);
        MinecraftServer server = source.getServer();
        MutableInt counter = new MutableInt(0);

        MutableText profile = MutableText.of(PlainTextContent.EMPTY);
        profile.append(Text.literal("\nPentamana config:"));

        if (config.has("manaPerPoint")) {
            profile.append(Text.literal("\n- manaPerPoint "));
            profile.append(Text.literal("" + config.get("manaPerPoint").getAsInt()).formatted(Formatting.GREEN));
            counter.increment();
        }

        if (config.has("manaCapacityBase")) {
            profile.append(Text.literal("\n- manaCapacityBase "));
            profile.append(Text.literal("" + config.get("manaCapacityBase").getAsFloat()).formatted(Formatting.GREEN));
            counter.increment();
        }

        if (config.has("manaRegenerationBase")) {
            profile.append(Text.literal("\n- manaRegenerationBase "));
            profile.append(Text.literal("" + config.get("manaRegenerationBase").getAsFloat()).formatted(Formatting.GREEN));
            counter.increment();
        }

        if (config.has("enchantmentCapacityBase")) {
            profile.append(Text.literal("\n- enchantmentCapacityBase "));
            profile.append(Text.literal("" + config.get("enchantmentCapacityBase").getAsFloat()).formatted(Formatting.GREEN));
            counter.increment();
        }

        if (config.has("enchantmentStreamBase")) {
            profile.append(Text.literal("\n- enchantmentStreamBase "));
            profile.append(Text.literal("" + config.get("enchantmentStreamBase").getAsFloat()).formatted(Formatting.GREEN));
            counter.increment();
        }

        if (config.has("enchantmentUtilizationBase")) {
            profile.append(Text.literal("\n- enchantmentUtilizationBase "));
            profile.append(Text.literal("" + config.get("enchantmentUtilizationBase").getAsFloat()).formatted(Formatting.GREEN));
            counter.increment();
        }

        if (config.has("enchantmentPotencyBase")) {
            profile.append(Text.literal("\n- enchantmentPotencyBase "));
            profile.append(Text.literal("" + config.get("enchantmentPotencyBase").getAsFloat()).formatted(Formatting.GREEN));
            counter.increment();
        }

        if (config.has("statusEffectManaBoostBase")) {
            profile.append(Text.literal("\n- statusEffectManaBoostBase "));
            profile.append(Text.literal("" + config.get("statusEffectManaBoostBase").getAsFloat()).formatted(Formatting.GREEN));
            counter.increment();
        }

        if (config.has("statusEffectManaReductionBase")) {
            profile.append(Text.literal("\n- statusEffectManaReductionBase "));
            profile.append(Text.literal("" + config.get("statusEffectManaReductionBase").getAsFloat()).formatted(Formatting.GREEN));
            counter.increment();
        }

        if (config.has("statusEffectInstantManaBase")) {
            profile.append(Text.literal("\n- statusEffectInstantManaBase "));
            profile.append(Text.literal("" + config.get("statusEffectInstantManaBase").getAsFloat()).formatted(Formatting.GREEN));
            counter.increment();
        }

        if (config.has("statusEffectInstantDepleteBase")) {
            profile.append(Text.literal("\n- statusEffectInstantDepleteBase "));
            profile.append(Text.literal("" + config.get("statusEffectInstantDepleteBase").getAsFloat()).formatted(Formatting.GREEN));
            counter.increment();
        }

        if (config.has("statusEffectManaPowerBase")) {
            profile.append(Text.literal("\n- statusEffectManaPowerBase "));
            profile.append(Text.literal("" + config.get("statusEffectManaPowerBase").getAsFloat()).formatted(Formatting.GREEN));
            counter.increment();
        }

        if (config.has("statusEffectManaSicknessBase")) {
            profile.append(Text.literal("\n- statusEffectManaSicknessBase "));
            profile.append(Text.literal("" + config.get("statusEffectManaSicknessBase").getAsFloat()).formatted(Formatting.GREEN));
            counter.increment();
        }

        if (config.has("statusEffectManaRegenerationBase")) {
            profile.append(Text.literal("\n- statusEffectManaRegenerationBase "));
            profile.append(Text.literal("" + config.get("statusEffectManaRegenerationBase").getAsInt()).formatted(Formatting.GREEN));
            counter.increment();
        }

        if (config.has("statusEffectManaInhibitionBase")) {
            profile.append(Text.literal("\n- statusEffectManaInhibitionBase "));
            profile.append(Text.literal("" + config.get("statusEffectManaInhibitionBase").getAsInt()).formatted(Formatting.GREEN));
            counter.increment();
        }

        if (config.has("isConversionExperienceLevel")) {
            profile.append(Text.literal("\n- isConversionExperienceLevel "));
            profile.append(config.get("isConversionExperienceLevel").getAsBoolean() ? Text.literal("true").formatted(Formatting.GREEN) : Text.literal("false").formatted(Formatting.RED));
            counter.increment();
        }

        if (config.has("conversionExperienceLevelBase")) {
            profile.append(Text.literal("\n- conversionExperienceLevelBase "));
            profile.append(Text.literal("" + config.get("conversionExperienceLevelBase").getAsFloat()).formatted(Formatting.GREEN));
            counter.increment();
        }

        if (config.has("displayIdleInterval")) {
            profile.append(Text.literal("\n- displayIdleInterval "));
            profile.append(Text.literal("" + config.get("displayIdleInterval").getAsByte()).formatted(Formatting.GREEN));
            counter.increment();
        }

        if (config.has("displaySuppressionInterval")) {
            profile.append(Text.literal("\n- displaySuppressionInterval "));
            profile.append(Text.literal("" + config.get("displaySuppressionInterval").getAsByte()).formatted(Formatting.GREEN));
            counter.increment();
        }

        if (config.has("isForceEnabled")) {
            profile.append(Text.literal("\n- isForceEnabled "));
            profile.append(config.get("isForceEnabled").getAsBoolean() ? Text.literal("true").formatted(Formatting.GREEN) : Text.literal("false").formatted(Formatting.RED));
            counter.increment();
        }

        if (config.has("isEnabled")) {
            profile.append(Text.literal("\n- isEnabled "));
            profile.append(config.get("isEnabled").getAsBoolean() ? Text.literal("true").formatted(Formatting.GREEN) : Text.literal("false").formatted(Formatting.RED));
            counter.increment();
        }

        if (config.has("manaBarPosition")) {
            profile.append(Text.literal("\n- manaBarPosition "));
            profile.append(Text.literal(config.get("manaBarPosition").getAsString()).formatted(Formatting.YELLOW));
            counter.increment();
        }

        if (config.has("manaPattern")) {
            profile.append(Text.literal("\n- manaPattern "));
            profile.append(
                Text.literal(
                    new ManaPattern(
                        config.get("pattern").getAsJsonArray().asList().stream()
                            .map(partialPattern -> Text.Serialization.fromJsonTree(partialPattern, server.getRegistryManager()))
                            .map(Text.class::cast)
                            .collect(Collectors.toList())
                    ).toText().toString()
                ).formatted(Formatting.YELLOW)
            );
            counter.increment();
        }

        if (config.has("manaRenderType")) {
            profile.append(Text.literal("\n- manaRenderType "));
            profile.append(Text.literal(config.get("manaRenderType").getAsString()).formatted(Formatting.YELLOW));
            counter.increment();
        }

        if (config.has("manaCharset")) {
            profile.append(Text.literal("\n- manaCharset "));
            profile.append(
                Text.literal(
                    new ManaCharset(
                        config.get("charset").getAsJsonArray().asList().stream()
                            .map(JsonElement::getAsJsonArray)
                            .map(JsonArray::asList)
                            .map(charsetType -> charsetType.stream()
                                .map(character -> Text.Serialization.fromJsonTree(character, server.getRegistryManager()))
                                .map(Text.class::cast)
                                .collect(Collectors.toList())
                            )
                            .collect(Collectors.toList())
                    ).toText().toString()
                ).formatted(Formatting.YELLOW)
            );
        }

        if (config.has("pointsPerCharacter")) {
            profile.append(Text.literal("\n- pointsPerCharacter "));
            profile.append(Text.literal("" + config.get("pointsPerCharacter").getAsInt()).formatted(Formatting.GREEN));
            counter.increment();
        }

        if (config.has("isCompression")) {
            profile.append(Text.literal("\n- isCompression "));
            profile.append(config.get("isCompression").getAsBoolean() ? Text.literal("true").formatted(Formatting.GREEN) : Text.literal("false").formatted(Formatting.RED));
            counter.increment();
        }

        if (config.has("compressionSize")) {
            profile.append(Text.literal("\n- compressionSize "));
            profile.append(Text.literal("" + config.get("compressionSize").getAsByte()).formatted(Formatting.GREEN));
            counter.increment();
        }

        if (config.has("isVisible")) {
            profile.append(Text.literal("\n- isVisible "));
            profile.append(config.get("isVisible").getAsBoolean() ? Text.literal("true").formatted(Formatting.GREEN) : Text.literal("false").formatted(Formatting.RED));
            counter.increment();
        }

        if (config.has("manaBarColor")) {
            profile.append(Text.literal("\n- manaBarColor "));
            profile.append(Text.literal(config.get("manaBarColor").getAsString()).formatted(Formatting.YELLOW));
            counter.increment();
        }

        if (config.has("manaBarStyle")) {
            profile.append(Text.literal("\n- manaBarStyle "));
            profile.append(Text.literal(config.get("manaBarStyle").getAsString()).formatted(Formatting.YELLOW));
            counter.increment();
        }

        source.sendFeedback(() -> profile, false);
        return counter.intValue();
    }

    public static int executeVersion(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal("Pentamana " + Pentamana.VERSION_MAJOR + "." + Pentamana.VERSION_MINOR + "." + Pentamana.VERSION_PATCH + (Pentamana.isForceEnabled ? " (Force Enabled Mode)" : "")), false);
        return 0;
    }
}
