package com.slopgobbler.buildgeometry.generator;

import com.slopgobbler.buildgeometry.math.Point3i;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ParabolicDomeGeneratorTest {

    @Test
    @DisplayName("Parabolic dome bounds match radius and apex height")
    void testDomeBounds() {
        ParabolicDomeGenerator generator = new ParabolicDomeGenerator();
        int rx = 10;
        int rz = 10;
        int height = 8;

        GeneratorContext ctx = GeneratorContext.builder()
                .origin(Point3i.ZERO)
                .radiusX(rx)
                .radiusY(rz)
                .height(height)
                .hollow(true)
                .thickness(1)
                .build();

        VoxelShape shape = generator.generate(ctx);
        assertNotNull(shape);
        assertFalse(shape.isEmpty());

        assertEquals(-rx, shape.getBounds().minX);
        assertEquals(rx, shape.getBounds().maxX);
        assertEquals(-rz, shape.getBounds().minZ);
        assertEquals(rz, shape.getBounds().maxZ);
        assertEquals(0, shape.getBounds().minY);
        assertEquals(height, shape.getBounds().maxY);

        // Apex voxel must be present
        assertTrue(shape.contains(new Point3i(0, height, 0)));
    }

    @Test
    @DisplayName("Hollow dome has open interior while solid dome is filled")
    void testHollowVsSolid() {
        ParabolicDomeGenerator generator = new ParabolicDomeGenerator();
        GeneratorContext hollowCtx = GeneratorContext.builder()
                .origin(Point3i.ZERO)
                .radiusX(12)
                .radiusY(12)
                .height(10)
                .hollow(true)
                .thickness(1)
                .build();
        VoxelShape hollowShape = generator.generate(hollowCtx);

        GeneratorContext solidCtx = GeneratorContext.builder()
                .origin(Point3i.ZERO)
                .radiusX(12)
                .radiusY(12)
                .height(10)
                .hollow(false)
                .build();
        VoxelShape solidShape = generator.generate(solidCtx);

        // Solid must contain significantly more voxels than hollow shell
        assertTrue(solidShape.size() > hollowShape.size() * 2,
                "Solid dome (" + solidShape.size() + ") should have more voxels than hollow shell (" + hollowShape.size() + ")");

        // Origin at (0, 0, 0) should be filled in solid, but absent in thin hollow dome
        assertTrue(solidShape.contains(Point3i.ZERO));
        assertFalse(hollowShape.contains(Point3i.ZERO));
    }

    @Test
    @DisplayName("Symmetry across X and Z axes")
    void testSymmetry() {
        ParabolicDomeGenerator generator = new ParabolicDomeGenerator();
        GeneratorContext ctx = GeneratorContext.builder()
                .origin(Point3i.ZERO)
                .radiusX(8)
                .radiusY(8)
                .height(6)
                .hollow(true)
                .build();

        VoxelShape shape = generator.generate(ctx);
        for (Point3i p : shape.getVoxels()) {
            assertTrue(shape.contains(new Point3i(-p.x, p.y, p.z)));
            assertTrue(shape.contains(new Point3i(p.x, p.y, -p.z)));
            assertTrue(shape.contains(new Point3i(-p.x, p.y, -p.z)));
        }
    }
}
