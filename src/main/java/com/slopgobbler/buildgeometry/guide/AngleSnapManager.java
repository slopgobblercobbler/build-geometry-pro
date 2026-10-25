package com.slopgobbler.buildgeometry.guide;

import com.slopgobbler.buildgeometry.math.Point3i;
import com.slopgobbler.buildgeometry.math.Vec3d;

/**
 * Precision angle snapping and axis locking system for architectural construction.
 * Snaps cursor orientation to configurable angular increments (15°, 30°, 45°, 90°)
 * and projects player placement vectors onto locked axes or 2D planes.
 */
public class AngleSnapManager {
    public enum LockMode {
        NONE("Free Cursor", "No axis constraint"),
        LOCK_X("X-Axis Lock", "Constrain movement exclusively to East/West axis"),
        LOCK_Y("Y-Axis Lock", "Constrain movement exclusively to Vertical axis"),
        LOCK_Z("Z-Axis Lock", "Constrain movement exclusively to North/South axis"),
        LOCK_PLANE_XZ("XZ-Plane Lock", "Constrain to horizontal floor plane (Y fixed)"),
        LOCK_PLANE_XY("XY-Plane Lock", "Constrain to vertical wall plane (Z fixed)"),
        LOCK_PLANE_YZ("YZ-Plane Lock", "Constrain to vertical cross-wall plane (X fixed)");

        private final String displayName;
        private final String description;

        LockMode(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }

    private boolean snappingEnabled = true;
    private double snapStepDegrees = 45.0; // 15.0, 30.0, 45.0, 90.0
    private LockMode lockMode = LockMode.NONE;
    private Point3i anchorPoint = null;

    public AngleSnapManager() {}

    public boolean isSnappingEnabled() { return snappingEnabled; }
    public void setSnappingEnabled(boolean snappingEnabled) { this.snappingEnabled = snappingEnabled; }

    public double getSnapStepDegrees() { return snapStepDegrees; }
    public void setSnapStepDegrees(double snapStepDegrees) {
        if (snapStepDegrees > 0.0) {
            this.snapStepDegrees = snapStepDegrees;
        }
    }

    public LockMode getLockMode() { return lockMode; }
    public void setLockMode(LockMode lockMode) { this.lockMode = lockMode; }

    public Point3i getAnchorPoint() { return anchorPoint; }
    public void setAnchorPoint(Point3i anchorPoint) { this.anchorPoint = anchorPoint; }

    /**
     * Cycles through axis locking modes.
     */
    public void cycleLockMode() {
        LockMode[] modes = LockMode.values();
        this.lockMode = modes[(this.lockMode.ordinal() + 1) % modes.length];
    }

    /**
     * Cycles through standard angle snap presets (15°, 30°, 45°, 90°).
     */
    public void cycleSnapStep() {
        if (snapStepDegrees >= 90.0) snapStepDegrees = 15.0;
        else if (snapStepDegrees >= 45.0) snapStepDegrees = 90.0;
        else if (snapStepDegrees >= 30.0) snapStepDegrees = 45.0;
        else snapStepDegrees = 30.0;
    }

    /**
     * Snaps an angle in degrees to the nearest multiple of the snap step.
     */
    public static double snapAngle(double degrees, double step) {
        if (step <= 0.0) return degrees;
        // Normalize to [0..360)
        double norm = ((degrees % 360.0) + 360.0) % 360.0;
        double snapped = Math.round(norm / step) * step;
        return (snapped >= 360.0) ? 0.0 : snapped;
    }

    /**
     * Snaps yaw (horizontal rotation) to the active angle step.
     */
    public double snapYaw(double yaw) {
        if (!snappingEnabled) return yaw;
        return snapAngle(yaw, snapStepDegrees);
    }

    /**
     * Snaps pitch to horizontal (0°), 30°, 45°, 60°, or 90° zenith/nadir.
     */
    public double snapPitch(double pitch) {
        if (!snappingEnabled) return pitch;
        double sign = Math.signum(pitch);
        double absPitch = Math.abs(pitch);
        double snapped = snapAngle(absPitch, snapStepDegrees);
        return Math.min(90.0, snapped) * sign;
    }

    /**
     * Projects a target position onto the locked axis or plane passing through anchor.
     */
    public Point3i projectPoint(Point3i target) {
        if (lockMode == LockMode.NONE || anchorPoint == null || target == null) {
            return target;
        }

        switch (lockMode) {
            case LOCK_X:
                // Only X varies; Y and Z locked to anchor
                return new Point3i(target.x, anchorPoint.y, anchorPoint.z);

            case LOCK_Y:
                // Only Y varies; X and Z locked to anchor
                return new Point3i(anchorPoint.x, target.y, anchorPoint.z);

            case LOCK_Z:
                // Only Z varies; X and Y locked to anchor
                return new Point3i(anchorPoint.x, anchorPoint.y, target.z);

            case LOCK_PLANE_XZ:
                // Horizontal plane: Y locked to anchor
                return new Point3i(target.x, anchorPoint.y, target.z);

            case LOCK_PLANE_XY:
                // Vertical North/South wall: Z locked to anchor
                return new Point3i(target.x, target.y, anchorPoint.z);

            case LOCK_PLANE_YZ:
                // Vertical East/West wall: X locked to anchor
                return new Point3i(anchorPoint.x, target.y, target.z);

            default:
                return target;
        }
    }

    /**
     * Converts snapped yaw/pitch to a 3D unit look vector.
     */
    public static Vec3d getLookVector(double yawDegrees, double pitchDegrees) {
        double yawRad = Math.toRadians(-yawDegrees - 90.0);
        double pitchRad = Math.toRadians(pitchDegrees);

        double cosPitch = Math.cos(pitchRad);
        double x = cosPitch * Math.cos(yawRad);
        double y = Math.sin(pitchRad);
        double z = cosPitch * Math.sin(yawRad);

        return new Vec3d(x, y, z).normalize();
    }
}
