package com.slopgobbler.buildgeometry.math;

import java.util.*;

/**
 * Fundamental discrete algorithms for rasterizing curves, lines, circles,
 * ellipses, and polygons onto the 3D voxel lattice.
 */
public final class DiscreteCurves {
    private DiscreteCurves() {}

    /**
     * 3D Bresenham line algorithm connecting two voxel endpoints inclusively.
     * Guaranteed 26-connected voxel trajectory.
     */
    public static List<Point3i> line3D(Point3i start, Point3i end) {
        List<Point3i> result = new ArrayList<>();
        int x1 = start.x, y1 = start.y, z1 = start.z;
        int x2 = end.x, y2 = end.y, z2 = end.z;

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int dz = Math.abs(z2 - z1);

        int xs = (x2 > x1) ? 1 : -1;
        int ys = (y2 > y1) ? 1 : -1;
        int zs = (z2 > z1) ? 1 : -1;

        int curX = x1;
        int curY = y1;
        int curZ = z1;

        result.add(new Point3i(curX, curY, curZ));

        // Driving axis determination
        if (dx >= dy && dx >= dz) {
            int p1 = 2 * dy - dx;
            int p2 = 2 * dz - dx;
            while (curX != x2) {
                curX += xs;
                if (p1 >= 0) {
                    curY += ys;
                    p1 -= 2 * dx;
                }
                if (p2 >= 0) {
                    curZ += zs;
                    p2 -= 2 * dx;
                }
                p1 += 2 * dy;
                p2 += 2 * dz;
                result.add(new Point3i(curX, curY, curZ));
            }
        } else if (dy >= dx && dy >= dz) {
            int p1 = 2 * dx - dy;
            int p2 = 2 * dz - dy;
            while (curY != y2) {
                curY += ys;
                if (p1 >= 0) {
                    curX += xs;
                    p1 -= 2 * dy;
                }
                if (p2 >= 0) {
                    curZ += zs;
                    p2 -= 2 * dy;
                }
                p1 += 2 * dx;
                p2 += 2 * dz;
                result.add(new Point3i(curX, curY, curZ));
            }
        } else {
            int p1 = 2 * dy - dz;
            int p2 = 2 * dx - dz;
            while (curZ != z2) {
                curZ += zs;
                if (p1 >= 0) {
                    curY += ys;
                    p1 -= 2 * dz;
                }
                if (p2 >= 0) {
                    curX += xs;
                    p2 -= 2 * dz;
                }
                p1 += 2 * dy;
                p2 += 2 * dx;
                result.add(new Point3i(curX, curY, curZ));
            }
        }

        return result;
    }

    /**
     * Bresenham circle algorithm in 2D (u, v) centered at (0, 0).
     * If filled is true, returns all voxels inside the disk; otherwise returns the 1-voxel circumference.
     */
    public static Set<Point3i> bresenhamCircle2D(int radius, boolean filled) {
        Set<Point3i> points = new LinkedHashSet<>();
        if (radius < 0) return points;
        if (radius == 0) {
            points.add(Point3i.ZERO);
            return points;
        }

        int x = 0;
        int y = radius;
        int d = 3 - 2 * radius;

        while (x <= y) {
            addCircleSymmetric(points, x, y, filled);
            if (d < 0) {
                d = d + 4 * x + 6;
            } else {
                d = d + 4 * (x - y) + 10;
                y--;
            }
            x++;
        }

        return points;
    }

    /**
     * Midpoint circle algorithm in 2D (u, v) centered at (0, 0).
     * Decision variable initialized to 1 - radius.
     */
    public static Set<Point3i> midpointCircle2D(int radius, boolean filled) {
        Set<Point3i> points = new LinkedHashSet<>();
        if (radius < 0) return points;
        if (radius == 0) {
            points.add(Point3i.ZERO);
            return points;
        }

        int x = 0;
        int y = radius;
        int p = 1 - radius;

        while (x <= y) {
            addCircleSymmetric(points, x, y, filled);
            x++;
            if (p < 0) {
                p += 2 * x + 1;
            } else {
                y--;
                p += 2 * (x - y) + 1;
            }
        }

        return points;
    }

    private static void addCircleSymmetric(Set<Point3i> points, int x, int y, boolean filled) {
        if (filled) {
            // Fill horizontal spans between symmetric coordinates
            fillSpan(points, -x, x, y);
            fillSpan(points, -x, x, -y);
            fillSpan(points, -y, y, x);
            fillSpan(points, -y, y, -x);
        } else {
            // 8-octant symmetry points
            points.add(new Point3i(x, 0, y));
            points.add(new Point3i(-x, 0, y));
            points.add(new Point3i(x, 0, -y));
            points.add(new Point3i(-x, 0, -y));
            points.add(new Point3i(y, 0, x));
            points.add(new Point3i(-y, 0, x));
            points.add(new Point3i(y, 0, -x));
            points.add(new Point3i(-y, 0, -x));
        }
    }

    private static void fillSpan(Set<Point3i> points, int minU, int maxU, int v) {
        for (int u = minU; u <= maxU; u++) {
            points.add(new Point3i(u, 0, v));
        }
    }

    /**
     * Bresenham / Midpoint Ellipse Algorithm in 2D with semi-major axis rx and semi-minor axis ry.
     */
    public static Set<Point3i> bresenhamEllipse2D(int rx, int ry, boolean filled) {
        Set<Point3i> points = new LinkedHashSet<>();
        if (rx < 0 || ry < 0) return points;
        if (rx == 0 && ry == 0) {
            points.add(Point3i.ZERO);
            return points;
        }
        if (rx == 0) {
            for (int y = -ry; y <= ry; y++) points.add(new Point3i(0, 0, y));
            return points;
        }
        if (ry == 0) {
            for (int x = -rx; x <= rx; x++) points.add(new Point3i(x, 0, 0));
            return points;
        }

        long rx2 = (long) rx * rx;
        long ry2 = (long) ry * ry;
        long twoRx2 = 2 * rx2;
        long twoRy2 = 2 * ry2;

        // Region 1: slope > -1
        int x = 0;
        int y = ry;
        long px = 0;
        long py = twoRx2 * y;
        long p = Math.round(ry2 - (rx2 * ry) + (0.25 * rx2));

        while (px < py) {
            addEllipseSymmetric(points, x, y, filled);
            x++;
            px += twoRy2;
            if (p < 0) {
                p += ry2 + px;
            } else {
                y--;
                py -= twoRx2;
                p += ry2 + px - py;
            }
        }

        // Region 2: slope < -1
        p = Math.round(ry2 * (x + 0.5) * (x + 0.5) + rx2 * (y - 1.0) * (y - 1.0) - rx2 * ry2);
        while (y >= 0) {
            addEllipseSymmetric(points, x, y, filled);
            y--;
            py -= twoRx2;
            if (p > 0) {
                p += rx2 - py;
            } else {
                x++;
                px += twoRy2;
                p += rx2 - py + px;
            }
        }

        return points;
    }

    private static void addEllipseSymmetric(Set<Point3i> points, int x, int y, boolean filled) {
        if (filled) {
            for (int u = -x; u <= x; u++) {
                points.add(new Point3i(u, 0, y));
                points.add(new Point3i(u, 0, -y));
            }
        } else {
            points.add(new Point3i(x, 0, y));
            points.add(new Point3i(-x, 0, y));
            points.add(new Point3i(x, 0, -y));
            points.add(new Point3i(-x, 0, -y));
        }
    }
}
