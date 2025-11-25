package net.ofatech.webcraftui.client;

import net.minecraft.client.gui.Font;
import net.ofatech.webcraftui.api.UiRegistration;
import net.ofatech.webcraftui.css.CssParser;
import net.ofatech.webcraftui.css.CssStylesheet;
import net.minecraft.resources.ResourceLocation;
import net.ofatech.webcraftui.WebcraftUI;
import net.ofatech.webcraftui.html.model.HtmlAttributes;
import net.ofatech.webcraftui.html.model.HtmlDocument;
import net.ofatech.webcraftui.html.model.HtmlElement;
import net.ofatech.webcraftui.html.model.HtmlNode;
import net.ofatech.webcraftui.html.model.HtmlTag;
import net.ofatech.webcraftui.html.model.HtmlText;
import net.ofatech.webcraftui.html.parser.SimpleHtmlParser;
import net.ofatech.webcraftui.ui.model.UiRoot;
import net.ofatech.webcraftui.ui.render.UiRenderer;
import net.ofatech.webcraftui.ui.transform.HtmlToUiTransformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Builds the UI model from HTML/CSS and renders it into vanilla widgets.
 */
public final class HtmlRenderEngine {
    private HtmlRenderEngine() {
    }

    public static UiRenderer.RenderResult build(UiRegistration registration, Font font, String html, Optional<String> css,
                                                int x, int y, int width,
                                                BiConsumer<String, java.util.Map<String, String>> actionDispatcher) {
        return build(registration, font, html, css, x, y, width, actionDispatcher, false);
    }

    public static UiRenderer.RenderResult build(UiRegistration registration, Font font, String html, Optional<String> css,
                                                int x, int y, int width,
                                                BiConsumer<String, java.util.Map<String, String>> actionDispatcher,
                                                boolean overlay) {
        HtmlDocument document = SimpleHtmlParser.parse(html, path -> UiDocumentLoader.loadPath(resolvePath(path)));
        CssStylesheet stylesheet = loadStyles(document, css);
        UiRoot root = HtmlToUiTransformer.transform(document.root(), stylesheet, overlay);
        return UiRenderer.render(font, root, registration.uiId(), x, y, width, actionDispatcher);
    }

    private static String resolvePath(String raw) {
        if (raw.startsWith("/")) {
            return raw.substring(1);
        }
        return raw;
    }

    private static CssStylesheet loadStyles(HtmlDocument document, Optional<String> inlineCss) {
        CssStylesheet stylesheet = new CssStylesheet();
        List<String> cssContents = new ArrayList<>();
        cssContents.add(loadDefaultTheme());
        collectLinkedStyles(document.root(), cssContents);
        inlineCss.ifPresent(cssContents::add);
        collectInlineStyles(document.root(), cssContents);
        for (String content : cssContents) {
            stylesheet.merge(CssParser.parse(content));
        }
        return stylesheet;
    }

    private static void collectLinkedStyles(HtmlElement root, List<String> sink) {
        for (HtmlNode child : root.children()) {
            if (child instanceof HtmlElement element) {
                if (HtmlTag.from(element.tagName()) == HtmlTag.LINK
                        && element.attribute(HtmlAttributes.REL).orElse("").equalsIgnoreCase("stylesheet")) {
                    element.attribute(HtmlAttributes.HREF)
                            .flatMap(HtmlRenderEngine::loadStylesheetResource)
                            .ifPresent(sink::add);
                }
                collectLinkedStyles(element, sink);
            }
        }
    }

    private static void collectInlineStyles(HtmlElement root, List<String> sink) {
        for (HtmlNode child : root.children()) {
            if (child instanceof HtmlElement element) {
                if (HtmlTag.from(element.tagName()) == HtmlTag.STYLE) {
                    StringBuilder builder = new StringBuilder();
                    for (HtmlNode styleChild : element.children()) {
                        if (styleChild instanceof HtmlText text) {
                            builder.append(text.text());
                        }
                    }
                    if (!builder.isEmpty()) {
                        sink.add(builder.toString());
                    }
                } else {
                    collectInlineStyles(element, sink);
                }
            }
        }
    }

    private static String loadDefaultTheme() {
        Optional<String> css = UiDocumentLoader.loadPath("webcraftui:gui/style/default_minecraft_ui.css");
        return css.orElse("button{margin:4px;}body{color:#ffffff;}");
    }

    private static Optional<String> loadStylesheetResource(String href) {
        if (!isStylesheetHref(href)) {
            WebcraftUI.LOGGER.debug("Skipping non-stylesheet href {}", href);
            return Optional.empty();
        }
        ResourceLocation location = ResourceLocation.tryParse(href);
        if (location == null) {
            WebcraftUI.LOGGER.warn("Invalid stylesheet href {}", href);
            return Optional.empty();
        }
        return UiDocumentLoader.loadPath(location.toString());
    }

    private static boolean isStylesheetHref(String href) {
        if (href == null || href.isBlank()) {
            return false;
        }
        ResourceLocation location = ResourceLocation.tryParse(href);
        if (location == null) {
            return false;
        }
        return location.getPath().startsWith("gui/style/") && location.getPath().endsWith(".css");
    }
}
