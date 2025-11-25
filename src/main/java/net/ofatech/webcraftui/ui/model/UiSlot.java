package net.ofatech.webcraftui.ui.model;

import net.ofatech.webcraftui.css.ResolvedStyle;

import java.util.Collections;
import java.util.List;

public final class UiSlot extends UiElement {
    private final int slotIndex;

    public UiSlot(String id, List<String> classes, ResolvedStyle style, int slotIndex) {
        super(id, classes, style);
        this.slotIndex = slotIndex;
    }

    public int slotIndex() {
        return slotIndex;
    }

    @Override
    public List<UiNode> children() {
        return Collections.emptyList();
    }
}
