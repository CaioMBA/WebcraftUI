package net.ofatech.webcraftui.css;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Final merged style for an element.
 */
public final class ResolvedStyle {
    private final Map<String, String> properties = new HashMap<>();

    public void put(String key, String value) {
        properties.put(key.toLowerCase(), value);
    }

    public Map<String, String> properties() {
        return Collections.unmodifiableMap(properties);
    }

    public String getOrDefault(String key, String fallback) {
        return properties.getOrDefault(key.toLowerCase(), fallback);
    }

    public int getInt(String key, int fallback) {
        try {
            String raw = properties.get(key.toLowerCase());
            if (raw == null) {
                return fallback;
            }
            raw = raw.replace("px", "").trim();
            return Integer.parseInt(raw);
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
