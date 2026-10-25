package com.slopgobbler.buildgeometry.render;

/**
 * Color representation and theme palettes for client ghost-block CAD projections.
 */
public final class ColorPalette {
    public static final class Color4f {
        public final float r;
        public final float g;
        public final float b;
        public final float a;

        public Color4f(float r, float g, float b, float a) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
        }

        public static Color4f fromRgba(int rgba) {
            float r = ((rgba >> 24) & 0xFF) / 255.0f;
            float g = ((rgba >> 16) & 0xFF) / 255.0f;
            float b = ((rgba >> 8) & 0xFF) / 255.0f;
            float a = (rgba & 0xFF) / 255.0f;
            return new Color4f(r, g, b, a);
        }

        public static Color4f fromRgb(int rgb, float alpha) {
            float r = ((rgb >> 16) & 0xFF) / 255.0f;
            float g = ((rgb >> 8) & 0xFF) / 255.0f;
            float b = (rgb & 0xFF) / 255.0f;
            return new Color4f(r, g, b, alpha);
        }

        public Color4f withAlpha(float newAlpha) {
            return new Color4f(this.r, this.g, this.b, newAlpha);
        }

        public int toRgbaInt() {
            int ir = (int) (r * 255.0f) & 0xFF;
            int ig = (int) (g * 255.0f) & 0xFF;
            int ib = (int) (b * 255.0f) & 0xFF;
            int ia = (int) (a * 255.0f) & 0xFF;
            return (ir << 24) | (ig << 16) | (ib << 8) | ia;
        }
    }

    public enum Theme {
        CYBER_HOLO("Cyber Cyan",
                new Color4f(0.0f, 0.9f, 1.0f, 0.45f), // Cyan blueprint
                new Color4f(0.0f, 0.9f, 0.45f, 0.35f), // Emerald placed
                new Color4f(1.0f, 0.1f, 0.25f, 0.65f), // Crimson obstructed
                new Color4f(1.0f, 0.85f, 0.0f, 0.75f)  // Gold active layer
        ),
        EMERALD_MATRIX("Emerald Matrix",
                new Color4f(0.0f, 1.0f, 0.5f, 0.45f),
                new Color4f(0.2f, 0.8f, 0.2f, 0.35f),
                new Color4f(1.0f, 0.2f, 0.2f, 0.65f),
                new Color4f(0.8f, 1.0f, 0.2f, 0.75f)
        ),
        GOLDEN_ARCHITECT("Golden Architect",
                new Color4f(1.0f, 0.75f, 0.1f, 0.45f),
                new Color4f(0.2f, 0.9f, 0.4f, 0.35f),
                new Color4f(0.9f, 0.15f, 0.15f, 0.65f),
                new Color4f(1.0f, 1.0f, 0.3f, 0.8f)
        ),
        NEON_VIOLET("Neon Violet",
                new Color4f(0.7f, 0.2f, 1.0f, 0.45f),
                new Color4f(0.1f, 0.9f, 0.6f, 0.35f),
                new Color4f(1.0f, 0.1f, 0.3f, 0.65f),
                new Color4f(1.0f, 0.4f, 0.9f, 0.75f)
        );

        private final String displayName;
        public final Color4f blueprint;
        public final Color4f placed;
        public final Color4f obstructed;
        public final Color4f activeLayer;

        Theme(String displayName, Color4f blueprint, Color4f placed, Color4f obstructed, Color4f activeLayer) {
            this.displayName = displayName;
            this.blueprint = blueprint;
            this.placed = placed;
            this.obstructed = obstructed;
            this.activeLayer = activeLayer;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Guide colors
    public static final Color4f GUIDE_AXIS_X = new Color4f(1.0f, 0.25f, 0.25f, 0.85f);
    public static final Color4f GUIDE_AXIS_Y = new Color4f(0.25f, 1.0f, 0.25f, 0.85f);
    public static final Color4f GUIDE_AXIS_Z = new Color4f(0.25f, 0.5f, 1.0f, 0.85f);
    public static final Color4f GUIDE_SNAP_RING = new Color4f(0.9f, 0.3f, 1.0f, 0.65f);

    private Theme currentTheme = Theme.CYBER_HOLO;
    private float globalAlpha = 0.45f;
    private boolean useHeightGradient = false;

    public ColorPalette() {}

    public Theme getCurrentTheme() { return currentTheme; }
    public void setCurrentTheme(Theme currentTheme) { this.currentTheme = currentTheme; }

    public float getGlobalAlpha() { return globalAlpha; }
    public void setGlobalAlpha(float globalAlpha) { this.globalAlpha = Math.max(0.05f, Math.min(1.0f, globalAlpha)); }

    public boolean isUseHeightGradient() { return useHeightGradient; }
    public void setUseHeightGradient(boolean useHeightGradient) { this.useHeightGradient = useHeightGradient; }

    /**
     * Resolves the render color for a ghost voxel based on its placement state and active layer.
     */
    public Color4f resolveColor(boolean isPlaced, boolean isObstructed, boolean isActiveLayer, int y, int minY, int maxY) {
        if (isObstructed) {
            return currentTheme.obstructed.withAlpha(globalAlpha + 0.2f);
        }
        if (isPlaced) {
            return currentTheme.placed.withAlpha(globalAlpha * 0.7f);
        }
        if (isActiveLayer) {
            return currentTheme.activeLayer.withAlpha(Math.min(1.0f, globalAlpha + 0.3f));
        }

        if (useHeightGradient && maxY > minY) {
            float t = (float) (y - minY) / (maxY - minY);
            // Smooth hue cycle from cyan (0.5) to magenta (0.85)
            float hue = 0.5f + t * 0.35f;
            return fromHsv(hue, 0.85f, 1.0f, globalAlpha);
        }

        return currentTheme.blueprint.withAlpha(globalAlpha);
    }

    public static Color4f fromHsv(float h, float s, float v, float a) {
        float r = 0, g = 0, b = 0;
        int i = (int) (h * 6);
        float f = h * 6 - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);
        switch (i % 6) {
            case 0: r = v; g = t; b = p; break;
            case 1: r = q; g = v; b = p; break;
            case 2: r = p; g = v; b = t; break;
            case 3: r = p; g = q; b = v; break;
            case 4: r = t; g = p; b = v; break;
            case 5: r = v; g = p; b = q; break;
        }
        return new Color4f(r, g, b, a);
    }
}
