package com.slopgobbler.buildgeometry;

import com.slopgobbler.buildgeometry.config.BuildGeometryConfig;
import com.slopgobbler.buildgeometry.event.ClientKeybindHandler;
import com.slopgobbler.buildgeometry.generator.ShapeRegistry;
import com.slopgobbler.buildgeometry.generator.VoxelShape;
import com.slopgobbler.buildgeometry.guide.AngleSnapManager;
import com.slopgobbler.buildgeometry.guide.CursorGuideRenderer;
import com.slopgobbler.buildgeometry.gui.BuildGeometryHudOverlay;
import com.slopgobbler.buildgeometry.math.Point3i;
import com.slopgobbler.buildgeometry.render.ColorPalette;
import com.slopgobbler.buildgeometry.render.GhostBlockRenderer;
import com.slopgobbler.buildgeometry.render.LayerSlicer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Main client entrypoint for BuildGeometry Pro.
 * Provides client-side architectural CAD tools, real-time hologram ghost projection,
 * layer-by-layer slicing, and angle-snapped cursor guidelines.
 */
public class BuildGeometryMod implements ClientModInitializer {
    public static final String MOD_ID = "buildgeometry";
    public static final String MOD_NAME = "BuildGeometry Pro";
    public static final String MOD_VERSION = "1.0.0";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    private static BuildGeometryMod instance;

    private BuildGeometryConfig config;
    private LayerSlicer layerSlicer;
    private ColorPalette colorPalette;
    private GhostBlockRenderer ghostRenderer;
    private AngleSnapManager snapManager;
    private CursorGuideRenderer guideRenderer;
    private BuildGeometryHudOverlay hudOverlay;

    public static BuildGeometryMod getInstance() {
        return instance;
    }

    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("[{}] Initializing Fabric client integration (v{})...", MOD_NAME, MOD_VERSION);

        try {
            // Load configuration
            File configFile = new File(Minecraft.getInstance().gameDirectory, "config/buildgeometry.json");
            this.config = BuildGeometryConfig.load(configFile);

            // Initialize CAD components
            this.layerSlicer = new LayerSlicer();
            this.layerSlicer.setMode(this.config.sliceMode);
            this.layerSlicer.setAutoAdvance(this.config.autoAdvance);

            this.colorPalette = new ColorPalette();
            this.colorPalette.setCurrentTheme(this.config.colorTheme);
            this.colorPalette.setGlobalAlpha(this.config.globalAlpha);
            this.colorPalette.setUseHeightGradient(this.config.useHeightGradient);

            this.ghostRenderer = new GhostBlockRenderer(this.layerSlicer, this.colorPalette);
            this.ghostRenderer.setEnabled(this.config.projectionEnabled);
            this.ghostRenderer.setRenderStyle(this.config.renderStyle);
            this.ghostRenderer.setMaxRenderDistance(this.config.maxRenderDistance);

            this.snapManager = new AngleSnapManager();
            this.snapManager.setSnappingEnabled(this.config.snappingEnabled);
            this.snapManager.setSnapStepDegrees(this.config.snapStepDegrees);
            this.snapManager.setLockMode(this.config.lockMode);

            this.guideRenderer = new CursorGuideRenderer(this.snapManager);
            this.hudOverlay = new BuildGeometryHudOverlay(this.ghostRenderer, this.guideRenderer);

            // Register Client Keybindings
            ClientKeybindHandler.register();

            // Client Tick Event
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                ClientKeybindHandler.onClientTick(client);
            });

            // Reset projection state on world disconnect
            ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
                this.ghostRenderer.setActiveShape(null);
                this.snapManager.setAnchorPoint(null);
            });

            LOGGER.info("[{}] Architectural CAD and hologram projection engines ready! Registered {} generators.",
                    MOD_NAME, ShapeRegistry.getAll().size());
        } catch (Throwable t) {
            LOGGER.error("[{}] Initialization error: {}", MOD_NAME, t.getMessage(), t);
        }
    }

    public BuildGeometryConfig getConfig() { return config; }
    public LayerSlicer getLayerSlicer() { return layerSlicer; }
    public ColorPalette getColorPalette() { return colorPalette; }
    public GhostBlockRenderer getGhostRenderer() { return ghostRenderer; }
    public AngleSnapManager getSnapManager() { return snapManager; }
    public CursorGuideRenderer getGuideRenderer() { return guideRenderer; }
    public BuildGeometryHudOverlay getHudOverlay() { return hudOverlay; }
}
