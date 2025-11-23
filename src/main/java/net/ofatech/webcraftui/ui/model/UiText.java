package net.ofatech.webcraftui.ui.model;

import net.ofatech.webcraftui.css.ResolvedStyle;

import java.util.Collections;
import java.util.List;

public final class UiText extends UiElement {
    private final String text;

    public UiText(String id, List<String> classes, ResolvedStyle style, String text) {
        super(id, classes, style);
        this.text = text;
    }

    public String text() {
        return text;
    }

    @Override
    public List<UiNode> children() {
        return Collections.emptyList();
    }
}
