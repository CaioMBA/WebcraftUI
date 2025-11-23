package net.ofatech.webcraftui.html.parser;

import net.ofatech.webcraftui.html.model.HtmlDocument;
import net.ofatech.webcraftui.html.model.HtmlElement;
import net.ofatech.webcraftui.html.model.HtmlNode;
import net.ofatech.webcraftui.html.model.HtmlText;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A deliberately small HTML parser for controlled UI templates.
 */
public final class SimpleHtmlParser {
    private static final Pattern TAG_PATTERN = Pattern.compile("<(/?)([a-zA-Z0-9:-]+)([^>]*)>");
    private static final Pattern ATTR_PATTERN = Pattern.compile("([a-zA-Z0-9:-]+)(?:\\s*=\\s*\"([^\"]*)\")?");

    private SimpleHtmlParser() {
    }

    public static HtmlDocument parse(String html) {
        return parse(html, name -> Optional.empty());
    }

    public static HtmlDocument parse(String html, Function<String, Optional<String>> componentLoader) {
        HtmlElement root = new HtmlElement("root");
        Deque<HtmlElement> stack = new ArrayDeque<>();
        stack.push(root);

        int index = 0;
        Matcher matcher = TAG_PATTERN.matcher(html);
        while (matcher.find()) {
            int start = matcher.start();
            if (start > index) {
                String text = html.substring(index, start);
                if (!text.isBlank()) {
                    current(stack).addChild(new HtmlText(text));
                }
            }

            boolean closing = !matcher.group(1).isEmpty();
            String tag = matcher.group(2).toLowerCase();
            String rawAttrs = matcher.group(3);
            boolean selfClosing = rawAttrs.endsWith("/");
            if (selfClosing) {
                rawAttrs = rawAttrs.substring(0, rawAttrs.length() - 1);
            }

            if (closing) {
                popUntilTag(stack, tag);
            } else if (tag.equals("component")) {
                Map<String, String> attributes = AttributeParser.parse(rawAttrs);
                String src = attributes.getOrDefault("src", "");
                componentLoader.apply(src).ifPresent(content -> {
                    HtmlDocument childDoc = parse(content, componentLoader);
                    childDoc.root().children().forEach(node -> current(stack).addChild(node));
                });
            } else {
                HtmlElement element = new HtmlElement(tag);
                AttributeParser.parse(rawAttrs).forEach(element::addAttribute);
                current(stack).addChild(element);
                if (!selfClosing && !isVoidElement(tag)) {
                    stack.push(element);
                }
            }
            index = matcher.end();
        }

        if (index < html.length()) {
            String text = html.substring(index);
            if (!text.isBlank()) {
                current(stack).addChild(new HtmlText(text));
            }
        }
        return new HtmlDocument(root);
    }

    private static HtmlElement current(Deque<HtmlElement> stack) {
        return stack.peek();
    }

    private static void popUntilTag(Deque<HtmlElement> stack, String tag) {
        while (stack.size() > 1) {
            HtmlElement element = stack.pop();
            if (element.tagName().equals(tag)) {
                return;
            }
        }
    }

    private static boolean isVoidElement(String tag) {
        return switch (tag) {
            case "br", "img", "input", "meta", "link" -> true;
            default -> false;
        };
    }

    private static final class AttributeParser {
        private static Map<String, String> parse(String raw) {
            java.util.HashMap<String, String> map = new java.util.HashMap<>();
            Matcher matcher = ATTR_PATTERN.matcher(raw);
            while (matcher.find()) {
                String name = matcher.group(1);
                String value = matcher.group(2) == null ? "" : matcher.group(2);
                map.put(name.toLowerCase(), value);
            }
            return map;
        }
    }
}
