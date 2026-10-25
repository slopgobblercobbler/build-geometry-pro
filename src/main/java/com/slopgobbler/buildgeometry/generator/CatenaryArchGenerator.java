package com.slopgobbler.buildgeometry.generator;

import com.slopgobbler.buildgeometry.math.Point3i;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Mathematical inverted catenary arch CAD generator.
 * Computes the ideal compression arch curve y = H - a * (cosh(x / a) - 1)
 * via numerical bisection for parameter a, ensuring continuous, gap-free voxel curves,
 * structural thickness, and 3D vault depth extrusion.
 */
public class CatenaryArchGenerator implements ShapeGenerator {

    @Override
    public ShapeType getType() {
        return ShapeType.CATENARY_ARCH;
    }

    /**
     * Solves for parameter 'a' in H = a * (cosh(W / (2 * a)) - 1) via bisection.
     */
    public static double solveCatenaryConstant(double span, double height) {
        if (span <= 0 || height <= 0) return 1.0;
        double halfSpan = span / 2.0;

        double low = 0.001;
        // Parabolic initial estimate W^2 / (8*H)
        double high = Math.max(1000.0, (span * span) / height);

        for (int iter = 0; iter < 60; iter++) {
            double mid = (low + high) * 0.5;
            double arg = halfSpan / mid;
            // Guard against cosh overflow for tiny mid values
            if (arg > 50.0) {
                low = mid;
                continue;
            }
            double val = mid * (Math.cosh(arg) - 1.0);
            if (val > height) {
                low = mid;
            } else {
                high = mid;
            }
        }

        return (low + high) * 0.5;
    }

    @Override
    public VoxelShape generate(GeneratorContext ctx) {
        int span = Math.max(2, ctx.getSpan());
        int height = Math.max(1, ctx.getHeight());
        int thickness = Math.max(1, ctx.getThickness());
        int depth = Math.max(1, ctx.getDepth());
        PlaneOrientation orientation = ctx.getOrientation();
        Point3i origin = ctx.getOrigin();

        double a = solveCatenaryConstant(span, height);
        double halfSpan = span / 2.0;

        Set<Point3i> worldVoxels = new LinkedHashSet<>();

        int minX = -(span / 2);
        int maxX = minX + span;

        Integer prevY = null;

        for (int x = minX; x <= maxX; x++) {
            double relX = x;
            double yVal = height - a * (Math.cosh(relX / a) - 1.0);
            int yInt = (int) Math.round(Math.max(0.0, yVal));

            // Ensure no vertical gaps between adjacent columns
            int yStart = yInt;
            int yEnd = yInt;
            if (prevY != null) {
                yStart = Math.min(yInt, prevY);
                yEnd = Math.max(yInt, prevY);
            }
            prevY = yInt;

            for (int y = yStart; y <= yEnd; y++) {
                // Apply radial / vertical thickness
                for (int t = 0; t < thickness; t++) {
                    int archY = y - t;
                    if (archY < 0 && !ctx.isInverted()) continue;

                    for (int d = 0; d < depth; d++) {
                        // Invert if upside-down arch requested
                        int finalY = ctx.isInverted() ? (height - archY) : archY;

                        Point3i mapped = orientation.transform(x, finalY, origin);
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
            }
        }

        return new VoxelShape(ShapeType.CATENARY_ARCH, origin, worldVoxels);
    }
}
