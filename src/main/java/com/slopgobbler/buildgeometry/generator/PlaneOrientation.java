package com.slopgobbler.buildgeometry.generator;

import com.slopgobbler.buildgeometry.math.Point3i;

/**
 * 2D projection plane orientations for planar curves (circles, ellipses, spirals, arches).
 */
public enum PlaneOrientation {
    HORIZONTAL_XZ("Horizontal (XZ)", "Floor / Ceiling horizontal plane"),
    VERTICAL_XY("Vertical (XY)", "Facing North / South (Z constant)"),
    VERTICAL_YZ("Vertical (YZ)", "Facing East / West (X constant)");

    private final String displayName;
    private final String description;

    PlaneOrientation(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Maps planar coordinate (u, v) centered at origin into world 3D space (x, y, z).
     */
    public Point3i transform(int u, int v, Point3i center) {
        switch (this) {
            case HORIZONTAL_XZ:
                return new Point3i(center.x + u, center.y, center.z + v);
            case VERTICAL_XY:
                return new Point3i(center.x + u, center.y + v, center.z);
            case VERTICAL_YZ:
                return new Point3i(center.x, center.y + v, center.z + u);
            default:
                return new Point3i(center.x + u, center.y, center.z + v);
        }
    }
}
