package com.slopgobbler.buildgeometry.generator;

import com.slopgobbler.buildgeometry.math.Point3i;

/**
 * Parameter configuration container passed to shape generators.
 * Uses builder pattern for fluent setup and immutable execution.
 */
public final class GeneratorContext {
    public enum CircleAlgorithm {
        BRESENHAM,
        MIDPOINT
    }

    public enum SpiralType {
        ARCHIMEDEAN,
        LOGARITHMIC,
        HELIX,
        DOUBLE_HELIX,
        STAIR_SPIRAL
    }

    public enum PolyhedronType {
        TETRAHEDRON,
        OCTAHEDRON,
        CUBE,
        ICOSAHEDRON,
        DODECAHEDRON,
        PYRAMID,
        PRISM
    }

    public enum PolyhedronRenderMode {
        WIREFRAME,
        HOLLOW_SHELL,
        SOLID
    }

    private final Point3i origin;
    private final PlaneOrientation orientation;
    private final int radius;
    private final int radiusX;
    private final int radiusY;
    private final int height;
    private final int span;
    private final int thickness;
    private final int depth;
    private final double turns;
    private final double pitch;
    private final boolean filled;
    private final boolean hollow;
    private final boolean inverted;
    private final CircleAlgorithm circleAlgorithm;
    private final SpiralType spiralType;
    private final PolyhedronType polyhedronType;
    private final PolyhedronRenderMode polyhedronRenderMode;

    private GeneratorContext(Builder builder) {
        this.origin = builder.origin;
        this.orientation = builder.orientation;
        this.radius = builder.radius;
        this.radiusX = builder.radiusX;
        this.radiusY = builder.radiusY;
        this.height = builder.height;
        this.span = builder.span;
        this.thickness = builder.thickness;
        this.depth = builder.depth;
        this.turns = builder.turns;
        this.pitch = builder.pitch;
        this.filled = builder.filled;
        this.hollow = builder.hollow;
        this.inverted = builder.inverted;
        this.circleAlgorithm = builder.circleAlgorithm;
        this.spiralType = builder.spiralType;
        this.polyhedronType = builder.polyhedronType;
        this.polyhedronRenderMode = builder.polyhedronRenderMode;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Point3i getOrigin() { return origin; }
    public PlaneOrientation getOrientation() { return orientation; }
    public int getRadius() { return radius; }
    public int getRadiusX() { return radiusX; }
    public int getRadiusY() { return radiusY; }
    public int getHeight() { return height; }
    public int getSpan() { return span; }
    public int getThickness() { return thickness; }
    public int getDepth() { return depth; }
    public double getTurns() { return turns; }
    public double getPitch() { return pitch; }
    public boolean isFilled() { return filled; }
    public boolean isHollow() { return hollow; }
    public boolean isInverted() { return inverted; }
    public CircleAlgorithm getCircleAlgorithm() { return circleAlgorithm; }
    public SpiralType getSpiralType() { return spiralType; }
    public PolyhedronType getPolyhedronType() { return polyhedronType; }
    public PolyhedronRenderMode getPolyhedronRenderMode() { return polyhedronRenderMode; }

    public static class Builder {
        private Point3i origin = Point3i.ZERO;
        private PlaneOrientation orientation = PlaneOrientation.HORIZONTAL_XZ;
        private int radius = 10;
        private int radiusX = 15;
        private int radiusY = 8;
        private int height = 12;
        private int span = 20;
        private int thickness = 1;
        private int depth = 1;
        private double turns = 3.0;
        private double pitch = 2.0;
        private boolean filled = false;
        private boolean hollow = true;
        private boolean inverted = false;
        private CircleAlgorithm circleAlgorithm = CircleAlgorithm.BRESENHAM;
        private SpiralType spiralType = SpiralType.HELIX;
        private PolyhedronType polyhedronType = PolyhedronType.ICOSAHEDRON;
        private PolyhedronRenderMode polyhedronRenderMode = PolyhedronRenderMode.WIREFRAME;

        public Builder origin(Point3i origin) { this.origin = origin; return this; }
        public Builder orientation(PlaneOrientation orientation) { this.orientation = orientation; return this; }
        public Builder radius(int radius) { this.radius = radius; return this; }
        public Builder radiusX(int radiusX) { this.radiusX = radiusX; return this; }
        public Builder radiusY(int radiusY) { this.radiusY = radiusY; return this; }
        public Builder height(int height) { this.height = height; return this; }
        public Builder span(int span) { this.span = span; return this; }
        public Builder thickness(int thickness) { this.thickness = thickness; return this; }
        public Builder depth(int depth) { this.depth = depth; return this; }
        public Builder turns(double turns) { this.turns = turns; return this; }
        public Builder pitch(double pitch) { this.pitch = pitch; return this; }
        public Builder filled(boolean filled) { this.filled = filled; return this; }
        public Builder hollow(boolean hollow) { this.hollow = hollow; return this; }
        public Builder inverted(boolean inverted) { this.inverted = inverted; return this; }
        public Builder circleAlgorithm(CircleAlgorithm circleAlgorithm) { this.circleAlgorithm = circleAlgorithm; return this; }
        public Builder spiralType(SpiralType spiralType) { this.spiralType = spiralType; return this; }
        public Builder polyhedronType(PolyhedronType polyhedronType) { this.polyhedronType = polyhedronType; return this; }
        public Builder polyhedronRenderMode(PolyhedronRenderMode mode) { this.polyhedronRenderMode = mode; return this; }

        public GeneratorContext build() {
            return new GeneratorContext(this);
        }
    }
}
