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
        for (UiRenderer.TextBlock block : overlay.textBlocks()) {
            block.backgroundColor().ifPresent(color -> graphics.fill(block.x() - 2, block.y() - 2,
                    block.x() + block.width() + 2, block.y() + block.height() + 2, 0xAA000000 | color));
            graphics.drawWordWrap(Minecraft.getInstance().font, block.text(), block.x(), block.y(), block.width(), block.color());
        }
        overlay.buttons().forEach(button -> button.render(graphics, 0, 0, 0));
    }
}
