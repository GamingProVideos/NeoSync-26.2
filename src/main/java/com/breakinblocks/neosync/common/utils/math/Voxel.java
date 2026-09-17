package com.breakinblocks.neosync.common.utils.math;

public class Voxel {
    public final float x;
    public final float y;
    public final float z;

    public Voxel(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Voxel voxel = (Voxel) obj;
        return Float.compare(voxel.x, x) == 0 &&
                Float.compare(voxel.y, y) == 0 &&
                Float.compare(voxel.z, z) == 0;
    }

    @Override
    public int hashCode() {
        int result = Float.floatToIntBits(x);
        result = 31 * result + Float.floatToIntBits(y);
        result = 31 * result + Float.floatToIntBits(z);
        return result;
    }

    @Override
    public String toString() {
        return "Voxel{x=" + x + ", y=" + y + ", z=" + z + "}";
    }
}