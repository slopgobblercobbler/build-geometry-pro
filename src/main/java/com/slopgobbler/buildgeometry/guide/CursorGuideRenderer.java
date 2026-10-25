package com.slopgobbler.buildgeometry.guide;

import com.slopgobbler.buildgeometry.math.Point3i;
import com.slopgobbler.buildgeometry.math.Vec3d;
import com.slopgobbler.buildgeometry.render.ColorPalette;

/**
 * Generates coordinate readouts, target projection rays, and holographic
 * axis guidelines for angle-snapped construction assistance.
 */
public class CursorGuideRenderer {
    public static final class GuideRayData {
        public final Vec3d start;
        public final Vec3d end;
        public final ColorPalette.Color4f color;

        public GuideRayData(Vec3d start, Vec3d end, ColorPalette.Color4f color) {
            this.start = start;
            this.end = end;
            this.color = color;
        }
    }

    public static final class HudMetrics {
        public final int deltaX;
        public final int deltaY;
        public final int deltaZ;
        public final double distance;
        public final double snappedYaw;
        public final double snappedPitch;
        public final AngleSnapManager.LockMode lockMode;

        public HudMetrics(int deltaX, int deltaY, int deltaZ, double distance, double snappedYaw, double snappedPitch, AngleSnapManager.LockMode lockMode) {
            this.deltaX = deltaX;
            this.deltaY = deltaY;
            this.deltaZ = deltaZ;
            this.distance = distance;
            this.snappedYaw = snappedYaw;
            this.snappedPitch = snappedPitch;
            this.lockMode = lockMode;
        }

        public String formatSummary() {
            return String.format("ΔX:%+d  ΔY:%+d  ΔZ:%+d | Dist:%.1fm | Yaw:%.0f° [%s]",
                    deltaX, deltaY, deltaZ, distance, snappedYaw, lockMode.getDisplayName());
        }
    }

    private final AngleSnapManager snapManager;

    public CursorGuideRenderer(AngleSnapManager snapManager) {
        this.snapManager = snapManager;
    }

    public AngleSnapManager getSnapManager() {
        return snapManager;
    }

    /**
     * Computes the HUD metrics between anchor and the current cursor target point.
     */
    public HudMetrics computeMetrics(Point3i rawTarget, double rawYaw, double rawPitch) {
        Point3i anchor = snapManager.getAnchorPoint();
        Point3i projected = snapManager.projectPoint(rawTarget);
        double snappedYaw = snapManager.snapYaw(rawYaw);
        double snappedPitch = snapManager.snapPitch(rawPitch);

        if (anchor == null || projected == null) {
            return new HudMetrics(0, 0, 0, 0.0, snappedYaw, snappedPitch, snapManager.getLockMode());
        }

        int dx = projected.x - anchor.x;
        int dy = projected.y - anchor.y;
        int dz = projected.z - anchor.z;
        double dist = anchor.distance(projected);

        return new HudMetrics(dx, dy, dz, dist, snappedYaw, snappedPitch, snapManager.getLockMode());
    }

    /**
     * Produces axis guide rays extending from anchor point along active constrained axis.
     */
    public GuideRayData createAxisRay(double rayLength) {
        Point3i anchor = snapManager.getAnchorPoint();
        if (anchor == null || snapManager.getLockMode() == AngleSnapManager.LockMode.NONE) {
            return null;
        }

        Vec3d center = anchor.toBlockCenter();
        Vec3d dir;
        ColorPalette.Color4f color;

        switch (snapManager.getLockMode()) {
            case LOCK_X:
                dir = Vec3d.UNIT_X;
                color = ColorPalette.GUIDE_AXIS_X;
                break;
            case LOCK_Y:
                dir = Vec3d.UNIT_Y;
                color = ColorPalette.GUIDE_AXIS_Y;
                break;
            case LOCK_Z:
                dir = Vec3d.UNIT_Z;
                color = ColorPalette.GUIDE_AXIS_Z;
                break;
            default:
                return null;
        }

        Vec3d start = center.subtract(dir.multiply(rayLength));
        Vec3d end = center.add(dir.multiply(rayLength));
        return new GuideRayData(start, end, color);
    }
}
