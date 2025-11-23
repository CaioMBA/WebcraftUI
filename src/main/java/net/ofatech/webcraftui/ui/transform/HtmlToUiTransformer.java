package net.ofatech.webcraftui.ui.transform;

import net.ofatech.webcraftui.css.CssStylesheet;
import net.ofatech.webcraftui.css.ResolvedStyle;
import net.ofatech.webcraftui.css.StyleResolver;
import net.ofatech.webcraftui.html.model.HtmlElement;
import net.ofatech.webcraftui.html.model.HtmlNode;
import net.ofatech.webcraftui.html.model.HtmlText;
import net.ofatech.webcraftui.ui.model.UiButton;
import net.ofatech.webcraftui.ui.model.UiContainer;
import net.ofatech.webcraftui.ui.model.UiElement;
import net.ofatech.webcraftui.ui.model.UiNode;
import net.ofatech.webcraftui.ui.model.UiPanel;
import net.ofatech.webcraftui.ui.model.UiRoot;
import net.ofatech.webcraftui.ui.model.UiText;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Converts HTML nodes with resolved CSS styles to the internal UI model tree.
 */
public final class HtmlToUiTransformer {
    private HtmlToUiTransformer() {
    }

    public static UiRoot transform(HtmlElement rootElement, CssStylesheet stylesheet, boolean overlay) {
        ResolvedStyle style = StyleResolver.resolve(rootElement, stylesheet);
        UiRoot root = new UiRoot(overlay, style);
        for (HtmlNode child : rootElement.children()) {
            transformNode(child, stylesheet).ifPresent(root::addChild);
        }
        return root;
    }

    private static Optional<UiNode> transformNode(HtmlNode node, CssStylesheet stylesheet) {
        if (node instanceof HtmlText text) {
            if (text.text().isBlank()) {
                return Optional.empty();
            }
            ResolvedStyle style = new ResolvedStyle();
            return Optional.of(new UiText(null, List.of(), style, text.text().trim()));
        }
        if (!(node instanceof HtmlElement element)) {
            return Optional.empty();
        }
        ResolvedStyle style = StyleResolver.resolve(element, stylesheet);
        List<String> classes = element.attribute("class").map(cls -> List.of(cls.split(" "))).orElse(List.of());
        String id = element.attribute("id").orElse(null);

        switch (element.tagName()) {
            case "button", "a", "input" -> {
                String label = element.children().stream()
                        .filter(HtmlText.class::isInstance)
                        .map(HtmlText.class::cast)
                        .map(HtmlText::text)
                        .findFirst()
                        .orElse(element.attribute("value").orElse("Action"));
                String actionId = element.attribute("data-action").orElse(element.attribute("data-handler").orElse(label));
                Map<String, String> args = collectArgs(element);
                return Optional.of(new UiButton(id, classes, style, label.trim(), actionId, args));
            }
            case "img" -> {
                UiPanel image = new UiPanel(id, classes, style);
                return Optional.of(image);
            }
            default -> {
                UiElement container = element.tagName().equals("panel") ? new UiPanel(id, classes, style)
                        : new UiContainer(id, classes, style);
                for (HtmlNode child : element.children()) {
                    transformNode(child, stylesheet).ifPresent(container::addChild);
                }
                return Optional.of(container);
            }
        }
    }

    private static Map<String, String> collectArgs(HtmlElement element) {
        Map<String, String> args = new HashMap<>();
        element.attributes().forEach((key, value) -> {
            if (key.startsWith("data-arg-")) {
                args.put(key.substring("data-arg-".length()), value);
            }
        });
        return args;
    }
}
