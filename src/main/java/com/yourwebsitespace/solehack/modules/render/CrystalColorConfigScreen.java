package com.yourwebsitespace.solehack.modules.render;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiSlider;

public class CrystalColorConfigScreen extends GuiScreen {

    private final GuiScreen parent;
    private final CrystalColor crystalColor;

    private static final int ID_RAINBOW_TOGGLE = 1;
    private static final int ID_RED = 2;
    private static final int ID_GREEN = 3;
    private static final int ID_BLUE = 4;
    private static final int ID_RAINBOW_SPEED = 5;
    private static final int ID_DONE = 999;

    private GuiButton rainbowToggleButton;

    public CrystalColorConfigScreen(GuiScreen parent, CrystalColor crystalColor) {
        this.parent = parent;
        this.crystalColor = crystalColor;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();

        int centerX = this.width / 2;
        int width = 200;
        int startY = 45;
        int spacing = 26;

        rainbowToggleButton = new GuiButton(ID_RAINBOW_TOGGLE, centerX - width / 2, startY, width, 20,
                "Mode: " + (crystalColor.isRainbow() ? "Rainbow" : "Solid Color"));
        this.buttonList.add(rainbowToggleButton);

        int color = crystalColor.getSolidColor();
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        this.buttonList.add(new GuiSlider(ID_RED, centerX - width / 2, startY + spacing, width, 20,
                "Red: ", "", 0, 255, red, false, true));

        this.buttonList.add(new GuiSlider(ID_GREEN, centerX - width / 2, startY + spacing * 2, width, 20,
                "Green: ", "", 0, 255, green, false, true));

        this.buttonList.add(new GuiSlider(ID_BLUE, centerX - width / 2, startY + spacing * 3, width, 20,
                "Blue: ", "", 0, 255, blue, false, true));

        this.buttonList.add(new GuiSlider(ID_RAINBOW_SPEED, centerX - width / 2, startY + spacing * 4, width, 20,
                "Rainbow Speed (s): ", "", 0.5, 15.0, crystalColor.getRainbowSpeedSeconds(), false, true));

        this.buttonList.add(new GuiButton(ID_DONE, centerX - 100, this.height - 30, 200, 20, "Done"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == ID_DONE) {
            this.mc.displayGuiScreen(parent);
            return;
        }
        if (button.id == ID_RAINBOW_TOGGLE) {
            crystalColor.setRainbow(!crystalColor.isRainbow());
            rainbowToggleButton.displayString = "Mode: " + (crystalColor.isRainbow() ? "Rainbow" : "Solid Color");
            return;
        }

        if (button instanceof GuiSlider) {
            GuiSlider slider = (GuiSlider) button;

            switch (button.id) {
                case ID_RED:
                case ID_GREEN:
                case ID_BLUE:
                    applyRgbSliders();
                    break;
                case ID_RAINBOW_SPEED:
                    crystalColor.setRainbowSpeedSeconds((float) slider.getValue());
                    break;
            }
        }
    }

    private void applyRgbSliders() {
        int red = 0, green = 0, blue = 0;
        for (Object obj : this.buttonList) {
            if (!(obj instanceof GuiSlider)) continue;
            GuiSlider slider = (GuiSlider) obj;
            if (slider.id == ID_RED) red = (int) slider.getValue();
            if (slider.id == ID_GREEN) green = (int) slider.getValue();
            if (slider.id == ID_BLUE) blue = (int) slider.getValue();
        }
        int color = (red << 16) | (green << 8) | blue;
        crystalColor.setSolidColor(color);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRenderer, "CrystalColor Config", this.width / 2, 15, 0xFFFFFF);

        int color = crystalColor.getSolidColor();
        this.drawCenteredString(this.fontRenderer, "Preview:", this.width / 2, this.height - 55, 0xFFFFFF);
        drawRect(this.width / 2 - 15, this.height - 50, this.width / 2 + 15, this.height - 40, 0xFF000000 | color);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}