package com.yourwebsitespace.solehack.modules.misc;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import java.io.IOException;

@SuppressWarnings("unused") // 🌟 This annotation instantly removes the yellow "never used" warnings
public class ProxyConfigScreen extends GuiScreen {
    private final GuiScreen parentScreen;
    private final com.yourwebsitespace.solehack.modules.misc.Proxy proxyModule;

    private GuiTextField ipField;
    private GuiTextField portField;
    private GuiButton toggleButton;

    // The constructor that takes your parent screen and your custom Proxy module instance
    public ProxyConfigScreen(GuiScreen parentScreen, com.yourwebsitespace.solehack.modules.misc.Proxy proxyModule) {
        this.parentScreen = parentScreen;
        this.proxyModule = proxyModule;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Input field for IP
        this.ipField = new GuiTextField(0, this.fontRenderer, centerX - 100, centerY - 40, 200, 20);
        this.ipField.setMaxStringLength(255);
        this.ipField.setFocused(true);
        this.ipField.setText(proxyModule.proxyIp);

        // Input field for Port
        this.portField = new GuiTextField(1, this.fontRenderer, centerX - 100, centerY, 200, 20);
        this.portField.setMaxStringLength(5);
        this.portField.setText(String.valueOf(proxyModule.proxyPort));

        // Interactive buttons
        this.toggleButton = new GuiButton(2, centerX - 100, centerY + 30, 200, 20, getToggleLabel());
        this.buttonList.add(this.toggleButton);
        this.buttonList.add(new GuiButton(3, centerX - 100, centerY + 55, 200, 20, "Done"));
    }

    private String getToggleLabel() {
        return "Status: " + (proxyModule.activeState ? "§aENABLED" : "§cDISABLED");
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.drawCenteredString(this.fontRenderer, "SoleHack Proxy Configuration", centerX, centerY - 80, 0xFFFFFF);
        this.drawString(this.fontRenderer, "Proxy IP / Hostname:", centerX - 100, centerY - 52, 0xA0A0A0);
        this.drawString(this.fontRenderer, "Proxy Port:", centerX - 100, centerY - 12, 0xA0A0A0);

        this.ipField.drawTextBox();
        this.portField.drawTextBox();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        this.ipField.textboxKeyTyped(typedChar, keyCode);
        this.portField.textboxKeyTyped(typedChar, keyCode);

        // Tab swapping focus between fields
        if (keyCode == Keyboard.KEY_TAB) {
            if (this.ipField.isFocused()) {
                this.ipField.setFocused(false);
                this.portField.setFocused(true);
            } else {
                this.ipField.setFocused(true);
                this.portField.setFocused(false);
            }
        }

        // Enter key confirms changes
        if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
            actionPerformed(this.buttonList.get(1));
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.ipField.mouseClicked(mouseX, mouseY, mouseButton);
        this.portField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) return;

        if (button.id == 2) {
            saveSettings();

            // Handles switching states using the custom variables in your Proxy module
            if (proxyModule.activeState) {
                proxyModule.onDisable();
            } else {
                proxyModule.onEnable();
            }

            this.toggleButton.displayString = getToggleLabel();
        }
        else if (button.id == 3) {
            saveSettings();
            this.mc.displayGuiScreen(this.parentScreen); // Returns back to the click GUI menu
        }
    }

    private void saveSettings() {
        String ip = this.ipField.getText().trim();
        int port;
        try {
            port = Integer.parseInt(this.portField.getText().trim());
        } catch (NumberFormatException e) {
            port = 9050;
        }
        proxyModule.setProxyConfig(ip, port);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }
}
