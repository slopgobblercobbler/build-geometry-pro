package com.slopgobbler.buildgeometry.math;

import java.util.Objects;

/**
 * Immutable 3D integer coordinate representing a voxel position in world space.
 * Provides vector arithmetic, distances, and coordinate transformations.
 */
public final class Point3i implements Comparable<Point3i> {
    public static final Point3i ZERO = new Point3i(0, 0, 0);

    public final int x;
    public final int y;
    public final int z;

    public Point3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Point3i of(int x, int y, int z) {
        return new Point3i(x, y, z);
    }

    public Point3i add(int dx, int dy, int dz) {
        return new Point3i(this.x + dx, this.y + dy, this.z + dz);
    }

    public Point3i add(Point3i other) {
        return add(other.x, other.y, other.z);
    }

    public Point3i subtract(int dx, int dy, int dz) {
        return new Point3i(this.x - dx, this.y - dy, this.z - dz);
    }

    public Point3i subtract(Point3i other) {
        return subtract(other.x, other.y, other.z);
    }

    public Point3i multiply(int factor) {
        return new Point3i(this.x * factor, this.y * factor, this.z * factor);
    }

    public double distanceSq(Point3i other) {
        int dx = this.x - other.x;
        int dy = this.y - other.y;
        int dz = this.z - other.z;
        return (double) dx * dx + (double) dy * dy + (double) dz * dz;
    }

    public double distance(Point3i other) {
        return Math.sqrt(distanceSq(other));
    }

    public double distanceSq(double ox, double oy, double oz) {
        double dx = this.x - ox;
        double dy = this.y - oy;
        double dz = this.z - oz;
        return dx * dx + dy * dy + dz * dz;
    }

    public double distance(double ox, double oy, double oz) {
        return Math.sqrt(distanceSq(ox, oy, oz));
    }

    public int manhattanDistance(Point3i other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y) + Math.abs(this.z - other.z);
    }

    public int dot(Point3i other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public Point3i cross(Point3i other) {
        return new Point3i(
                this.y * other.z - this.z * other.y,
                this.z * other.x - this.x * other.z,
                this.x * other.y - this.y * other.x
        );
    }

    public Vec3d toVec3d() {
        return new Vec3d(this.x, this.y, this.z);
    }

    public Vec3d toBlockCenter() {
        return new Vec3d(this.x + 0.5, this.y + 0.5, this.z + 0.5);
    }

    @Override
    public int compareTo(Point3i o) {
        if (this.y != o.y) return Integer.compare(this.y, o.y);
        if (this.z != o.z) return Integer.compare(this.z, o.z);
        return Integer.compare(this.x, o.x);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point3i point3i = (Point3i) o;
        return x == point3i.x && y == point3i.y && z == point3i.z;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + z;
        return result;
    }

    @Override
    public String toString() {
        return "Point3i(" + x + ", " + y + ", " + z + ")";
    }
}
