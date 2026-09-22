package com.yourwebsitespace.solehack;

import com.yourwebsitespace.solehack.modules.combat.BedAura;
import com.yourwebsitespace.solehack.modules.combat.BedAuraConfigScreen;
import com.yourwebsitespace.solehack.modules.combat.CrystalAura;
import com.yourwebsitespace.solehack.modules.combat.CrystalAuraConfigScreen;
import com.yourwebsitespace.solehack.modules.misc.Proxy;
import com.yourwebsitespace.solehack.modules.misc.ProxyConfigScreen;
import com.yourwebsitespace.solehack.modules.render.CrystalColor;
import com.yourwebsitespace.solehack.modules.render.CrystalColorConfigScreen;
import com.yourwebsitespace.solehack.modules.render.HandView;
import com.yourwebsitespace.solehack.modules.render.HandViewConfigScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigScreen extends GuiScreen {
    private final GuiScreen parent;
    private Category selectedCategory = Category.COMBAT;
    private Module awaitingBind = null;

    // Search Box Components
    private GuiTextField searchField;
    private String searchQuery = "";

    public ConfigScreen(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {

        this.buttonList.clear();


        // Calculated horizontal margins to host all 5 category headers cleanly
        int tabWidth = 68;
        int startX = this.width / 2 - (tabWidth * Category.values().length) / 2;
        int tabY = 25;
        Category[] categories = Category.values();

        for (int i = 0; i < categories.length; i++) {
            GuiButton tabButton = new GuiButton(100 + i, startX + i * tabWidth, tabY, tabWidth - 2, 20, capitalize(categories[i].name()));
            if (categories[i] == selectedCategory) {
                tabButton.enabled = false;
            }
            this.buttonList.add(tabButton);
        }

        // Initialize our search text input bar field
        this.searchField = new GuiTextField(99, this.fontRenderer, this.width / 2 - 100, 52, 200, 20);
        this.searchField.setMaxStringLength(32);
        this.searchField.setFocused(selectedCategory == Category.SEARCH);
        this.searchField.setText(searchQuery);

        addModuleButtons();
        this.buttonList.add(new GuiButton(999, this.width / 2 - 100, this.height - 30, 200, 20, "Done"));
    }

    private void addModuleButtons() {
        List<Module> modules = getActiveCategoryModules();

        // Lower rendering rows slightly if the text field is present
        int startY = (selectedCategory == Category.SEARCH) ? 80 : 55;
        int centerX = this.width / 2;

        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            int rowY = startY + (i * 24);
            if (rowY > this.height - 60) break;

            // FIXED: Using the native public getName() getter method to bypass private field visibility rules
            String label = module.getName() + ": " + (module.isEnabled() ? "ON" : "OFF");
            this.buttonList.add(new GuiButton(200 + i, centerX - 100, rowY, 130, 20, label));

            String keyLabel = (awaitingBind == module) ? "..." : (module.getKeyCode() == -1 ? "Bind" : Keyboard.getKeyName(module.getKeyCode()));
            this.buttonList.add(new GuiButton(400 + i, centerX + 35, rowY, 65, 20, keyLabel));

            if (module instanceof HandView || module instanceof CrystalColor || module instanceof CrystalAura || module instanceof Proxy || module instanceof BedAura) {
                this.buttonList.add(new GuiButton(300 + i, centerX + 105, rowY, 35, 20, "Edit"));
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 999) {
            this.mc.displayGuiScreen(parent);
            return;
        }

        if (button.id >= 100 && button.id < 100 + Category.values().length) {
            selectedCategory = Category.values()[button.id - 100];
            awaitingBind = null;
            if (selectedCategory != Category.SEARCH) searchQuery = "";
            initGui();
            return;
        }

        if (button.id >= 400) {
            List<Module> modules = getActiveCategoryModules();
            int index = button.id - 400;
            if (index >= 0 && index < modules.size()) {
                awaitingBind = modules.get(index);
                initGui();
            }
            return;
        }

        if (button.id >= 300 && button.id < 400) {
            List<Module> modules = getActiveCategoryModules();
            int index = button.id - 300;
            if (index >= 0 && index < modules.size()) {
                Module module = modules.get(index);
                if (module instanceof HandView) this.mc.displayGuiScreen(new HandViewConfigScreen(this, (HandView) module));
                else if (module instanceof CrystalColor) this.mc.displayGuiScreen(new CrystalColorConfigScreen(this, (CrystalColor) module));
                else if (module instanceof CrystalAura) this.mc.displayGuiScreen(new CrystalAuraConfigScreen(this));
                else if (module instanceof Proxy) this.mc.displayGuiScreen(new ProxyConfigScreen(this, (Proxy) module));
                else if (module instanceof BedAura) this.mc.displayGuiScreen(new BedAuraConfigScreen(this, (BedAura) module));
            }
            return;
        }

        if (button.id >= 200 && button.id < 300) {
            List<Module> modules = getActiveCategoryModules();
            int index = button.id - 200;
            if (index >= 0 && index < modules.size()) {
                modules.get(index).setEnabled(!modules.get(index).isEnabled());
                initGui();
            }
        }
    }

    private List<Module> getActiveCategoryModules() {
        if (selectedCategory == Category.SEARCH) {
            List<Module> searchResults = new ArrayList<>();
            for (Category cat : Category.values()) {
                if (cat == Category.SEARCH) continue;
                List<Module> categoryModules = ModuleManager.getModulesByCategory(cat);
                if (categoryModules != null) {
                    for (Module m : categoryModules) {
                        // FIXED: Using public getName() to securely check string query data parameters
                        if (m != null && m.getName().toLowerCase().contains(searchQuery.toLowerCase())) {
                            searchResults.add(m);
                        }
                    }
                }
            }
            return searchResults;
        }
        return ModuleManager.getModulesByCategory(selectedCategory);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (awaitingBind != null) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                awaitingBind.setKeyCode(-1);
            } else {
                awaitingBind.setKeyCode(keyCode);
            }
            awaitingBind = null;
            initGui();
            return;
        }

        if (selectedCategory == Category.SEARCH && searchField.isFocused()) {
            searchField.textboxKeyTyped(typedChar, keyCode);
            searchQuery = searchField.getText();

            this.buttonList.removeIf(b -> b.id >= 200 && b.id < 900);
            addModuleButtons();
            return;
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (selectedCategory == Category.SEARCH && this.searchField != null) {
            this.searchField.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        int centerX = this.width / 2;
        this.drawCenteredString(this.fontRenderer, "SoleHack Config", centerX, 10, 0xFFFFFF);

        if (selectedCategory == Category.SEARCH && this.searchField != null) {
            this.searchField.drawTextBox();
            if (searchField.getText().isEmpty() && !searchField.isFocused()) {
                this.drawString(this.fontRenderer, "Type module name...", centerX - 94, 58, 0x777777);
            }
        }

        if (awaitingBind != null) {
            // FIXED: Patched private access warning line to use public getName() tracker mapping securely
            this.drawCenteredString(this.fontRenderer, "Press a key to bind \"" + awaitingBind.getName() + "\" (ESC to clear)", centerX, this.height - 45, 0xFFFF55);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}
