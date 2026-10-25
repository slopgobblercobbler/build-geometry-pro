package com.slopgobbler.buildgeometry.generator;

import com.slopgobbler.buildgeometry.math.DiscreteCurves;
import com.slopgobbler.buildgeometry.math.Point3i;
import com.slopgobbler.buildgeometry.math.Vec3d;

import java.util.*;

/**
 * Mathematical polyhedron voxel generator supporting Platonic solids (Tetrahedron,
 * Octahedron, Cube, Icosahedron, Dodecahedron) along with Pyramids and Prisms.
 * Supports Wireframe edges, Hollow-Shell triangular rasterization, and Solid convex volumes.
 */
public class PolyhedronGenerator implements ShapeGenerator {

    @Override
    public ShapeType getType() {
        return ShapeType.POLYHEDRON;
    }

    private static class Mesh {
        final List<Vec3d> vertices = new ArrayList<>();
        final List<int[]> edges = new ArrayList<>();
        final List<int[]> faces = new ArrayList<>(); // Indices into vertices forming triangles
    }

    @Override
    public VoxelShape generate(GeneratorContext ctx) {
        int radius = Math.max(2, ctx.getRadius());
        Point3i origin = ctx.getOrigin();
        GeneratorContext.PolyhedronType type = ctx.getPolyhedronType();
        GeneratorContext.PolyhedronRenderMode mode = ctx.getPolyhedronRenderMode();

        Mesh mesh = buildMesh(type, radius);
        Set<Point3i> worldVoxels = new LinkedHashSet<>();

        // Wireframe edges
        if (mode == GeneratorContext.PolyhedronRenderMode.WIREFRAME) {
            for (int[] edge : mesh.edges) {
                Point3i p1 = origin.add(mesh.vertices.get(edge[0]).toPoint3iRound());
                Point3i p2 = origin.add(mesh.vertices.get(edge[1]).toPoint3iRound());
                worldVoxels.addAll(DiscreteCurves.line3D(p1, p2));
            }
        } else {
            // Hollow Shell or Solid: rasterize all triangular faces
            Set<Point3i> shellVoxels = new LinkedHashSet<>();
            for (int[] face : mesh.faces) {
                Vec3d v0 = mesh.vertices.get(face[0]);
                Vec3d v1 = mesh.vertices.get(face[1]);
                Vec3d v2 = mesh.vertices.get(face[2]);

                rasterizeTriangle(shellVoxels, origin, v0, v1, v2);
            }

            // Also ensure all edge boundaries are sharply defined
            for (int[] edge : mesh.edges) {
                Point3i p1 = origin.add(mesh.vertices.get(edge[0]).toPoint3iRound());
                Point3i p2 = origin.add(mesh.vertices.get(edge[1]).toPoint3iRound());
                shellVoxels.addAll(DiscreteCurves.line3D(p1, p2));
            }

            if (mode == GeneratorContext.PolyhedronRenderMode.HOLLOW_SHELL) {
                worldVoxels.addAll(shellVoxels);
            } else {
                // Solid mode: fill inside the bounding shell along horizontal scanlines
                worldVoxels.addAll(fillConvexShell(shellVoxels));
            }
        }

        return new VoxelShape(ShapeType.POLYHEDRON, origin, worldVoxels);
    }

    private void rasterizeTriangle(Set<Point3i> dest, Point3i origin, Vec3d v0, Vec3d v1, Vec3d v2) {
        double d01 = v0.distance(v1);
        double d12 = v1.distance(v2);
        double d20 = v2.distance(v0);
        double maxEdge = Math.max(d01, Math.max(d12, d20));
        int steps = Math.max(4, (int) Math.ceil(maxEdge * 2.5));

        for (int i = 0; i <= steps; i++) {
            double u = (double) i / steps;
            for (int j = 0; j <= steps - i; j++) {
                double v = (double) j / steps;
                double w = 1.0 - u - v;

                double px = w * v0.x + u * v1.x + v * v2.x;
                double py = w * v0.y + u * v1.y + v * v2.y;
                double pz = w * v0.z + u * v1.z + v * v2.z;

                Point3i p = origin.add((int) Math.round(px), (int) Math.round(py), (int) Math.round(pz));
                dest.add(p);
            }
        }
    }

    private Set<Point3i> fillConvexShell(Set<Point3i> shell) {
        Set<Point3i> solid = new LinkedHashSet<>(shell);
        Map<Integer, Map<Integer, int[]>> scanlines = new HashMap<>();

        for (Point3i p : shell) {
            scanlines.computeIfAbsent(p.y, k -> new HashMap<>())
                    .compute(p.z, (k, range) -> {
                        if (range == null) return new int[]{p.x, p.x};
                        range[0] = Math.min(range[0], p.x);
                        range[1] = Math.max(range[1], p.x);
                        return range;
                    });
        }

        for (Map.Entry<Integer, Map<Integer, int[]>> yEntry : scanlines.entrySet()) {
            int y = yEntry.getKey();
            for (Map.Entry<Integer, int[]> zEntry : yEntry.getValue().entrySet()) {
                int z = zEntry.getKey();
                int minX = zEntry.getValue()[0];
                int maxX = zEntry.getValue()[1];
                for (int x = minX; x <= maxX; x++) {
                    solid.add(new Point3i(x, y, z));
                }
            }
        }

        return solid;
    }

    private Mesh buildMesh(GeneratorContext.PolyhedronType type, double scale) {
        Mesh mesh = new Mesh();
        switch (type) {
            case TETRAHEDRON:
                buildTetrahedron(mesh, scale);
                break;
            case OCTAHEDRON:
                buildOctahedron(mesh, scale);
                break;
            case CUBE:
                buildCube(mesh, scale);
                break;
            case ICOSAHEDRON:
                buildIcosahedron(mesh, scale);
                break;
            case DODECAHEDRON:
                buildDodecahedron(mesh, scale);
                break;
            case PYRAMID:
                buildPyramid(mesh, scale);
                break;
            case PRISM:
                buildPrism(mesh, scale);
                break;
        }
        return mesh;
    }

    private void buildTetrahedron(Mesh m, double s) {
        m.vertices.add(new Vec3d(s, s, s));
        m.vertices.add(new Vec3d(s, -s, -s));
        m.vertices.add(new Vec3d(-s, s, -s));
        m.vertices.add(new Vec3d(-s, -s, s));

        addEdge(m, 0, 1); addEdge(m, 0, 2); addEdge(m, 0, 3);
        addEdge(m, 1, 2); addEdge(m, 1, 3); addEdge(m, 2, 3);

        m.faces.add(new int[]{0, 1, 2});
        m.faces.add(new int[]{0, 2, 3});
        m.faces.add(new int[]{0, 3, 1});
        m.faces.add(new int[]{1, 3, 2});
    }

    private void buildOctahedron(Mesh m, double s) {
        m.vertices.add(new Vec3d(s, 0, 0));
        m.vertices.add(new Vec3d(-s, 0, 0));
        m.vertices.add(new Vec3d(0, s, 0));
        m.vertices.add(new Vec3d(0, -s, 0));
        m.vertices.add(new Vec3d(0, 0, s));
        m.vertices.add(new Vec3d(0, 0, -s));

        // Upper pyramid edges
        addEdge(m, 2, 0); addEdge(m, 2, 1); addEdge(m, 2, 4); addEdge(m, 2, 5);
        // Lower pyramid edges
        addEdge(m, 3, 0); addEdge(m, 3, 1); addEdge(m, 3, 4); addEdge(m, 3, 5);
        // Equator edges
        addEdge(m, 0, 4); addEdge(m, 4, 1); addEdge(m, 1, 5); addEdge(m, 5, 0);

        m.faces.add(new int[]{2, 0, 4});
        m.faces.add(new int[]{2, 4, 1});
        m.faces.add(new int[]{2, 1, 5});
        m.faces.add(new int[]{2, 5, 0});
        m.faces.add(new int[]{3, 4, 0});
        m.faces.add(new int[]{3, 1, 4});
        m.faces.add(new int[]{3, 5, 1});
        m.faces.add(new int[]{3, 0, 5});
    }

    private void buildCube(Mesh m, double s) {
        m.vertices.add(new Vec3d(-s, -s, -s)); // 0
        m.vertices.add(new Vec3d(s, -s, -s));  // 1
        m.vertices.add(new Vec3d(s, s, -s));   // 2
        m.vertices.add(new Vec3d(-s, s, -s));  // 3
        m.vertices.add(new Vec3d(-s, -s, s));  // 4
        m.vertices.add(new Vec3d(s, -s, s));   // 5
        m.vertices.add(new Vec3d(s, s, s));    // 6
        m.vertices.add(new Vec3d(-s, s, s));   // 7

        addEdge(m, 0, 1); addEdge(m, 1, 2); addEdge(m, 2, 3); addEdge(m, 3, 0);
        addEdge(m, 4, 5); addEdge(m, 5, 6); addEdge(m, 6, 7); addEdge(m, 7, 4);
        addEdge(m, 0, 4); addEdge(m, 1, 5); addEdge(m, 2, 6); addEdge(m, 3, 7);

        // 6 square faces divided into 12 triangles
        addQuad(m, 0, 1, 2, 3);
        addQuad(m, 5, 4, 7, 6);
        addQuad(m, 4, 0, 3, 7);
        addQuad(m, 1, 5, 6, 2);
        addQuad(m, 3, 2, 6, 7);
        addQuad(m, 4, 5, 1, 0);
    }

    private void buildIcosahedron(Mesh m, double s) {
        double phi = (1.0 + Math.sqrt(5.0)) * 0.5;
        double norm = Math.sqrt(1.0 + phi * phi);
        double a = (1.0 / norm) * s;
        double b = (phi / norm) * s;

        // 12 vertices
        m.vertices.add(new Vec3d(-a, b, 0));  // 0
        m.vertices.add(new Vec3d(a, b, 0));   // 1
        m.vertices.add(new Vec3d(-a, -b, 0)); // 2
        m.vertices.add(new Vec3d(a, -b, 0));  // 3
        m.vertices.add(new Vec3d(0, -a, b));  // 4
        m.vertices.add(new Vec3d(0, a, b));   // 5
        m.vertices.add(new Vec3d(0, -a, -b)); // 6
        m.vertices.add(new Vec3d(0, a, -b));  // 7
        m.vertices.add(new Vec3d(b, 0, -a));  // 8
        m.vertices.add(new Vec3d(b, 0, a));   // 9
        m.vertices.add(new Vec3d(-b, 0, -a)); // 10
        m.vertices.add(new Vec3d(-b, 0, a));  // 11

        int[][] faces = {
                {0, 11, 5}, {0, 5, 1}, {0, 1, 7}, {0, 7, 10}, {0, 10, 11},
                {1, 5, 9}, {5, 11, 4}, {11, 10, 2}, {10, 7, 6}, {7, 1, 8},
                {3, 9, 4}, {3, 4, 2}, {3, 2, 6}, {3, 6, 8}, {3, 8, 9},
                {4, 9, 5}, {2, 4, 11}, {6, 2, 10}, {8, 6, 7}, {9, 8, 1}
        };

        for (int[] f : faces) {
            m.faces.add(f);
            addEdge(m, f[0], f[1]);
            addEdge(m, f[1], f[2]);
            addEdge(m, f[2], f[0]);
        }
    }

    private void buildDodecahedron(Mesh m, double s) {
        double phi = (1.0 + Math.sqrt(5.0)) * 0.5;
        double invPhi = 1.0 / phi;
        double norm = Math.sqrt(3.0);
        double a = (1.0 / norm) * s;
        double b = (invPhi / norm) * s;
        double c = (phi / norm) * s;

        // 8 vertices from cube (+-1, +-1, +-1)
        for (int x = -1; x <= 1; x += 2) {
            for (int y = -1; y <= 1; y += 2) {
                for (int z = -1; z <= 1; z += 2) {
                    m.vertices.add(new Vec3d(x * a, y * a, z * a));
                }
            }
        }
        // 4 vertices (0, +-invPhi, +-phi)
        for (int y = -1; y <= 1; y += 2) {
            for (int z = -1; z <= 1; z += 2) {
                m.vertices.add(new Vec3d(0, y * b, z * c));
            }
        }
        // 4 vertices (+-invPhi, +-phi, 0)
        for (int x = -1; x <= 1; x += 2) {
            for (int y = -1; y <= 1; y += 2) {
                m.vertices.add(new Vec3d(x * b, y * c, 0));
            }
        }
        // 4 vertices (+-phi, 0, +-invPhi)
        for (int x = -1; x <= 1; x += 2) {
            for (int z = -1; z <= 1; z += 2) {
                m.vertices.add(new Vec3d(x * c, 0, z * b));
            }
        }

        // Triangulate 12 pentagonal faces into 3 triangles each
        int[][] pentagons = {
                {0, 8, 9, 1, 16}, {0, 16, 17, 2, 12}, {1, 9, 11, 3, 18},
                {2, 17, 3, 11, 10}, {4, 12, 13, 6, 17}, {4, 17, 16, 0, 8},
                {5, 18, 19, 7, 14}, {5, 14, 15, 6, 13}, {7, 19, 1, 16, 8},
                {6, 15, 10, 2, 13}, {7, 8, 0, 12, 4}, {5, 13, 2, 3, 11}
        };

        for (int[] p : pentagons) {
            addEdge(m, p[0], p[1]); addEdge(m, p[1], p[2]);
            addEdge(m, p[2], p[3]); addEdge(m, p[3], p[4]);
            addEdge(m, p[4], p[0]);

            m.faces.add(new int[]{p[0], p[1], p[2]});
            m.faces.add(new int[]{p[0], p[2], p[3]});
            m.faces.add(new int[]{p[0], p[3], p[4]});
        }
    }

    private void buildPyramid(Mesh m, double s) {
        m.vertices.add(new Vec3d(-s, -s, -s)); // 0
        m.vertices.add(new Vec3d(s, -s, -s));  // 1
        m.vertices.add(new Vec3d(s, -s, s));   // 2
        m.vertices.add(new Vec3d(-s, -s, s));  // 3
        m.vertices.add(new Vec3d(0, s, 0));    // 4 Apex

        addEdge(m, 0, 1); addEdge(m, 1, 2); addEdge(m, 2, 3); addEdge(m, 3, 0);
        addEdge(m, 0, 4); addEdge(m, 1, 4); addEdge(m, 2, 4); addEdge(m, 3, 4);

        addQuad(m, 0, 1, 2, 3);
        m.faces.add(new int[]{0, 1, 4});
        m.faces.add(new int[]{1, 2, 4});
        m.faces.add(new int[]{2, 3, 4});
        m.faces.add(new int[]{3, 0, 4});
    }

    private void buildPrism(Mesh m, double s) {
        // Hexagonal prism
        int n = 6;
        for (int i = 0; i < n; i++) {
            double angle = (2.0 * Math.PI * i) / n;
            double x = s * Math.cos(angle);
            double z = s * Math.sin(angle);
            m.vertices.add(new Vec3d(x, -s, z)); // Bottom i
            m.vertices.add(new Vec3d(x, s, z));  // Top i + n
        }

        for (int i = 0; i < n; i++) {
            int bCurr = i * 2;
            int bNext = ((i + 1) % n) * 2;
            int tCurr = bCurr + 1;
            int tNext = bNext + 1;

            addEdge(m, bCurr, bNext);
            addEdge(m, tCurr, tNext);
            addEdge(m, bCurr, tCurr);

            addQuad(m, bCurr, bNext, tNext, tCurr);
        }
    }

    private void addQuad(Mesh m, int v0, int v1, int v2, int v3) {
        m.faces.add(new int[]{v0, v1, v2});
        m.faces.add(new int[]{v0, v2, v3});
    }

    private void addEdge(Mesh m, int i1, int i2) {
        int a = Math.min(i1, i2);
        int b = Math.max(i1, i2);
        for (int[] e : m.edges) {
            if (e[0] == a && e[1] == b) return;
        }
        m.edges.add(new int[]{a, b});
    }
}
