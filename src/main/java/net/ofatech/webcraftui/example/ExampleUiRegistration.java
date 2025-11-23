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
    public static final ResourceLocation UI_ID = ResourceLocation.fromNamespaceAndPath("examplemod", "test_ui");

    private ExampleUiRegistration() {
    }

    public static void registerExamples() {
        ResourceLocation html = ResourceLocation.fromNamespaceAndPath(UI_ID.getNamespace(), "ui/test_ui.html");
        ResourceLocation css = ResourceLocation.fromNamespaceAndPath(UI_ID.getNamespace(), "ui/test_ui.css");
        ResourceLocation js = ResourceLocation.fromNamespaceAndPath(UI_ID.getNamespace(), "ui/test_ui.js");

        WebcraftUIApi.registerUi(UI_ID, UiDocumentSource.resource(html),
                Optional.of(UiDocumentSource.resource(css)), Optional.of(UiDocumentSource.resource(js)), ExampleUiRegistration::handleAction);
    }

    private static void handleAction(ServerPlayer player, String actionId, Map<String, String> payload) {
        // The payload map can include form fields or arbitrary client data.
        WebcraftUI.LOGGER.info("Example UI action {} triggered by {} with payload {}", actionId, player.getGameProfile().getName(), payload);
    }
}
