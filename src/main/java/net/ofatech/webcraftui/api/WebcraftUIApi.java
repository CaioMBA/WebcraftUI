package net.ofatech.webcraftui.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.ofatech.webcraftui.WebcraftUI;
import net.ofatech.webcraftui.client.ClientUiManager;
import net.ofatech.webcraftui.network.UiActionPayload;
import net.neoforged.fml.DistExecutor;

import java.util.Map;
import java.util.Optional;

/**
 * Entry point for other mods. Register your HTML UI and provide a callback for actions.
 */
public final class WebcraftUIApi {
    private static final UiRegistry REGISTRY = new UiRegistry(WebcraftUI.LOGGER);

    private WebcraftUIApi() {
    }

    /**
     * Register a UI that can be opened later with {@link #openScreen(Player, ResourceLocation)}.
     */
    public static UiRegistration registerUi(ResourceLocation uiId, UiDocumentSource html, Optional<UiDocumentSource> css,
                                            Optional<UiDocumentSource> js, UiActionHandler handler) {
        return REGISTRY.register(uiId, html, css, js, handler);
    }

    /**
     * Convenience overload when CSS/JS are not provided.
     */
    public static UiRegistration registerUi(ResourceLocation uiId, UiDocumentSource html, UiActionHandler handler) {
        return registerUi(uiId, html, Optional.empty(), Optional.empty(), handler);
    }

    /**
     * Called on the client to ask the renderer to open the UI for the player.
     * Rendering is intentionally not implemented here; the API only dispatches the request.
     */
    public static void openScreen(Player player, ResourceLocation uiId) {
        if (player.level().isClientSide) {
            WebcraftUI.LOGGER.info("Requesting UI {} to open for client {}", uiId, player.getGameProfile().getName());
            DistExecutor.unsafeRunWhenOn(net.neoforged.fml.Dist.CLIENT, () -> () -> ClientUiManager.openLocal(uiId));
        } else if (player instanceof ServerPlayer serverPlayer) {
            WebcraftUI.LOGGER.warn("Attempted to open UI {} on server side for {}. Server->client packets are required to open UIs remotely.", uiId, serverPlayer.getGameProfile().getName());
        }
    }

    /**
     * Resolve an incoming action on the server and invoke the registered handler.
     */
    public static void dispatchAction(ServerPlayer player, UiActionPayload payload) {
        REGISTRY.lookup(payload.uiId()).ifPresentOrElse(registration -> {
            registration.handler().onAction(player, payload.actionId(), payload.payload());
        }, () -> WebcraftUI.LOGGER.warn("Received UI action for unknown id {}", payload.uiId()));
    }

    /**
     * Look up a registration by id. Exposed for client side loaders.
     */
    public static Optional<UiRegistration> find(ResourceLocation uiId) {
        return REGISTRY.lookup(uiId);
    }

    /**
     * Optional helper for pushing state back to a client-side UI renderer.
     * Rendering/network forwarding is intentionally left to the consumer; we simply log for now.
     */
    public static void pushStateToClient(ServerPlayer player, ResourceLocation uiId, Map<String, String> state) {
        WebcraftUI.LOGGER.info("Push state to {} for UI {}: {}", player.getGameProfile().getName(), uiId, state);
    }
}
