package net.ofatech.webcraftui.html.model;

/**
 * Root HTML document wrapper.
 */
public final class HtmlDocument {
    private final HtmlElement root;

    public HtmlDocument(HtmlElement root) {
        this.root = root;
    }

    public HtmlElement root() {
        return root;
    }
}
