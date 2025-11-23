package net.ofatech.webcraftui.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.ofatech.webcraftui.WebcraftUI;
import net.ofatech.webcraftui.api.HtmlActionScanner;
import net.ofatech.webcraftui.api.UiRegistration;
import net.ofatech.webcraftui.network.UiActionPayload;

import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Simple client screen that surfaces HTML/CSS/JS content to the player. This is intentionally
 * lightweight but demonstrates how actions can be dispatched back to the server.
 */
public class HtmlUiScreen extends Screen {
    private final UiRegistration registration;
    private final String html;
    private final Optional<String> css;
    private final Optional<String> js;
    private MultiLineLabel htmlLabel = MultiLineLabel.EMPTY;
    private final Set<String> actions = new LinkedHashSet<>();

    private HtmlUiScreen(UiRegistration registration, String html, Optional<String> css, Optional<String> js) {
        super(Component.literal(registration.uiId().toString()));
        this.registration = registration;
        this.html = html;
        this.css = css;
        this.js = js;
        this.actions.addAll(HtmlActionScanner.findActions(html));
        if (this.actions.isEmpty()) {
            this.actions.addAll(registration.knownActions());
        }
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
        this.htmlLabel = MultiLineLabel.create(this.font, Component.literal(html), this.width - 20);

        int top = 20;
        int buttonWidth = Math.min(200, this.width - 40);
        int x = (this.width - buttonWidth) / 2;
        for (String action : actions) {
            int y = top;
            top += 24;
            addRenderableWidget(Button.builder(Component.literal(action), btn -> sendAction(action))
                    .pos(x, y)
                    .size(buttonWidth, 20)
                    .build());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawString(this.font, Component.literal("HTML content"), 10, this.height / 2, 0xFFFFFF);
        this.htmlLabel.renderCentered(graphics, this.width / 2, this.height / 2 + 12);

        int infoY = this.height - 40;
        css.ifPresent(cssContent -> graphics.drawString(this.font, Component.literal("CSS loaded"), 10, infoY, 0xA0A0A0));
        js.ifPresent(jsContent -> graphics.drawString(this.font, Component.literal("JS loaded"), 100, infoY, 0xA0A0A0));
    }

    private void sendAction(String actionId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            UiActionPayload payload = new UiActionPayload(registration.uiId(), actionId, Map.of());
            mc.getConnection().send(payload);
            mc.player.closeContainer();
        }
    }
}
