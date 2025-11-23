package net.ofatech.webcraftui.ui;

import net.ofatech.webcraftui.css.CssParser;
import net.ofatech.webcraftui.css.CssStylesheet;
import net.ofatech.webcraftui.html.model.HtmlDocument;
import net.ofatech.webcraftui.html.parser.SimpleHtmlParser;
import net.ofatech.webcraftui.ui.model.UiButton;
import net.ofatech.webcraftui.ui.model.UiRoot;
import net.ofatech.webcraftui.ui.transform.HtmlToUiTransformer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HtmlToUiTransformerTest {
    @Test
    void buildsButtonsWithActions() {
        HtmlDocument doc = SimpleHtmlParser.parse("<button data-action=\"ok\">OK</button>");
        CssStylesheet sheet = CssParser.parse("");
        UiRoot root = HtmlToUiTransformer.transform(doc.root(), sheet, false);
        assertEquals(1, root.children().size());
        UiButton button = (UiButton) root.children().get(0);
        assertEquals("ok", button.actionId());
    }
}
