package net.ofatech.webcraftui.html.model;

import java.util.Collections;
import java.util.List;

/**
 * Simple text node.
 */
public final class HtmlText implements HtmlNode {
    private final String text;

    public HtmlText(String text) {
        this.text = text;
    }

    public String text() {
        return text;
    }

    @Override
    public List<HtmlNode> children() {
        return Collections.emptyList();
    }
}
