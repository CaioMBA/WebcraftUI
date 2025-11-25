package net.ofatech.webcraftui.css;

import net.ofatech.webcraftui.html.model.HtmlAttributes;
import net.ofatech.webcraftui.html.model.HtmlElement;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Resolves styles for elements using simple precedence rules.
 */
public final class StyleResolver {
    private StyleResolver() {
    }

    public static ResolvedStyle resolve(HtmlElement element, CssStylesheet stylesheet) {
        Map<String, Integer> priority = new HashMap<>();
        Map<String, String> values = new HashMap<>();

        applyTagRules(element, stylesheet, values, priority, 1);
        applyClassRules(element, stylesheet, values, priority, 2);
        applyIdRules(element, stylesheet, values, priority, 3);
        applyInlineRules(element, values, priority, 4);

        ResolvedStyle resolved = new ResolvedStyle();
        values.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> resolved.put(entry.getKey(), entry.getValue()));
        return resolved;
    }

    private static void applyTagRules(HtmlElement element, CssStylesheet stylesheet, Map<String, String> values,
                                      Map<String, Integer> priority, int level) {
        stylesheet.rules().forEach(rule -> {
            if (rule.selectors().stream().anyMatch(selector -> selector.type() == CssSelectorType.TAG
                    && selector.matches(element.tagName(), element.attribute(HtmlAttributes.ID).orElse(""),
                    element.attribute(HtmlAttributes.CLASS).orElse("")))) {
                merge(rule, values, priority, level);
            }
        });
    }

    private static void applyClassRules(HtmlElement element, CssStylesheet stylesheet, Map<String, String> values,
                                       Map<String, Integer> priority, int level) {
        String classes = element.attribute(HtmlAttributes.CLASS).orElse("");
        if (classes.isEmpty()) {
            return;
        }
        for (String cls : classes.split(" ")) {
            if (cls.isBlank()) continue;
            stylesheet.rules().forEach(rule -> {
                if (rule.selectors().stream().anyMatch(selector -> selector.type() == CssSelectorType.CLASS
                        && selector.value().equalsIgnoreCase(cls))) {
                    merge(rule, values, priority, level);
                }
            });
        }
    }

    private static void applyIdRules(HtmlElement element, CssStylesheet stylesheet, Map<String, String> values,
                                     Map<String, Integer> priority, int level) {
        Optional<String> id = element.attribute(HtmlAttributes.ID);
        if (id.isEmpty()) {
            return;
        }
        stylesheet.rules().stream()
                .filter(rule -> rule.selectors().stream().anyMatch(sel -> sel.type() == CssSelectorType.ID && sel.value().equalsIgnoreCase(id.get())))
                .forEach(rule -> merge(rule, values, priority, level));
    }

    private static void applyInlineRules(HtmlElement element, Map<String, String> values, Map<String, Integer> priority, int level) {
        element.attribute(HtmlAttributes.STYLE).ifPresent(style -> {
            Map<String, String> declarations = CssParser.parseDeclarations(style);
            declarations.forEach((key, value) -> merge(values, priority, level, key, value));
        });
    }

    private static void merge(CssRule rule, Map<String, String> values, Map<String, Integer> priority, int level) {
        rule.declarations().forEach((key, value) -> merge(values, priority, level, key, value));
    }

    private static void merge(Map<String, String> values, Map<String, Integer> priority, int level, String key, String value) {
        Integer existing = priority.get(key);
        if (existing == null || existing <= level) {
            values.put(key, value);
            priority.put(key, level);
        }
    }
}
