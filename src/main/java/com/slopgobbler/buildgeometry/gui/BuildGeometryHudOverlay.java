package com.slopgobbler.buildgeometry.gui;

import com.slopgobbler.buildgeometry.config.BuildGeometryConfig;
import com.slopgobbler.buildgeometry.generator.VoxelShape;
import com.slopgobbler.buildgeometry.guide.AngleSnapManager;
import com.slopgobbler.buildgeometry.guide.CursorGuideRenderer;
import com.slopgobbler.buildgeometry.math.Point3i;
import com.slopgobbler.buildgeometry.render.GhostBlockRenderer;
import com.slopgobbler.buildgeometry.render.LayerSlicer;

import java.util.Collections;
import java.util.Set;

/**
 * High-tech architectural CAD HUD information overlay.
 * Formats shape metadata, layer progression, snapping indicators, and delta coordinates.
 */
public class BuildGeometryHudOverlay {
    private final GhostBlockRenderer ghostRenderer;
    private final CursorGuideRenderer guideRenderer;

    public BuildGeometryHudOverlay(GhostBlockRenderer ghostRenderer, CursorGuideRenderer guideRenderer) {
        this.ghostRenderer = ghostRenderer;
        this.guideRenderer = guideRenderer;
    }

    public GhostBlockRenderer getGhostRenderer() {
        return ghostRenderer;
    }

    public CursorGuideRenderer getGuideRenderer() {
        return guideRenderer;
    }

    /**
     * Formats top-level CAD blueprint status for screen or action bar.
     */
    public String getStatusLine(Set<Point3i> placedBlocks) {
        BuildGeometryConfig cfg = BuildGeometryConfig.getInstance();
        if (!cfg.projectionEnabled) {
            return "§7[BuildGeometry CAD] Projection DISABLED (Press 'G' to enable)";
        }

        VoxelShape shape = ghostRenderer.getActiveShape();
        if (shape == null || shape.isEmpty()) {
            return "§b[BuildGeometry CAD]§r No active structure loaded.";
        }

        LayerSlicer slicer = ghostRenderer.getLayerSlicer();
        int activeLayer = slicer.getActiveLayer();
        int totalLayers = shape.getSortedYLevels().size();
        int layerIdx = shape.getSortedYLevels().indexOf(activeLayer) + 1;
        if (layerIdx <= 0) layerIdx = 1;

        double layerProgress = slicer.calculateLayerProgress(shape, placedBlocks) * 100.0;
        double totalProgress = slicer.calculateTotalProgress(shape, placedBlocks) * 100.0;

        AngleSnapManager snap = guideRenderer.getSnapManager();
        String lockStr = (snap.getLockMode() != AngleSnapManager.LockMode.NONE)
                ? String.format("§e[%s]§r", snap.getLockMode().getDisplayName())
                : "§a[Free]§r";

        return String.format(
                "§b[CAD]§r §f%s§r | Voxels: §a%d§r | Layer: §e%d/%d§r (§a%.0f%%§r) | Total: §a%.0f%%§r | Snap: §b%.0f°§r %s",
                shape.getType().getDisplayName(),
                shape.size(),
                layerIdx,
                Math.max(1, totalLayers),
                layerProgress,
                totalProgress,
                snap.getSnapStepDegrees(),
                lockStr
        );
    }

    /**
     * Formats cursor delta guide line.
     */
    public String getCursorDeltaLine(Point3i rawTarget, double yaw, double pitch) {
        CursorGuideRenderer.HudMetrics m = guideRenderer.computeMetrics(rawTarget, yaw, pitch);
        return m.formatSummary();
    }
}
