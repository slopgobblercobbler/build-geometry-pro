package com.slopgobbler.buildgeometry.render;

import com.slopgobbler.buildgeometry.generator.VoxelShape;
import com.slopgobbler.buildgeometry.math.Point3i;
import com.slopgobbler.buildgeometry.math.Vec3d;

import java.util.*;

/**
 * High-performance client-side hologram and ghost-block renderer.
 * Manages spatial batching, distance culling, state-dependent colorization
 * (blueprint, completed, obstructed, active layer), and translucent mesh generation.
 */
public class GhostBlockRenderer {
    public enum RenderStyle {
        TRANSLUCENT_CUBES("Translucent Cubes", "Solid tinted translucent voxel cubes"),
        WIREFRAME_EDGES("Wireframe Only", "Clean CAD grid wireframe edges"),
        HYBRID("Hybrid", "Both translucent tinted faces and bright boundary wireframes");

        private final String displayName;
        private final String description;

        RenderStyle(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }

    public interface WorldBlockInspector {
        boolean isAir(Point3i pos);
        boolean isMatchingBlock(Point3i pos);
        boolean isObstructed(Point3i pos);
    }

    private final LayerSlicer layerSlicer;
    private final ColorPalette colorPalette;
    private VoxelShape activeShape = null;
    private boolean enabled = true;
    private RenderStyle renderStyle = RenderStyle.HYBRID;
    private double maxRenderDistance = 64.0;
    private WorldBlockInspector worldInspector = null;

    public GhostBlockRenderer(LayerSlicer slicer, ColorPalette palette) {
        this.layerSlicer = slicer;
        this.colorPalette = palette;
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public RenderStyle getRenderStyle() { return renderStyle; }
    public void setRenderStyle(RenderStyle renderStyle) { this.renderStyle = renderStyle; }

    public double getMaxRenderDistance() { return maxRenderDistance; }
    public void setMaxRenderDistance(double maxRenderDistance) { this.maxRenderDistance = maxRenderDistance; }

    public VoxelShape getActiveShape() { return activeShape; }
    public void setActiveShape(VoxelShape shape) { this.activeShape = shape; }

    public LayerSlicer getLayerSlicer() { return layerSlicer; }
    public ColorPalette getColorPalette() { return colorPalette; }

    public void setWorldInspector(WorldBlockInspector inspector) {
        this.worldInspector = inspector;
    }

    /**
     * Voxel render element with coordinates, resolved RGBA color, and face visibility mask.
     */
    public static final class VoxelRenderData {
        public final Point3i position;
        public final ColorPalette.Color4f color;
        public final boolean isPlaced;
        public final boolean isObstructed;
        public final boolean isActiveLayer;

        public VoxelRenderData(Point3i position, ColorPalette.Color4f color, boolean isPlaced, boolean isObstructed, boolean isActiveLayer) {
            this.position = position;
            this.color = color;
            this.isPlaced = isPlaced;
            this.isObstructed = isObstructed;
            this.isActiveLayer = isActiveLayer;
        }
    }

    /**
     * Collects all visible sliced voxels within the render distance, resolving their colors and states.
     */
    public List<VoxelRenderData> collectRenderBatch(Vec3d cameraPos) {
        if (!enabled || activeShape == null || activeShape.isEmpty()) {
            return Collections.emptyList();
        }

        List<Point3i> visiblePositions = layerSlicer.filterVisibleVoxels(activeShape);
        List<VoxelRenderData> batch = new ArrayList<>(visiblePositions.size());
        double maxDistSq = maxRenderDistance * maxRenderDistance;

        int activeY = layerSlicer.getActiveLayer();
        int minY = activeShape.getMinY();
        int maxY = activeShape.getMaxY();

        for (Point3i pos : visiblePositions) {
            if (cameraPos != null && pos.distanceSq(cameraPos.x, cameraPos.y, cameraPos.z) > maxDistSq) {
                continue; // Distance culling
            }

            boolean isPlaced = false;
            boolean isObstructed = false;
            if (worldInspector != null) {
                isPlaced = worldInspector.isMatchingBlock(pos);
                isObstructed = worldInspector.isObstructed(pos);
            }

            boolean isActiveLayer = (pos.y == activeY);
            ColorPalette.Color4f color = colorPalette.resolveColor(isPlaced, isObstructed, isActiveLayer, pos.y, minY, maxY);

            batch.add(new VoxelRenderData(pos, color, isPlaced, isObstructed, isActiveLayer));
        }

        return batch;
    }
}
