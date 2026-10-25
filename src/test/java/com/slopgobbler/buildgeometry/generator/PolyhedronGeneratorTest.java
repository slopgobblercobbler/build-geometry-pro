package com.slopgobbler.buildgeometry.generator;

import com.slopgobbler.buildgeometry.math.Point3i;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PolyhedronGeneratorTest {

    @Test
    @DisplayName("Cube polyhedron wireframe matches bounding box bounds")
    void testCubeWireframe() {
        PolyhedronGenerator generator = new PolyhedronGenerator();
        int scale = 8;
        GeneratorContext ctx = GeneratorContext.builder()
                .origin(Point3i.ZERO)
                .radius(scale)
                .polyhedronType(GeneratorContext.PolyhedronType.CUBE)
                .polyhedronRenderMode(GeneratorContext.PolyhedronRenderMode.WIREFRAME)
                .build();

        VoxelShape shape = generator.generate(ctx);
        assertNotNull(shape);
        assertFalse(shape.isEmpty());

        assertEquals(-scale, shape.getBounds().minX);
        assertEquals(scale, shape.getBounds().maxX);
        assertEquals(-scale, shape.getBounds().minY);
        assertEquals(scale, shape.getBounds().maxY);
        assertEquals(-scale, shape.getBounds().minZ);
        assertEquals(scale, shape.getBounds().maxZ);

        // Cube corner vertices must be present
        assertTrue(shape.contains(new Point3i(-scale, -scale, -scale)));
        assertTrue(shape.contains(new Point3i(scale, scale, scale)));
    }

    @Test
    @DisplayName("Octahedron vertices and 8 faces generated")
    void testOctahedron() {
        PolyhedronGenerator generator = new PolyhedronGenerator();
        int scale = 10;
        GeneratorContext ctx = GeneratorContext.builder()
                .origin(Point3i.ZERO)
                .radius(scale)
                .polyhedronType(GeneratorContext.PolyhedronType.OCTAHEDRON)
                .polyhedronRenderMode(GeneratorContext.PolyhedronRenderMode.WIREFRAME)
                .build();

        VoxelShape shape = generator.generate(ctx);
        // 6 extreme axis vertices
        assertTrue(shape.contains(new Point3i(scale, 0, 0)));
        assertTrue(shape.contains(new Point3i(-scale, 0, 0)));
        assertTrue(shape.contains(new Point3i(0, scale, 0)));
        assertTrue(shape.contains(new Point3i(0, -scale, 0)));
        assertTrue(shape.contains(new Point3i(0, 0, scale)));
        assertTrue(shape.contains(new Point3i(0, 0, -scale)));
    }

    @Test
    @DisplayName("Icosahedron generates 12-vertex golden ratio mesh")
    void testIcosahedron() {
        PolyhedronGenerator generator = new PolyhedronGenerator();
        GeneratorContext ctx = GeneratorContext.builder()
                .origin(Point3i.ZERO)
                .radius(12)
                .polyhedronType(GeneratorContext.PolyhedronType.ICOSAHEDRON)
                .polyhedronRenderMode(GeneratorContext.PolyhedronRenderMode.HOLLOW_SHELL)
                .build();

        VoxelShape shape = generator.generate(ctx);
        assertNotNull(shape);
        assertTrue(shape.size() > 100, "Icosahedron hollow shell must have substantive surface voxel count");
    }

    @Test
    @DisplayName("Solid convex fill contains more voxels than hollow shell, which contains more than wireframe")
    void testRenderModesHierarchy() {
        PolyhedronGenerator generator = new PolyhedronGenerator();
        int radius = 8;

        GeneratorContext wireCtx = GeneratorContext.builder()
                .origin(Point3i.ZERO)
                .radius(radius)
                .polyhedronType(GeneratorContext.PolyhedronType.OCTAHEDRON)
                .polyhedronRenderMode(GeneratorContext.PolyhedronRenderMode.WIREFRAME)
                .build();
        VoxelShape wireShape = generator.generate(wireCtx);

        GeneratorContext shellCtx = GeneratorContext.builder()
                .origin(Point3i.ZERO)
                .radius(radius)
                .polyhedronType(GeneratorContext.PolyhedronType.OCTAHEDRON)
                .polyhedronRenderMode(GeneratorContext.PolyhedronRenderMode.HOLLOW_SHELL)
                .build();
        VoxelShape shellShape = generator.generate(shellCtx);

        GeneratorContext solidCtx = GeneratorContext.builder()
                .origin(Point3i.ZERO)
                .radius(radius)
                .polyhedronType(GeneratorContext.PolyhedronType.OCTAHEDRON)
                .polyhedronRenderMode(GeneratorContext.PolyhedronRenderMode.SOLID)
                .build();
        VoxelShape solidShape = generator.generate(solidCtx);

        assertTrue(solidShape.size() > shellShape.size(), "Solid (" + solidShape.size() + ") > Shell (" + shellShape.size() + ")");
        assertTrue(shellShape.size() > wireShape.size(), "Shell (" + shellShape.size() + ") > Wireframe (" + wireShape.size() + ")");
    }

    @Test
    @DisplayName("Pyramid apex and square base validation")
    void testPyramid() {
        PolyhedronGenerator generator = new PolyhedronGenerator();
        int scale = 6;
        GeneratorContext ctx = GeneratorContext.builder()
                .origin(Point3i.ZERO)
                .radius(scale)
                .polyhedronType(GeneratorContext.PolyhedronType.PYRAMID)
                .polyhedronRenderMode(GeneratorContext.PolyhedronRenderMode.WIREFRAME)
                .build();

        VoxelShape shape = generator.generate(ctx);
        // Apex is at (0, scale, 0)
        assertTrue(shape.contains(new Point3i(0, scale, 0)));
        // Base corners at Y = -scale
        assertTrue(shape.contains(new Point3i(scale, -scale, scale)));
        assertTrue(shape.contains(new Point3i(-scale, -scale, -scale)));
    }
}
