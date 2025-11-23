package net.ofatech.webcraftui.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.ofatech.webcraftui.WebcraftUI;

import java.util.HashMap;
import java.util.Map;

/**
 * Sent from client to server when an HTML element with {@code data-action} triggers.
 */
public record UiActionPayload(ResourceLocation uiId, String actionId, Map<String, String> payload) implements CustomPacketPayload {
    public static final Type<UiActionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(WebcraftUI.MOD_ID, "ui_action"));

    public static final StreamCodec<ByteBuf, UiActionPayload> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, UiActionPayload::uiId,
            ByteBufCodecs.STRING_UTF8, UiActionPayload::actionId,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8), UiActionPayload::payload,
            UiActionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
