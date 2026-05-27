package net.hederamc.pentamana.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.hederamc.pentamana.client.api.ManaBarGui;
import net.hederamc.pentamana.client.config.PentamanaConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin implements ManaBarGui {
    @Inject(
        method = "extractHotbarAndDecorations(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
        at = @At("TAIL")
    )
    private void extractManaBar(CallbackInfo ci, @Local GuiGraphicsExtractor context) {
        this.extractManaBar(context);
    }

    @Override
    public void extractManaBar(GuiGraphicsExtractor context) {
        Minecraft client = Minecraft.getInstance();
        float manaCapacity = client.player.getManaCapacity();
        if (manaCapacity <= 0.0f) {
            return;
        }

        float mana = client.player.getMana();

        PentamanaConfig config = PentamanaConfig.HANDLER.instance();
        int offsetX = config.manaBarOffsetX;
        int offsetY = config.manaBarOffsetY;
        int maxContainers = config.manaBarMaxStars;
        Vector2i normal = config.manaBarDirection.getNormal();
        float alignmentPercent = config.manaBarAlignment.getPercent();

        int containers = Math.min((int) manaCapacity / 2, maxContainers);
        int manaPoints = (int) (mana / manaCapacity * containers * 2);
        int length = containers * 8;
        int alignmentOffset = (int) (length * alignmentPercent);
        int x = context.guiWidth() / 2 + offsetX + (int) (normal.x * alignmentOffset);
        int y = context.guiHeight() + offsetY + (int) (normal.y * alignmentOffset);
        for (int i = 0; i < containers; i++) {
            context.blitSprite(RenderPipelines.GUI_TEXTURED, MANA_CONTAINER_SPRITE, x, y, 9, 9);

            if (manaPoints >= 2) {
                context.blitSprite(RenderPipelines.GUI_TEXTURED, MANA_FULL_SPRITE, x, y, 9, 9);
            } else if (manaPoints >= 1) {
                context.blitSprite(RenderPipelines.GUI_TEXTURED, MANA_HALF_SPRITE, x, y, 9, 9);
            }

            manaPoints -= 2;
            x += 8 * normal.x;
            y -= 8 * normal.y;
        }
    }
}
