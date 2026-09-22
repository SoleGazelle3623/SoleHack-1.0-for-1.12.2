package com.yourwebsitespace.solehack.modules.misc;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class ShulkerPreview extends Module {

    public ShulkerPreview() {
        super("ShulkerPreview", Category.MISC);
    }

    @Override
    protected void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected void onUpdate() {

    }

    @Override
    protected void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void onDrawForeground(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!(event.getGui() instanceof GuiContainer)) return;
        GuiContainer gui = (GuiContainer) event.getGui();

        Slot hovered = gui.getSlotUnderMouse();
        if (hovered == null || !hovered.getHasStack()) return;

        ItemStack stack = hovered.getStack();
        if (!isShulkerBox(stack)) return;

        List<ItemStack> contents = getShulkerContents(stack);
        if (contents.isEmpty()) return;

        drawPreview(gui, event.getMouseX(), event.getMouseY(), contents);
    }

    private boolean isShulkerBox(ItemStack stack) {
        if (stack.getItem().getRegistryName() == null) return false;
        return stack.getItem().getRegistryName().toString().contains("shulker_box");
    }

    private List<ItemStack> getShulkerContents(ItemStack stack) {
        List<ItemStack> items = new ArrayList<>();
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey("BlockEntityTag")) return items;

        NBTTagCompound blockEntity = tag.getCompoundTag("BlockEntityTag");
        if (!blockEntity.hasKey("Items")) return items;

        NBTTagList itemList = blockEntity.getTagList("Items", 10);
        for (int i = 0; i < itemList.tagCount(); i++) {
            ItemStack itemStack = new ItemStack(itemList.getCompoundTagAt(i));
            if (!itemStack.isEmpty()) {
                items.add(itemStack);
            }
        }
        return items;
    }

    private void drawPreview(GuiContainer gui, int mouseX, int mouseY, List<ItemStack> contents) {
        int cols = 9;
        int rows = (int) Math.ceil(contents.size() / (double) cols);
        int slotSize = 18;

        int width = cols * slotSize + 8;
        int height = rows * slotSize + 8;

        int x = mouseX + 12;
        int y = mouseY - height - 12;

        if (y < 0) y = mouseY + 12;
        if (x + width > gui.width) x = gui.width - width - 4;

        Minecraft mc = Minecraft.getMinecraft();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 300); // draw above tooltips

        Gui.drawRect(x, y, x + width, y + height, 0xF0100010);

        RenderHelper.enableGUIStandardItemLighting();
        for (int i = 0; i < contents.size(); i++) {
            int col = i % cols;
            int row = i / cols;
            int slotX = x + 4 + col * slotSize;
            int slotY = y + 4 + row * slotSize;

            mc.getRenderItem().renderItemAndEffectIntoGUI(contents.get(i), slotX, slotY);
            mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, contents.get(i), slotX, slotY, null);
        }
        RenderHelper.disableStandardItemLighting();

        GlStateManager.popMatrix();
    }
}