package net.ofatech.webcraftui.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.ofatech.webcraftui.api.UiRegistration;
import net.ofatech.webcraftui.api.WebcraftUIApi;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class UiCarouselScreen extends Screen {
    private final List<UiRegistration> registrations;
    private int index = 0;
    private Button openButton;

    public UiCarouselScreen(List<UiRegistration> registrations) {
        super(Component.literal("WebcraftUI Carousel"));
        this.registrations = registrations;
    }

    public static void open() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }

        List<UiRegistration> sorted = WebcraftUIApi.allRegistrations().stream()
                .sorted(Comparator.comparing(registration -> registration.uiId().toString()))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        minecraft.setScreen(new UiCarouselScreen(sorted));
    }

    @Override
    protected void init() {
        super.init();
        int buttonWidth = 60;
        int buttonHeight = 20;
        int centerX = this.width / 2;
        int bottomY = this.height - 40;

        addRenderableWidget(Button.builder(Component.literal("<"), btn -> shiftSelection(-1))
                .pos(centerX - buttonWidth - 70, bottomY)
                .size(buttonWidth, buttonHeight)
                .build());

        addRenderableWidget(Button.builder(Component.literal(">"), btn -> shiftSelection(1))
                .pos(centerX + 70, bottomY)
                .size(buttonWidth, buttonHeight)
                .build());

        openButton = addRenderableWidget(Button.builder(Component.literal("Open UI"), btn -> openSelected())
                .pos(centerX - 50, bottomY)
                .size(100, buttonHeight)
                .build());

        updateButtonState();
    }

    private void shiftSelection(int delta) {
        if (registrations.isEmpty()) {
            return;
        }
        index = (index + delta) % registrations.size();
        if (index < 0) {
            index += registrations.size();
        }
    }

    private void openSelected() {
        if (registrations.isEmpty()) {
            return;
        }

        UiRegistration registration = registrations.get(index);
        this.minecraft.setScreen(null);
        ClientUiManager.openLocal(registration.uiId());
    }

    private void updateButtonState() {
        if (openButton != null) {
            openButton.active = !registrations.isEmpty();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        Component header = Component.literal("Available WebcraftUI screens");
        graphics.drawString(this.font, header, (this.width - this.font.width(header)) / 2, 20, 0xFFFFFF);

        if (registrations.isEmpty()) {
            Component empty = Component.literal("No screens registered.");
            graphics.drawString(this.font, empty, (this.width - this.font.width(empty)) / 2, this.height / 2, 0xAAAAAA);
            return;
        }

        UiRegistration current = registrations.get(index);
        Component name = Component.literal(current.uiId().toString());
        Component counter = Component.literal((index + 1) + " / " + registrations.size());
        Component actionInfo = Component.literal("Actions: " + current.knownActions().size());

        int centerY = this.height / 2;
        graphics.drawString(this.font, name, (this.width - this.font.width(name)) / 2, centerY - 10, 0xFFFFFF);
        graphics.drawString(this.font, counter, (this.width - this.font.width(counter)) / 2, centerY + 10, 0xCCCCCC);
        graphics.drawString(this.font, actionInfo, (this.width - this.font.width(actionInfo)) / 2, centerY + 30, 0xCCCCCC);
    }
}
