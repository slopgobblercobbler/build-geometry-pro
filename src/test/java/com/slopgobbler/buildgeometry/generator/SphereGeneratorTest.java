package com.slopgobbler.buildgeometry.generator;

import com.slopgobbler.buildgeometry.math.Point3i;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SphereGeneratorTest {

    @Test
    @DisplayName("Hollow sphere voxels lie within radial shell bounds")
    void testHollowSphereShellBounds() {
        SphereGenerator generator = new SphereGenerator();
        int radius = 10;
        int thickness = 1;

        GeneratorContext ctx = GeneratorContext.builder()
                .origin(Point3i.ZERO)
                .radius(radius)
                .thickness(thickness)
                .hollow(true)
                .build();

        VoxelShape shape = generator.generate(ctx);
        assertNotNull(shape);
        assertFalse(shape.isEmpty());

        for (Point3i p : shape.getVoxels()) {
            double dist = Math.sqrt(p.x * p.x + p.y * p.y + p.z * p.z);
            assertTrue(dist <= radius + 0.8, "Voxel dist " + dist + " should not exceed radius + tolerance");
            assertTrue(dist >= radius - thickness - 0.5, "Voxel dist " + dist + " should not be inside inner cavity");
        }

        // Center must not be present in hollow sphere
        assertFalse(shape.contains(Point3i.ZERO));
    }

    @Test
    @DisplayName("Solid sphere contains center and volume approximates (4/3)*pi*r^3")
    void testSolidSphere() {
        SphereGenerator generator = new SphereGenerator();
        int radius = 6;
        GeneratorContext ctx = GeneratorContext.builder()
                .origin(Point3i.ZERO)
                .radius(radius)
                .hollow(false)
                .build();

        VoxelShape shape = generator.generate(ctx);
        assertTrue(shape.contains(Point3i.ZERO));

        // Discrete sphere volume for (r+0.5)=6.5 approximates (4/3)*pi*(6.5)^3 = ~1150
        assertTrue(shape.size() >= 800 && shape.size() <= 1300,
                "Solid sphere voxel count (" + shape.size() + ") should approximate 4/3*pi*r^3");
    }

    @Test
    @DisplayName("8-octant reflective symmetry across all three axes")
    void testEightOctantSymmetry() {
        SphereGenerator generator = new SphereGenerator();
        GeneratorContext ctx = GeneratorContext.builder()
                .origin(Point3i.ZERO)
                .radius(8)
                .hollow(true)
                .build();

        VoxelShape shape = generator.generate(ctx);
        for (Point3i p : shape.getVoxels()) {
            assertTrue(shape.contains(new Point3i(-p.x, p.y, p.z)));
            assertTrue(shape.contains(new Point3i(p.x, -p.y, p.z)));
            assertTrue(shape.contains(new Point3i(p.x, p.y, -p.z)));
            assertTrue(shape.contains(new Point3i(-p.x, -p.y, -p.z)));
        }
    }

    @Test
    @DisplayName("Hemisphere mode restricts voxels to Y >= 0")
    void testHemisphere() {
        SphereGenerator generator = new SphereGenerator();
        GeneratorContext ctx = GeneratorContext.builder()
                .origin(Point3i.ZERO)
                .radius(8)
                .hollow(true)
                .inverted(true) // Triggers hemisphere mode in SphereGenerator
                .build();

        VoxelShape shape = generator.generate(ctx);
        for (Point3i p : shape.getVoxels()) {
            assertTrue(p.y >= 0, "Hemisphere voxels must have Y >= 0");
        }
    }
}
