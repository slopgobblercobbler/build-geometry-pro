package com.slopgobbler.buildgeometry.generator;

import com.slopgobbler.buildgeometry.math.BoundingBox3i;
import com.slopgobbler.buildgeometry.math.Point3i;

import java.util.*;

/**
 * Result container storing voxel coordinates produced by a geometric generator.
 * Provides spatial bounding boxes, Y-sliced indexing, and layer progress tracking.
 */
public final class VoxelShape {
    private final ShapeType type;
    private final Point3i origin;
    private final Set<Point3i> voxels;
    private final BoundingBox3i bounds;
    private final Map<Integer, List<Point3i>> voxelsByY;
    private final List<Integer> sortedYLevels;

    public VoxelShape(ShapeType type, Point3i origin, Collection<Point3i> points) {
        this.type = type;
        this.origin = origin;
        this.voxels = Collections.unmodifiableSet(new LinkedHashSet<>(points));

        if (this.voxels.isEmpty()) {
            this.bounds = BoundingBox3i.single(origin);
            this.voxelsByY = Collections.emptyMap();
            this.sortedYLevels = Collections.emptyList();
            return;
        }

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        Map<Integer, List<Point3i>> byY = new TreeMap<>();

        for (Point3i p : this.voxels) {
            if (p.x < minX) minX = p.x;
            if (p.y < minY) minY = p.y;
            if (p.z < minZ) minZ = p.z;
            if (p.x > maxX) maxX = p.x;
            if (p.y > maxY) maxY = p.y;
            if (p.z > maxZ) maxZ = p.z;

            byY.computeIfAbsent(p.y, k -> new ArrayList<>()).add(p);
        }

        this.bounds = new BoundingBox3i(minX, minY, minZ, maxX, maxY, maxZ);

        Map<Integer, List<Point3i>> unmodifiableByY = new TreeMap<>();
        for (Map.Entry<Integer, List<Point3i>> entry : byY.entrySet()) {
            unmodifiableByY.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
        }
        this.voxelsByY = Collections.unmodifiableMap(unmodifiableByY);
        this.sortedYLevels = Collections.unmodifiableList(new ArrayList<>(unmodifiableByY.keySet()));
    }

    public ShapeType getType() {
        return type;
    }

    public Point3i getOrigin() {
        return origin;
    }

    public Set<Point3i> getVoxels() {
        return voxels;
    }

    public int size() {
        return voxels.size();
    }

    public boolean isEmpty() {
        return voxels.isEmpty();
    }

    public boolean contains(Point3i point) {
        return voxels.contains(point);
    }

    public BoundingBox3i getBounds() {
        return bounds;
    }

    public List<Point3i> getLayer(int y) {
        return voxelsByY.getOrDefault(y, Collections.emptyList());
    }

    public Map<Integer, List<Point3i>> getVoxelsByY() {
        return voxelsByY;
    }

    public List<Integer> getSortedYLevels() {
        return sortedYLevels;
    }

    public int getMinY() {
        return bounds.minY;
    }

    public int getMaxY() {
        return bounds.maxY;
    }

    public int getHeight() {
        return bounds.getSizeY();
    }
}
