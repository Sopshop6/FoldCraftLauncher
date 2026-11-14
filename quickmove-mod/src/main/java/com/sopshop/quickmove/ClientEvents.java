package com.sopshop.quickmove;

import com.sopshop.quickmove.mixin.ACSAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 双击物品 = shift 点击（整堆快速转移，容器/背包双向生效）。
 * 选中物品时，在物品栏上方显示名字，停留 2 秒。
 */
@Mod.EventBusSubscriber(modid = QuickMoveMod.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {
    private static final long DOUBLE_CLICK_MS = 300L;
    private static final long LABEL_MS = 2000L;

    private static long lastClickAt = 0L;
    private static int lastSlotIndex = -1;

    private static String label = "";
    private static long labelUntil = 0L;

    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getButton() != 0) {
            return;
        }
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            return;
        }

        Slot slot = ((ACSAccessor) screen).quickmove$getHoveredSlot();
        if (slot == null || !slot.hasItem()) {
            lastSlotIndex = -1;
            lastClickAt = 0L;
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null) {
            return;
        }

        ItemStack stack = slot.getItem();
        showLabel(stack.getHoverName().getString());

        long now = System.currentTimeMillis();
        boolean doubleClick = (now - lastClickAt) <= DOUBLE_CLICK_MS && lastSlotIndex == slot.index;

        if (doubleClick) {
            if (!mc.player.containerMenu.getCarried().isEmpty()) {
                mc.gameMode.handleInventoryMouseClick(
                        screen.getMenu().containerId, slot.index, 0, ClickType.PICKUP, mc.player);
            }
            mc.gameMode.handleInventoryMouseClick(
                    screen.getMenu().containerId, slot.index, 0, ClickType.QUICK_MOVE, mc.player);
            event.setCanceled(true);
            lastClickAt = 0L;
            lastSlotIndex = -1;
        } else {
            lastClickAt = now;
            lastSlotIndex = slot.index;
        }
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        var screen = event.getScreen();
        drawLabel(event.getGuiGraphics(), screen.width, screen.height);
    }

    @SubscribeEvent
    public static void onHudRender(RenderGuiEvent.Post event) {
        GuiGraphics graphics = event.getGuiGraphics();
        drawLabel(graphics, graphics.guiWidth(), graphics.guiHeight());
    }

    private static void showLabel(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        label = text;
        labelUntil = System.currentTimeMillis() + LABEL_MS;
    }

    private static void drawLabel(GuiGraphics graphics, int width, int height) {
        if (System.currentTimeMillis() > labelUntil || label.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.font == null) {
            return;
        }
        int textWidth = mc.font.width(label);
        int x = (width - textWidth) / 2;
        int y = height - 46;
        graphics.fill(x - 6, y - 4, x + textWidth + 6, y + 10, 0x99000000);
        graphics.drawString(mc.font, label, x, y, 0xFFFFE0C0, true);
    }
}
