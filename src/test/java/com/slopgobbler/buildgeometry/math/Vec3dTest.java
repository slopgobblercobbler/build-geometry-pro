package com.slopgobbler.buildgeometry.math;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Vec3dTest {

    @Test
    @DisplayName("Vector basic operations: add, scale, length, normalize")
    void testVectorBasics() {
        Vec3d v = new Vec3d(3.0, 4.0, 0.0);
        assertEquals(25.0, v.lengthSq(), 1e-6);
        assertEquals(5.0, v.length(), 1e-6);

        Vec3d norm = v.normalize();
        assertEquals(1.0, norm.length(), 1e-6);
        assertEquals(0.6, norm.x, 1e-6);
        assertEquals(0.8, norm.y, 1e-6);
    }

    @Test
    @DisplayName("Linear interpolation (lerp)")
    void testLerp() {
        Vec3d a = new Vec3d(0, 10, 20);
        Vec3d b = new Vec3d(10, 20, 40);

        Vec3d mid = a.lerp(b, 0.5);
        assertEquals(5.0, mid.x, 1e-6);
        assertEquals(15.0, mid.y, 1e-6);
        assertEquals(30.0, mid.z, 1e-6);
    }

    @Test
    @DisplayName("Axis rotations: 90 degrees around Y")
    void testRotationY() {
        Vec3d unitX = new Vec3d(1.0, 0.0, 0.0);
        Vec3d rotated = unitX.rotateY(Math.PI / 2.0);

        assertEquals(0.0, rotated.x, 1e-6);
        assertEquals(0.0, rotated.y, 1e-6);
        assertEquals(-1.0, rotated.z, 1e-6);
    }

    @Test
    @DisplayName("Rodrigues rotation formula: 90 degrees around Z axis")
    void testRodriguesRotation() {
        Vec3d v = new Vec3d(1.0, 0.0, 0.0);
        Vec3d axis = new Vec3d(0.0, 0.0, 1.0);

        Vec3d rotated = v.rotateAround(axis, Math.PI / 2.0);
        assertEquals(0.0, rotated.x, 1e-6);
        assertEquals(1.0, rotated.y, 1e-6);
        assertEquals(0.0, rotated.z, 1e-6);
    }

    @Test
    @DisplayName("Voxel rounding and flooring conversions")
    void testDiscretization() {
        Vec3d v = new Vec3d(2.7, 3.2, -1.8);
        assertEquals(new Point3i(3, 3, -2), v.toPoint3iRound());
        assertEquals(new Point3i(2, 3, -2), v.toPoint3iFloor());
    }
}
