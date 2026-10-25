package com.slopgobbler.buildgeometry.generator;

import com.slopgobbler.buildgeometry.math.DiscreteCurves;
import com.slopgobbler.buildgeometry.math.Point3i;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Algorithmic circle voxel generator supporting both Bresenham and Midpoint integer algorithms,
 * filled solid discs, single-voxel outlines, plane orientation mapping, and depth extrusion.
 */
public class CircleGenerator implements ShapeGenerator {

    @Override
    public ShapeType getType() {
        return ShapeType.CIRCLE;
    }

    @Override
    public VoxelShape generate(GeneratorContext ctx) {
        Set<Point3i> planarPoints;
        int r = Math.max(0, ctx.getRadius());

        if (ctx.getCircleAlgorithm() == GeneratorContext.CircleAlgorithm.MIDPOINT) {
            planarPoints = DiscreteCurves.midpointCircle2D(r, ctx.isFilled());
        } else {
            planarPoints = DiscreteCurves.bresenhamCircle2D(r, ctx.isFilled());
        }

        Set<Point3i> worldVoxels = new LinkedHashSet<>();
        PlaneOrientation orientation = ctx.getOrientation();
        Point3i origin = ctx.getOrigin();
        int depth = Math.max(1, ctx.getDepth());

        for (Point3i p : planarPoints) {
            // Planar points in DiscreteCurves have u in x, v in z
            int u = p.x;
            int v = p.z;

            for (int d = 0; d < depth; d++) {
                Point3i mapped = orientation.transform(u, v, origin);
                // Apply depth along the plane's normal axis
                Point3i finalPos;
                switch (orientation) {
                    case HORIZONTAL_XZ:
                        finalPos = mapped.add(0, d, 0);
                        break;
                    case VERTICAL_XY:
                        finalPos = mapped.add(0, 0, d);
                        break;
                    case VERTICAL_YZ:
                        finalPos = mapped.add(d, 0, 0);
                        break;
                    default:
                        finalPos = mapped;
                }
                worldVoxels.add(finalPos);
            }
        }

        return new VoxelShape(ShapeType.CIRCLE, origin, worldVoxels);
    }
}
