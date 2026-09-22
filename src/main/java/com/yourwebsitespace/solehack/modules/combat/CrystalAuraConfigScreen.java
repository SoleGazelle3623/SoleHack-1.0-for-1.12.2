package com.yourwebsitespace.solehack.modules.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nonnull;
import java.io.IOException;

public class CrystalAuraConfigScreen extends GuiScreen {

    private final GuiScreen parent;

    // Element IDs
    private static final int BUTTON_DONE = 999;
    private static final int SLIDER_RANGE = 0;
    private static final int SLIDER_WALLS_RANGE = 1;
    private static final int TOGGLE_ROTATIONS = 2;
    private static final int TOGGLE_PROTOCOL13 = 3;
    private static final int TOGGLE_SILENT = 4;

    public CrystalAuraConfigScreen(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        int centerX = this.width / 2;
        int startY = 40;

        CrystalAura module = CrystalAura.INSTANCE;
        if (module == null) return;

        // Custom Sliders for Ranges
        this.buttonList.add(new GuiSlider(SLIDER_RANGE, centerX - 100, startY, "Range: ", 1.0f, 6.0f, (float) module.range));
        this.buttonList.add(new GuiSlider(SLIDER_WALLS_RANGE, centerX - 100, startY + 24, "Walls Range: ", 1.0f, 6.0f, (float) module.wallsRange));

        // Toggle Buttons
        this.buttonList.add(new GuiButton(TOGGLE_ROTATIONS, centerX - 100, startY + 58, 200, 20, "Strict Rotations: " + (module.strictRotations ? "ON" : "OFF")));
        this.buttonList.add(new GuiButton(TOGGLE_PROTOCOL13, centerX - 100, startY + 82, 200, 20, "1.13 Placement (protocol13): " + (module.protocol13 ? "ON" : "OFF")));
        this.buttonList.add(new GuiButton(TOGGLE_SILENT, centerX - 100, startY + 106, 200, 20, "Silent Switch: " + (module.silentSwitch ? "ON" : "OFF")));

        // Close Button
        this.buttonList.add(new GuiButton(BUTTON_DONE, centerX - 100, this.height - 35, 200, 20, "Done"));
    }

    @Override
    protected void actionPerformed(@Nonnull GuiButton button) throws IOException {
        if (button.id == BUTTON_DONE) {
            this.mc.displayGuiScreen(parent);
            return;
        }

        CrystalAura module = CrystalAura.INSTANCE;
        if (module == null) return;

        if (button.id == TOGGLE_ROTATIONS) {
            module.strictRotations = !module.strictRotations;
            button.displayString = "Strict Rotations: " + (module.strictRotations ? "ON" : "OFF");
        }
        if (button.id == TOGGLE_PROTOCOL13) {
            module.protocol13 = !module.protocol13;
            button.displayString = "1.13 Placement (protocol13): " + (module.protocol13 ? "ON" : "OFF");
        }
        if (button.id == TOGGLE_SILENT) {
            module.silentSwitch = !module.silentSwitch;
            button.displayString = "Silent Switch: " + (module.silentSwitch ? "ON" : "OFF");
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        updateModuleValues();
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        updateModuleValues();
    }

    private void updateModuleValues() {
        CrystalAura module = CrystalAura.INSTANCE;
        if (module == null) return;

        for (GuiButton button : this.buttonList) {
            if (button instanceof GuiSlider) {
                GuiSlider slider = (GuiSlider) button;
                if (slider.id == SLIDER_RANGE) module.range = slider.getValue();
                if (slider.id == SLIDER_WALLS_RANGE) module.wallsRange = slider.getValue();
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        String title = (CrystalAura.INSTANCE != null) ? CrystalAura.INSTANCE.getName() + " Configuration" : "CrystalAura Configuration";
        this.drawCenteredString(this.fontRenderer, title, this.width / 2, 15, 0xFFFFFF);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private static class GuiSlider extends GuiButton {
        private final String prefix;
        private final float min;
        private final float max;
        private float sliderValue;
        private boolean dragging = false;

        public GuiSlider(int id, int x, int y, String prefix, float min, float max, float current) {
            super(id, x, y, 200, 20, "");
            this.prefix = prefix;
            this.min = min;
            this.max = max;
            this.sliderValue = (current - min) / (max - min);
            this.displayString = prefix + String.format("%.1f", getActualValue());
        }

        public float getValue() {
            return getActualValue();
        }

        private float getActualValue() {
            return min + (sliderValue * (max - min));
        }

        @Override
        protected int getHoverState(boolean mouseOver) {
            return 0;
        }

        @Override
        protected void mouseDragged(@Nonnull Minecraft mcInstance, int mouseX, int mouseY) {
            if (this.visible) {
                if (this.dragging) {
                    this.sliderValue = (float) (mouseX - (this.x + 4)) / (float) (this.width - 8);
                    this.sliderValue = MathHelper.clamp(this.sliderValue, 0.0f, 1.0f);
                    this.displayString = prefix + String.format("%.1f", getActualValue());
                }

                mcInstance.getTextureManager().bindTexture(BUTTON_TEXTURES);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.drawTexturedModalRect(this.x + (int) (this.sliderValue * (this.width - 8)), this.y, 0, 66, 4, 20);
                this.drawTexturedModalRect(this.x + (int) (this.sliderValue * (this.width - 8)) + 4, this.y, 196, 66, 4, 20);
            }
        }

        @Override
        public boolean mousePressed(@Nonnull Minecraft mcInstance, int mouseX, int mouseY) {
            if (super.mousePressed(mcInstance, mouseX, mouseY)) {
                this.sliderValue = (float) (mouseX - (this.x + 4)) / (float) (this.width - 8);
                this.sliderValue = MathHelper.clamp(this.sliderValue, 0.0f, 1.0f);
                this.displayString = prefix + String.format("%.1f", getActualValue());
                this.dragging = true;
                return true;
            }
            return false;
        }

        @Override
        public void mouseReleased(int mouseX, int mouseY) {
            this.dragging = false;
        }
    }
}