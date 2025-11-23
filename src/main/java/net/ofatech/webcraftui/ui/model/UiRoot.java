package net.ofatech.webcraftui.ui.model;

import net.ofatech.webcraftui.css.ResolvedStyle;

import java.util.List;

public final class UiRoot extends UiContainer {
    private final boolean overlay;

    public UiRoot(boolean overlay, ResolvedStyle style) {
        super("root", List.of(), style);
        this.overlay = overlay;
    }

    public boolean overlay() {
        return overlay;
    }
}
