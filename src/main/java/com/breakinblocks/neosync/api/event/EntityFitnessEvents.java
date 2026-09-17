package com.breakinblocks.neosync.api.event;

import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Events about the workout of {@linkplain Entity entities}.
 *
 * <p>These events can be categorized into two groups:
 * <ol>
 * <li>Simple listeners: {@link #START_RUNNING} and {@link #STOP_RUNNING}</li>
 * <li>Modifiers: {@link #MODIFY_OUTPUT_ENERGY_QUANTITY}</li>
 * </ol></p>
 *
 * <p>Fitness events are useful for registering custom entities as valid treadmill users.
 * Custom treadmill users generally only need a custom {@link #MODIFY_OUTPUT_ENERGY_QUANTITY} callback,
 * but the other events might be useful as well.</p>
 */
public final class EntityFitnessEvents {
    private static final List<StartRunning> startRunningListeners = new CopyOnWriteArrayList<>();
    private static final List<StopRunning> stopRunningListeners = new CopyOnWriteArrayList<>();
    private static final List<ModifyOutputEnergyQuantity> modifyEnergyListeners = new CopyOnWriteArrayList<>();

    /**
     * An event that is called when an entity starts treadmill running.
     */
    public static final Event<StartRunning> START_RUNNING = new Event<>(
            startRunningListeners,
            new StartRunning() {
                @Override
                public void onStartRunning(Entity entity, EnergyHandler energyStorage) {
                    for (StartRunning callback : startRunningListeners) {
                        callback.onStartRunning(entity, energyStorage);
                    }
                }
            }
    );

    /**
     * An event that is called when an entity stops treadmill running.
     */
    public static final Event<StopRunning> STOP_RUNNING = new Event<>(
            stopRunningListeners,
            new StopRunning() {
                @Override
                public void onStopRunning(Entity entity, EnergyHandler energyStorage) {
                    for (StopRunning callback : stopRunningListeners) {
                        callback.onStopRunning(entity, energyStorage);
                    }
                }
            }
    );

    /**
     * An event that can be used to provide amount of energy being produced by an entity if missing.
     */
    public static final Event<ModifyOutputEnergyQuantity> MODIFY_OUTPUT_ENERGY_QUANTITY = new Event<>(
            modifyEnergyListeners,
            new ModifyOutputEnergyQuantity() {
                @Override
                @Nullable
                public Long modifyOutputEnergyQuantity(Entity entity, EnergyHandler energyStorage, @Nullable Long outputEnergyQuantity) {
                    Long result = outputEnergyQuantity;
                    for (ModifyOutputEnergyQuantity callback : modifyEnergyListeners) {
                        result = callback.modifyOutputEnergyQuantity(entity, energyStorage, result);
                    }
                    return result;
                }
            }
    );

    @FunctionalInterface
    public interface ModifyOutputEnergyQuantity {
        /**
         * Modifies or provides amount of energy being produced by an entity.
         *
         * @param entity The running entity.
         * @param energyStorage The energy storage that stores energy being produced by the entity.
         * @param outputEnergyQuantity Amount of energy that will be produced by the entity every tick, or null if not determined by the mod logic.
         * @return Amount of energy that will be produced by the entity every tick, or null if the given entity cannot use treadmills.
         */
        @Nullable
        Long modifyOutputEnergyQuantity(Entity entity, EnergyHandler energyStorage, @Nullable Long outputEnergyQuantity);
    }

    @FunctionalInterface
    public interface StartRunning {
        /**
         * Called when an entity starts treadmill running.
         *
         * @param entity The running entity.
         * @param energyStorage The energy storage that stores energy being produced by the entity.
         */
        void onStartRunning(Entity entity, EnergyHandler energyStorage);
    }

    @FunctionalInterface
    public interface StopRunning {
        /**
         * Called when an entity stops treadmill running.
         *
         * @param entity The running entity.
         * @param energyStorage The energy storage that stores energy being produced by the entity.
         */
        void onStopRunning(Entity entity, EnergyHandler energyStorage);
    }

    /**
     * Simple event implementation that mimics Fabric's event system for Forge
     */
    public static class Event<T> {
        private final List<T> listeners;
        private final T invoker;

        public Event(List<T> listeners, T invoker) {
            this.listeners = listeners;
            this.invoker = invoker;
        }

        public void register(T listener) {
            listeners.add(listener);
        }

        public T invoker() {
            return invoker;
        }
    }

    private EntityFitnessEvents() {
    }
}