package com.slopgobbler.buildgeometry.generator;

import com.slopgobbler.buildgeometry.math.DiscreteCurves;
import com.slopgobbler.buildgeometry.math.Point3i;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Algorithmic ellipse generator utilizing 2-region discrete decision logic for arbitrary
 * semi-major (rx) and semi-minor (ry) axes with filled/outline configurations and depth extrusion.
 */
public class EllipseGenerator implements ShapeGenerator {

    @Override
    public ShapeType getType() {
        return ShapeType.ELLIPSE;
    }

    @Override
    public VoxelShape generate(GeneratorContext ctx) {
        int rx = Math.max(0, ctx.getRadiusX());
        int ry = Math.max(0, ctx.getRadiusY());
        Set<Point3i> planar = DiscreteCurves.bresenhamEllipse2D(rx, ry, ctx.isFilled());

        Set<Point3i> worldVoxels = new LinkedHashSet<>();
        PlaneOrientation orientation = ctx.getOrientation();
        Point3i origin = ctx.getOrigin();
        int depth = Math.max(1, ctx.getDepth());

        for (Point3i p : planar) {
            int u = p.x;
            int v = p.z;

            for (int d = 0; d < depth; d++) {
                Point3i mapped = orientation.transform(u, v, origin);
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

        return new VoxelShape(ShapeType.ELLIPSE, origin, worldVoxels);
    }
}
