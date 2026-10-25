package com.slopgobbler.buildgeometry.guide;

import com.slopgobbler.buildgeometry.math.Point3i;
import com.slopgobbler.buildgeometry.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AngleSnapManagerTest {

    @Test
    @DisplayName("Angle snapping rounds correctly to nearest step multiple")
    void testAngleSnapping() {
        assertEquals(45.0, AngleSnapManager.snapAngle(42.0, 45.0), 1e-6);
        assertEquals(45.0, AngleSnapManager.snapAngle(47.0, 45.0), 1e-6);
        assertEquals(90.0, AngleSnapManager.snapAngle(88.0, 90.0), 1e-6);
        assertEquals(0.0, AngleSnapManager.snapAngle(358.0, 45.0), 1e-6);
        assertEquals(15.0, AngleSnapManager.snapAngle(12.0, 15.0), 1e-6);
        assertEquals(30.0, AngleSnapManager.snapAngle(34.0, 15.0), 1e-6);
    }

    @Test
    @DisplayName("Axis locking constraints: LOCK_X locks Y and Z")
    void testLockXConstraint() {
        AngleSnapManager snap = new AngleSnapManager();
        Point3i anchor = new Point3i(100, 64, -200);
        snap.setAnchorPoint(anchor);
        snap.setLockMode(AngleSnapManager.LockMode.LOCK_X);

        Point3i target = new Point3i(125, 70, -180);
        Point3i projected = snap.projectPoint(target);

        assertEquals(125, projected.x, "X coordinate should be retained");
        assertEquals(64, projected.y, "Y coordinate must be locked to anchor");
        assertEquals(-200, projected.z, "Z coordinate must be locked to anchor");
    }

    @Test
    @DisplayName("Axis locking constraints: LOCK_Y locks X and Z")
    void testLockYConstraint() {
        AngleSnapManager snap = new AngleSnapManager();
        Point3i anchor = new Point3i(100, 64, -200);
        snap.setAnchorPoint(anchor);
        snap.setLockMode(AngleSnapManager.LockMode.LOCK_Y);

        Point3i target = new Point3i(125, 80, -180);
        Point3i projected = snap.projectPoint(target);

        assertEquals(100, projected.x);
        assertEquals(80, projected.y);
        assertEquals(-200, projected.z);
    }

    @Test
    @DisplayName("Plane locking: LOCK_PLANE_XZ locks Y to anchor while X and Z move freely")
    void testLockPlaneXZ() {
        AngleSnapManager snap = new AngleSnapManager();
        Point3i anchor = new Point3i(0, 72, 0);
        snap.setAnchorPoint(anchor);
        snap.setLockMode(AngleSnapManager.LockMode.LOCK_PLANE_XZ);

        Point3i target = new Point3i(15, 99, -40);
        Point3i projected = snap.projectPoint(target);

        assertEquals(15, projected.x);
        assertEquals(72, projected.y, "Y must be locked to 72");
        assertEquals(-40, projected.z);
    }

    @Test
    @DisplayName("Unit look vector is normalized and points in expected direction")
    void testLookVector() {
        // Yaw 0, Pitch 0 -> Facing South (positive Z)
        Vec3d look = AngleSnapManager.getLookVector(0.0, 0.0);
        assertEquals(1.0, look.length(), 1e-6);

        // Yaw 0, Pitch 90 -> Looking straight up
        Vec3d up = AngleSnapManager.getLookVector(0.0, 90.0);
        assertEquals(1.0, up.y, 1e-6);
        assertEquals(0.0, up.x, 1e-6);
        assertEquals(0.0, up.z, 1e-6);
    }
}
