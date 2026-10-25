package com.slopgobbler.buildgeometry.generator;

import com.slopgobbler.buildgeometry.math.Point3i;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CircleGeneratorTest {

    @Test
    @DisplayName("Bresenham circle outline satisfies radius proximity")
    void testBresenhamCircleOutline() {
        CircleGenerator generator = new CircleGenerator();
        int radius = 10;
        GeneratorContext ctx = GeneratorContext.builder()
                .origin(Point3i.ZERO)
                .radius(radius)
                .circleAlgorithm(GeneratorContext.CircleAlgorithm.BRESENHAM)
                .filled(false)
                .orientation(PlaneOrientation.HORIZONTAL_XZ)
                .build();

        VoxelShape shape = generator.generate(ctx);
        assertNotNull(shape);
        assertFalse(shape.isEmpty());

        // For an outline ring of radius 10, all points should be within [R-1, R+1]
        for (Point3i p : shape.getVoxels()) {
            double dist = Math.sqrt(p.x * p.x + p.z * p.z);
            assertTrue(dist >= 8.5 && dist <= 11.0,
                    "Voxel distance " + dist + " should be close to radius " + radius);
        }

        // Center should not be present in an outline
        assertFalse(shape.contains(Point3i.ZERO));
    }

    @Test
    @DisplayName("Midpoint circle filled disc contains origin and expected voxel count")
    void testMidpointCircleFilled() {
        CircleGenerator generator = new CircleGenerator();
        int radius = 8;
        GeneratorContext ctx = GeneratorContext.builder()
                .origin(Point3i.ZERO)
                .radius(radius)
                .circleAlgorithm(GeneratorContext.CircleAlgorithm.MIDPOINT)
                .filled(true)
                .orientation(PlaneOrientation.HORIZONTAL_XZ)
                .build();

        VoxelShape shape = generator.generate(ctx);
        assertTrue(shape.contains(Point3i.ZERO), "Filled circle must contain center");

        // Filled area approx pi * r^2 = ~201
        assertTrue(shape.size() >= 170 && shape.size() <= 230,
                "Voxel count (" + shape.size() + ") should approximate pi*r^2");
    }

    @Test
    @DisplayName("Plane orientation mapping produces correct active axes")
    void testPlaneOrientations() {
        CircleGenerator generator = new CircleGenerator();

        // Horizontal XZ: Y should be constant (origin Y)
        GeneratorContext ctxXZ = GeneratorContext.builder()
                .origin(new Point3i(10, 50, 10))
                .radius(5)
                .orientation(PlaneOrientation.HORIZONTAL_XZ)
                .build();
        VoxelShape shapeXZ = generator.generate(ctxXZ);
        for (Point3i p : shapeXZ.getVoxels()) {
            assertEquals(50, p.y, "All XZ voxels should lie on Y=50 plane");
        }

        // Vertical XY: Z should be constant (origin Z)
        GeneratorContext ctxXY = GeneratorContext.builder()
                .origin(new Point3i(10, 50, 10))
                .radius(5)
                .orientation(PlaneOrientation.VERTICAL_XY)
                .build();
        VoxelShape shapeXY = generator.generate(ctxXY);
        for (Point3i p : shapeXY.getVoxels()) {
            assertEquals(10, p.z, "All XY voxels should lie on Z=10 plane");
        }
    }

    @Test
    @DisplayName("8-fold reflective symmetry of circle coordinates")
    void testSymmetry() {
        CircleGenerator generator = new CircleGenerator();
        GeneratorContext ctx = GeneratorContext.builder()
                .origin(Point3i.ZERO)
                .radius(12)
                .filled(false)
                .build();
        VoxelShape shape = generator.generate(ctx);

        for (Point3i p : shape.getVoxels()) {
            // Check octant reflections exist
            assertTrue(shape.contains(new Point3i(-p.x, p.y, p.z)), "Symmetric in -X");
            assertTrue(shape.contains(new Point3i(p.x, p.y, -p.z)), "Symmetric in -Z");
            assertTrue(shape.contains(new Point3i(-p.x, p.y, -p.z)), "Symmetric in -X, -Z");
        }
    }
}
