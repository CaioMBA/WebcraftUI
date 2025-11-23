package net.ofatech.webcraftui.css;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CssStylesheet {
    private final List<CssRule> rules = new ArrayList<>();

    public void addRule(CssRule rule) {
        rules.add(rule);
    }

    public List<CssRule> rules() {
        return Collections.unmodifiableList(rules);
    }

    public void merge(CssStylesheet other) {
        rules.addAll(other.rules);
    }
}
