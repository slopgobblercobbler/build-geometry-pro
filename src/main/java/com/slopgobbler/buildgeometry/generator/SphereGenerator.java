package com.slopgobbler.buildgeometry.generator;

import com.slopgobbler.buildgeometry.math.Point3i;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 3D spherical voxel generator.
 * Produces hollow shells of exact thickness T, solid spheres, or hemispheres,
 * utilizing 8-octant reflective symmetry.
 */
public class SphereGenerator implements ShapeGenerator {

    @Override
    public ShapeType getType() {
        return ShapeType.SPHERE;
    }

    @Override
    public VoxelShape generate(GeneratorContext ctx) {
        int r = Math.max(0, ctx.getRadius());
        int thickness = Math.max(1, ctx.getThickness());
        boolean hollow = ctx.isHollow();
        Point3i origin = ctx.getOrigin();

        Set<Point3i> worldVoxels = new LinkedHashSet<>();

        if (r == 0) {
            worldVoxels.add(origin);
            return new VoxelShape(ShapeType.SPHERE, origin, worldVoxels);
        }

        double outerRadiusSq = Math.pow(r + 0.5, 2);
        double innerRadius = Math.max(0.0, r - thickness + 0.5);
        double innerRadiusSq = Math.pow(innerRadius, 2);

        for (int x = 0; x <= r; x++) {
            double x2 = (double) x * x;
            for (int y = 0; y <= r; y++) {
                double y2 = (double) y * y;
                for (int z = 0; z <= r; z++) {
                    double distSq = x2 + y2 + (double) z * z;

                    if (distSq <= outerRadiusSq) {
                        if (!hollow || distSq >= innerRadiusSq) {
                            addOctantSymmetric(worldVoxels, origin, x, y, z, ctx.isInverted());
                        }
                    }
                }
            }
        }

        return new VoxelShape(ShapeType.SPHERE, origin, worldVoxels);
    }

    private void addOctantSymmetric(Set<Point3i> points, Point3i origin, int x, int y, int z, boolean hemisphereOnly) {
        int[] signsX = (x == 0) ? new int[]{0} : new int[]{1, -1};
        int[] signsY = (y == 0) ? new int[]{0} : (hemisphereOnly ? new int[]{1} : new int[]{1, -1});
        int[] signsZ = (z == 0) ? new int[]{0} : new int[]{1, -1};

        for (int sx : signsX) {
            for (int sy : signsY) {
                for (int sz : signsZ) {
                    points.add(origin.add(x * sx, y * sy, z * sz));
                }
            }
        }
    }
}
