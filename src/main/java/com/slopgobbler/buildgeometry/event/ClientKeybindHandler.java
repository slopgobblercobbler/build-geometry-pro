package com.slopgobbler.buildgeometry.event;

import com.mojang.blaze3d.platform.InputConstants;
import com.slopgobbler.buildgeometry.BuildGeometryMod;
import com.slopgobbler.buildgeometry.config.BuildGeometryConfig;
import com.slopgobbler.buildgeometry.generator.ShapeRegistry;
import com.slopgobbler.buildgeometry.generator.VoxelShape;
import com.slopgobbler.buildgeometry.guide.AngleSnapManager;
import com.slopgobbler.buildgeometry.math.Point3i;
import com.slopgobbler.buildgeometry.render.GhostBlockRenderer;
import com.slopgobbler.buildgeometry.render.LayerSlicer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;

/**
 * Client keybindings handler for on-the-fly CAD projection controls,
 * layer slicing navigation, angle snapping, and axis constraint locking.
 */
public class ClientKeybindHandler {
    private static KeyMapping toggleProjectionKey;
    private static KeyMapping nextLayerKey;
    private static KeyMapping prevLayerKey;
    private static KeyMapping cycleSliceModeKey;
    private static KeyMapping toggleSnapKey;
    private static KeyMapping cycleLockModeKey;
    private static KeyMapping rebuildShapeKey;

    public static void register() {
        toggleProjectionKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.buildgeometry.toggle_projection",
                InputConstants.Type.KEYBOARD,
                GLFW.GLFW_KEY_G,
                KeyMapping.Category.MISC
        ));

        nextLayerKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.buildgeometry.next_layer",
                InputConstants.Type.KEYBOARD,
                GLFW.GLFW_KEY_PAGE_UP,
                KeyMapping.Category.MISC
        ));

        prevLayerKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.buildgeometry.prev_layer",
                InputConstants.Type.KEYBOARD,
                GLFW.GLFW_KEY_PAGE_DOWN,
                KeyMapping.Category.MISC
        ));

        cycleSliceModeKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.buildgeometry.cycle_slice_mode",
                InputConstants.Type.KEYBOARD,
                GLFW.GLFW_KEY_HOME,
                KeyMapping.Category.MISC
        ));

        toggleSnapKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.buildgeometry.toggle_snap",
                InputConstants.Type.KEYBOARD,
                GLFW.GLFW_KEY_K,
                KeyMapping.Category.MISC
        ));

        cycleLockModeKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.buildgeometry.cycle_lock_mode",
                InputConstants.Type.KEYBOARD,
                GLFW.GLFW_KEY_L,
                KeyMapping.Category.MISC
        ));

        rebuildShapeKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.buildgeometry.rebuild_shape",
                InputConstants.Type.KEYBOARD,
                GLFW.GLFW_KEY_H,
                KeyMapping.Category.MISC
        ));
    }

    public static void onClientTick(Minecraft client) {
        if (client.player == null) return;

        BuildGeometryConfig cfg = BuildGeometryConfig.getInstance();
        BuildGeometryMod mod = BuildGeometryMod.getInstance();
        if (mod == null) return;

        GhostBlockRenderer renderer = mod.getGhostRenderer();
        LayerSlicer slicer = renderer.getLayerSlicer();
        AngleSnapManager snap = mod.getSnapManager();

        // 1. Toggle Projection
        while (toggleProjectionKey != null && toggleProjectionKey.consumeClick()) {
            cfg.projectionEnabled = !cfg.projectionEnabled;
            renderer.setEnabled(cfg.projectionEnabled);
            String msg = cfg.projectionEnabled ? "§a[CAD]§r Hologram Projection ENABLED" : "§7[CAD]§r Hologram Projection DISABLED";
            client.player.sendOverlayMessage(Component.literal(msg));
        }

        // 2. Next Layer
        while (nextLayerKey != null && nextLayerKey.consumeClick()) {
            boolean changed = slicer.nextLayer(renderer.getActiveShape());
            if (changed) {
                client.player.sendOverlayMessage(Component.literal(
                        String.format("§e[CAD]§r Active Layer: §bY=%d§r", slicer.getActiveLayer())
                ));
            }
        }

        // 3. Prev Layer
        while (prevLayerKey != null && prevLayerKey.consumeClick()) {
            boolean changed = slicer.prevLayer(renderer.getActiveShape());
            if (changed) {
                client.player.sendOverlayMessage(Component.literal(
                        String.format("§e[CAD]§r Active Layer: §bY=%d§r", slicer.getActiveLayer())
                ));
            }
        }

        // 4. Cycle Slice Mode
        while (cycleSliceModeKey != null && cycleSliceModeKey.consumeClick()) {
            slicer.cycleMode();
            cfg.sliceMode = slicer.getMode();
            client.player.sendOverlayMessage(Component.literal(
                    String.format("§b[CAD]§r Slicing Mode: §f%s§r", slicer.getMode().getDisplayName())
            ));
        }

        // 5. Toggle Angle Snapping
        while (toggleSnapKey != null && toggleSnapKey.consumeClick()) {
            snap.cycleSnapStep();
            cfg.snapStepDegrees = snap.getSnapStepDegrees();
            client.player.sendOverlayMessage(Component.literal(
                    String.format("§b[CAD]§r Angle Snap Step: §f%.0f°§r", snap.getSnapStepDegrees())
            ));
        }

        // 6. Cycle Lock Mode
        while (cycleLockModeKey != null && cycleLockModeKey.consumeClick()) {
            snap.cycleLockMode();
            cfg.lockMode = snap.getLockMode();
            // Set anchor to player's current block position if locking
            if (snap.getLockMode() != AngleSnapManager.LockMode.NONE) {
                snap.setAnchorPoint(new Point3i(
                        client.player.getBlockX(),
                        client.player.getBlockY(),
                        client.player.getBlockZ()
                ));
            }
            client.player.sendOverlayMessage(Component.literal(
                    String.format("§e[CAD]§r Axis Lock: §f%s§r", snap.getLockMode().getDisplayName())
            ));
        }

        // 7. Rebuild / Project at Player Cursor
        while (rebuildShapeKey != null && rebuildShapeKey.consumeClick()) {
            Point3i origin = new Point3i(
                    client.player.getBlockX(),
                    client.player.getBlockY(),
                    client.player.getBlockZ()
            );
            VoxelShape shape = ShapeRegistry.generate(cfg.activeShapeType, cfg.toContext(origin));
            renderer.setActiveShape(shape);
            slicer.setActiveLayer(shape.getMinY());
            client.player.sendOverlayMessage(Component.literal(
                    String.format("§a[CAD]§r Projected §b%s§r at (%d, %d, %d) with §e%d§r voxels",
                            shape.getType().getDisplayName(), origin.x, origin.y, origin.z, shape.size())
            ));
        }
    }
}
