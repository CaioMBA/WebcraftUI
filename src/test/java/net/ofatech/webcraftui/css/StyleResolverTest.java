package net.ofatech.webcraftui.css;

import net.ofatech.webcraftui.html.model.HtmlDocument;
import net.ofatech.webcraftui.html.model.HtmlElement;
import net.ofatech.webcraftui.html.parser.SimpleHtmlParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StyleResolverTest {
    @Test
    void mergesSelectorsWithPrecedence() {
        CssStylesheet stylesheet = CssParser.parse("button{color:#111111;} .primary{color:#222222;} #ok{color:#333333;}");
        HtmlDocument doc = SimpleHtmlParser.parse("<button id=\"ok\" class=\"primary\"></button>");
        HtmlElement element = (HtmlElement) doc.root().children().get(0);
        ResolvedStyle style = StyleResolver.resolve(element, stylesheet);
        assertEquals("#333333", style.getOrDefault("color", ""));
    }
}
