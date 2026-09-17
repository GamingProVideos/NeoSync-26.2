package com.breakinblocks.neosync.common.entity;

/**
 * Interface for entities that can be killed in special ways.
 * Used for shell entities to handle their destruction properly.
 */
public interface KillableEntity {
    /**
     * Called when this killable entity dies.
     * Can be used to trigger special death effects or cleanup.
     */
    default void onKillableEntityDeath() { }

    /**
     * Called to update the entity after death.
     * @return true if the entity should continue updating post-death
     */
    default boolean updateKillableEntityPostDeath() {
        return false;
    }
}