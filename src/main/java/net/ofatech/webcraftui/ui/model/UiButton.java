package net.ofatech.webcraftui.ui.model;

import net.ofatech.webcraftui.css.ResolvedStyle;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class UiButton extends UiElement {
    private final String label;
    private final String actionId;
    private final Map<String, String> arguments;

    public UiButton(String id, List<String> classes, ResolvedStyle style, String label, String actionId, Map<String, String> arguments) {
        super(id, classes, style);
        this.label = label;
        this.actionId = actionId;
        this.arguments = arguments == null ? Map.of() : Map.copyOf(arguments);
    }

    public String label() {
        return label;
    }

    public String actionId() {
        return actionId;
    }

    public Map<String, String> arguments() {
        return arguments;
    }

    @Override
    public List<UiNode> children() {
        return Collections.emptyList();
    }
}
