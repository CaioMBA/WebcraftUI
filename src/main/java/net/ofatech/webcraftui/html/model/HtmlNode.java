package net.ofatech.webcraftui.html.model;

import java.util.List;

/**
 * Base marker for parsed HTML nodes.
 */
public interface HtmlNode {
    List<HtmlNode> children();
}
