package net.ofatech.webcraftui.api;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;

/**
 * Describes where HTML, CSS or JS for a UI is located.
 * Mod authors can point to a resource (assets/&lt;modid&gt;/...) or provide inline content.
 */
public final class UiDocumentSource {
    private final ResourceLocation resource;
    private final String inlineContent;

    private UiDocumentSource(ResourceLocation resource, String inlineContent) {
        this.resource = resource;
        this.inlineContent = inlineContent;
    }

    /** Use a resource location inside your mod jar. */
    public static UiDocumentSource resource(ResourceLocation location) {
        Objects.requireNonNull(location, "location");
        return new UiDocumentSource(location, null);
    }

    /**
     * Provide content inline. This is useful for tests, examples or generated UIs.
     */
    public static UiDocumentSource inline(String content) {
        Objects.requireNonNull(content, "content");
        return new UiDocumentSource(null, content);
    }

    public Optional<ResourceLocation> resource() {
        return Optional.ofNullable(resource);
    }

    public Optional<String> inlineContent() {
        return Optional.ofNullable(inlineContent);
    }

    @Override
    public String toString() {
        return resource != null ? resource.toString() : "inline";
    }
}
