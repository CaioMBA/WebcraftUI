package net.ofatech.webcraftui.ui.render;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.ofatech.webcraftui.css.CssProperties;
import net.ofatech.webcraftui.css.ResolvedStyle;
import net.ofatech.webcraftui.ui.model.UiButton;
import net.ofatech.webcraftui.ui.model.UiContainer;
import net.ofatech.webcraftui.ui.model.UiImage;
import net.ofatech.webcraftui.ui.model.UiNode;
import net.ofatech.webcraftui.ui.model.UiRoot;
import net.ofatech.webcraftui.ui.model.UiSlot;
import net.ofatech.webcraftui.ui.model.UiText;
import net.ofatech.webcraftui.ui.transform.UiActionRegistry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UiRenderer {
    private static final Pattern COLOR_PATTERN = Pattern.compile("#?([0-9a-fA-F]{3,4}|[0-9a-fA-F]{6,8})");

    private UiRenderer() {
    }

    public static RenderResult render(Font font, UiRoot root, ResourceLocation uiId, int startX, int startY, int width,
                                       BiConsumer<String, Map<String, String>> networkDispatcher) {
        RenderContext context = new RenderContext(font, uiId, networkDispatcher);
        int cursorY = startY;
        for (UiNode child : root.children()) {
            cursorY += context.renderNode(child, startX, cursorY, width);
        }
        return new RenderResult(context.textBlocks, context.buttons, context.actions, context.decorations, context.images,
                context.slots);
    }

    public record TextBlock(Component text, int x, int y, int width, int height, int color, OptionalInt backgroundColor,
                            boolean bold, boolean italic, int fontSize) {
    }

    public record Decoration(int x, int y, int width, int height, OptionalInt backgroundColor, Optional<Border> border,
                             Optional<ResourceLocation> texture) {
    }

    public record Border(int color, int thickness, int radius) {
    }

    public record ImageRender(ResourceLocation texture, int x, int y, int width, int height) {
    }

    public record SlotRender(int slotIndex, int x, int y, int size) {
    }

    public record RenderResult(List<TextBlock> textBlocks, List<Button> buttons, Set<String> actions,
                               List<Decoration> decorations, List<ImageRender> images, List<SlotRender> slots) {
    }

    private static final class RenderContext {
        private final Font font;
        private final ResourceLocation uiId;
        private final List<TextBlock> textBlocks = new ArrayList<>();
        private final List<Button> buttons = new ArrayList<>();
        private final Set<String> actions = new HashSet<>();
        private final List<Decoration> decorations = new ArrayList<>();
        private final List<ImageRender> images = new ArrayList<>();
        private final List<SlotRender> slots = new ArrayList<>();
        private final BiConsumer<String, Map<String, String>> networkDispatcher;

        RenderContext(Font font, ResourceLocation uiId, BiConsumer<String, Map<String, String>> networkDispatcher) {
            this.font = font;
            this.uiId = uiId;
            this.networkDispatcher = networkDispatcher;
        }

        int renderNode(UiNode node, int x, int y, int availableWidth) {
            if (node instanceof UiText text) {
                return renderText(text, x, y, availableWidth);
            } else if (node instanceof UiButton button) {
                return renderButton(button, x, y, availableWidth);
            } else if (node instanceof UiContainer container) {
                return renderContainer(container, x, y, availableWidth);
            } else if (node instanceof UiImage image) {
                return renderImage(image, x, y, availableWidth);
            } else if (node instanceof UiSlot slot) {
                return renderSlot(slot, x, y, availableWidth);
            }
            return 0;
        }

        private int renderContainer(UiContainer container, int x, int y, int availableWidth) {
            int[] padding = parseBox(container.style(), CssProperties.PADDING, 0);
            int[] margin = parseBox(container.style(), CssProperties.MARGIN, 0);
            int offsetX = positionOffsetX(container.style());
            int offsetY = positionOffsetY(container.style());

            int posX = x + margin[3] + offsetX;
            int posY = y + margin[0] + offsetY;
            int width = container.style().getInt(CssProperties.WIDTH, Math.max(0, availableWidth - margin[1] - margin[3]));
            int contentWidth = Math.max(0, width - padding[1] - padding[3]);

            int cursor = posY + padding[0];
            for (UiNode child : container.children()) {
                cursor += renderNode(child, posX + padding[3], cursor, contentWidth);
            }
            int computedHeight = cursor - posY + padding[2];
            computedHeight = Math.max(computedHeight, container.style().getInt(CssProperties.HEIGHT, computedHeight));

            addDecoration(posX, posY, width, computedHeight, container.style());
            return margin[0] + offsetY + computedHeight + margin[2];
        }

        private int renderButton(UiButton button, int x, int y, int availableWidth) {
            int[] margin = parseBox(button.style(), CssProperties.MARGIN, 2);
            int offsetX = positionOffsetX(button.style());
            int offsetY = positionOffsetY(button.style());

            int posX = x + margin[3] + offsetX;
            int posY = y + margin[0] + offsetY;
            int btnWidth = Math.min(availableWidth, Math.max(80, font.width(button.label()) + 20));
            btnWidth = button.style().getInt(CssProperties.WIDTH, btnWidth);
            int btnHeight = button.style().getInt(CssProperties.HEIGHT, 20);

            Button vanilla = Button.builder(Component.literal(button.label()), b -> {
                        UiActionRegistry.dispatch(uiId, button.actionId(), button.arguments());
                        networkDispatcher.accept(button.actionId(), button.arguments());
                    })
                    .pos(posX, posY)
                    .size(btnWidth, btnHeight)
                    .build();

            buttons.add(vanilla);
            actions.add(button.actionId());
            return margin[0] + offsetY + btnHeight + margin[2];
        }

        private int renderText(UiText text, int x, int y, int availableWidth) {
            int[] margin = parseBox(text.style(), CssProperties.MARGIN, 0);
            int[] padding = parseBox(text.style(), CssProperties.PADDING, 0);
            int offsetX = positionOffsetX(text.style());
            int offsetY = positionOffsetY(text.style());

            int posX = x + margin[3] + offsetX;
            int posY = y + margin[0] + offsetY;
            int fontSize = text.style().getInt(CssProperties.FONT_SIZE, font.lineHeight);
            int width = text.style().getInt(CssProperties.WIDTH,
                    Math.max(0, availableWidth - margin[1] - margin[3] - padding[1] - padding[3]));
            int color = parseColor(text.style().getOrDefault(CssProperties.COLOR, "#ffffff"));
            OptionalInt background = parseOptionalColor(text.style().getOrDefault(CssProperties.BACKGROUND, ""));

            boolean bold = isBold(text.style());
            boolean italic = isItalic(text.style());
            int height = Math.max(fontSize, font.wordWrapHeight(text.text(), width));
            Component rendered = Component.literal(text.text()).withStyle(style -> style.withBold(bold).withItalic(italic));

            textBlocks.add(new TextBlock(rendered, posX + padding[3], posY + padding[0], width, height, color, background,
                    bold, italic, fontSize));
            return margin[0] + offsetY + padding[0] + height + padding[2] + margin[2];
        }

        private int renderImage(UiImage image, int x, int y, int availableWidth) {
            int[] margin = parseBox(image.style(), CssProperties.MARGIN, 0);
            int offsetX = positionOffsetX(image.style());
            int offsetY = positionOffsetY(image.style());

            int posX = x + margin[3] + offsetX;
            int posY = y + margin[0] + offsetY;
            int width = image.style().getInt(CssProperties.WIDTH, Math.max(16, availableWidth - margin[1] - margin[3]));
            int height = image.style().getInt(CssProperties.HEIGHT, 16);
            image.source().ifPresent(texture -> images.add(new ImageRender(texture, posX, posY, width, height)));

            addDecoration(posX, posY, width, height, image.style());
            return margin[0] + offsetY + height + margin[2];
        }

        private int renderSlot(UiSlot slot, int x, int y, int availableWidth) {
            int[] margin = parseBox(slot.style(), CssProperties.MARGIN, 0);
            int offsetX = positionOffsetX(slot.style());
            int offsetY = positionOffsetY(slot.style());

            int posX = x + margin[3] + offsetX;
            int posY = y + margin[0] + offsetY;
            int defaultSize = Math.max(18, availableWidth - margin[1] - margin[3]);
            int size = slot.style().getInt(CssProperties.WIDTH,
                    slot.style().getInt(CssProperties.HEIGHT, defaultSize));
            slots.add(new SlotRender(slot.slotIndex(), posX, posY, size));
            addDecoration(posX, posY, size, size, slot.style());
            return margin[0] + offsetY + size + margin[2];
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

        private boolean isBold(ResolvedStyle style) {
            String weight = style.getOrDefault(CssProperties.FONT_WEIGHT, "");
            return weight.equalsIgnoreCase("bold") || weight.equals("700");
        }

        private boolean isItalic(ResolvedStyle style) {
            String fontStyle = style.getOrDefault(CssProperties.FONT_STYLE, "");
            return fontStyle.equalsIgnoreCase("italic");
        }

        private int positionOffsetX(ResolvedStyle style) {
            return style.getInt(CssProperties.X, style.getInt(CssProperties.LEFT, 0));
        }

        private int positionOffsetY(ResolvedStyle style) {
            return style.getInt(CssProperties.Y, style.getInt(CssProperties.TOP, 0));
        }

        private int parseColor(String color) {
            return parseOptionalColor(color).orElse(0xFFFFFF);
        }

        private OptionalInt parseOptionalColor(String color) {
            Matcher matcher = COLOR_PATTERN.matcher(color.trim());
            if (matcher.find()) {
                String hex = matcher.group(1);
                if (hex.length() <= 4) {
                    String expanded = hex.chars()
                            .mapToObj(ch -> String.valueOf((char) ch))
                            .map(ch -> ch + ch)
                            .reduce(String::concat)
                            .orElse(hex);
                    hex = expanded;
                }
                return OptionalInt.of((int) Long.parseLong(hex, 16));
            }
            return OptionalInt.empty();
        }

        private Optional<ResourceLocation> parseTexture(ResolvedStyle style) {
            String rawTexture = style.getOrDefault(CssProperties.BACKGROUND_IMAGE, "").trim();
            if (rawTexture.isBlank()) {
                return Optional.empty();
            }
            String cleaned = rawTexture;
            if (cleaned.startsWith("url(")) {
                cleaned = cleaned.substring(4, cleaned.length() - 1);
            }
            return Optional.ofNullable(ResourceLocation.tryParse(cleaned.trim()));
        }

        private Optional<Border> parseBorder(ResolvedStyle style) {
            int thickness = style.getInt(CssProperties.BORDER_WIDTH, 0);
            if (thickness <= 0) {
                return Optional.empty();
            }
            int color = parseColor(style.getOrDefault(CssProperties.BORDER_COLOR, "#FFFFFF"));
            int radius = style.getInt(CssProperties.BORDER_RADIUS, 0);
            return Optional.of(new Border(color, thickness, radius));
        }

        private void addDecoration(int x, int y, int width, int height, ResolvedStyle style) {
            OptionalInt background = parseOptionalColor(style.getOrDefault(CssProperties.BACKGROUND, ""));
            Optional<Border> border = parseBorder(style);
            Optional<ResourceLocation> texture = parseTexture(style);
            if (background.isEmpty() && border.isEmpty() && texture.isEmpty()) {
                return;
            }
            decorations.add(new Decoration(x, y, width, height, background, border, texture));
        }
    }
}
