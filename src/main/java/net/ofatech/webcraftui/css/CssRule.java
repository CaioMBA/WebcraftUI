package net.ofatech.webcraftui.css;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a CSS rule with selectors and declarations.
 */
public final class CssRule {
    private final List<CssSelector> selectors;
    private final Map<String, String> declarations;

    public CssRule(List<CssSelector> selectors, Map<String, String> declarations) {
        this.selectors = List.copyOf(selectors);
        this.declarations = new LinkedHashMap<>(declarations);
    }

    public List<CssSelector> selectors() {
        return selectors;
    }

    public Map<String, String> declarations() {
        return Collections.unmodifiableMap(declarations);
    }
}
