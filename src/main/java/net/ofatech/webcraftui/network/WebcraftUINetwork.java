package net.ofatech.webcraftui.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.ofatech.webcraftui.WebcraftUI;
import net.ofatech.webcraftui.api.WebcraftUIApi;

/**
 * Wires the custom payload for UI actions. The client should send {@link UiActionPayload}
 * when a data-action element is triggered; the handler runs server-side callbacks.
 */
@EventBusSubscriber(modid = WebcraftUI.MOD_ID)
public final class WebcraftUINetwork {
    private WebcraftUINetwork() {
    }

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(WebcraftUI.MOD_ID);
        registrar.playToServer(UiActionPayload.TYPE, UiActionPayload.STREAM_CODEC, WebcraftUINetwork::handleAction);
    }

    private static void handleAction(UiActionPayload payload, IPayloadContext context) {
        context.workHandler().submitAsync(() -> {
            if (context.flow().isServerbound() && context.player() instanceof ServerPlayer serverPlayer) {
                WebcraftUIApi.dispatchAction(serverPlayer, payload);
            }
        });
    }
}
