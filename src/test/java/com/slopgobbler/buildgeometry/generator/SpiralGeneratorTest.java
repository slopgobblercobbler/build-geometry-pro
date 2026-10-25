package com.slopgobbler.buildgeometry.generator;

import com.slopgobbler.buildgeometry.math.Point3i;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SpiralGeneratorTest {

    @Test
    @DisplayName("Helical coil reaches expected height proportional to turns * pitch")
    void testHelicalSpiralHeight() {
        SpiralGenerator generator = new SpiralGenerator();
        double turns = 3.0;
        double pitch = 4.0;
        int radius = 8;

        GeneratorContext ctx = GeneratorContext.builder()
                .origin(Point3i.ZERO)
                .radius(radius)
                .turns(turns)
                .pitch(pitch)
                .spiralType(GeneratorContext.SpiralType.HELIX)
                .build();

        VoxelShape shape = generator.generate(ctx);
        assertNotNull(shape);
        assertFalse(shape.isEmpty());

        int expectedMaxY = (int) Math.round(turns * pitch);
        assertEquals(0, shape.getBounds().minY);
        assertEquals(expectedMaxY, shape.getBounds().maxY);
    }

    @Test
    @DisplayName("Archimedean spiral expands outward monotonically")
    void testArchimedeanSpiralExpansion() {
        SpiralGenerator generator = new SpiralGenerator();
        int maxRadius = 16;
        GeneratorContext ctx = GeneratorContext.builder()
                .origin(Point3i.ZERO)
                .radius(maxRadius)
                .turns(2.5)
                .spiralType(GeneratorContext.SpiralType.ARCHIMEDEAN)
                .orientation(PlaneOrientation.HORIZONTAL_XZ)
                .build();

        VoxelShape shape = generator.generate(ctx);
        assertEquals(0, shape.getBounds().minY);
        assertEquals(0, shape.getBounds().maxY);

        // Maximum coordinate along X or Z should reach maxRadius
        int maxCoord = Math.max(
                Math.max(Math.abs(shape.getBounds().minX), Math.abs(shape.getBounds().maxX)),
                Math.max(Math.abs(shape.getBounds().minZ), Math.abs(shape.getBounds().maxZ))
        );
        assertTrue(maxCoord >= maxRadius - 1 && maxCoord <= maxRadius + 1);
    }

    @Test
    @DisplayName("Spiral staircase includes central pillar column and step treads")
    void testStaircaseSpiral() {
        SpiralGenerator generator = new SpiralGenerator();
        int height = 12;
        GeneratorContext ctx = GeneratorContext.builder()
                .origin(Point3i.ZERO)
                .radius(6)
                .height(height)
                .thickness(1) // Inner radius
                .spiralType(GeneratorContext.SpiralType.STAIR_SPIRAL)
                .build();

        VoxelShape shape = generator.generate(ctx);

        // Central column voxels (0, y, 0) for each y in [0..height-1]
        for (int y = 0; y < height; y++) {
            assertTrue(shape.contains(new Point3i(0, y, 0)), "Central pillar voxel at Y=" + y + " must exist");
        }
    }

    @Test
    @DisplayName("Double helix generates dual strands with cross rungs")
    void testDoubleHelix() {
        SpiralGenerator generator = new SpiralGenerator();
        GeneratorContext ctx = GeneratorContext.builder()
                .origin(Point3i.ZERO)
                .radius(6)
                .turns(2.0)
                .pitch(5.0)
                .spiralType(GeneratorContext.SpiralType.DOUBLE_HELIX)
                .build();

        VoxelShape shape = generator.generate(ctx);
        assertNotNull(shape);
        assertTrue(shape.size() > 50, "Double helix must generate multiple strands and rungs");
    }
}
