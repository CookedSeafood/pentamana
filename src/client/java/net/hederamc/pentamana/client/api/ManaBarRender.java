package net.hederamc.pentamana.client.api;

import net.hederamc.pentamana.Pentamana;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

public interface ManaBarRender {
    Identifier MANA_CONTAINER_TEXTURE = Identifier.fromNamespaceAndPath(Pentamana.MOD_NAMESPACE, "hud/mana/container");
    Identifier MANA_FULL_TEXTURE = Identifier.fromNamespaceAndPath(Pentamana.MOD_NAMESPACE, "hud/mana/full");
    Identifier MANA_HALF_TEXTURE = Identifier.fromNamespaceAndPath(Pentamana.MOD_NAMESPACE, "hud/mana/half");

    default void renderManaBar(GuiGraphics context) {
    }
}
