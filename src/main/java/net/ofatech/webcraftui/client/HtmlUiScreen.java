package net.ofatech.webcraftui.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.ofatech.webcraftui.WebcraftUI;
import net.ofatech.webcraftui.api.UiRegistration;
import net.ofatech.webcraftui.network.UiActionPayload;
import net.ofatech.webcraftui.ui.render.UiRenderer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.OptionalInt;

/**
 * Simple client screen that surfaces HTML/CSS/JS content to the player. This is intentionally
 * lightweight but demonstrates how actions can be dispatched back to the server.
 */
public class HtmlUiScreen extends Screen {
    private final UiRegistration registration;
    private final String html;
    private final Optional<String> css;
    private final Optional<String> js;
    private final Set<String> actions = new LinkedHashSet<>();
    private List<UiRenderer.TextBlock> textBlocks = List.of();
    private List<UiRenderer.Decoration> decorations = List.of();
    private List<UiRenderer.ImageRender> images = List.of();
    private List<UiRenderer.SlotRender> slots = List.of();
    private final List<net.minecraft.client.gui.components.Button> htmlButtons = new ArrayList<>();

    private HtmlUiScreen(UiRegistration registration, String html, Optional<String> css, Optional<String> js) {
        super(Component.literal(registration.uiId().toString()));
        this.registration = registration;
        this.html = html;
        this.css = css;
        this.js = js;
        this.actions.addAll(registration.knownActions());
    }

    @Nullable
    public static HtmlUiScreen fromRegistration(UiRegistration registration) {
        Optional<String> html = UiDocumentLoader.load(registration.html());
        if (html.isEmpty()) {
            WebcraftUI.LOGGER.warn("No HTML could be loaded for UI {}", registration.uiId());
            return null;
        }

        Optional<String> css = registration.css().flatMap(UiDocumentLoader::load);
        Optional<String> js = registration.js().flatMap(UiDocumentLoader::load);
        return new HtmlUiScreen(registration, html.get(), css, js);
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();
        try {
            UiRenderer.RenderResult layout = HtmlRenderEngine.build(this.registration, this.font, html, css, 20, 30,
                    this.width - 40, this::sendAction);

            this.textBlocks = layout.textBlocks();
            this.decorations = layout.decorations();
            this.images = layout.images();
            this.slots = layout.slots();
            this.htmlButtons.clear();
            this.htmlButtons.addAll(layout.buttons());
            this.actions.addAll(layout.actions());
            this.htmlButtons.forEach(this::addRenderableWidget);
        } catch (Exception exception) {
            WebcraftUI.LOGGER.error("Failed to build HTML UI {}", registration.uiId(), exception);
            showFallback(Component.literal("WebcraftUI failed to load: " + exception.getMessage()));
        }
    }

    private void showFallback(Component message) {
        this.htmlButtons.clear();
        int messageWidth = this.width - 40;
        int height = Math.max(this.font.lineHeight * 2, this.font.wordWrapHeight(message.getString(), messageWidth));
        this.textBlocks = List.of(new UiRenderer.TextBlock(message, 20, 40, messageWidth, height, 0xFF5555, OptionalInt.empty(),
                false, false, this.font.lineHeight));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawString(this.font, Component.literal("HTML preview"), 10, 10, 0xFFFFFF);
        int infoY = this.height - 40;
        css.ifPresent(cssContent -> graphics.drawString(this.font, Component.literal("CSS loaded"), 10, infoY, 0xA0A0A0));
        js.ifPresent(jsContent -> graphics.drawString(this.font, Component.literal("JS detected"), 100, infoY, 0xA0A0A0));

        renderDecorations(graphics);
        renderImages(graphics);
        renderSlots(graphics);
        for (UiRenderer.TextBlock block : textBlocks) {
            block.backgroundColor().ifPresent(color -> graphics.fill(block.x() - 2, block.y() - 2,
                    block.x() + block.width() + 2, block.y() + block.height() + 2, applyAlpha(color)));
            float scale = block.fontSize() / (float) this.font.lineHeight;
            graphics.pose().pushPose();
            graphics.pose().translate(block.x(), block.y(), 0);
            graphics.pose().scale(scale, scale, 1.0f);
            graphics.drawWordWrap(this.font, block.text(), 0, 0, (int) (block.width() / scale), block.color());
            graphics.pose().popPose();
        }
    }

    private void renderDecorations(GuiGraphics graphics) {
        for (UiRenderer.Decoration decoration : decorations) {
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
        }
    }

    private void renderImages(GuiGraphics graphics) {
        for (UiRenderer.ImageRender image : images) {
            graphics.blit(image.texture(), image.x(), image.y(), 0, 0, image.width(), image.height(), image.width(), image.height());
        }
    }

    private void renderSlots(GuiGraphics graphics) {
        for (UiRenderer.SlotRender slot : slots) {
            int color = 0xFF000000;
            int bg = 0xFF555555;
            graphics.fill(slot.x(), slot.y(), slot.x() + slot.size(), slot.y() + slot.size(), bg);
            graphics.fill(slot.x(), slot.y(), slot.x() + slot.size(), slot.y() + 1, color);
            graphics.fill(slot.x(), slot.y(), slot.x() + 1, slot.y() + slot.size(), color);
            graphics.fill(slot.x() + slot.size() - 1, slot.y(), slot.x() + slot.size(), slot.y() + slot.size(), color);
            graphics.fill(slot.x(), slot.y() + slot.size() - 1, slot.x() + slot.size(), slot.y() + slot.size(), color);
        }
    }

    private int applyAlpha(int color) {
        return color > 0xFFFFFF ? color : 0xFF000000 | color;
    }

    private void sendAction(String actionId) {
        sendAction(actionId, Map.of());
    }

    private void sendAction(String actionId, Map<String, String> payload) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            UiActionPayload actionPayload = new UiActionPayload(registration.uiId(), actionId, payload);
            mc.getConnection().send(actionPayload);
            mc.player.closeContainer();
        }
    }
}
