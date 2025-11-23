package net.ofatech.webcraftui.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.ofatech.webcraftui.WebcraftUI;
import net.ofatech.webcraftui.api.UiRegistration;
import net.ofatech.webcraftui.api.WebcraftUIApi;

/**
 * Resolves registrations to actual client screens.
 */
public final class ClientUiManager {
    private ClientUiManager() {
    }

    public static void openLocal(ResourceLocation uiId) {
        Minecraft mc = Minecraft.getInstance();
        WebcraftUIApi.find(uiId).ifPresentOrElse(registration -> {
            HtmlUiScreen screen = HtmlUiScreen.fromRegistration(registration);
            if (screen != null) {
                mc.setScreen(screen);
            } else {
                WebcraftUI.LOGGER.warn("Failed to build UI screen for {}", uiId);
            }
        }, () -> WebcraftUI.LOGGER.warn("Requested UI {} is not registered on the client", uiId));
    }
}
