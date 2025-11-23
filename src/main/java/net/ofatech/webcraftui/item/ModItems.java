package net.ofatech.webcraftui.item;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ofatech.webcraftui.WebcraftUI;
import net.ofatech.webcraftui.item.UiEditorItem;

public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(WebcraftUI.MOD_ID);

    public static final DeferredItem<Item> UI_EDITOR = ITEMS.register(
            "ui_editor",
            () -> new UiEditorItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
