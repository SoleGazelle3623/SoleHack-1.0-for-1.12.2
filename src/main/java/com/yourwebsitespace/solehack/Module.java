package com.yourwebsitespace.solehack;

public abstract class Module {
    private final String name;
    private final Category category;
    private boolean enabled = false;
    protected int keyCode = -1; // -1 = unbound

    public Module(String name, Category category) {
        this.name = name;
        this.category = category;
    }

    public String getName() { return name; }
    public Category getCategory() { return category; }
    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) onEnable(); else onDisable();
    }

    public int getKeyCode() { return keyCode; }
    public void setKeyCode(int keyCode) { this.keyCode = keyCode; }

    protected void onEnable() {}

    protected abstract void onUpdate();

    protected void onDisable() {}

    private int key = org.lwjgl.input.Keyboard.KEY_NONE;

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }
}