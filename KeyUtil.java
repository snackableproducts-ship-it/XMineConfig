package dev.xmine.config.input;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

/** Raw keyboard helpers built on GLFW + stable KeyBinding APIs (InputUtil's signatures changed in 1.21.9). */
public final class KeyUtil {
    private KeyUtil() {}

    /** Keyboard key code currently bound to a vanilla KeyBinding, or -1 (unbound / mouse button). */
    public static int boundCode(KeyBinding kb) {
        try {
            String t = kb.getBoundKeyTranslationKey();
            if (t == null || !t.startsWith("key.keyboard.")) return -1;
            int c = InputUtil.fromTranslationKey(t).getCode();
            return c < 0 ? -1 : c;
        } catch (Exception e) {
            return -1;
        }
    }

    public static boolean down(int code) {
        if (code < 0) return false;
        MinecraftClient mc = MinecraftClient.getInstance();
        return mc.getWindow() != null && GLFW.glfwGetKey(mc.getWindow().getHandle(), code) == GLFW.GLFW_PRESS;
    }

    /** True if the physical key bound to this control is held, ignoring KeyBinding.isPressed() state. */
    public static boolean rawDown(KeyBinding kb) { return down(boundCode(kb)); }

    public static String name(int key) {
        if (key < 0) return "NONE";
        switch (key) {
            case GLFW.GLFW_KEY_SPACE: return "SPACE";
            case GLFW.GLFW_KEY_LEFT_SHIFT: return "L-SHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT: return "R-SHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL: return "L-CTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL: return "R-CTRL";
            case GLFW.GLFW_KEY_LEFT_ALT: return "L-ALT";
            case GLFW.GLFW_KEY_RIGHT_ALT: return "R-ALT";
            case GLFW.GLFW_KEY_TAB: return "TAB";
            case GLFW.GLFW_KEY_ENTER: return "ENTER";
            case GLFW.GLFW_KEY_CAPS_LOCK: return "CAPS";
            case GLFW.GLFW_KEY_UP: return "UP";
            case GLFW.GLFW_KEY_DOWN: return "DOWN";
            case GLFW.GLFW_KEY_LEFT: return "LEFT";
            case GLFW.GLFW_KEY_RIGHT: return "RIGHT";
            case GLFW.GLFW_KEY_INSERT: return "INSERT";
            case GLFW.GLFW_KEY_DELETE: return "DELETE";
            case GLFW.GLFW_KEY_HOME: return "HOME";
            case GLFW.GLFW_KEY_END: return "END";
            case GLFW.GLFW_KEY_PAGE_UP: return "PGUP";
            case GLFW.GLFW_KEY_PAGE_DOWN: return "PGDN";
            default: break;
        }
        if (key >= GLFW.GLFW_KEY_F1 && key <= GLFW.GLFW_KEY_F25) return "F" + (key - GLFW.GLFW_KEY_F1 + 1);
        if (key >= GLFW.GLFW_KEY_KP_0 && key <= GLFW.GLFW_KEY_KP_9) return "NUM" + (key - GLFW.GLFW_KEY_KP_0);
        try {
            String n = GLFW.glfwGetKeyName(key, 0);
            if (n != null && !n.isEmpty()) return n.toUpperCase(Locale.ROOT);
        } catch (Throwable ignored) {}
        return "KEY " + key;
    }
}
