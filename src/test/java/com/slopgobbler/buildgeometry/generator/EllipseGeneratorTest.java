package com.slopgobbler.buildgeometry.generator;

import com.slopgobbler.buildgeometry.math.Point3i;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EllipseGeneratorTest {

    @Test
    @DisplayName("Bresenham ellipse spans both semi-axes accurately")
    void testEllipseAxesBounds() {
        EllipseGenerator generator = new EllipseGenerator();
        int rx = 15;
        int ry = 7;
        GeneratorContext ctx = GeneratorContext.builder()
                .origin(Point3i.ZERO)
                .radiusX(rx)
                .radiusY(ry)
                .filled(false)
                .orientation(PlaneOrientation.HORIZONTAL_XZ)
                .build();

        VoxelShape shape = generator.generate(ctx);
        assertNotNull(shape);
        assertFalse(shape.isEmpty());

        assertEquals(-rx, shape.getBounds().minX);
        assertEquals(rx, shape.getBounds().maxX);
        assertEquals(-ry, shape.getBounds().minZ);
        assertEquals(ry, shape.getBounds().maxZ);

        // Major axis extremities must be present
        assertTrue(shape.contains(new Point3i(rx, 0, 0)) || shape.contains(new Point3i(rx - 1, 0, 0)));
        assertTrue(shape.contains(new Point3i(0, 0, ry)) || shape.contains(new Point3i(0, 0, ry - 1)));
    }

    @Test
    @DisplayName("4-quadrant symmetry for ellipse voxels")
    void testEllipseSymmetry() {
        EllipseGenerator generator = new EllipseGenerator();
        GeneratorContext ctx = GeneratorContext.builder()
                .origin(Point3i.ZERO)
                .radiusX(12)
                .radiusY(8)
                .filled(false)
                .build();

        VoxelShape shape = generator.generate(ctx);
        for (Point3i p : shape.getVoxels()) {
            assertTrue(shape.contains(new Point3i(-p.x, p.y, p.z)));
            assertTrue(shape.contains(new Point3i(p.x, p.y, -p.z)));
            assertTrue(shape.contains(new Point3i(-p.x, p.y, -p.z)));
        }
    }

    @Test
    @DisplayName("Filled ellipse volume approximates pi * rx * ry")
    void testFilledEllipse() {
        EllipseGenerator generator = new EllipseGenerator();
        int rx = 10;
        int ry = 5;
        GeneratorContext ctx = GeneratorContext.builder()
                .origin(Point3i.ZERO)
                .radiusX(rx)
                .radiusY(ry)
                .filled(true)
                .build();

        VoxelShape shape = generator.generate(ctx);
        assertTrue(shape.contains(Point3i.ZERO));

        // Discrete ellipse area approximates pi * (rx+0.5) * (ry+0.5) = ~181.4
        assertTrue(shape.size() >= 135 && shape.size() <= 200,
                "Voxel count (" + shape.size() + ") should approximate pi * rx * ry");
    }
}
