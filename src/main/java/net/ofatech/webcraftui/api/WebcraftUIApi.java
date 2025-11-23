package net.ofatech.webcraftui.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.ofatech.webcraftui.WebcraftUI;
import net.ofatech.webcraftui.client.ClientUiManager;
import net.ofatech.webcraftui.client.HudOverlayManager;
import net.ofatech.webcraftui.network.UiActionPayload;
import net.ofatech.webcraftui.ui.model.UiActionContext;
import net.ofatech.webcraftui.ui.transform.UiActionRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public final class WebcraftUIApi {
    private static final UiRegistry REGISTRY = new UiRegistry(WebcraftUI.LOGGER);

    private WebcraftUIApi() {
    }

    public static UiRegistration registerUi(ResourceLocation uiId, UiDocumentSource html, Optional<UiDocumentSource> css,
                                            Optional<UiDocumentSource> js, UiActionHandler handler) {
        return REGISTRY.register(uiId, html, css, js, handler);
    }

    public static UiRegistration registerUi(ResourceLocation uiId, UiDocumentSource html, UiActionHandler handler) {
        return registerUi(uiId, html, Optional.empty(), Optional.empty(), handler);
    }

    public static void openScreen(Player player, ResourceLocation uiId) {
        if (player.level().isClientSide) {
            WebcraftUI.LOGGER.info("Requesting UI {} to open for client {}", uiId, player.getGameProfile().getName());
            if (FMLEnvironment.dist == Dist.CLIENT) {
                ClientUiManager.openLocal(uiId);
            }
        } else if (player instanceof ServerPlayer serverPlayer) {
            WebcraftUI.LOGGER.warn("Attempted to open UI {} on server side for {}. Server->client packets are required to open UIs remotely.", uiId, serverPlayer.getGameProfile().getName());
        }
    }

    public static void showHud(Player player, ResourceLocation uiId) {
        if (player.level().isClientSide && FMLEnvironment.dist == Dist.CLIENT) {
            find(uiId).ifPresent(HudOverlayManager::show);
        }
    }

    public static void dispatchAction(ServerPlayer player, UiActionPayload payload) {
        REGISTRY.lookup(payload.uiId()).ifPresentOrElse(registration -> {
            registration.handler().onAction(player, payload.actionId(), payload.payload());
        }, () -> WebcraftUI.LOGGER.warn("Received UI action for unknown id {}", payload.uiId()));
    }

    public static void registerAction(String name, java.util.function.Consumer<UiActionContext> handler) {
        UiActionRegistry.registerAction(name, handler);
    }

    public static Optional<UiRegistration> find(ResourceLocation uiId) {
        return REGISTRY.lookup(uiId);
    }

    public static Collection<UiRegistration> allRegistrations() {
        return REGISTRY.all();
    }

    public static void pushStateToClient(ServerPlayer player, ResourceLocation uiId, Map<String, String> state) {
        WebcraftUI.LOGGER.info("Push state to {} for UI {}: {}", player.getGameProfile().getName(), uiId, state);
    }
}
