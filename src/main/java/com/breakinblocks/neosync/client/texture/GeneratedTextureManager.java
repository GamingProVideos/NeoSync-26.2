package com.breakinblocks.neosync.client.texture;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class GeneratedTextureManager {
    private static final Map<TextureGenerator, Identifier[]> GENERATED_TEXTURES = new HashMap<>();
    private static final Identifier[] EMPTY_TEXTURES = new Identifier[0];

    public static Identifier[] getTextures(TextureGenerator generator) {
        Identifier[] textures = GENERATED_TEXTURES.get(generator);
        if (textures == null) {
            if (!RenderSystem.isOnRenderThread()) {
                return EMPTY_TEXTURES;
            }
            textures = genTextures(generator, GENERATED_TEXTURES.size());
            GENERATED_TEXTURES.put(generator, textures);
        }
        return textures;
    }

    private static Identifier[] genTextures(TextureGenerator generator, int generatorId) {
        int textureCounter = -1;
        String format = generator.getClass().getSimpleName().toLowerCase() + "_" + generatorId + "_";
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        List<Identifier> textures = new ArrayList<>();

        Iterator<AbstractTexture> it = generator.generateTextures().iterator();
        while (it.hasNext()) {
            Identifier id = Identifier.fromNamespaceAndPath("__dynamic", format + (++textureCounter));
            textureManager.register(id, it.next());
            textures.add(id);
        }

        return textures.toArray(new Identifier[0]);
    }
}
