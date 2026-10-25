package com.slopgobbler.buildgeometry.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.slopgobbler.buildgeometry.generator.GeneratorContext;
import com.slopgobbler.buildgeometry.generator.PlaneOrientation;
import com.slopgobbler.buildgeometry.generator.ShapeType;
import com.slopgobbler.buildgeometry.guide.AngleSnapManager;
import com.slopgobbler.buildgeometry.math.Point3i;
import com.slopgobbler.buildgeometry.render.ColorPalette;
import com.slopgobbler.buildgeometry.render.GhostBlockRenderer;
import com.slopgobbler.buildgeometry.render.LayerSlicer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Global configuration model for BuildGeometry Pro.
 * Persists user presets, active CAD shapes, holographic themes, and snapping options.
 */
public class BuildGeometryConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static BuildGeometryConfig INSTANCE = new BuildGeometryConfig();

    // General
    public boolean projectionEnabled = true;
    public ShapeType activeShapeType = ShapeType.CATENARY_ARCH;
    public PlaneOrientation planeOrientation = PlaneOrientation.VERTICAL_XY;

    // Slicing
    public LayerSlicer.SliceMode sliceMode = LayerSlicer.SliceMode.ALL_LAYERS;
    public int activeLayer = 0;
    public boolean autoAdvance = true;

    // Snapping & Guides
    public boolean snappingEnabled = true;
    public double snapStepDegrees = 45.0;
    public AngleSnapManager.LockMode lockMode = AngleSnapManager.LockMode.NONE;

    // Rendering & Themes
    public GhostBlockRenderer.RenderStyle renderStyle = GhostBlockRenderer.RenderStyle.HYBRID;
    public ColorPalette.Theme colorTheme = ColorPalette.Theme.CYBER_HOLO;
    public float globalAlpha = 0.45f;
    public boolean useHeightGradient = true;
    public double maxRenderDistance = 64.0;

    // Shape Parameters
    public int radius = 12;
    public int radiusX = 16;
    public int radiusY = 10;
    public int height = 14;
    public int span = 24;
    public int thickness = 2;
    public int depth = 3;
    public double turns = 3.0;
    public double pitch = 2.0;
    public boolean filled = false;
    public boolean hollow = true;
    public boolean inverted = false;
    public GeneratorContext.CircleAlgorithm circleAlgorithm = GeneratorContext.CircleAlgorithm.BRESENHAM;
    public GeneratorContext.SpiralType spiralType = GeneratorContext.SpiralType.HELIX;
    public GeneratorContext.PolyhedronType polyhedronType = GeneratorContext.PolyhedronType.ICOSAHEDRON;
    public GeneratorContext.PolyhedronRenderMode polyhedronRenderMode = GeneratorContext.PolyhedronRenderMode.WIREFRAME;

    public static BuildGeometryConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new BuildGeometryConfig();
        }
        return INSTANCE;
    }

    public static void setInstance(BuildGeometryConfig config) {
        INSTANCE = config;
    }

    public GeneratorContext toContext(Point3i origin) {
        return GeneratorContext.builder()
                .origin(origin)
                .orientation(planeOrientation)
                .radius(radius)
                .radiusX(radiusX)
                .radiusY(radiusY)
                .height(height)
                .span(span)
                .thickness(thickness)
                .depth(depth)
                .turns(turns)
                .pitch(pitch)
                .filled(filled)
                .hollow(hollow)
                .inverted(inverted)
                .circleAlgorithm(circleAlgorithm)
                .spiralType(spiralType)
                .polyhedronType(polyhedronType)
                .polyhedronRenderMode(polyhedronRenderMode)
                .build();
    }

    public void save(File configFile) {
        try {
            if (configFile.getParentFile() != null) {
                configFile.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static BuildGeometryConfig load(File configFile) {
        if (!configFile.exists()) {
            BuildGeometryConfig def = new BuildGeometryConfig();
            def.save(configFile);
            return def;
        }

        try (FileReader reader = new FileReader(configFile)) {
            BuildGeometryConfig loaded = GSON.fromJson(reader, BuildGeometryConfig.class);
            return (loaded != null) ? loaded : new BuildGeometryConfig();
        } catch (Exception e) {
            e.printStackTrace();
            return new BuildGeometryConfig();
        }
    }
}
