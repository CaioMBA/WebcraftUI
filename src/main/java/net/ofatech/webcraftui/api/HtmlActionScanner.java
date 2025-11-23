package net.ofatech.webcraftui.api;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lightweight helper that discovers {@code data-action} / {@code data-handler} attributes in HTML snippets.
 * This avoids pulling a full HTML parser dependency for the simple attribute scanning we need at runtime.
 */
public final class HtmlActionScanner {
    private static final Pattern ACTION_PATTERN = Pattern.compile("data-(?:action|handler)\\s*=\\s*\"([^\"]+)\"");

    private HtmlActionScanner() {
    }

    public static Set<String> findActions(String html) {
        Set<String> actions = new HashSet<>();
        Matcher matcher = ACTION_PATTERN.matcher(html);
        while (matcher.find()) {
            actions.add(matcher.group(1));
        }
        return actions;
    }
}
