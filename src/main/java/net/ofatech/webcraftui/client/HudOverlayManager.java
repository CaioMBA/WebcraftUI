package net.ofatech.webcraftui.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.ofatech.webcraftui.WebcraftUI;
import net.ofatech.webcraftui.api.UiRegistration;
import net.ofatech.webcraftui.ui.render.UiRenderer;

import java.util.Optional;

/**
 * Renders HTML-driven HUD overlays on the client.
 */
public final class HudOverlayManager {
    private static UiRenderer.RenderResult overlay;
    private static UiRegistration registration;

    private HudOverlayManager() {
    }

    public static void show(UiRegistration reg) {
        registration = reg;
        Optional<String> html = UiDocumentLoader.load(reg.html());
        if (html.isEmpty()) {
            WebcraftUI.LOGGER.warn("HUD overlay {} has no HTML", reg.uiId());
            return;
        }
        Optional<String> css = reg.css().flatMap(UiDocumentLoader::load);
        overlay = HtmlRenderEngine.build(reg, Minecraft.getInstance().font, html.get(), css,
                10, 10, 200, (action, payload) -> {} , true);
    }

    public static void render(GuiGraphics graphics) {
        if (overlay == null) {
            return;
        }
        overlay.decorations().forEach(decoration -> {
            decoration.backgroundColor().ifPresent(color -> graphics.fill(decoration.x(), decoration.y(),
                    decoration.x() + decoration.width(), decoration.y() + decoration.height(), applyAlpha(color)));
            decoration.texture().ifPresent(texture -> graphics.blit(texture, decoration.x(), decoration.y(), 0, 0,
                    decoration.width(), decoration.height(), decoration.width(), decoration.height()));
            decoration.border().ifPresent(border -> {
                int argb = applyAlpha(border.color());
                int x = decoration.x();
                int y = decoration.y();
                int w = decoration.width();
                int h = decoration.height();
                int t = Math.max(1, border.thickness());
                graphics.fill(x, y, x + w, y + t, argb);
                graphics.fill(x, y + h - t, x + w, y + h, argb);
                graphics.fill(x, y, x + t, y + h, argb);
                graphics.fill(x + w - t, y, x + w, y + h, argb);
            });
        });
        overlay.images().forEach(image -> graphics.blit(image.texture(), image.x(), image.y(), 0, 0,
                image.width(), image.height(), image.width(), image.height()));
        overlay.slots().forEach(slot -> {
            int color = 0xFF000000;
            int bg = 0xFF555555;
            graphics.fill(slot.x(), slot.y(), slot.x() + slot.size(), slot.y() + slot.size(), bg);
            graphics.fill(slot.x(), slot.y(), slot.x() + slot.size(), slot.y() + 1, color);
            graphics.fill(slot.x(), slot.y(), slot.x() + 1, slot.y() + slot.size(), color);
            graphics.fill(slot.x() + slot.size() - 1, slot.y(), slot.x() + slot.size(), slot.y() + slot.size(), color);
            graphics.fill(slot.x(), slot.y() + slot.size() - 1, slot.x() + slot.size(), slot.y() + slot.size(), color);
        });
        for (UiRenderer.TextBlock block : overlay.textBlocks()) {
            block.backgroundColor().ifPresent(color -> graphics.fill(block.x() - 2, block.y() - 2,
                    block.x() + block.width() + 2, block.y() + block.height() + 2, applyAlpha(color)));
            float scale = block.fontSize() / (float) Minecraft.getInstance().font.lineHeight;
            graphics.pose().pushPose();
            graphics.pose().translate(block.x(), block.y(), 0);
            graphics.pose().scale(scale, scale, 1.0f);
            graphics.drawWordWrap(Minecraft.getInstance().font, block.text(), 0, 0, (int) (block.width() / scale), block.color());
            graphics.pose().popPose();
        }
        overlay.buttons().forEach(button -> button.render(graphics, 0, 0, 0));
    }

    private static int applyAlpha(int color) {
        return color > 0xFFFFFF ? color : 0xFF000000 | color;
    }
}
