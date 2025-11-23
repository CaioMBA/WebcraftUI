package net.ofatech.webcraftui.client;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.ofatech.webcraftui.WebcraftUI;
import net.ofatech.webcraftui.api.UiDocumentSource;

import java.io.IOException;
import java.io.Reader;
import java.util.Optional;

/**
 * Client-side loader that resolves {@link UiDocumentSource} instances to strings.
 */
public final class UiDocumentLoader {
    private UiDocumentLoader() {
    }

    public static Optional<String> load(UiDocumentSource source) {
        if (source.inlineContent().isPresent()) {
            return source.inlineContent();
        }
        return source.resource().flatMap(UiDocumentLoader::loadResource);
    }

    public static Optional<String> loadPath(String path) {
        return loadResource(new net.minecraft.resources.ResourceLocation(path));
    }

    private static Optional<String> loadResource(net.minecraft.resources.ResourceLocation location) {
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        try {
            Optional<Resource> resource = manager.getResource(location);
            if (resource.isEmpty()) {
                WebcraftUI.LOGGER.warn("UI resource {} was not found", location);
                return Optional.empty();
            }

            try (Reader reader = resource.get().openAsReader()) {
                java.io.StringWriter writer = new java.io.StringWriter();
                reader.transferTo(writer);
                return Optional.of(writer.toString());
            }
        } catch (IOException e) {
            WebcraftUI.LOGGER.error("Failed to read UI resource {}", location, e);
            return Optional.empty();
        }
    }
}
