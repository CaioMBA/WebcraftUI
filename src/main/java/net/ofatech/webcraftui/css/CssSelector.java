package net.ofatech.webcraftui.css;

/**
 * Minimal selector representation supporting tag, class, and id selectors.
 */
public record CssSelector(CssSelectorType type, String value) {
    public boolean matches(String tagName, String id, String classes) {
        return switch (type) {
            case TAG -> value.equalsIgnoreCase(tagName);
            case CLASS -> classes != null && java.util.Arrays.stream(classes.split(" ")).anyMatch(cls -> cls.equalsIgnoreCase(value));
            case ID -> value.equalsIgnoreCase(id);
        };
    }
}
