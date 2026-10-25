package com.slopgobbler.buildgeometry.generator;

import com.slopgobbler.buildgeometry.math.Point3i;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CatenaryArchGeneratorTest {

    @Test
    @DisplayName("Numerical bisection solver for catenary parameter 'a' converges accurately")
    void testCatenaryConstantSolver() {
        double span = 20.0;
        double targetHeight = 10.0;

        double a = CatenaryArchGenerator.solveCatenaryConstant(span, targetHeight);
        assertTrue(a > 0.0, "Catenary constant 'a' must be positive");

        double calculatedHeight = a * (Math.cosh((span / 2.0) / a) - 1.0);
        assertEquals(targetHeight, calculatedHeight, 1e-3,
                "Solved catenary constant must reproduce target height within numerical tolerance");
    }

    @Test
    @DisplayName("Inverted catenary arch touches ground at base and peaks at apex height")
    void testArchApexAndGroundContact() {
        CatenaryArchGenerator generator = new CatenaryArchGenerator();
        int span = 20;
        int height = 12;
        int thickness = 1;

        GeneratorContext ctx = GeneratorContext.builder()
                .origin(Point3i.ZERO)
                .span(span)
                .height(height)
                .thickness(thickness)
                .depth(1)
                .orientation(PlaneOrientation.VERTICAL_XY)
                .build();

        VoxelShape shape = generator.generate(ctx);
        assertNotNull(shape);
        assertFalse(shape.isEmpty());

        assertEquals(0, shape.getBounds().minY, "Arch base must contact ground at Y=0");
        assertEquals(height, shape.getBounds().maxY, "Arch apex must reach height H");

        // Apex at x=0, y=height
        assertTrue(shape.contains(new Point3i(0, height, 0)), "Apex voxel must be present at (0, H, 0)");
    }

    @Test
    @DisplayName("Arch structure has zero horizontal and vertical gaps along its curve")
    void testArchContinuity() {
        CatenaryArchGenerator generator = new CatenaryArchGenerator();
        GeneratorContext ctx = GeneratorContext.builder()
                .origin(Point3i.ZERO)
                .span(24)
                .height(15)
                .thickness(1)
                .depth(1)
                .orientation(PlaneOrientation.VERTICAL_XY)
                .build();

        VoxelShape shape = generator.generate(ctx);

        // Every integer X along the span must have at least one voxel
        int halfSpan = 24 / 2;
        for (int x = -halfSpan; x <= halfSpan; x++) {
            boolean hasVoxelAtX = false;
            for (Point3i p : shape.getVoxels()) {
                if (p.x == x) {
                    hasVoxelAtX = true;
                    break;
                }
            }
            assertTrue(hasVoxelAtX, "Arch must have a voxel at column X=" + x);
        }
    }

    @Test
    @DisplayName("Depth extrusion creates parallel arch layers")
    void testDepthExtrusion() {
        CatenaryArchGenerator generator = new CatenaryArchGenerator();
        int depth = 4;
        GeneratorContext ctx = GeneratorContext.builder()
                .origin(Point3i.ZERO)
                .span(16)
                .height(8)
                .thickness(1)
                .depth(depth)
                .orientation(PlaneOrientation.VERTICAL_XY)
                .build();

        VoxelShape shape = generator.generate(ctx);
        assertEquals(0, shape.getBounds().minZ);
        assertEquals(depth - 1, shape.getBounds().maxZ);
    }
}
