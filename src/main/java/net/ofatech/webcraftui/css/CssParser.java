package net.ofatech.webcraftui.css;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minimal CSS parser capable of tag/class/id selectors and flat declarations.
 */
public final class CssParser {
    private static final Pattern RULE_PATTERN = Pattern.compile("([^{}]+)\\{([^}]*)}*");

    private CssParser() {
    }

    public static CssStylesheet parse(String css) {
        CssStylesheet stylesheet = new CssStylesheet();
        Matcher matcher = RULE_PATTERN.matcher(css);
        while (matcher.find()) {
            String selectorRaw = matcher.group(1).trim();
            String declarationsRaw = matcher.group(2).trim();
            if (selectorRaw.isEmpty() || declarationsRaw.isEmpty()) {
                continue;
            }
            List<CssSelector> selectors = parseSelectors(selectorRaw);
            Map<String, String> declarations = parseDeclarations(declarationsRaw);
            stylesheet.addRule(new CssRule(selectors, declarations));
        }
        return stylesheet;
    }

    private static List<CssSelector> parseSelectors(String raw) {
        String[] parts = raw.split(",");
        List<CssSelector> selectors = new ArrayList<>();
        for (String part : parts) {
            String selector = part.trim();
            if (selector.startsWith("#")) {
                selectors.add(new CssSelector(CssSelectorType.ID, selector.substring(1)));
            } else if (selector.startsWith(".")) {
                selectors.add(new CssSelector(CssSelectorType.CLASS, selector.substring(1)));
            } else if (!selector.isEmpty()) {
                selectors.add(new CssSelector(CssSelectorType.TAG, selector));
            }
        }
        return selectors;
    }

    public static Map<String, String> parseDeclarations(String raw) {
        Map<String, String> map = new HashMap<>();
        for (String declaration : raw.split(";")) {
            if (declaration.isBlank()) {
                continue;
            }
            String[] parts = declaration.split(":", 2);
            if (parts.length == 2) {
                map.put(parts[0].trim().toLowerCase(), parts[1].trim());
            }
        }
        return map;
    }
}
