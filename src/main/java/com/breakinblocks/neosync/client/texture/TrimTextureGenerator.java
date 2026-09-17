package com.breakinblocks.neosync.client.texture;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class TrimTextureGenerator implements TextureGenerator {
    private final Identifier baseTextureId;
    private final Identifier trimmedTextureId;

    public TrimTextureGenerator(Identifier baseTextureId, Identifier trimmedTextureId) {
        this.baseTextureId = baseTextureId;
        this.trimmedTextureId = trimmedTextureId;
    }

    public Identifier getTexture(@Nullable DyeColor color, Identifier fallback) {
        if (color == null) {
            return fallback;
        }

        Identifier[] textures = GeneratedTextureManager.getTextures(this);
        return textures.length > color.getId() ? textures[color.getId()] : fallback;
    }

    @Override
    public Stream<AbstractTexture> generateTextures() {
        NativeImage base = load(this.baseTextureId);
        NativeImage trimmed = load(this.trimmedTextureId);
        if (base == null || trimmed == null || base.getWidth() != trimmed.getWidth() || base.getHeight() != trimmed.getHeight()) {
            if (base != null) {
                base.close();
            }
            if (trimmed != null) {
                trimmed.close();
            }
            return Stream.empty();
        }

        try {
            int cyanMax = maxComponent(DyeColor.CYAN.getTextureDiffuseColor());
            List<AbstractTexture> textures = new ArrayList<>(16);
            for (int i = 0; i < 16; ++i) {
                textures.add(generate(base, trimmed, DyeColor.byId(i), cyanMax));
            }
            return textures.stream();
        } finally {
            base.close();
            trimmed.close();
        }
    }

    private static AbstractTexture generate(NativeImage base, NativeImage trimmed, DyeColor dye, int cyanMax) {
        int width = trimmed.getWidth();
        int height = trimmed.getHeight();
        NativeImage img = new NativeImage(width, height, false);

        int dyeColor = dye.getTextureDiffuseColor();
        int dyeR = ARGB.red(dyeColor);
        int dyeG = ARGB.green(dyeColor);
        int dyeB = ARGB.blue(dyeColor);
        int dyeMax = Math.max(dyeR, Math.max(dyeG, dyeB));
        float maxScale = dyeMax == 0 ? 0F : 255F / dyeMax;

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                int trimPixel = trimmed.getPixel(x, y);
                if (trimPixel == base.getPixel(x, y)) {
                    img.setPixel(x, y, trimPixel);
                    continue;
                }

                int a = ARGB.alpha(trimPixel);
                int r = ARGB.red(trimPixel);
                int g = ARGB.green(trimPixel);
                int b = ARGB.blue(trimPixel);

                float scale = Math.min(Math.max(r, Math.max(g, b)) / (float)cyanMax, maxScale);
                int outR = Math.round(dyeR * scale);
                int outG = Math.round(dyeG * scale);
                int outB = Math.round(dyeB * scale);
                img.setPixel(x, y, ARGB.color(a, outR, outG, outB));
            }
        }

        return new DynamicTexture(() -> "neosync_trim_" + dye.getSerializedName(), img);
    }

    private static int maxComponent(int argb) {
        return Math.max(ARGB.red(argb), Math.max(ARGB.green(argb), ARGB.blue(argb)));
    }

    @Nullable
    private static NativeImage load(Identifier id) {
        try (InputStream stream = Minecraft.getInstance().getResourceManager().open(id)) {
            return NativeImage.read(stream);
        } catch (IOException e) {
            return null;
        }
    }
}
