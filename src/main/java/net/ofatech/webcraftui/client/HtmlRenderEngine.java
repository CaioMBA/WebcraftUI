package net.ofatech.webcraftui.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Very small HTML layout helper that turns common elements into vanilla widgets/text blocks.
 * This is intentionally limited – it is not a browser – but it respects inline styles and
 * {@code data-action}/{@code data-handler} hooks so real interactions can be dispatched.
 */
public final class HtmlRenderEngine {
    private static final Pattern COLOR_PATTERN = Pattern.compile("color\\s*:\\s*([^;]+)");
    private static final Pattern BACKGROUND_PATTERN = Pattern.compile("background(?:-color)?\\s*:\\s*([^;]+)");
    private static final Pattern WIDTH_PATTERN = Pattern.compile("width\\s*:\\s*(\\d+)px");
    private static final Pattern HEX_PATTERN = Pattern.compile("#?([0-9a-fA-F]{6})");
    private static final Pattern RGB_PATTERN = Pattern.compile("rgb\\s*\\(\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*\\)");

    private HtmlRenderEngine() {
    }

    public static RenderResult build(Font font, String html, Optional<String> css, int x, int y, int width,
                                     BiConsumer<String, Map<String, String>> actionDispatcher) {
        Document document = Jsoup.parse(html);
        css.ifPresent(content -> document.head().appendElement("style").text(content));

        RenderContext context = new RenderContext(font, x, y, width, actionDispatcher);
        document.body().childNodes().forEach(node -> context.renderNode(node, document.body()));
        return new RenderResult(context.textBlocks, context.buttons, context.actions);
    }

    public record RenderResult(List<TextBlock> textBlocks, List<Button> buttons, Set<String> actions) {
    }

    public record TextBlock(Component text, int x, int y, int width, int height, int color, OptionalInt backgroundColor) {
    }

    private static final class RenderContext {
        private final Font font;
        private final int x;
        private final int width;
        private int cursorY;
        private final List<TextBlock> textBlocks = new ArrayList<>();
        private final List<Button> buttons = new ArrayList<>();
        private final Set<String> actions = new HashSet<>();
        private final BiConsumer<String, Map<String, String>> actionDispatcher;

        RenderContext(Font font, int x, int y, int width, BiConsumer<String, Map<String, String>> actionDispatcher) {
            this.font = font;
            this.x = x;
            this.cursorY = y;
            this.width = width;
            this.actionDispatcher = actionDispatcher;
        }

        void renderNode(Node node, Element parent) {
            if (node instanceof TextNode textNode) {
                addTextBlock(textNode.text(), parent, 0);
            } else if (node instanceof Element element) {
                switch (element.tagName()) {
                    case "br" -> cursorY += font.lineHeight;
                    case "button", "a" -> addButton(element);
                    case "input" -> handleInput(element);
                    default -> renderContainer(element);
                }
            }
        }

        private void renderContainer(Element element) {
            if (!element.ownText().isBlank()) {
                int extraMargin = element.tagName().matches("h[1-6]") ? font.lineHeight : 0;
                addTextBlock(element.ownText(), element, extraMargin);
            }
            element.childNodes().forEach(child -> renderNode(child, element));
            if (element.tagName().equals("p") || element.tagName().equals("div")) {
                cursorY += font.lineHeight / 2;
            }
        }

        private void addButton(Element element) {
            String labelText = element.text().isBlank() ? "Action" : element.text();
            String actionId = actionId(element).orElse(labelText);
            Map<String, String> payload = buildPayload(element);
            int buttonWidth = Math.min(width, Math.max(80, font.width(labelText) + 20));
            Matcher widthMatcher = WIDTH_PATTERN.matcher(element.attr("style"));
            if (widthMatcher.find()) {
                buttonWidth = Math.min(width, Integer.parseInt(widthMatcher.group(1)));
            }

            Button button = Button.builder(Component.literal(labelText), btn -> actionDispatcher.accept(actionId, payload))
                    .pos(x, cursorY)
                    .size(buttonWidth, 20)
                    .build();
            buttons.add(button);
            actions.add(actionId);
            cursorY += button.getHeight() + 6;
        }

        private void handleInput(Element element) {
            String type = element.attr("type");
            if (type.equalsIgnoreCase("button") || type.equalsIgnoreCase("submit")) {
                addButton(element);
            } else {
                addTextBlock("[" + (type.isBlank() ? "input" : type) + " field not supported in preview]", element, 0);
            }
        }

        private void addTextBlock(String rawText, Element element, int extraMargin) {
            String text = rawText.strip();
            if (text.isEmpty()) {
                return;
            }
            int color = parseColor(element).orElse(0xFFFFFF);
            OptionalInt background = parseBackground(element);
            Component component = Component.literal(text);
            int height = Math.max(font.lineHeight, font.wordWrapHeight(text, width));
            textBlocks.add(new TextBlock(component, x, cursorY, width, height, color, background));
            cursorY += height + 4 + extraMargin;
        }

        private Optional<String> actionId(Element element) {
            if (element.hasAttr("data-action")) {
                return Optional.of(element.attr("data-action"));
            }
            if (element.hasAttr("data-handler")) {
                return Optional.of(element.attr("data-handler"));
            }
            if (!element.id().isBlank()) {
                return Optional.of(element.id());
            }
            return Optional.empty();
        }

        private Map<String, String> buildPayload(Element element) {
            Map<String, String> payload = new HashMap<>();
            element.attributes().forEach(attribute -> {
                String key = attribute.getKey();
                if (key.startsWith("data-param-")) {
                    payload.put(key.substring("data-param-".length()), attribute.getValue());
                }
            });
            return payload;
        }
    }

    private static OptionalInt parseColor(Element element) {
        return parseColorFromStyle(element.attr("style"), COLOR_PATTERN);
    }

    private static OptionalInt parseBackground(Element element) {
        return parseColorFromStyle(element.attr("style"), BACKGROUND_PATTERN);
    }

    private static OptionalInt parseColorFromStyle(String style, Pattern pattern) {
        if (style == null || style.isBlank()) {
            return OptionalInt.empty();
        }
        Matcher matcher = pattern.matcher(style);
        if (matcher.find()) {
            return parseColorString(matcher.group(1).trim());
        }
        return OptionalInt.empty();
    }

    private static OptionalInt parseColorString(String value) {
        Matcher hex = HEX_PATTERN.matcher(value);
        if (hex.find()) {
            int rgb = Integer.parseInt(hex.group(1), 16) & 0xFFFFFF;
            return OptionalInt.of(rgb);
        }
        Matcher rgbMatcher = RGB_PATTERN.matcher(value);
        if (rgbMatcher.matches()) {
            int r = clamp(Integer.parseInt(rgbMatcher.group(1)));
            int g = clamp(Integer.parseInt(rgbMatcher.group(2)));
            int b = clamp(Integer.parseInt(rgbMatcher.group(3)));
            return OptionalInt.of((r << 16) | (g << 8) | b);
        }
        return OptionalInt.empty();
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
