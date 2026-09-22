package com.yourwebsitespace.solehack.modules.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nonnull;

public class BedAuraConfigScreen extends GuiScreen {

    private final GuiScreen parent;
    private final BedAura module;

    // Element IDs
    private final int BUTTON_DONE = 999;
    private final int SLIDER_RANGE = 0;
    private final int SLIDER_EXPLODE_DELAY = 1;
    private final int TOGGLE_ROTATIONS = 2;

    public BedAuraConfigScreen(GuiScreen parent, BedAura module) {
        this.parent = parent;
        this.module = module;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        int centerX = this.width / 2;
        int startY = 55;

        // --- Column 1: Targeting Options (Left Side) ---
        int leftColX = centerX - 155;
        this.buttonList.add(new GuiSlider(SLIDER_RANGE, leftColX, startY, "Reach Range: ", 1.0f, 6.0f, (float) module.range));
        this.buttonList.add(new GuiButton(TOGGLE_ROTATIONS, leftColX, startY + 26, 150, 20, "Strict Rotations: " + (module.strictRotations ? "ON" : "OFF")));

        // --- Column 2: Performance Options (Right Side) ---
        int rightColX = centerX + 5;
        this.buttonList.add(new GuiSlider(SLIDER_EXPLODE_DELAY, rightColX, startY, "Explode Delay (ms): ", 0.0f, 500.0f, (float) module.explodedelayMs));

        // Main Navigation Close Button
        this.buttonList.add(new GuiButton(BUTTON_DONE, centerX - 100, this.height - 35, 200, 20, "Apply Settings"));
    }

    @Override
    protected void actionPerformed(@Nonnull GuiButton button) {
        if (button.id == BUTTON_DONE) {
            this.mc.displayGuiScreen(parent);
            return;
        }

        if (button.id == TOGGLE_ROTATIONS) {
            module.strictRotations = !module.strictRotations;
            button.displayString = "Strict Rotations: " + (module.strictRotations ? "ON" : "OFF");
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws java.io.IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        updateModuleValues();
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        updateModuleValues();
    }

    private void updateModuleValues() {
        for (GuiButton button : this.buttonList) {
            if (button instanceof GuiSlider) {
                GuiSlider slider = (GuiSlider) button;
                if (slider.id == SLIDER_RANGE) module.range = slider.getValue();
                if (slider.id == SLIDER_EXPLODE_DELAY) module.explodedelayMs = (long) slider.getValue();
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        int centerX = this.width / 2;

        // Main Title Header
        this.drawCenteredString(this.fontRenderer, "SoleHack - BedAura Configuration Panel", centerX, 15, 0xFF5555);

        // Column Category Labels
        this.drawString(this.fontRenderer, "Targeting Matrix", centerX - 155, 42, 0xAAAAAA);
        this.drawString(this.fontRenderer, "Speed Calibration", centerX + 5, 42, 0xAAAAAA);

        // Live Performance Summary Box Overlay
        int summaryY = 115;
        GuiScreen.drawRect(centerX - 155, summaryY, centerX + 155, summaryY + 45, 0x44000000);
        this.drawString(this.fontRenderer, "Active Explosive Profile Summary:", centerX - 145, summaryY + 6, 0xFFFF55);
        this.drawString(this.fontRenderer, "Target Engagement Limit: " + String.format("%.2f blocks", module.range), centerX - 145, summaryY + 20, 0xFFFFFF);
        this.drawString(this.fontRenderer, "Sequence Fire Rate Cooldown: " + module.explodedelayMs + " ms", centerX - 145, summaryY + 32, 0xFFFFFF);
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
            super(id, x, y, 150, 20, "");
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

        // FIXED: Added @Nonnull annotation tags to perfectly pass strict project lint profiles
        @Override
        protected void mouseDragged(@Nonnull Minecraft mcIn, int mouseX, int mouseY) {
            if (this.visible) {
                if (this.dragging) {
                    this.sliderValue = (float) (mouseX - (this.x + 4)) / (float) (this.width - 8);
                    this.sliderValue = MathHelper.clamp(this.sliderValue, 0.0f, 1.0f);
                    this.displayString = prefix + String.format("%.1f", getActualValue());
                }

                mcIn.getTextureManager().bindTexture(BUTTON_TEXTURES);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.drawTexturedModalRect(this.x + (int) (this.sliderValue * (this.width - 8)), this.y, 0, 66, 4, 20);
                this.drawTexturedModalRect(this.x + (int) (this.sliderValue * (this.width - 8)) + 4, this.y, 196, 66, 4, 20);
            }
        }

        // FIXED: Added @Nonnull annotation tags to perfectly pass strict project lint profiles
        @Override
        public boolean mousePressed(@Nonnull Minecraft mcIn, int mouseX, int mouseY) {
            if (super.mousePressed(mcIn, mouseX, mouseY)) {
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
