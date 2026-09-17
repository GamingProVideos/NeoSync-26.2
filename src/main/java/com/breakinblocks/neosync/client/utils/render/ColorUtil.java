package com.breakinblocks.neosync.client.utils.render;

import net.minecraft.world.item.DyeColor;

public final class ColorUtil {
    public static int fromDyeColor(DyeColor color) {
        return fromDyeColor(color, 1F);
    }

    public static int fromDyeColor(DyeColor color, float a) {
        float[] rgb = toFloatRgb(color);
        return fromRGBA(rgb[0], rgb[1], rgb[2], a);
    }

    public static float[] toFloatRgb(DyeColor color) {
        int packed = color.getTextureDiffuseColor();
        float r = ((packed >> 16) & 0xFF) / 255F;
        float g = ((packed >> 8) & 0xFF) / 255F;
        float b = (packed & 0xFF) / 255F;
        return new float[] { r, g, b };
    }

    public static int fromRGBA(float r, float g, float b, float a) {
        return (clamp255(a) << 24) | (clamp255(r) << 16) | (clamp255(g) << 8) | clamp255(b);
    }

    public static float[] toRGBA(int color) {
        float a = ((color >> 24) & 255) / 255F;
        float r = ((color >> 16) & 255) / 255F;
        float g = ((color >> 8) & 255) / 255F;
        float b = (color & 255) / 255F;

        return new float[] { r, g, b, a };
    }

    private static int clamp255(float v) {
        int i = Math.round(v * 255F);
        if (i < 0) return 0;
        if (i > 255) return 255;
        return i;
    }

    private ColorUtil() {
    }
}
