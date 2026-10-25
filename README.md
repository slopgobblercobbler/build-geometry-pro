# BuildGeometry Pro 📐✨
**Client-Side Mathematical Structure CAD & Hologram Curve Projector for Fabric**

[![Build Status](https://github.com/slopgobblercobbler/build-geometry-pro/actions/workflows/build.yml/badge.svg)](https://github.com/slopgobblercobbler/build-geometry-pro/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1%20%7C%2026.3-brightgreen.svg)](https://minecraft.net)
[![Fabric](https://img.shields.io/badge/Loader-Fabric-lightgrey.svg)](https://fabricmc.net)
[![Tests](https://img.shields.io/badge/Tests-52%2F52%20Passing%20(100%25)-success.svg)](src/test/java)

---

## 🌟 Overview

**BuildGeometry Pro** is an advanced client-side mathematical computer-aided design (CAD) suite and real-time holographic curve projector built for Minecraft Fabric. It bridges the gap between pure mathematical geometry and discrete voxel construction, allowing builders and architects to project mathematically rigorous curves, domes, arches, spirals, and polyhedra directly into their field of view.

### Key Capabilities
- **Algorithmic Voxel Generators**: Discrete rasterization of circles (Bresenham & Midpoint), parametric ellipses, hyperbolic catenary compression arches, parabolic domes, hollow spheres, multi-form spirals, and polyhedral Platonic solids.
- **Real-Time Client Hologram Projection**: In-world translucent ghost-block blueprints with configurable RGBA alpha, dynamic vertical height gradients, and real-time state detection (blueprint, completed, obstructed, active layer).
- **Layer-by-Layer Slicing with Auto-Advance**: Isolate individual horizontal slices, range-clamp vertical bounds, track layer completion percentages in real time, and automatically advance to the next layer as voxels are placed.
- **Angle-Snapped Cursor Guides**: Snap player look yaw and pitch to precision increments (15°, 30°, 45°, 90°) and lock cursor movement to Cartesian axes (X, Y, Z) or 2D projection planes with real-time HUD delta readouts.
- **Zero-Compromise Standalone Build**: Includes standard Fabric Loom Gradle setup as well as a standalone `build_native_mod.py` Python build script with an integrated JUnit 5 test runner.

---

## 🏛️ Mathematical Foundations & Generators

BuildGeometry Pro implements discrete numerical formulations of classical curves and surfaces to ensure airtight, gap-free voxel structures:

```
+-----------------------------------------------------------------------------+
|                          BUILDGEOMETRY PRO ARCHETYPES                       |
+-----------------------------------------------------------------------------+
|  [Circle & Ellipse]       [Catenary Arch]             [Parabolic Dome]      |
|        .----.                 .---.                         .---.           |
|       /      \               /     \                       /     \          |
|      |   (o)  |             /   ^   \                     /   ^   \         |
|       \      /             /    |    \                   |    |    |        |
|        '----'             /|    H    |\                  |----+----|        |
|   Bresenham / Midpoint   /__|___W____|__\                \___(Rx,Rz)___/    |
|   8-Fold Symmetry        y = H - a*(cosh(x/a)-1)         z = H*(1-r^2/R^2)  |
+-----------------------------------------------------------------------------+
|  [Hollow Sphere]          [Parametric Spirals]        [Polyhedra CAD]       |
|         .---.                 _   _                       /\                |
|       /  .-.  \              / \_/ \                     /  \               |
|      |  (   )  |            |   @   |                   / /\ \              |
|       \  '-'  /              \_/ \_/                   / /  \ \             |
|         '---'               Helix / Archimedean       / /____\ \            |
|   (R-T)^2 <= d^2 <= R^2     Staircase / Double-DNA    Platonic / Golden Phi |
+-----------------------------------------------------------------------------+
```

### 1. Circle Generator (`CircleGenerator.java`)
- **Algorithms**: Midpoint circle algorithm and Bresenham's decision parameter $d = 3 - 2R$.
- **Symmetry**: 8-way octant reflective symmetry:
  $$( \pm x, \pm y ), \quad ( \pm y, \pm x )$$
- **Features**: Single-voxel outline rings, filled circular discs, and planar orientation mapping (Horizontal XZ, Vertical XY, Vertical YZ) with arbitrary extrusion depth.

### 2. Ellipse Generator (`EllipseGenerator.java`)
- **Algorithm**: Two-region integer decision parameter evaluation:
  - **Region 1**: Slope magnitude $< 1$ ($2 r_y^2 x \le 2 r_x^2 y$)
  - **Region 2**: Slope magnitude $> 1$ ($2 r_y^2 x > 2 r_x^2 y$)
- **Features**: Exact semi-major ($R_x$) and semi-minor ($R_y$) axis constraints, 4-quadrant symmetry, and filled or outline rendering.

### 3. Catenary Arch Generator (`CatenaryArchGenerator.java`)
- **Physical Model**: Inverted catenary curve representing the ideal compression line for self-supporting masonry vaults without bending moments:
  $$y(x) = H - a \left(\cosh\left(\frac{x}{a}\right) - 1\right)$$
- **Numerical Bisection**: Solves for the tension/sag constant $a$ from span $W$ and apex height $H$:
  $$f(a) = a \left(\cosh\left(\frac{W}{2a}\right) - 1\right) - H = 0$$
  Converges to $10^{-6}$ numerical tolerance in $< 40$ iterations.
- **Continuous Sealing**: Enforces 26-connectivity across vertical column steps so steep sides have zero air holes.
- **Features**: Radial thickness $T$, depth extrusion $D$ (vault corridors), and upside-down suspension mode.

### 4. Parabolic Dome Generator (`ParabolicDomeGenerator.java`)
- **Formula**: Elliptic paraboloid dome cap:
  $$z(x, y) = H \left(1 - \frac{x^2}{R_x^2} - \frac{y^2}{R_y^2}\right)$$
- **Step Seam Sealing**: Compares local neighbor height gradients to seal steep base perimeters for watertight hollow shells.
- **Features**: Hollow shell (thickness $T$), solid volume fill, circular and elliptical base geometry.

### 5. Hollow Sphere Generator (`SphereGenerator.java`)
- **Spherical Shell Equation**:
  $$\max\left(0, R - T + 0.5\right)^2 \le x^2 + y^2 + z^2 \le (R + 0.5)^2$$
- **Symmetry**: 8-octant spatial reflection for instant, deterministic generation.
- **Features**: Precision thickness $T$, solid spheres, and upper hemisphere dome variant ($y \ge 0$).

### 6. Multi-Mode Spiral Generator (`SpiralGenerator.java`)
- **Archimedean Spiral**: $r(\theta) = b \cdot \theta$ for planar geometric expansion.
- **Logarithmic Spiral**: $r(\theta) = a \cdot e^{k \theta}$ (golden spiral / nautilus shell).
- **3D Helical Coil**: $x = R \cos \theta, \; z = R \sin \theta, \; y = \frac{\text{pitch} \cdot \theta}{2\pi}$.
- **Double Helix**: Dual intertwined strands with 180° phase separation and periodic transverse base-pair rungs.
- **Architectural Spiral Staircase**: Radial step treads projecting from a solid central supporting column with outer handrails.

### 7. Polyhedron CAD Generator (`PolyhedronGenerator.java`)
- **Platonic Solids**:
  - **Tetrahedron**: 4 vertices, 6 edges, 4 faces
  - **Octahedron**: 6 vertices, 12 edges, 8 faces
  - **Cube / Hexahedron**: 8 vertices, 12 edges, 6 faces
  - **Icosahedron**: 12 vertices derived from the golden ratio $\phi = \frac{1 + \sqrt{5}}{2}$, 30 edges, 20 triangular faces
  - **Dodecahedron**: 20 vertices, 30 edges, 12 pentagonal faces (dual of icosahedron)
  - **Pyramids & Prisms**: Square base pyramids and hexagonal prisms
- **Render Modes**:
  - `WIREFRAME`: 3D Bresenham edge lines connecting vertices.
  - `HOLLOW_SHELL`: Subdivided barycentric rasterization across all polygonal faces.
  - `SOLID`: Scanline convex hull filling inside the polyhedron boundary.

---

## 🎮 Keybindings & Controls

All hotkeys are client-side only, non-conflicting, and configurable in Minecraft's Controls menu under **BuildGeometry Pro CAD**:

| Hotkey | Default Key | Action |
|:---|:---:|:---|
| **Toggle Projection** | `G` | Toggles in-world hologram ghost-block projection ON / OFF |
| **Project / Rebuild** | `H` | Instantly generates and projects the active CAD shape at player's cursor position |
| **Next Layer** | `PAGE_UP` | Advances active slice upward by one Y level |
| **Previous Layer** | `PAGE_DOWN` | Decrements active slice downward by one Y level |
| **Cycle Slice Mode** | `HOME` | Cycles slicing: `All Layers` $\rightarrow$ `Single Layer` $\rightarrow$ `Up To Current` $\rightarrow$ `Range` |
| **Cycle Angle Snap** | `K` | Cycles look angle snapping step: `15°` $\rightarrow$ `30°` $\rightarrow$ `45°` $\rightarrow$ `90°` |
| **Cycle Axis Lock** | `L` | Cycles cursor constraints: `None` $\rightarrow$ `Lock X` $\rightarrow$ `Lock Y` $\rightarrow$ `Lock Z` $\rightarrow$ `Plane XZ` $\rightarrow$ `Plane XY` $\rightarrow$ `Plane YZ` |

---

## 🎨 Holographic Visual System & Layer Slicing

### State-Dependent Color Coding
Ghost voxels dynamically adapt their RGBA tint based on real-time world inspection:

| State | Default Tint | Meaning |
|:---|:---:|:---|
| **Blueprint** | Cyan `#00E5FF` | Unplaced blueprint voxel waiting to be built |
| **Active Layer** | Golden Yellow `#FFD600` | Current working layer slice (high visual prominence) |
| **Completed** | Emerald Green `#00E676` | Correct block has been placed in the world |
| **Obstructed** | Crimson Red `#FF1744` | Incorrect block or obstacle occupying blueprint voxel |

### Themes
Includes 4 high-contrast HUD color palettes:
1. **Cyber Cyan**: Classic holographic blueprint styling.
2. **Emerald Matrix**: High-visibility green matrix aesthetic.
3. **Golden Architect**: Warm amber architectural drawing scheme.
4. **Neon Violet**: Futuristic cyberpunk synthwave aesthetic.

### Auto-Advance Workflow
When `autoAdvance` is enabled in configuration:
1. The builder selects `Single Layer` mode.
2. BuildGeometry Pro monitors placed blocks on layer $Y$.
3. When the layer completion ratio hits **100%**, the system automatically increments the active layer to $Y+1$ and chimes an on-screen actionbar notification!

---

## 🧭 Angle Snapping & Axis Locking

The `AngleSnapManager` allows pixel-perfect manual alignment without placing throwaway scaffolding:

- **Look Snapping**: Round yaw and pitch to nearest $15^\circ$, $30^\circ$, $45^\circ$, or $90^\circ$ increments.
- **Axis Locking**: When locked to an axis (e.g., `LOCK_X`), cursor raycasts ignore movement along $Y$ and $Z$, forcing straight lines.
- **HUD Readout**: Live coordinate delta display:
  ```
  [CAD] Catenary Arch | Voxels: 142 | Layer: 7/16 (100%) | Snap: 45° [X-Axis Lock]
  ΔX: +14  ΔY: 0  ΔZ: 0 | Dist: 14.0m | Yaw: 90° [X-Axis Lock]
  ```

---

## 📦 Project Structure

```
workspaces/build-geometry-pro/
├── .github/
│   └── workflows/
│       └── build.yml               # GitHub Actions CI for Java 21 test & jar build
├── gradle/
│   └── wrapper/                    # Gradle 8.14.6 wrapper jar & properties
├── src/
│   ├── main/
│   │   ├── java/com/slopgobbler/buildgeometry/
│   │   │   ├── BuildGeometryMod.java               # ClientModInitializer entrypoint
│   │   │   ├── compat/
│   │   │   │   └── ModMenuCompat.java              # ModMenu config screen integration
│   │   │   ├── config/
│   │   │   │   └── BuildGeometryConfig.java        # GSON configuration persistence
│   │   │   ├── event/
│   │   │   │   └── ClientKeybindHandler.java       # Keymapping polling & tick hooks
│   │   │   ├── generator/                          # Algorithmic voxel generators
│   │   │   │   ├── CatenaryArchGenerator.java      # y = H - a*(cosh(x/a)-1) bisection
│   │   │   │   ├── CircleGenerator.java            # Bresenham & Midpoint circles
│   │   │   │   ├── EllipseGenerator.java           # 2-region discrete ellipse logic
│   │   │   │   ├── GeneratorContext.java           # Immutable generator parameters
│   │   │   │   ├── ParabolicDomeGenerator.java     # Paraboloid cap & seam sealing
│   │   │   │   ├── PlaneOrientation.java           # XZ, XY, YZ plane transforms
│   │   │   │   ├── PolyhedronGenerator.java        # Platonic solids, mesh & scanline
│   │   │   │   ├── ShapeGenerator.java             # Generator interface contract
│   │   │   │   ├── ShapeRegistry.java              # Central archetype registry
│   │   │   │   ├── ShapeType.java                  # Shape enumeration
│   │   │   │   ├── SphereGenerator.java            # 8-octant hollow/solid sphere
│   │   │   │   ├── SpiralGenerator.java            # Helix, Archimedean, Staircase
│   │   │   │   └── VoxelShape.java                 # Immutable voxel & layer model
│   │   │   ├── gui/
│   │   │   │   └── BuildGeometryHudOverlay.java    # Actionbar / HUD CAD metrics
│   │   │   ├── guide/
│   │   │   │   ├── AngleSnapManager.java           # Angular snap & axis constraints
│   │   │   │   └── CursorGuideRenderer.java        # Guide ray & delta computation
│   │   │   ├── math/                               # Core mathematical vector library
│   │   │   │   ├── BoundingBox3i.java              # AABB integer bounding box
│   │   │   │   ├── DiscreteCurves.java             # Bresenham 3D lines, circles, ellipses
│   │   │   │   ├── Point3i.java                    # Immutable 3D integer coordinate
│   │   │   │   └── Vec3d.java                      # 3D double vector, Rodrigues rotation
│   │   │   └── render/                             # Real-time ghost projection
│   │   │       ├── ColorPalette.java               # Theme palettes, HSV & RGBA
│   │   │       ├── GhostBlockRenderer.java         # Batch collection, LOD culling
│   │   │       └── LayerSlicer.java                # Layer progression & auto-advance
│   │   └── resources/
│   │       ├── assets/buildgeometry/
│   │       │   ├── icon.png                        # Custom 64x64 CAD hologram icon
│   │       │   └── lang/en_us.json                 # English localization strings
│   │       └── fabric.mod.json                     # Fabric metadata & entrypoints
│   └── test/
│       └── java/com/slopgobbler/buildgeometry/
│           ├── generator/                          # Generator unit tests
│           │   ├── CatenaryArchGeneratorTest.java
│           │   ├── CircleGeneratorTest.java
│           │   ├── EllipseGeneratorTest.java
│           │   ├── ParabolicDomeGeneratorTest.java
│           │   ├── PolyhedronGeneratorTest.java
│           │   ├── SphereGeneratorTest.java
│           │   └── SpiralGeneratorTest.java
│           ├── guide/
│           │   └── AngleSnapManagerTest.java       # Angle & axis locking tests
│           ├── math/                               # Math & curve algorithm tests
│           │   ├── BoundingBox3iTest.java
│           │   ├── DiscreteCurvesTest.java
│           │   ├── Point3iTest.java
│           │   └── Vec3dTest.java
│           ├── render/
│           │   └── LayerSlicerTest.java            # Slicing & auto-advance tests
│           └── testrunner/
│               └── StandaloneTestRunner.java       # Standalone JUnit 5 test harness
├── build.gradle                                    # Fabric Loom 1.7 Gradle build
├── build_native_mod.py                             # Zero-dependency Python build & test script
├── gradle.properties                               # Minecraft 1.21.1 / Loader 0.16.5 config
├── gradlew / gradlew.bat                           # Gradle wrapper executables
├── LICENSE                                         # MIT License
├── README.md                                       # Comprehensive documentation
└── settings.gradle                                 # Gradle project settings
```

---

## 🛠️ Building & Testing

### Option A: Standalone Native Python Build (Recommended)
BuildGeometry Pro provides `build_native_mod.py`, a completely autonomous build and test pipeline that discovers local JDKs, compiles the full Java tree, executes the 52-scenario JUnit 5 test suite, packages the Fabric JAR, and computes SHA256 checksums:

```powershell
python build_native_mod.py
```

Sample output:
```
================================================================================
 BUILDGEOMETRY PRO - JUNIT 5 TEST EXECUTION SUITE
================================================================================
Running Point3iTest...               [PASS 5/5]
Running Vec3dTest...                 [PASS 5/5]
Running BoundingBox3iTest...         [PASS 3/3]
Running DiscreteCurvesTest...        [PASS 2/2]
Running CircleGeneratorTest...       [PASS 4/4]
Running EllipseGeneratorTest...      [PASS 3/3]
Running CatenaryArchGeneratorTest... [PASS 4/4]
Running ParabolicDomeGeneratorTest...[PASS 3/3]
Running SphereGeneratorTest...       [PASS 4/4]
Running SpiralGeneratorTest...       [PASS 4/4]
Running PolyhedronGeneratorTest...   [PASS 5/5]
Running LayerSlicerTest...           [PASS 5/5]
Running AngleSnapManagerTest...      [PASS 5/5]
--------------------------------------------------------------------------------
TEST EXECUTION COMPLETED in 114 ms
Total: 52 | Passed: 52 | Failed: 0
--------------------------------------------------------------------------------
ALL TESTS PASSED SUCCESSFULLY! (100% PASS RATE)
================================================================================
 BUILD SUCCESSFUL!
 Output JAR: build/libs/buildgeometry-1.0.0.jar
 SHA256:     a775e68804862ba5ddd14b925d28b97f71ae470f871c0bc614006384b6fd21f2
================================================================================
```

### Option B: Gradle Loom
Standard Fabric build with Gradle:

```powershell
# Run JUnit 5 test suite
./gradlew test

# Build mod JAR
./gradlew build
```

---

## 🧪 Comprehensive Unit Test Suite

The test suite consists of **13 test classes** covering **52 exhaustive unit test scenarios**:

1. **`Point3iTest`** (5 tests): Addition, subtraction, scalar multiplication, Euclidean distance, Manhattan distance, dot product, cross product, lexicographical comparison ($Y \rightarrow Z \rightarrow X$), equals/hashcode contract.
2. **`Vec3dTest`** (5 tests): Length, normalization, linear interpolation (lerp), Euler rotations ($Y$), Rodrigues arbitrary axis rotation, discrete voxel rounding and flooring.
3. **`BoundingBox3iTest`** (3 tests): Dimensions, volume calculation, point containment, box intersection, and margin expansion.
4. **`DiscreteCurvesTest`** (2 tests): 3D Bresenham line continuity (26-connectivity check), zero-length line handling.
5. **`CircleGeneratorTest`** (4 tests): Bresenham radial proximity, Midpoint filled disc volume, plane orientation projections, 8-fold octant symmetry.
6. **`EllipseGeneratorTest`** (3 tests): Semi-major/minor bounding box extents, 4-quadrant symmetry, discrete filled ellipse volume approximation ($\approx \pi R_x R_y$).
7. **`CatenaryArchGeneratorTest`** (4 tests): Bisection solver convergence for parameter $a$, apex height match, ground contact at $y=0$, 26-connected continuous curve, parallel depth extrusion.
8. **`ParabolicDomeGeneratorTest`** (3 tests): Paraboloid bounds, apex coordinate, hollow interior vs solid volume, $X/Z$ symmetry.
9. **`SphereGeneratorTest`** (4 tests): Shell distance bounds ($R-T \le d \le R$), solid sphere volume approximation ($\approx \frac{4}{3}\pi R^3$), 8-octant symmetry, hemisphere mode ($Y \ge 0$).
10. **`SpiralGeneratorTest`** (4 tests): Helical pitch height ($y = \text{pitch} \cdot \text{turns}$), Archimedean monotonic radius growth, staircase central column & treads, double helix dual strands.
11. **`PolyhedronGeneratorTest`** (5 tests): Cube wireframe bounds, Octahedron 6-apex vertices, Icosahedron golden ratio mesh, wireframe vs shell vs solid volume hierarchy, Pyramid apex & base corners.
12. **`LayerSlicerTest`** (5 tests): Single layer isolation, up-to-current cumulative filtering, layer advance/retreat navigation, active layer completion percentage, automatic layer advancement.
13. **`AngleSnapManagerTest`** (5 tests): Degree snap rounding ($15^\circ, 30^\circ, 45^\circ, 90^\circ$), axis constraint locking (`LOCK_X`, `LOCK_Y`), horizontal plane locking (`LOCK_PLANE_XZ`), normalized look vector derivation.

---

## 📄 License

BuildGeometry Pro is released under the **MIT License**. See [LICENSE](LICENSE) for full details.
