package com.slopgobbler.buildgeometry.generator;

/**
 * Base contract for mathematical structure voxel generators.
 */
public interface ShapeGenerator {
    /**
     * Executes the algorithmic generator to produce a discrete voxel shape.
     *
     * @param context configuration parameters for the geometry
     * @return generated VoxelShape containing discrete point set and spatial indices
     */
    VoxelShape generate(GeneratorContext context);

    /**
     * Returns the architectural shape type supported by this generator.
     */
    ShapeType getType();
}
