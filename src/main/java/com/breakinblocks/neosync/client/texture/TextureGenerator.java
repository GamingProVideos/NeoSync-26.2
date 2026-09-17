package com.breakinblocks.neosync.client.texture;

import net.minecraft.client.renderer.texture.AbstractTexture;

import java.util.stream.Stream;

@FunctionalInterface
public interface TextureGenerator {
    Stream<AbstractTexture> generateTextures();
}
