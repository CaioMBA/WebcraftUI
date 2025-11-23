package net.ofatech.webcraftui.api;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Internal registry that stores UI registrations by id.
 */
public final class UiRegistry {
    private final Map<ResourceLocation, UiRegistration> registrations = new ConcurrentHashMap<>();
    private final Logger logger;

    public UiRegistry(Logger logger) {
        this.logger = logger;
    }

    public UiRegistration register(ResourceLocation uiId, UiDocumentSource html, Optional<UiDocumentSource> css,
                                   Optional<UiDocumentSource> js, UiActionHandler handler) {
        Set<String> discoveredActions = html.inlineContent()
                .map(HtmlActionScanner::findActions)
                .orElseGet(Set::of);
        UiRegistration registration = new UiRegistration(uiId, html, css, js, handler, discoveredActions);
        registrations.put(uiId, registration);
        logger.info("Registered UI {} ({} actions discovered)", uiId, discoveredActions.size());
        return registration;
    }

    public Optional<UiRegistration> lookup(ResourceLocation uiId) {
        return Optional.ofNullable(registrations.get(uiId));
    }

    public Set<UiRegistration> all() {
        return Set.copyOf(registrations.values());
    }
}
