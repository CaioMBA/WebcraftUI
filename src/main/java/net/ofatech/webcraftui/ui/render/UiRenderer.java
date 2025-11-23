package net.ofatech.webcraftui.ui.render;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.ofatech.webcraftui.css.ResolvedStyle;
import net.ofatech.webcraftui.ui.model.UiButton;
import net.ofatech.webcraftui.ui.model.UiContainer;
import net.ofatech.webcraftui.ui.model.UiNode;
import net.ofatech.webcraftui.ui.model.UiRoot;
import net.ofatech.webcraftui.ui.model.UiText;
import net.ofatech.webcraftui.ui.transform.UiActionRegistry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UiRenderer {
    private static final Pattern COLOR_PATTERN = Pattern.compile("#?([0-9a-fA-F]{6})");

    private UiRenderer() {
    }

    public static RenderResult render(Font font, UiRoot root, ResourceLocation uiId, int startX, int startY, int width,
                                       BiConsumer<String, Map<String, String>> networkDispatcher) {
        RenderContext context = new RenderContext(font, uiId, startX, startY, width, networkDispatcher);
        for (UiNode child : root.children()) {
            context.render(child, startX, startY, width);
        }
        return new RenderResult(context.textBlocks, context.buttons, context.actions);
    }

    public record TextBlock(Component text, int x, int y, int width, int height, int color, OptionalInt backgroundColor) {
    }

    public record RenderResult(List<TextBlock> textBlocks, List<Button> buttons, Set<String> actions) {
    }

    private static final class RenderContext {
        private final Font font;
        private final ResourceLocation uiId;
        private final int startX;
        private final int width;
        private int cursorY;
        private final List<TextBlock> textBlocks = new ArrayList<>();
        private final List<Button> buttons = new ArrayList<>();
        private final Set<String> actions = new HashSet<>();
        private final BiConsumer<String, Map<String, String>> networkDispatcher;

        RenderContext(Font font, ResourceLocation uiId, int startX, int startY, int width,
                      BiConsumer<String, Map<String, String>> networkDispatcher) {
            this.font = font;
            this.uiId = uiId;
            this.startX = startX;
            this.cursorY = startY;
            this.width = width;
            this.networkDispatcher = networkDispatcher;
        }

        void render(UiNode node, int x, int y, int availableWidth) {
            if (node instanceof UiText text) {
                renderText(text, x, y, availableWidth);
            } else if (node instanceof UiButton button) {
                renderButton(button, x, y, availableWidth);
            } else if (node instanceof UiContainer container) {
                renderContainer(container, x, y, availableWidth);
            }
        }

        private void renderContainer(UiContainer container, int x, int y, int availableWidth) {
            int[] padding = parseBox(container.style(), "padding", 2);
            int[] margin = parseBox(container.style(), "margin", 2);
            int contentX = x + padding[3];
            int contentWidth = availableWidth - padding[1] - padding[3];
            cursorY = y + margin[0] + padding[0];
            for (UiNode child : container.children()) {
                render(child, contentX, cursorY, contentWidth);
            }
            cursorY += padding[2] + margin[2];
        }

        private void renderButton(UiButton button, int x, int y, int availableWidth) {
            int btnWidth = Math.min(availableWidth, Math.max(80, font.width(button.label()) + 20));
            btnWidth = button.style().getInt("width", btnWidth);
            int[] margin = parseBox(button.style(), "margin", 2);
            int posY = cursorY + margin[0];
            Button vanilla = Button.builder(Component.literal(button.label()), b -> {
                        UiActionRegistry.dispatch(uiId, button.actionId(), button.arguments());
                        networkDispatcher.accept(button.actionId(), button.arguments());
                    })
                    .pos(x + margin[3], posY)
                    .size(btnWidth, 20)
                    .build();
            buttons.add(vanilla);
            actions.add(button.actionId());
            cursorY = posY + vanilla.getHeight() + margin[2];
        }

        private void renderText(UiText text, int x, int y, int availableWidth) {
            int[] margin = parseBox(text.style(), "margin", 2);
            int[] padding = parseBox(text.style(), "padding", 2);
            int color = parseColor(text.style().getOrDefault("color", "#ffffff"));
            OptionalInt background = parseOptionalColor(text.style().getOrDefault("background", ""));
            int posY = cursorY + margin[0] + padding[0];
            int height = Math.max(font.lineHeight, font.wordWrapHeight(text.text(), availableWidth));
            textBlocks.add(new TextBlock(Component.literal(text.text()), x + margin[3] + padding[3], posY,
                    availableWidth - padding[1] - padding[3], height, color, background));
            cursorY = posY + height + margin[2] + padding[2];
        }

        private int[] parseBox(ResolvedStyle style, String key, int defaultVal) {
            String raw = style.getOrDefault(key, "");
            if (raw.isBlank()) {
                return new int[]{defaultVal, defaultVal, defaultVal, defaultVal};
            }
            String[] parts = raw.split(" ");
            int[] values = new int[]{defaultVal, defaultVal, defaultVal, defaultVal};
            for (int i = 0; i < parts.length && i < 4; i++) {
                try {
                    values[i] = Integer.parseInt(parts[i].replace("px", ""));
                } catch (NumberFormatException ignored) {
                }
            }
            if (parts.length == 1) {
                values[1] = values[0];
                values[2] = values[0];
                values[3] = values[0];
            }
            return values;
        }

        private int parseColor(String color) {
            return parseOptionalColor(color).orElse(0xFFFFFF);
        }

        private OptionalInt parseOptionalColor(String color) {
            Matcher matcher = COLOR_PATTERN.matcher(color.trim());
            if (matcher.find()) {
                return OptionalInt.of(Integer.parseInt(matcher.group(1), 16));
            }
            return OptionalInt.empty();
        }
    }
}
