package com.slopgobbler.buildgeometry.generator;

import com.slopgobbler.buildgeometry.math.Point3i;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Parabolic dome / paraboloid surface voxel generator.
 * Implements y(x, z) = H * (1 - x^2/Rx^2 - z^2/Rz^2) with gap-free shell sealing,
 * hollow or solid volume generation, and elliptical or circular base configurations.
 */
public class ParabolicDomeGenerator implements ShapeGenerator {

    @Override
    public ShapeType getType() {
        return ShapeType.PARABOLIC_DOME;
    }

    @Override
    public VoxelShape generate(GeneratorContext ctx) {
        int rx = Math.max(1, ctx.getRadiusX());
        int rz = Math.max(1, ctx.getRadiusY()); // Radius along Z
        int height = Math.max(1, ctx.getHeight());
        int thickness = Math.max(1, ctx.getThickness());
        boolean hollow = ctx.isHollow();
        Point3i origin = ctx.getOrigin();

        Set<Point3i> worldVoxels = new LinkedHashSet<>();
        int[][] heightMap = new int[2 * rx + 3][2 * rz + 3];
        boolean[][] inBounds = new boolean[2 * rx + 3][2 * rz + 3];

        double rx2 = (double) rx * rx;
        double rz2 = (double) rz * rz;

        // Step 1: Compute height surface for all grid points
        for (int x = -rx; x <= rx; x++) {
            int gridX = x + rx;
            for (int z = -rz; z <= rz; z++) {
                int gridZ = z + rz;
                double normDistSq = (x * x) / rx2 + (z * z) / rz2;
                if (normDistSq <= 1.0) {
                    double y = height * (1.0 - normDistSq);
                    heightMap[gridX][gridZ] = (int) Math.round(y);
                    inBounds[gridX][gridZ] = true;
                } else {
                    heightMap[gridX][gridZ] = -1;
                    inBounds[gridX][gridZ] = false;
                }
            }
        }

        // Step 2: Rasterize with vertical step seam sealing for gap-free domes
        for (int x = -rx; x <= rx; x++) {
            int gridX = x + rx;
            for (int z = -rz; z <= rz; z++) {
                int gridZ = z + rz;
                if (!inBounds[gridX][gridZ]) continue;

                int topY = heightMap[gridX][gridZ];
                int bottomY;

                if (!hollow) {
                    bottomY = 0;
                } else {
                    // Find lowest adjacent surface height to prevent visual holes at steep slope boundaries
                    int minAdj = topY;
                    if (gridX > 0 && inBounds[gridX - 1][gridZ]) minAdj = Math.min(minAdj, heightMap[gridX - 1][gridZ]);
                    if (gridX + 1 < inBounds.length && inBounds[gridX + 1][gridZ]) minAdj = Math.min(minAdj, heightMap[gridX + 1][gridZ]);
                    if (gridZ > 0 && inBounds[gridX][gridZ - 1]) minAdj = Math.min(minAdj, heightMap[gridX][gridZ - 1]);
                    if (gridZ + 1 < inBounds[0].length && inBounds[gridX][gridZ + 1]) minAdj = Math.min(minAdj, heightMap[gridX][gridZ + 1]);

                    bottomY = Math.max(0, Math.min(topY - thickness + 1, minAdj));
                }

                for (int y = bottomY; y <= topY; y++) {
                    int finalY = ctx.isInverted() ? (height - y) : y;
                    worldVoxels.add(origin.add(x, finalY, z));
                }
            }
        }

        return new VoxelShape(ShapeType.PARABOLIC_DOME, origin, worldVoxels);
    }
}
