package net.hederamc.pentamana.api;

import net.hederamc.pentamana.data.PentamanaPreference;
import net.hederamc.pentamana.render.ManaBar;
import net.hederamc.pentamana.render.ManaCharset;
import net.hederamc.pentamana.render.ManaPattern;
import net.hederamc.pentamana.render.ManaRender;
import net.hederamc.pentamana.render.ManaTextual;
import net.hederamc.pentamana.render.ServerManaBar;
import net.minecraft.entity.boss.BossBar;

public interface ServerPlayerEntityApi {
    default void putManaBarDisplay(ManaBar.Position position) {
    }

    default void putManaBarDisplay() {
    }

    default void removeManaBarDisplay(ManaBar.Position position) {
    }

    default void removeManaBarDisplay() {
    }

    default boolean isManaBarDisplayOutdate(boolean isValueChanged) {
        return false;
    }

    default ServerManaBar getManaBar() {
        return null;
    }

    default ManaTextual getManaTextual() {
        return null;
    }

    default ManaRender getManaRender() {
        return null;
    }

    default PentamanaPreference getPentamanaPreference() {
        return null;
    }

    default void setPentamanaPreference(PentamanaPreference value) {
    }

    default ManaBar.Position getManaBarPosition() {
        return null;
    }

    default void setManaBarPosition(ManaBar.Position position) {
    }

    default ManaRender.Type getManaRenderType() {
        return null;
    }

    default void setManaRenderType(ManaRender.Type type) {
    }

    default ManaPattern getManaPattern() {
        return null;
    }

    default void setManaPattern(ManaPattern pattern) {
    }

    default ManaCharset getManaCharset() {
        return null;
    }

    default void setManaCharset(ManaCharset charset) {
    }

    default int getManaPointsPerCharacter() {
        return 0;
    }

    default void setManaPointsPerCharacter(int value) {
    }

    default boolean isManaBarVisible() {
        return false;
    }

    default void setManaBarVisibility(boolean value) {
    }

    default boolean isManaBarSuppressed() {
        return false;
    }

    default void setManaBarSuppression(boolean value) {
    }

    default boolean isManaRenderCompressed() {
        return false;
    }

    default void setManaRenderCompression(boolean value) {
    }

    default byte getManaRenderCompressionSize() {
        return 0;
    }

    default void setManaRenderCompressionSize(byte value) {
    }

    default BossBar.Color getManaBarColor() {
        return null;
    }

    default void setManaBarColor(BossBar.Color color) {
    }

    default BossBar.Style getManaBarStyle() {
        return null;
    }

    default void setManaBarStyle(BossBar.Style style) {
    }
}
