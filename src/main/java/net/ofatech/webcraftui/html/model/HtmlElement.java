package net.ofatech.webcraftui.html.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Parsed HTML element with a deterministic child order.
 */
public final class HtmlElement implements HtmlNode {
    private final String tagName;
    private final Map<String, String> attributes = new HashMap<>();
    private final List<HtmlNode> children = new ArrayList<>();

    public HtmlElement(String tagName) {
        this.tagName = tagName.toLowerCase();
    }

    public String tagName() {
        return tagName;
    }

    public void addAttribute(String name, String value) {
        attributes.put(name.toLowerCase(), value);
    }

    public Map<String, String> attributes() {
        return Collections.unmodifiableMap(attributes);
    }

    public Optional<String> attribute(String name) {
        return Optional.ofNullable(attributes.get(name.toLowerCase()));
    }

    public void addChild(HtmlNode node) {
        children.add(node);
    }

    @Override
    public List<HtmlNode> children() {
        return Collections.unmodifiableList(children);
    }
}
