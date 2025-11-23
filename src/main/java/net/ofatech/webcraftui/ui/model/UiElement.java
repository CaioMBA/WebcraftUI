package net.ofatech.webcraftui.ui.model;

import net.ofatech.webcraftui.css.ResolvedStyle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class UiElement implements UiNode {
    private final String id;
    private final List<String> classes;
    private final ResolvedStyle style;
    private final List<UiNode> children = new ArrayList<>();

    protected UiElement(String id, List<String> classes, ResolvedStyle style) {
        this.id = id;
        this.classes = classes == null ? List.of() : List.copyOf(classes);
        this.style = style;
    }

    public String id() {
        return id;
    }

    public List<String> classes() {
        return classes;
    }

    @Override
    public List<UiNode> children() {
        return Collections.unmodifiableList(children);
    }

    public void addChild(UiNode node) {
        children.add(node);
    }

    @Override
    public ResolvedStyle style() {
        return style;
    }
}
