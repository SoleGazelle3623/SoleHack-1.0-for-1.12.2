package com.yourwebsitespace.solehack.modules.render;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiSlider;

public class HandViewConfigScreen extends GuiScreen {

    private final GuiScreen parent;
    private final HandView handView;

    private static final int ID_SCALE = 1;
    private static final int ID_OFFSET_X = 2;
    private static final int ID_OFFSET_Y = 3;
    private static final int ID_OFFSET_Z = 4;
    private static final int ID_DONE = 999;

    public HandViewConfigScreen(GuiScreen parent, HandView handView) {
        this.parent = parent;
        this.handView = handView;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();

        int centerX = this.width / 2;
        int width = 200;
        int startY = 50;
        int spacing = 26;

        this.buttonList.add(new GuiSlider(ID_SCALE, centerX - width / 2, startY, width, 20,
                "Scale: ", "", 0.1, 2.0, handView.getScale(), false, true));

        this.buttonList.add(new GuiSlider(ID_OFFSET_X, centerX - width / 2, startY + spacing, width, 20,
                "Offset X: ", "", -1.0, 1.0, handView.getOffsetX(), false, true));

        this.buttonList.add(new GuiSlider(ID_OFFSET_Y, centerX - width / 2, startY + spacing * 2, width, 20,
                "Offset Y: ", "", -1.0, 1.0, handView.getOffsetY(), false, true));

        this.buttonList.add(new GuiSlider(ID_OFFSET_Z, centerX - width / 2, startY + spacing * 3, width, 20,
                "Offset Z: ", "", -1.0, 1.0, handView.getOffsetZ(), false, true));

        this.buttonList.add(new GuiButton(ID_DONE, centerX - 100, this.height - 30, 200, 20, "Done"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == ID_DONE) {
            this.mc.displayGuiScreen(parent);
            return;
        }

        if (button instanceof GuiSlider) {
            GuiSlider slider = (GuiSlider) button;
            float value = (float) slider.getValue();

            switch (button.id) {
                case ID_SCALE:
                    handView.setScale(value);
                    break;
                case ID_OFFSET_X:
                    handView.setOffsetX(value);
                    break;
                case ID_OFFSET_Y:
                    handView.setOffsetY(value);
                    break;
                case ID_OFFSET_Z:
                    handView.setOffsetZ(value);
                    break;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRenderer, "HandView Config", this.width / 2, 15, 0xFFFFFF);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}