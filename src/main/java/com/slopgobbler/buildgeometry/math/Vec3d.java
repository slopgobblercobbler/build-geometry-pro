package com.slopgobbler.buildgeometry.math;

import java.util.Objects;

/**
 * Immutable 3D continuous double vector for geometric CAD calculations,
 * parametric curves, matrix rotations, and angle snapping.
 */
public final class Vec3d {
    public static final Vec3d ZERO = new Vec3d(0.0, 0.0, 0.0);
    public static final Vec3d UNIT_X = new Vec3d(1.0, 0.0, 0.0);
    public static final Vec3d UNIT_Y = new Vec3d(0.0, 1.0, 0.0);
    public static final Vec3d UNIT_Z = new Vec3d(0.0, 0.0, 1.0);

    public final double x;
    public final double y;
    public final double z;

    public Vec3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Vec3d of(double x, double y, double z) {
        return new Vec3d(x, y, z);
    }

    public Vec3d add(double dx, double dy, double dz) {
        return new Vec3d(this.x + dx, this.y + dy, this.z + dz);
    }

    public Vec3d add(Vec3d other) {
        return add(other.x, other.y, other.z);
    }

    public Vec3d subtract(double dx, double dy, double dz) {
        return new Vec3d(this.x - dx, this.y - dy, this.z - dz);
    }

    public Vec3d subtract(Vec3d other) {
        return subtract(other.x, other.y, other.z);
    }

    public Vec3d multiply(double scalar) {
        return new Vec3d(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    public double dot(Vec3d other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public Vec3d cross(Vec3d other) {
        return new Vec3d(
                this.y * other.z - this.z * other.y,
                this.z * other.x - this.x * other.z,
                this.x * other.y - this.y * other.x
        );
    }

    public double lengthSq() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public double length() {
        return Math.sqrt(lengthSq());
    }

    public Vec3d normalize() {
        double len = length();
        if (len < 1e-9) {
            return ZERO;
        }
        return new Vec3d(this.x / len, this.y / len, this.z / len);
    }

    public double distanceSq(Vec3d other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        double dz = this.z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public double distance(Vec3d other) {
        return Math.sqrt(distanceSq(other));
    }

    public Vec3d lerp(Vec3d other, double t) {
        return new Vec3d(
                this.x + (other.x - this.x) * t,
                this.y + (other.y - this.y) * t,
                this.z + (other.z - this.z) * t
        );
    }

    public Vec3d rotateX(double angleRad) {
        double cos = Math.cos(angleRad);
        double sin = Math.sin(angleRad);
        return new Vec3d(this.x, this.y * cos - this.z * sin, this.y * sin + this.z * cos);
    }

    public Vec3d rotateY(double angleRad) {
        double cos = Math.cos(angleRad);
        double sin = Math.sin(angleRad);
        return new Vec3d(this.x * cos + this.z * sin, this.y, -this.x * sin + this.z * cos);
    }

    public Vec3d rotateZ(double angleRad) {
        double cos = Math.cos(angleRad);
        double sin = Math.sin(angleRad);
        return new Vec3d(this.x * cos - this.y * sin, this.x * sin + this.y * cos, this.z);
    }

    /**
     * Rodrigues' rotation formula: rotates this vector around an arbitrary axis by theta radians.
     */
    public Vec3d rotateAround(Vec3d axis, double angleRad) {
        Vec3d k = axis.normalize();
        double cos = Math.cos(angleRad);
        double sin = Math.sin(angleRad);

        // v_rot = v*cos + (k x v)*sin + k*(k . v)*(1 - cos)
        Vec3d term1 = this.multiply(cos);
        Vec3d term2 = k.cross(this).multiply(sin);
        Vec3d term3 = k.multiply(k.dot(this) * (1.0 - cos));
        return term1.add(term2).add(term3);
    }

    public Point3i toPoint3iRound() {
        return new Point3i(
                (int) Math.round(this.x),
                (int) Math.round(this.y),
                (int) Math.round(this.z)
        );
    }

    public Point3i toPoint3iFloor() {
        return new Point3i(
                (int) Math.floor(this.x),
                (int) Math.floor(this.y),
                (int) Math.floor(this.z)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vec3d vec3d = (Vec3d) o;
        return Double.compare(vec3d.x, x) == 0 &&
                Double.compare(vec3d.y, y) == 0 &&
                Double.compare(vec3d.z, z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return String.format("Vec3d(%.3f, %.3f, %.3f)", x, y, z);
    }
}
