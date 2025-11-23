package net.ofatech.webcraftui.ui.model;

import net.ofatech.webcraftui.css.ResolvedStyle;

import java.util.List;

public interface UiNode {
    List<UiNode> children();

    ResolvedStyle style();
}
