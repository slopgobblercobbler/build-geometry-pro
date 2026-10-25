package com.slopgobbler.buildgeometry.math;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BoundingBox3iTest {

    @Test
    @DisplayName("Bounding box dimensions and volume")
    void testDimensionsAndVolume() {
        BoundingBox3i box = new BoundingBox3i(0, 0, 0, 10, 5, 2);

        assertEquals(11, box.getSizeX());
        assertEquals(6, box.getSizeY());
        assertEquals(3, box.getSizeZ());
        assertEquals(198, box.getVolume());
    }

    @Test
    @DisplayName("Point containment checks")
    void testContains() {
        BoundingBox3i box = new BoundingBox3i(-5, -5, -5, 5, 5, 5);

        assertTrue(box.contains(new Point3i(0, 0, 0)));
        assertTrue(box.contains(new Point3i(-5, 5, 0)));
        assertFalse(box.contains(new Point3i(6, 0, 0)));
        assertFalse(box.contains(new Point3i(0, -6, 0)));
    }

    @Test
    @DisplayName("Box intersection and expansion")
    void testIntersectionAndExpansion() {
        BoundingBox3i b1 = new BoundingBox3i(0, 0, 0, 10, 10, 10);
        BoundingBox3i b2 = new BoundingBox3i(5, 5, 5, 15, 15, 15);
        BoundingBox3i b3 = new BoundingBox3i(20, 20, 20, 30, 30, 30);

        assertTrue(b1.intersects(b2));
        assertFalse(b1.intersects(b3));

        BoundingBox3i expanded = b1.expand(2);
        assertEquals(-2, expanded.minX);
        assertEquals(12, expanded.maxX);
    }
}
