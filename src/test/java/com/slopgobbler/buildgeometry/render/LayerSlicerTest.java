package com.slopgobbler.buildgeometry.render;

import com.slopgobbler.buildgeometry.generator.CircleGenerator;
import com.slopgobbler.buildgeometry.generator.GeneratorContext;
import com.slopgobbler.buildgeometry.generator.PlaneOrientation;
import com.slopgobbler.buildgeometry.generator.VoxelShape;
import com.slopgobbler.buildgeometry.math.Point3i;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class LayerSlicerTest {

    private VoxelShape createTestVerticalCylinder() {
        CircleGenerator generator = new CircleGenerator();
        GeneratorContext ctx = GeneratorContext.builder()
                .origin(Point3i.ZERO)
                .radius(5)
                .depth(4) // 4 vertical Y layers: 0, 1, 2, 3
                .orientation(PlaneOrientation.HORIZONTAL_XZ)
                .filled(false)
                .build();
        return generator.generate(ctx);
    }

    @Test
    @DisplayName("Single layer mode isolates voxels on active Y plane")
    void testSingleLayerFiltering() {
        VoxelShape cylinder = createTestVerticalCylinder();
        LayerSlicer slicer = new LayerSlicer();
        slicer.setMode(LayerSlicer.SliceMode.SINGLE_LAYER);
        slicer.setActiveLayer(2);

        List<Point3i> visible = slicer.filterVisibleVoxels(cylinder);
        assertFalse(visible.isEmpty());

        for (Point3i p : visible) {
            assertEquals(2, p.y, "All filtered voxels must be at Y=2");
        }
    }

    @Test
    @DisplayName("Up-to-current mode filters all voxels up to active Y plane")
    void testUpToCurrentFiltering() {
        VoxelShape cylinder = createTestVerticalCylinder();
        LayerSlicer slicer = new LayerSlicer();
        slicer.setMode(LayerSlicer.SliceMode.UP_TO_CURRENT);
        slicer.setActiveLayer(1);

        List<Point3i> visible = slicer.filterVisibleVoxels(cylinder);
        for (Point3i p : visible) {
            assertTrue(p.y <= 1, "Voxel Y (" + p.y + ") must be <= 1");
        }
    }

    @Test
    @DisplayName("Layer advance and retreat navigation")
    void testLayerNavigation() {
        VoxelShape cylinder = createTestVerticalCylinder();
        LayerSlicer slicer = new LayerSlicer();
        slicer.setActiveLayer(0);

        assertTrue(slicer.nextLayer(cylinder));
        assertEquals(1, slicer.getActiveLayer());

        assertTrue(slicer.nextLayer(cylinder));
        assertEquals(2, slicer.getActiveLayer());

        assertTrue(slicer.prevLayer(cylinder));
        assertEquals(1, slicer.getActiveLayer());
    }

    @Test
    @DisplayName("Progress calculation for active layer and total structure")
    void testProgressCalculation() {
        VoxelShape cylinder = createTestVerticalCylinder();
        LayerSlicer slicer = new LayerSlicer();
        slicer.setActiveLayer(0);

        List<Point3i> layer0 = cylinder.getLayer(0);
        assertFalse(layer0.isEmpty());

        Set<Point3i> placed = new HashSet<>();
        assertEquals(0.0, slicer.calculateLayerProgress(cylinder, placed), 1e-6);

        // Place half the layer
        int halfCount = layer0.size() / 2;
        for (int i = 0; i < halfCount; i++) {
            placed.add(layer0.get(i));
        }

        double progress = slicer.calculateLayerProgress(cylinder, placed);
        assertEquals((double) halfCount / layer0.size(), progress, 1e-6);
    }

    @Test
    @DisplayName("Auto-advance triggers when active layer is completely placed")
    void testAutoAdvance() {
        VoxelShape cylinder = createTestVerticalCylinder();
        LayerSlicer slicer = new LayerSlicer();
        slicer.setAutoAdvance(true);
        slicer.setActiveLayer(0);

        Set<Point3i> placed = new HashSet<>();
        List<Point3i> layer0 = cylinder.getLayer(0);

        // Incomplete layer: should return false and not advance
        placed.addAll(layer0.subList(0, layer0.size() - 1));
        assertFalse(slicer.checkAndAutoAdvance(cylinder, placed));
        assertEquals(0, slicer.getActiveLayer());

        // Place the final block
        placed.add(layer0.get(layer0.size() - 1));
        assertTrue(slicer.checkAndAutoAdvance(cylinder, placed));
        assertEquals(1, slicer.getActiveLayer(), "Auto-advance should move to Y=1");
    }
}
