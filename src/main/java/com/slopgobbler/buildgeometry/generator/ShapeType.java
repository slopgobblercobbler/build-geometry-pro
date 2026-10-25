package com.slopgobbler.buildgeometry.generator;

/**
 * Geometric shape generator archetypes supported by BuildGeometry Pro.
 */
public enum ShapeType {
    CIRCLE("Circle", "Bresenham & Midpoint circular disc or ring"),
    ELLIPSE("Ellipse", "Parametric 2D/3D ellipse with dual semi-axes"),
    CATENARY_ARCH("Catenary Arch", "Ideal self-supporting compression arch y = a*cosh(x/a)"),
    PARABOLIC_DOME("Parabolic Dome", "Paraboloid dome surface with hollow shell support"),
    SPHERE("Sphere", "Spherical hollow shell with configurable wall thickness"),
    SPIRAL("Spiral", "Archimedean, Logarithmic, Helical, and Staircase spirals"),
    POLYHEDRON("Polyhedron", "Platonic solids, pyramids, prisms, and geodesic wireframes");

    private final String displayName;
    private final String description;

    ShapeType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
