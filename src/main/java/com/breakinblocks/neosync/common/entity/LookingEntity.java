package com.breakinblocks.neosync.common.entity;

/**
 * Interface for entities that can have their look direction controlled externally.
 * Used for shell entities to sync look direction with the controlling player.
 */
public interface LookingEntity {
    /**
     * Changes the look direction of this entity based on cursor movement.
     * @param cursorDeltaX the horizontal cursor movement delta
     * @param cursorDeltaY the vertical cursor movement delta
     * @return true if the look direction was changed
     */
    default boolean changeLookingEntityLookDirection(double cursorDeltaX, double cursorDeltaY) {
        return false;
    }
}