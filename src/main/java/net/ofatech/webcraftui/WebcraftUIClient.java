package net.ofatech.webcraftui;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.ofatech.webcraftui.client.HudOverlayManager;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = WebcraftUI.MOD_ID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = WebcraftUI.MOD_ID, value = Dist.CLIENT)
public class WebcraftUIClient {
    public WebcraftUIClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
    }

    @SubscribeEvent
    static void renderHud(RenderGuiEvent.Pre event) {
        HudOverlayManager.render(event.getGuiGraphics());
    }
}
