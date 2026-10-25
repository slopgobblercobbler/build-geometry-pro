package com.slopgobbler.buildgeometry.generator;

import com.slopgobbler.buildgeometry.math.DiscreteCurves;
import com.slopgobbler.buildgeometry.math.Point3i;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Multi-mode algorithmic spiral voxel generator:
 * - Archimedean Spiral: r = a + b * theta
 * - Logarithmic Spiral: r = a * exp(k * theta)
 * - 3D Helical Coil: x = R*cos(theta), z = R*sin(theta), y = pitch * theta
 * - Double Helix: Paired helical strands with transverse connecting base rungs
 * - Staircase Spiral: Architectural winding staircase with step tread depth and central column
 */
public class SpiralGenerator implements ShapeGenerator {

    @Override
    public ShapeType getType() {
        return ShapeType.SPIRAL;
    }

    @Override
    public VoxelShape generate(GeneratorContext ctx) {
        GeneratorContext.SpiralType spiralType = ctx.getSpiralType();
        switch (spiralType) {
            case ARCHIMEDEAN:
                return generateArchimedean(ctx);
            case LOGARITHMIC:
                return generateLogarithmic(ctx);
            case DOUBLE_HELIX:
                return generateDoubleHelix(ctx);
            case STAIR_SPIRAL:
                return generateStaircase(ctx);
            case HELIX:
            default:
                return generateHelix(ctx);
        }
    }

    private VoxelShape generateHelix(GeneratorContext ctx) {
        int r = Math.max(1, ctx.getRadius());
        double turns = Math.max(0.5, ctx.getTurns());
        double pitch = Math.max(0.2, ctx.getPitch());
        Point3i origin = ctx.getOrigin();

        Set<Point3i> worldVoxels = new LinkedHashSet<>();
        double totalAngle = turns * 2.0 * Math.PI;
        int steps = (int) Math.ceil(totalAngle * Math.max(r, 8) * 2.0);

        Point3i prev = null;
        for (int i = 0; i <= steps; i++) {
            double theta = (totalAngle * i) / steps;
            int x = (int) Math.round(r * Math.cos(theta));
            int z = (int) Math.round(r * Math.sin(theta));
            int y = (int) Math.round((pitch * theta) / (2.0 * Math.PI));

            Point3i current = origin.add(x, y, z);
            if (prev != null) {
                List<Point3i> segment = DiscreteCurves.line3D(prev, current);
                worldVoxels.addAll(segment);
            } else {
                worldVoxels.add(current);
            }
            prev = current;
        }

        return new VoxelShape(ShapeType.SPIRAL, origin, worldVoxels);
    }

    private VoxelShape generateDoubleHelix(GeneratorContext ctx) {
        int r = Math.max(2, ctx.getRadius());
        double turns = Math.max(0.5, ctx.getTurns());
        double pitch = Math.max(0.5, ctx.getPitch());
        Point3i origin = ctx.getOrigin();

        Set<Point3i> worldVoxels = new LinkedHashSet<>();
        double totalAngle = turns * 2.0 * Math.PI;
        int steps = (int) Math.ceil(totalAngle * Math.max(r, 8) * 2.0);

        Point3i prev1 = null;
        Point3i prev2 = null;

        for (int i = 0; i <= steps; i++) {
            double theta = (totalAngle * i) / steps;
            int y = (int) Math.round((pitch * theta) / (2.0 * Math.PI));

            // Strand 1
            int x1 = (int) Math.round(r * Math.cos(theta));
            int z1 = (int) Math.round(r * Math.sin(theta));
            Point3i p1 = origin.add(x1, y, z1);

            // Strand 2 (180 degree phase shift)
            int x2 = (int) Math.round(r * Math.cos(theta + Math.PI));
            int z2 = (int) Math.round(r * Math.sin(theta + Math.PI));
            Point3i p2 = origin.add(x2, y, z2);

            if (prev1 != null) {
                worldVoxels.addAll(DiscreteCurves.line3D(prev1, p1));
                worldVoxels.addAll(DiscreteCurves.line3D(prev2, p2));
            } else {
                worldVoxels.add(p1);
                worldVoxels.add(p2);
            }
            prev1 = p1;
            prev2 = p2;

            // Transverse base pair rungs every ~45 degrees
            if (i % Math.max(1, steps / (int) (turns * 8)) == 0) {
                worldVoxels.addAll(DiscreteCurves.line3D(p1, p2));
            }
        }

        return new VoxelShape(ShapeType.SPIRAL, origin, worldVoxels);
    }

    private VoxelShape generateArchimedean(GeneratorContext ctx) {
        int maxRadius = Math.max(2, ctx.getRadius());
        double turns = Math.max(0.5, ctx.getTurns());
        Point3i origin = ctx.getOrigin();
        PlaneOrientation orientation = ctx.getOrientation();

        Set<Point3i> worldVoxels = new LinkedHashSet<>();
        double totalAngle = turns * 2.0 * Math.PI;
        double b = maxRadius / totalAngle;
        int steps = (int) Math.ceil(totalAngle * maxRadius * 3.0);

        Point3i prev = null;
        for (int i = 0; i <= steps; i++) {
            double theta = (totalAngle * i) / steps;
            double r = b * theta;
            int u = (int) Math.round(r * Math.cos(theta));
            int v = (int) Math.round(r * Math.sin(theta));

            Point3i current = orientation.transform(u, v, origin);
            if (prev != null) {
                worldVoxels.addAll(DiscreteCurves.line3D(prev, current));
            } else {
                worldVoxels.add(current);
            }
            prev = current;
        }

        return new VoxelShape(ShapeType.SPIRAL, origin, worldVoxels);
    }

    private VoxelShape generateLogarithmic(GeneratorContext ctx) {
        int maxRadius = Math.max(2, ctx.getRadius());
        double turns = Math.max(0.5, ctx.getTurns());
        Point3i origin = ctx.getOrigin();
        PlaneOrientation orientation = ctx.getOrientation();

        Set<Point3i> worldVoxels = new LinkedHashSet<>();
        double totalAngle = turns * 2.0 * Math.PI;
        double a = 1.0;
        double k = Math.log(maxRadius / a) / totalAngle;
        int steps = (int) Math.ceil(totalAngle * maxRadius * 3.0);

        Point3i prev = null;
        for (int i = 0; i <= steps; i++) {
            double theta = (totalAngle * i) / steps;
            double r = a * Math.exp(k * theta);
            int u = (int) Math.round(r * Math.cos(theta));
            int v = (int) Math.round(r * Math.sin(theta));

            Point3i current = orientation.transform(u, v, origin);
            if (prev != null) {
                worldVoxels.addAll(DiscreteCurves.line3D(prev, current));
            } else {
                worldVoxels.add(current);
            }
            prev = current;
        }

        return new VoxelShape(ShapeType.SPIRAL, origin, worldVoxels);
    }

    private VoxelShape generateStaircase(GeneratorContext ctx) {
        int outerRadius = Math.max(3, ctx.getRadius());
        int innerRadius = Math.max(1, ctx.getThickness());
        int height = Math.max(4, ctx.getHeight());
        double turns = Math.max(0.5, ctx.getTurns());
        Point3i origin = ctx.getOrigin();

        Set<Point3i> worldVoxels = new LinkedHashSet<>();

        // Central pillar column
        for (int y = 0; y < height; y++) {
            worldVoxels.add(origin.add(0, y, 0));
        }

        // Steps along height
        double totalAngle = turns * 2.0 * Math.PI;
        for (int y = 0; y < height; y++) {
            double progress = (double) y / height;
            double theta = progress * totalAngle;
            double cos = Math.cos(theta);
            double sin = Math.sin(theta);

            // Step tread ray from inner to outer radius
            for (int r = innerRadius; r <= outerRadius; r++) {
                int x = (int) Math.round(r * cos);
                int z = (int) Math.round(r * sin);
                worldVoxels.add(origin.add(x, y, z));
            }

            // Outer handrail / banister
            int hx = (int) Math.round(outerRadius * cos);
            int hz = (int) Math.round(outerRadius * sin);
            worldVoxels.add(origin.add(hx, y + 1, hz));
        }

        return new VoxelShape(ShapeType.SPIRAL, origin, worldVoxels);
    }
}
