package com.slopgobbler.buildgeometry.generator;

import com.slopgobbler.buildgeometry.math.Point3i;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Central registry mapping each geometric ShapeType to its dedicated generator implementation.
 */
public final class ShapeRegistry {
    private static final Map<ShapeType, ShapeGenerator> GENERATORS = new EnumMap<>(ShapeType.class);

    static {
        register(new CircleGenerator());
        register(new EllipseGenerator());
        register(new CatenaryArchGenerator());
        register(new ParabolicDomeGenerator());
        register(new SphereGenerator());
        register(new SpiralGenerator());
        register(new PolyhedronGenerator());
    }

    private ShapeRegistry() {}

    public static void register(ShapeGenerator generator) {
        GENERATORS.put(generator.getType(), generator);
    }

    public static ShapeGenerator get(ShapeType type) {
        ShapeGenerator gen = GENERATORS.get(type);
        if (gen == null) {
            throw new IllegalArgumentException("No generator registered for shape type: " + type);
        }
        return gen;
    }

    public static Map<ShapeType, ShapeGenerator> getAll() {
        return Collections.unmodifiableMap(GENERATORS);
    }

    public static VoxelShape generate(ShapeType type, GeneratorContext context) {
        return get(type).generate(context);
    }

    /**
     * Creates default context tuned specifically for the given shape type.
     */
    public static GeneratorContext createDefaultContext(ShapeType type, Point3i origin) {
        GeneratorContext.Builder b = GeneratorContext.builder().origin(origin);
        switch (type) {
            case CIRCLE:
                return b.radius(12).orientation(PlaneOrientation.HORIZONTAL_XZ).filled(false).build();
            case ELLIPSE:
                return b.radiusX(18).radiusY(10).orientation(PlaneOrientation.HORIZONTAL_XZ).filled(false).build();
            case CATENARY_ARCH:
                return b.span(24).height(14).thickness(2).depth(3).orientation(PlaneOrientation.VERTICAL_XY).build();
            case PARABOLIC_DOME:
                return b.radiusX(16).radiusY(16).height(12).thickness(1).hollow(true).build();
            case SPHERE:
                return b.radius(14).thickness(1).hollow(true).build();
            case SPIRAL:
                return b.radius(10).pitch(2.5).turns(4.0).spiralType(GeneratorContext.SpiralType.HELIX).build();
            case POLYHEDRON:
                return b.radius(12).polyhedronType(GeneratorContext.PolyhedronType.ICOSAHEDRON)
                        .polyhedronRenderMode(GeneratorContext.PolyhedronRenderMode.WIREFRAME).build();
            default:
                return b.build();
        }
    }
}
