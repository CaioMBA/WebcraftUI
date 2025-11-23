package net.ofatech.webcraftui.html;

import net.ofatech.webcraftui.html.model.HtmlDocument;
import net.ofatech.webcraftui.html.model.HtmlElement;
import net.ofatech.webcraftui.html.model.HtmlText;
import net.ofatech.webcraftui.html.parser.SimpleHtmlParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleHtmlParserTest {
    @Test
    void parsesElementsAndText() {
        HtmlDocument doc = SimpleHtmlParser.parse("<div class=\"box\">Hello <button data-action=\"ok\">Ok</button></div>");
        HtmlElement root = doc.root();
        assertEquals(1, root.children().size());
        HtmlElement div = (HtmlElement) root.children().get(0);
        assertEquals("div", div.tagName());
        assertEquals("box", div.attribute("class").orElse(""));
        assertEquals(2, div.children().size());
        assertTrue(div.children().get(0) instanceof HtmlText);
    }
}
