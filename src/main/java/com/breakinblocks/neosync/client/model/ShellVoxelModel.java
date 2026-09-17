package com.breakinblocks.neosync.client.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import com.breakinblocks.neosync.api.shell.ShellState;
import com.breakinblocks.neosync.client.render.block.entity.ShellContainerRenderState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class ShellVoxelModel extends Model<ShellContainerRenderState> {
    private static final Set<Direction> ALL_FACES = Arrays.stream(Direction.values()).collect(Collectors.toSet());
    private static final int TEXTURE_SIZE = 4;
    private static final long SHUFFLE_SEED = 0x5E11L;
    private static final float GROW = 0.05F;
    private static final List<String> SOURCE_PARTS = List.of("head", "body", "right_arm", "left_arm", "right_leg", "left_leg");

    private final List<ModelPart> voxels;

    public ShellVoxelModel(ModelPart humanoidRoot) {
        this(buildVoxels(humanoidRoot));
    }

    private ShellVoxelModel(List<ModelPart> voxels) {
        super(toRoot(voxels), RenderTypes::entityCutoutCull);
        this.voxels = voxels;
    }

    private static ModelPart toRoot(List<ModelPart> voxels) {
        Map<String, ModelPart> children = new LinkedHashMap<>();
        for (int i = 0; i < voxels.size(); ++i) {
            children.put(Integer.toString(i), voxels.get(i));
        }
        return new ModelPart(List.of(), children);
    }

    private static List<ModelPart> buildVoxels(ModelPart humanoidRoot) {
        Map<Float, List<float[]>> byLayer = new HashMap<>();
        for (String name : SOURCE_PARTS) {
            if (humanoidRoot.hasChild(name)) {
                collect(humanoidRoot.getChild(name), 0F, 0F, 0F, byLayer);
            }
        }

        Random random = new Random(SHUFFLE_SEED);
        for (List<float[]> layer : byLayer.values()) {
            java.util.Collections.shuffle(layer, random);
        }

        return byLayer.entrySet().stream()
                .sorted(Comparator.comparing((Map.Entry<Float, List<float[]>> e) -> e.getKey()).reversed())
                .flatMap(e -> e.getValue().stream())
                .map(ShellVoxelModel::createVoxel)
                .collect(Collectors.toList());
    }

    private static void collect(ModelPart part, float x, float y, float z, Map<Float, List<float[]>> byLayer) {
        float originX = x + part.x;
        float originY = y + part.y;
        float originZ = z + part.z;

        for (ModelPart.Cube cube : part.cubes) {
            for (int iy = Mth.floor(cube.minY); iy < Mth.ceil(cube.maxY); ++iy) {
                float voxelY = originY + iy;
                List<float[]> layer = byLayer.computeIfAbsent(voxelY, k -> new ArrayList<>());
                for (int ix = Mth.floor(cube.minX); ix < Mth.ceil(cube.maxX); ++ix) {
                    for (int iz = Mth.floor(cube.minZ); iz < Mth.ceil(cube.maxZ); ++iz) {
                        layer.add(new float[] { originX + ix, voxelY, originZ + iz });
                    }
                }
            }
        }

        for (ModelPart child : part.children.values()) {
            collect(child, originX, originY, originZ, byLayer);
        }
    }

    private static ModelPart createVoxel(float[] pos) {
        ModelPart.Cube cube = new ModelPart.Cube(0, 0, 0F, 0F, 0F, 1F, 1F, 1F, GROW, GROW, GROW, false, TEXTURE_SIZE, TEXTURE_SIZE, ALL_FACES);
        ModelPart part = new ModelPart(List.of(cube), Map.of());
        part.setPos(pos[0], pos[1], pos[2]);
        return part;
    }

    @Override
    public void setupAnim(ShellContainerRenderState state) {
        int size = this.voxels.size();
        if (state.shellProgress < ShellState.PROGRESS_PRINTING) {
            float printed = Mth.clamp(state.shellProgress / ShellState.PROGRESS_PRINTING, 0F, 1F);
            int built = Mth.clamp(Math.round(size * printed), 0, size);
            for (int i = 0; i < size; ++i) {
                this.voxels.get(i).visible = i < built;
            }
        } else {
            float painted = Mth.clamp((state.shellProgress - ShellState.PROGRESS_PRINTING) / ShellState.PROGRESS_PAINTING, 0F, 1F);
            int bare = Mth.clamp(Math.round(size * painted), 0, size);
            for (int i = 0; i < size; ++i) {
                this.voxels.get(i).visible = i >= bare;
            }
        }
    }
}
