package com.slopgobbler.buildgeometry.math;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Point3iTest {

    @Test
    @DisplayName("Vector arithmetic: add, subtract, multiply")
    void testArithmetic() {
        Point3i p1 = new Point3i(10, 20, 30);
        Point3i p2 = new Point3i(5, -2, 4);

        Point3i sum = p1.add(p2);
        assertEquals(new Point3i(15, 18, 34), sum);

        Point3i diff = p1.subtract(p2);
        assertEquals(new Point3i(5, 22, 26), diff);

        Point3i scaled = p2.multiply(3);
        assertEquals(new Point3i(15, -6, 12), scaled);
    }

    @Test
    @DisplayName("Euclidean and Manhattan distances")
    void testDistances() {
        Point3i p1 = new Point3i(0, 0, 0);
        Point3i p2 = new Point3i(3, 4, 12);

        assertEquals(169.0, p1.distanceSq(p2), 1e-6);
        assertEquals(13.0, p1.distance(p2), 1e-6);
        assertEquals(19, p1.manhattanDistance(p2));
    }

    @Test
    @DisplayName("Dot and cross products")
    void testVectorProducts() {
        Point3i unitX = new Point3i(1, 0, 0);
        Point3i unitY = new Point3i(0, 1, 0);

        assertEquals(0, unitX.dot(unitY));
        assertEquals(new Point3i(0, 0, 1), unitX.cross(unitY));
    }

    @Test
    @DisplayName("Comparison and sorting order: Y then Z then X")
    void testComparable() {
        Point3i p1 = new Point3i(10, 5, 20);
        Point3i p2 = new Point3i(2, 8, 1);
        Point3i p3 = new Point3i(5, 5, 20);

        assertTrue(p1.compareTo(p2) < 0, "Lower Y should come first");
        assertTrue(p3.compareTo(p1) < 0, "Same Y and Z, lower X should come first");
        assertEquals(0, p1.compareTo(new Point3i(10, 5, 20)));
    }

    @Test
    @DisplayName("Equality and HashCode contract")
    void testEquality() {
        Point3i a = new Point3i(42, -99, 100);
        Point3i b = new Point3i(42, -99, 100);
        Point3i c = new Point3i(43, -99, 100);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }
}
