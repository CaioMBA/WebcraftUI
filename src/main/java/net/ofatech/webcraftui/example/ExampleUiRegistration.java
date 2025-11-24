package net.ofatech.webcraftui.example;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.ofatech.webcraftui.WebcraftUI;
import net.ofatech.webcraftui.api.UiDocumentSource;
import net.ofatech.webcraftui.api.WebcraftUIApi;

import java.util.Map;
import java.util.Optional;

/**
 * Demonstrates how another mod would register a UI and handle callbacks.
 */
public final class ExampleUiRegistration {
    public static final ResourceLocation UI_ID = ResourceLocation.fromNamespaceAndPath(WebcraftUI.MOD_ID, "example_inventory");
    public static final ResourceLocation HUD_ID = ResourceLocation.fromNamespaceAndPath(WebcraftUI.MOD_ID, "hud_status");

    private ExampleUiRegistration() {
    }

    public static void registerExamples() {
        ResourceLocation html = ResourceLocation.fromNamespaceAndPath(UI_ID.getNamespace(), "gui/html/example_inventory.html");
        ResourceLocation css = ResourceLocation.fromNamespaceAndPath(UI_ID.getNamespace(), "gui/style/default_minecraft_ui.css");

        WebcraftUIApi.registerUi(UI_ID, UiDocumentSource.resource(html), Optional.of(UiDocumentSource.resource(css)), Optional.empty(), ExampleUiRegistration::handleAction);

        ResourceLocation hudHtml = ResourceLocation.fromNamespaceAndPath(UI_ID.getNamespace(), "gui/html/hud_status.html");
        WebcraftUIApi.registerUi(HUD_ID, UiDocumentSource.resource(hudHtml), Optional.of(UiDocumentSource.resource(css)), Optional.empty(), ExampleUiRegistration::handleAction);
    }

    private static void handleAction(ServerPlayer player, String actionId, Map<String, String> payload) {
        // The payload map can include form fields or arbitrary client data.
        WebcraftUI.LOGGER.info("Example UI action {} triggered by {} with payload {}", actionId, player.getGameProfile().getName(), payload);
    }
}
