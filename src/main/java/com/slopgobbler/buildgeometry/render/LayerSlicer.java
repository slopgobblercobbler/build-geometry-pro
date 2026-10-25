package com.slopgobbler.buildgeometry.render;

import com.slopgobbler.buildgeometry.generator.VoxelShape;
import com.slopgobbler.buildgeometry.math.Point3i;

import java.util.*;

/**
 * Manages layer-by-layer slicing, active layer progression, voxel visibility filtering,
 * and completion metrics for manual or semi-automated construction workflows.
 */
public class LayerSlicer {
    public enum SliceMode {
        ALL_LAYERS("All Layers", "Display complete projected structure"),
        SINGLE_LAYER("Single Layer", "Display only voxels on the active Y plane"),
        UP_TO_CURRENT("Up To Current", "Display all voxels from base up to the active Y plane"),
        RANGE("Layer Range", "Display voxels between custom min and max Y boundaries");

        private final String displayName;
        private final String description;

        SliceMode(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }

    private SliceMode mode = SliceMode.ALL_LAYERS;
    private int activeLayer = 0;
    private int rangeMin = 0;
    private int rangeMax = 255;
    private boolean autoAdvance = true;

    public LayerSlicer() {}

    public SliceMode getMode() { return mode; }
    public void setMode(SliceMode mode) { this.mode = mode; }

    public int getActiveLayer() { return activeLayer; }
    public void setActiveLayer(int activeLayer) { this.activeLayer = activeLayer; }

    public int getRangeMin() { return rangeMin; }
    public void setRangeMin(int rangeMin) { this.rangeMin = Math.min(rangeMin, rangeMax); }

    public int getRangeMax() { return rangeMax; }
    public void setRangeMax(int rangeMax) { this.rangeMax = Math.max(rangeMin, rangeMax); }

    public boolean isAutoAdvance() { return autoAdvance; }
    public void setAutoAdvance(boolean autoAdvance) { this.autoAdvance = autoAdvance; }

    /**
     * Cycles between slice modes.
     */
    public void cycleMode() {
        SliceMode[] modes = SliceMode.values();
        this.mode = modes[(this.mode.ordinal() + 1) % modes.length];
    }

    /**
     * Advances to the next Y layer in the given shape.
     */
    public boolean nextLayer(VoxelShape shape) {
        if (shape == null || shape.isEmpty()) {
            activeLayer++;
            return true;
        }
        List<Integer> yLevels = shape.getSortedYLevels();
        int idx = Collections.binarySearch(yLevels, activeLayer);
        if (idx < 0) {
            int insertionPoint = -idx - 1;
            if (insertionPoint < yLevels.size()) {
                activeLayer = yLevels.get(insertionPoint);
                return true;
            }
            return false;
        } else if (idx + 1 < yLevels.size()) {
            activeLayer = yLevels.get(idx + 1);
            return true;
        }
        return false;
    }

    /**
     * Decrements to the previous Y layer in the given shape.
     */
    public boolean prevLayer(VoxelShape shape) {
        if (shape == null || shape.isEmpty()) {
            activeLayer--;
            return true;
        }
        List<Integer> yLevels = shape.getSortedYLevels();
        int idx = Collections.binarySearch(yLevels, activeLayer);
        if (idx < 0) {
            int insertionPoint = -idx - 1;
            if (insertionPoint > 0) {
                activeLayer = yLevels.get(insertionPoint - 1);
                return true;
            }
            return false;
        } else if (idx > 0) {
            activeLayer = yLevels.get(idx - 1);
            return true;
        }
        return false;
    }

    /**
     * Filters shape voxels based on current slicing mode and active layer.
     */
    public List<Point3i> filterVisibleVoxels(VoxelShape shape) {
        if (shape == null || shape.isEmpty()) {
            return Collections.emptyList();
        }

        switch (mode) {
            case SINGLE_LAYER:
                return shape.getLayer(activeLayer);

            case UP_TO_CURRENT: {
                List<Point3i> visible = new ArrayList<>();
                for (Map.Entry<Integer, List<Point3i>> entry : shape.getVoxelsByY().entrySet()) {
                    if (entry.getKey() <= activeLayer) {
                        visible.addAll(entry.getValue());
                    }
                }
                return visible;
            }

            case RANGE: {
                List<Point3i> visible = new ArrayList<>();
                for (Map.Entry<Integer, List<Point3i>> entry : shape.getVoxelsByY().entrySet()) {
                    int y = entry.getKey();
                    if (y >= rangeMin && y <= rangeMax) {
                        visible.addAll(entry.getValue());
                    }
                }
                return visible;
            }

            case ALL_LAYERS:
            default:
                return new ArrayList<>(shape.getVoxels());
        }
    }

    /**
     * Calculates the completion ratio of the active layer (0.0 to 1.0).
     */
    public double calculateLayerProgress(VoxelShape shape, Set<Point3i> placedBlocks) {
        if (shape == null || shape.isEmpty() || placedBlocks == null) return 0.0;
        List<Point3i> layer = shape.getLayer(activeLayer);
        if (layer.isEmpty()) return 1.0;

        int placed = 0;
        for (Point3i p : layer) {
            if (placedBlocks.contains(p)) {
                placed++;
            }
        }
        return (double) placed / layer.size();
    }

    /**
     * Calculates total structure completion ratio (0.0 to 1.0).
     */
    public double calculateTotalProgress(VoxelShape shape, Set<Point3i> placedBlocks) {
        if (shape == null || shape.isEmpty() || placedBlocks == null) return 0.0;
        int total = shape.size();
        if (total == 0) return 1.0;

        int placed = 0;
        for (Point3i p : shape.getVoxels()) {
            if (placedBlocks.contains(p)) {
                placed++;
            }
        }
        return (double) placed / total;
    }

    /**
     * Checks if current active layer is fully built, and if autoAdvance is enabled, advances to the next layer.
     * Returns true if layer was completed.
     */
    public boolean checkAndAutoAdvance(VoxelShape shape, Set<Point3i> placedBlocks) {
        if (shape == null || shape.isEmpty() || placedBlocks == null) return false;
        List<Point3i> layer = shape.getLayer(activeLayer);
        if (layer.isEmpty()) return false;

        for (Point3i p : layer) {
            if (!placedBlocks.contains(p)) {
                return false;
            }
        }

        // Layer is complete
        if (autoAdvance) {
            nextLayer(shape);
        }
        return true;
    }
}
