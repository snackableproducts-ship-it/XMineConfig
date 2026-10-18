package dev.xmine.config.module;

import dev.xmine.config.core.Category;
import dev.xmine.config.core.XModule;
import dev.xmine.config.setting.*;
import dev.xmine.config.input.KeyUtil;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.util.math.Vec3d;

public final class MovementModules {
    private MovementModules() {}

    public static final class Sprint extends XModule {
        private final BoolSetting omni = add(new BoolSetting("All directions", false));
        private boolean forced;
        public Sprint() { super("Sprint", "Always sprint while moving", Category.MOVEMENT); }
        @Override public void onTick() {
            ClientPlayerEntity p = mc.player;
            boolean fwd = mc.options.forwardKey.isPressed();
            boolean any = fwd || mc.options.backKey.isPressed() || mc.options.leftKey.isPressed() || mc.options.rightKey.isPressed();
            boolean want = (omni.get() ? any : fwd) && !p.isSneaking() && p.getHungerManager().getFoodLevel() > 6;
            if (want) { mc.options.sprintKey.setPressed(true); forced = true; }
            else if (forced) { mc.options.sprintKey.setPressed(false); forced = false; }
        }
        @Override public void onDisable() { if (forced) mc.options.sprintKey.setPressed(false); forced = false; }
    }

    public static final class AutoJump extends XModule {
        private Boolean prev;
        public AutoJump() { super("Auto Jump", "Automatically jumps up one-block ledges", Category.MOVEMENT); }
        @Override public void onEnable() { if (mc.options != null) { prev = mc.options.getAutoJump().getValue(); mc.options.getAutoJump().setValue(true); } }
        @Override public void onTick() { if (!mc.options.getAutoJump().getValue()) mc.options.getAutoJump().setValue(true); }
        @Override public void onDisable() { if (mc.options != null && prev != null) mc.options.getAutoJump().setValue(prev); prev = null; }
    }

    public static final class Step extends XModule {
        private final NumberSetting height = add(new NumberSetting("Height", 1.0, 0.6, 2.5, 0.1).suffix(" blocks"));
        public Step() { super("Step", "Walk up blocks without jumping", Category.MOVEMENT); }
        private void apply(double h) {
            if (mc.player == null) return;
            EntityAttributeInstance a = mc.player.getAttributeInstance(EntityAttributes.STEP_HEIGHT);
            if (a != null && a.getBaseValue() != h) a.setBaseValue(h);
        }
        @Override public void onTick() { apply(height.get()); }
        @Override public void onDisable() { apply(0.6); }
    }

    public static final class NoSlow extends XModule {
        /** Read by ClientPlayerEntityMixin. */
        public static boolean active;
        public NoSlow() { super("No Slow", "No slowdown while using items (eating, blocking, bows)", Category.MOVEMENT); }
        @Override public void onEnable() { active = true; }
        @Override public void onDisable() { active = false; }
    }

    public static final class SafeWalk extends XModule {
        /** Read by PlayerEntityMixin. */
        public static boolean active;
        public SafeWalk() { super("Safe Walk", "Never walk off block edges (like sneaking, without the slowdown)", Category.MOVEMENT); }
        @Override public void onEnable() { active = true; }
        @Override public void onDisable() { active = false; }
    }

    public static final class Speed extends XModule {
        private final NumberSetting speed = add(new NumberSetting("Speed", 0.3, 0.1, 1.0, 0.01).suffix(" b/t"));
        private final BoolSetting ground = add(new BoolSetting("Only on ground", false));
        public Speed() { super("Speed", "Move faster horizontally", Category.MOVEMENT); }
        @Override public void onTick() {
            ClientPlayerEntity p = mc.player;
            if (p == null || p.isSneaking() || p.hasVehicle() || p.isFallFlying()) return;
            float f = (mc.options.forwardKey.isPressed() ? 1f : 0f) - (mc.options.backKey.isPressed() ? 1f : 0f);
            float s = (mc.options.leftKey.isPressed() ? 1f : 0f) - (mc.options.rightKey.isPressed() ? 1f : 0f);
            if (f == 0 && s == 0) return;
            if (ground.get() && !p.isOnGround()) return;
            double len = Math.sqrt(f * f + s * s);
            f /= len; s /= len;
            double yaw = Math.toRadians(p.getYaw()), sin = Math.sin(yaw), cos = Math.cos(yaw), sp = speed.get();
            Vec3d v = p.getVelocity();
            p.setVelocity((s * cos - f * sin) * sp, v.y, (f * cos + s * sin) * sp);
        }
    }

    public static final class Flight extends XModule {
        private final NumberSetting speed = add(new NumberSetting("Fly speed", 0.05, 0.01, 0.5, 0.01));
        public Flight() { super("Flight", "Creative-style flight (kicked on vanilla servers)", Category.MOVEMENT); }
        @Override public void onEnable() { if (mc.player != null) { mc.player.getAbilities().allowFlying = true; mc.player.getAbilities().flying = true; } }
        @Override public void onTick() {
            PlayerAbilities a = mc.player.getAbilities();
            a.allowFlying = true;
            a.setFlySpeed(speed.getFloat());
        }
        @Override public void onDisable() {
            if (mc.player == null) return;
            PlayerAbilities a = mc.player.getAbilities();
            a.setFlySpeed(0.05f);
            if (!mc.player.isCreative() && !mc.player.isSpectator()) { a.allowFlying = false; a.flying = false; }
        }
    }

    public static final class HighJump extends XModule {
        private final NumberSetting power = add(new NumberSetting("Jump power", 0.7, 0.42, 1.5, 0.01));
        private boolean wasGround;
        public HighJump() { super("High Jump", "Jump much higher", Category.MOVEMENT); }
        @Override public void onTick() {
            ClientPlayerEntity p = mc.player;
            Vec3d v = p.getVelocity();
            // A jump just happened: we were on the ground last tick, are airborne now and moving up
            if (wasGround && !p.isOnGround() && v.y > 0.1 && mc.options.jumpKey.isPressed())
                p.setVelocity(v.x, (power.get() - 0.08) * 0.98, v.z);
            wasGround = p.isOnGround();
        }
    }

    public static final class InventoryMove extends XModule {
        public InventoryMove() { super("Inventory Move", "Keep walking and jumping while an inventory is open", Category.MOVEMENT); }
        private void sync(KeyBinding kb) {
            int code = KeyUtil.boundCode(kb);
            if (code >= 0) kb.setPressed(KeyUtil.down(code));
        }
        @Override public void onTick() {
            // Only container screens without text fields
            if (!(mc.currentScreen instanceof HandledScreen<?>) || mc.currentScreen instanceof CreativeInventoryScreen
                    || mc.currentScreen instanceof AnvilScreen) return;
            sync(mc.options.forwardKey); sync(mc.options.backKey); sync(mc.options.leftKey);
            sync(mc.options.rightKey); sync(mc.options.jumpKey); sync(mc.options.sprintKey);
        }
    }
}
