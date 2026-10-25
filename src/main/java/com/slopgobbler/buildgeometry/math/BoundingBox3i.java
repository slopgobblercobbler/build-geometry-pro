package com.slopgobbler.buildgeometry.math;

import java.util.Objects;

/**
 * Axis-aligned integer bounding box in 3D voxel space [minX..maxX, minY..maxY, minZ..maxZ] inclusive.
 */
public final class BoundingBox3i {
    public static final BoundingBox3i EMPTY = new BoundingBox3i(0, 0, 0, 0, 0, 0);

    public final int minX;
    public final int minY;
    public final int minZ;
    public final int maxX;
    public final int maxY;
    public final int maxZ;

    public BoundingBox3i(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.minX = Math.min(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxX = Math.max(minX, maxX);
        this.maxY = Math.max(minY, maxY);
        this.maxZ = Math.max(minZ, maxZ);
    }

    public static BoundingBox3i of(Point3i p1, Point3i p2) {
        return new BoundingBox3i(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z);
    }

    public static BoundingBox3i single(Point3i p) {
        return new BoundingBox3i(p.x, p.y, p.z, p.x, p.y, p.z);
    }

    public boolean contains(Point3i point) {
        return point.x >= minX && point.x <= maxX &&
               point.y >= minY && point.y <= maxY &&
               point.z >= minZ && point.z <= maxZ;
    }

    public boolean contains(int x, int y, int z) {
        return x >= minX && x <= maxX &&
               y >= minY && y <= maxY &&
               z >= minZ && z <= maxZ;
    }

    public boolean intersects(BoundingBox3i other) {
        return this.maxX >= other.minX && this.minX <= other.maxX &&
               this.maxY >= other.minY && this.minY <= other.maxY &&
               this.maxZ >= other.minZ && this.minZ <= other.maxZ;
    }

    public BoundingBox3i expand(int delta) {
        return new BoundingBox3i(
                minX - delta, minY - delta, minZ - delta,
                maxX + delta, maxY + delta, maxZ + delta
        );
    }

    public BoundingBox3i union(Point3i point) {
        return new BoundingBox3i(
                Math.min(minX, point.x),
                Math.min(minY, point.y),
                Math.min(minZ, point.z),
                Math.max(maxX, point.x),
                Math.max(maxY, point.y),
                Math.max(maxZ, point.z)
        );
    }

    public BoundingBox3i union(BoundingBox3i other) {
        return new BoundingBox3i(
                Math.min(this.minX, other.minX),
                Math.min(this.minY, other.minY),
                Math.min(this.minZ, other.minZ),
                Math.max(this.maxX, other.maxX),
                Math.max(this.maxY, other.maxY),
                Math.max(this.maxZ, other.maxZ)
        );
    }

    public int getSizeX() {
        return maxX - minX + 1;
    }

    public int getSizeY() {
        return maxY - minY + 1;
    }

    public int getSizeZ() {
        return maxZ - minZ + 1;
    }

    public long getVolume() {
        return (long) getSizeX() * getSizeY() * getSizeZ();
    }

    public Vec3d getCenter() {
        return new Vec3d(
                (minX + maxX) * 0.5,
                (minY + maxY) * 0.5,
                (minZ + maxZ) * 0.5
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoundingBox3i that = (BoundingBox3i) o;
        return minX == that.minX && minY == that.minY && minZ == that.minZ &&
               maxX == that.maxX && maxY == that.maxY && maxZ == that.maxZ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public String toString() {
        return String.format("[%d..%d, %d..%d, %d..%d]", minX, maxX, minY, maxY, minZ, maxZ);
    }
}
