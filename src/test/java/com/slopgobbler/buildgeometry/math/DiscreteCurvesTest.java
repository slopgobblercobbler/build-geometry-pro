package com.slopgobbler.buildgeometry.math;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DiscreteCurvesTest {

    @Test
    @DisplayName("3D Bresenham line connects start and end points without gaps")
    void testLine3DContinuity() {
        Point3i start = new Point3i(0, 0, 0);
        Point3i end = new Point3i(5, 12, 9);

        List<Point3i> line = DiscreteCurves.line3D(start, end);
        assertFalse(line.isEmpty());

        assertEquals(start, line.get(0), "Line must start at start point");
        assertEquals(end, line.get(line.size() - 1), "Line must end at end point");

        // 26-connected check: step distance between consecutive points must not exceed 1 along any axis
        for (int i = 0; i < line.size() - 1; i++) {
            Point3i cur = line.get(i);
            Point3i nxt = line.get(i + 1);

            int dx = Math.abs(nxt.x - cur.x);
            int dy = Math.abs(nxt.y - cur.y);
            int dz = Math.abs(nxt.z - cur.z);

            assertTrue(dx <= 1 && dy <= 1 && dz <= 1,
                    "Step between " + cur + " and " + nxt + " exceeds single-voxel connectivity");
            assertTrue(dx + dy + dz > 0, "No duplicate consecutive points");
        }
    }

    @Test
    @DisplayName("Bresenham line handles degenerate zero-length line")
    void testZeroLengthLine() {
        Point3i pt = new Point3i(7, 8, 9);
        List<Point3i> line = DiscreteCurves.line3D(pt, pt);

        assertEquals(1, line.size());
        assertEquals(pt, line.get(0));
    }
}
