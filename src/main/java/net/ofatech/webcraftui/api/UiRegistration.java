package net.ofatech.webcraftui.api;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Bundles all metadata for a UI entry.
 */
public final class UiRegistration {
    private final ResourceLocation uiId;
    private final UiDocumentSource html;
    private final Optional<UiDocumentSource> css;
    private final Optional<UiDocumentSource> js;
    private final UiActionHandler handler;
    private final Set<String> knownActions;

    public UiRegistration(ResourceLocation uiId, UiDocumentSource html, Optional<UiDocumentSource> css,
                          Optional<UiDocumentSource> js, UiActionHandler handler, Set<String> knownActions) {
        this.uiId = Objects.requireNonNull(uiId, "uiId");
        this.html = Objects.requireNonNull(html, "html");
        this.css = Objects.requireNonNull(css, "css");
        this.js = Objects.requireNonNull(js, "js");
        this.handler = Objects.requireNonNull(handler, "handler");
        this.knownActions = Set.copyOf(Objects.requireNonNull(knownActions, "knownActions"));
    }

    public ResourceLocation uiId() {
        return uiId;
    }

    public UiDocumentSource html() {
        return html;
    }

    public Optional<UiDocumentSource> css() {
        return css;
    }

    public Optional<UiDocumentSource> js() {
        return js;
    }

    public UiActionHandler handler() {
        return handler;
    }

    /**
     * Actions discovered in the HTML (via {@code data-action} or {@code data-handler}).
     */
    public Set<String> knownActions() {
        return Collections.unmodifiableSet(knownActions);
    }
}
