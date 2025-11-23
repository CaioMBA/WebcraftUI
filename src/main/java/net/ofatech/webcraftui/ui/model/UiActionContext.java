package net.ofatech.webcraftui.ui.model;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public record UiActionContext(ResourceLocation uiId, String actionId, Map<String, String> arguments, Player player) {
    public static UiActionContext of(ResourceLocation uiId, String actionId, Map<String, String> arguments) {
        Player player = Minecraft.getInstance().player;
        return new UiActionContext(uiId, actionId, arguments, player);
    }
}
