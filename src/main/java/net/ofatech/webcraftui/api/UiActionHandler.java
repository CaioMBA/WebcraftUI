package net.ofatech.webcraftui.api;

import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

/**
 * Callback surface that receives UI actions coming from the client.
 */
@FunctionalInterface
public interface UiActionHandler {
    /**
     * @param player   server-side player who triggered the action
     * @param actionId resolved identifier from {@code data-action} or {@code data-handler}
     * @param payload  arbitrary string payload (form data, JSON snippets, etc.)
     */
    void onAction(ServerPlayer player, String actionId, Map<String, String> payload);
}
