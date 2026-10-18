package dev.xmine.config.input;

import dev.xmine.config.configuration.ConfigManager;

/** A single optional keyboard key (-1 = unbound). */
public final class Keybind {
    private int key = -1, def = -1;

    public int get() { return key; }
    public boolean isBound() { return key >= 0; }

    public void set(int k) {
        if (k == key) return;
        key = k;
        KeybindManager.invalidate();
        ConfigManager.markDirty();
    }

    public void clear() { set(-1); }
    public void setDefault(int k) { def = k; key = k; }
    public void reset() { set(def); }

    public String display() { return KeyUtil.name(key); }
}
