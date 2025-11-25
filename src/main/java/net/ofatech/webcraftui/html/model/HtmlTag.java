package net.ofatech.webcraftui.html.model;

import java.util.Locale;

/**
 * HTML tag names with helper logic to avoid ad-hoc string checks.
 */
public enum HtmlTag {
    ROOT("root"),
    COMPONENT("component"),
    LINK("link"),
    STYLE("style"),
    BUTTON("button"),
    INPUT("input"),
    ANCHOR("a"),
    IMAGE("img"),
    BR("br"),
    META("meta"),
    PANEL("panel"),
    DIV("div"),
    BODY("body"),
    SPAN("span"),
    PARAGRAPH("p"),
    LABEL("label"),
    SECTION("section"),
    SLOT("slot"),
    UNKNOWN("unknown");

    private final String tagName;

    HtmlTag(String tagName) {
        this.tagName = tagName;
    }

    public String tagName() {
        return tagName;
    }

    public boolean matches(String rawName) {
        return tagName.equalsIgnoreCase(rawName);
    }

    public static HtmlTag from(String rawName) {
        String lowered = rawName.toLowerCase(Locale.ROOT);
        for (HtmlTag tag : values()) {
            if (tag.tagName.equals(lowered)) {
                return tag;
            }
        }
        return UNKNOWN;
    }

    public boolean isVoid() {
        return switch (this) {
            case IMAGE, INPUT, LINK, BR, META -> true;
            default -> false;
        };
    }
}
