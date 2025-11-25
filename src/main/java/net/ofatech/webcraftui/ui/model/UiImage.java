package net.ofatech.webcraftui.ui.model;

import net.minecraft.resources.ResourceLocation;
import net.ofatech.webcraftui.css.ResolvedStyle;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class UiImage extends UiElement {
    private final ResourceLocation source;

    public UiImage(String id, List<String> classes, ResolvedStyle style, ResourceLocation source) {
        super(id, classes, style);
        this.source = source;
    }

    public Optional<ResourceLocation> source() {
        return Optional.ofNullable(source);
    }

    @Override
    public List<UiNode> children() {
        return Collections.emptyList();
    }
}
