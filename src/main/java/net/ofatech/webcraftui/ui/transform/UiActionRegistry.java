package net.ofatech.webcraftui.ui.transform;

import net.minecraft.resources.ResourceLocation;
import net.ofatech.webcraftui.ui.model.UiActionContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Registry for UI actions that can be invoked from rendered widgets.
 */
public final class UiActionRegistry {
    private static final Map<String, Consumer<UiActionContext>> ACTIONS = new ConcurrentHashMap<>();

    private UiActionRegistry() {
    }

    public static void registerAction(String name, Consumer<UiActionContext> handler) {
        ACTIONS.put(name, handler);
    }

    public static void dispatch(ResourceLocation uiId, String action, Map<String, String> args) {
        Consumer<UiActionContext> consumer = ACTIONS.get(action);
        if (consumer != null) {
            consumer.accept(UiActionContext.of(uiId, action, args));
        }
    }
}
