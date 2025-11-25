package net.ofatech.webcraftui.ui.transform;

import net.minecraft.resources.ResourceLocation;
import net.ofatech.webcraftui.css.CssStylesheet;
import net.ofatech.webcraftui.css.ResolvedStyle;
import net.ofatech.webcraftui.css.StyleResolver;
import net.ofatech.webcraftui.html.model.HtmlAttributes;
import net.ofatech.webcraftui.html.model.HtmlElement;
import net.ofatech.webcraftui.html.model.HtmlNode;
import net.ofatech.webcraftui.html.model.HtmlTag;
import net.ofatech.webcraftui.html.model.HtmlText;
import net.ofatech.webcraftui.ui.model.UiButton;
import net.ofatech.webcraftui.ui.model.UiContainer;
import net.ofatech.webcraftui.ui.model.UiElement;
import net.ofatech.webcraftui.ui.model.UiImage;
import net.ofatech.webcraftui.ui.model.UiNode;
import net.ofatech.webcraftui.ui.model.UiPanel;
import net.ofatech.webcraftui.ui.model.UiRoot;
import net.ofatech.webcraftui.ui.model.UiSlot;
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
        List<String> classes = element.attribute(HtmlAttributes.CLASS).map(cls -> List.of(cls.split(" "))).orElse(List.of());
        String id = element.attribute(HtmlAttributes.ID).orElse(null);

        HtmlTag tag = HtmlTag.from(element.tagName());
        switch (tag) {
            case BUTTON, INPUT, ANCHOR -> {
                String label = extractTextContent(element)
                        .orElse(element.attribute(HtmlAttributes.VALUE).orElse("Action"));
                String actionId = element.attribute(HtmlAttributes.DATA_ACTION)
                        .orElse(element.attribute(HtmlAttributes.DATA_HANDLER).orElse(label));
                Map<String, String> args = collectArgs(element);
                return Optional.of(new UiButton(id, classes, style, label.trim(), actionId, args));
            }
            case IMAGE -> {
                ResourceLocation src = element.attribute(HtmlAttributes.SRC).map(ResourceLocation::tryParse).orElse(null);
                return Optional.of(new UiImage(id, classes, style, src));
            }
            case SLOT -> {
                int slotIndex = element.attribute(HtmlAttributes.DATA_SLOT).map(HtmlToUiTransformer::parseSlotIndex).orElse(-1);
                return Optional.of(new UiSlot(id, classes, style, slotIndex));
            }
            case LABEL, SPAN, PARAGRAPH -> {
                return extractTextContent(element)
                        .map(text -> new UiText(id, classes, style, text.trim()))
                        .map(Optional::of)
                        .orElse(Optional.empty());
            }
            default -> {
                UiElement container = (tag == HtmlTag.PANEL || tag == HtmlTag.BODY || tag == HtmlTag.SECTION || tag == HtmlTag.DIV)
                        ? new UiPanel(id, classes, style)
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
            if (key.startsWith(HtmlAttributes.DATA_ARG_PREFIX)) {
                args.put(key.substring(HtmlAttributes.DATA_ARG_PREFIX.length()), value);
            }
        });
        return args;
    }

    private static int parseSlotIndex(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static Optional<String> extractTextContent(HtmlElement element) {
        StringBuilder builder = new StringBuilder();
        for (HtmlNode child : element.children()) {
            if (child instanceof HtmlText text) {
                builder.append(text.text());
            }
        }
        String text = builder.toString();
        return text.isBlank() ? Optional.empty() : Optional.of(text);
    }
}
