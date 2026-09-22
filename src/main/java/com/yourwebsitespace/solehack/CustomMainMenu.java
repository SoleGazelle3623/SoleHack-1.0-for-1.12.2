package com.yourwebsitespace.solehack;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class CustomMainMenu extends GuiMainMenu {

    private static final ResourceLocation BACKGROUND_IMAGE =
            new ResourceLocation("solehack", "textures/gui/background.png");

    private static final ResourceLocation PARALLAX_IMAGE =
            new ResourceLocation("solehack", "textures/gui/parallax_logo.png");

    // IMPORTANT: set these to your background.png's actual real pixel
    // dimensions. This is required to compute correct "cover"/"contain"
    // scaling below - if these don't match the real file, the image will
    // appear stretched/squashed.
    private static final int BG_IMAGE_WIDTH = 1920;
    private static final int BG_IMAGE_HEIGHT = 1080;

    private static final float MAX_OFFSET = 12f;
    private static final float SMOOTHING = 0.08f;

    private float currentOffsetX = 0f;
    private float currentOffsetY = 0f;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Not calling super.drawScreen() - that draws vanilla's panorama.
        drawCustomBackground();

        for (GuiButton button : this.buttonList) {
            button.drawButton(this.mc, mouseX, mouseY, partialTicks);
        }

        updateParallax(mouseX, mouseY);
        drawParallaxImage();

        this.drawCenteredString(this.fontRenderer, "SoleHack Client", this.width / 2, 10, 0xFFFFFF);
    }

    private void drawCustomBackground() {
        GlStateManager.disableLighting();
        this.mc.getTextureManager().bindTexture(BACKGROUND_IMAGE);

        drawBlurredCoverLayer();
        drawSharpContainLayer();
    }

    // Fills the entire screen with a zoomed-in, cropped copy of the image
    // (scaled so its SHORT side matches the screen - "cover" fit, so no
    // empty bars are left anywhere), then fakes a blur by drawing it several
    // more times at small pixel offsets with low alpha. This softens hard
    // edges into something that reads as "blurred" without needing a real
    // GLSL shader, which isn't readily available in this Forge version.
    private void drawBlurredCoverLayer() {
        float coverScale = Math.max(
                this.width / (float) BG_IMAGE_WIDTH,
                this.height / (float) BG_IMAGE_HEIGHT
        );
        int coverW = (int) (BG_IMAGE_WIDTH * coverScale);
        int coverH = (int) (BG_IMAGE_HEIGHT * coverScale);
        int coverX = (this.width - coverW) / 2;
        int coverY = (this.height - coverH) / 2;

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        // Base layer at full opacity so there's no gap before the blur passes
        GlStateManager.color(1f, 1f, 1f, 1f);
        Gui.drawModalRectWithCustomSizedTexture(coverX, coverY, 0, 0, coverW, coverH, BG_IMAGE_WIDTH, BG_IMAGE_HEIGHT);

        // Several offset low-alpha passes around it to soften into a
        // pseudo-blur. More offsets / larger spread = blurrier but more
        // expensive; tune passOffsets and alpha to taste.
        int[][] passOffsets = {
                {-3, 0}, {3, 0}, {0, -3}, {0, 3},
                {-2, -2}, {2, -2}, {-2, 2}, {2, 2}
        };
        GlStateManager.color(1f, 1f, 1f, 0.12f);
        for (int[] offset : passOffsets) {
            Gui.drawModalRectWithCustomSizedTexture(
                    coverX + offset[0], coverY + offset[1], 0, 0, coverW, coverH, BG_IMAGE_WIDTH, BG_IMAGE_HEIGHT);
        }

        // Slight dark overlay so the blur reads as "background," not "main image"
        GlStateManager.color(1f, 1f, 1f, 1f);
        Gui.drawRect(0, 0, this.width, this.height, 0x66000000);

        GlStateManager.disableBlend();
    }

    // Draws the image at its correct, undistorted aspect ratio, scaled to
    // fit fully within the screen ("contain" fit - no cropping). This sits
    // on top of the blurred cover layer, so any letterbox space is filled
    // by the blur instead of being empty.
    private void drawSharpContainLayer() {
        float containScale = Math.min(
                this.width / (float) BG_IMAGE_WIDTH,
                this.height / (float) BG_IMAGE_HEIGHT
        );
        int containW = (int) (BG_IMAGE_WIDTH * containScale);
        int containH = (int) (BG_IMAGE_HEIGHT * containScale);
        int containX = (this.width - containW) / 2;
        int containY = (this.height - containH) / 2;

        GlStateManager.enableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);
        this.mc.getTextureManager().bindTexture(BACKGROUND_IMAGE);
        Gui.drawModalRectWithCustomSizedTexture(containX, containY, 0, 0, containW, containH, BG_IMAGE_WIDTH, BG_IMAGE_HEIGHT);
        GlStateManager.disableBlend();
    }

    private void updateParallax(int mouseX, int mouseY) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        float normalizedX = MathHelper.clamp((mouseX - centerX) / (float) centerX, -1f, 1f);
        float normalizedY = MathHelper.clamp((mouseY - centerY) / (float) centerY, -1f, 1f);

        float targetOffsetX = normalizedX * MAX_OFFSET;
        float targetOffsetY = normalizedY * MAX_OFFSET;

        currentOffsetX += (targetOffsetX - currentOffsetX) * SMOOTHING;
        currentOffsetY += (targetOffsetY - currentOffsetY) * SMOOTHING;
    }

    private void drawParallaxImage() {
        int imageWidth = 64;
        int imageHeight = 64;

        int baseX = this.width / 2 - imageWidth / 2;
        int baseY = 40;

        int drawX = (int) (baseX + currentOffsetX);
        int drawY = (int) (baseY + currentOffsetY);

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        this.mc.getTextureManager().bindTexture(PARALLAX_IMAGE);
        Gui.drawModalRectWithCustomSizedTexture(drawX, drawY, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}