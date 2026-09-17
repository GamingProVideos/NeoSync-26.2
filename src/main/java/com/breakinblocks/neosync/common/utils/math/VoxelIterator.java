package com.breakinblocks.neosync.common.utils.math;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class VoxelIterator implements Iterator<Voxel> {
    private final float pivotX;
    private final float pivotY;
    private final float pivotZ;

    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;

    private int x;
    private int y;
    private int z;

    public VoxelIterator(float pivotX, float pivotY, float pivotZ, int sizeX, int sizeY, int sizeZ) {
        this.pivotX = pivotX;
        this.pivotY = pivotY;
        this.pivotZ = pivotZ;

        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;

        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    @Override
    public boolean hasNext() {
        return this.y < this.sizeY;
    }

    @Override
    public Voxel next() {
        if (!this.hasNext()) {
            throw new NoSuchElementException();
        }

        Voxel voxel = new Voxel(this.pivotX + this.x, this.pivotY + this.y, this.pivotZ + this.z);

        this.z++;
        if (this.z >= this.sizeZ) {
            this.z = 0;
            this.x++;
            if (this.x >= this.sizeX) {
                this.x = 0;
                this.y++;
            }
        }

        return voxel;
    }
}