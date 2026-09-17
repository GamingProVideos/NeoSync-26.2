package com.breakinblocks.neosync.api.shell;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.breakinblocks.neosync.common.item.SimpleInventory;
import com.breakinblocks.neosync.common.utils.WorldUtil;
import com.breakinblocks.neosync.common.utils.math.Radians;
import com.breakinblocks.neosync.common.utils.nbt.NbtSerializer;
import com.breakinblocks.neosync.common.utils.nbt.NbtSerializerFactory;
import com.breakinblocks.neosync.common.utils.nbt.NbtSerializerFactoryBuilder;
import com.breakinblocks.neosync.common.utils.nbt.SyncRegistries;
import com.mojang.authlib.properties.Property;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * A state that can be applied to a shell.
 */
public class ShellState {
    public static final float PROGRESS_START = 0F;
    public static final float PROGRESS_DONE = 1F;
    public static final float PROGRESS_PRINTING = 0.75F;
    public static final float PROGRESS_PAINTING = PROGRESS_DONE - PROGRESS_PRINTING;
    public static final int MAX_NAME_LENGTH = 32;

    public static final StreamCodec<RegistryFriendlyByteBuf, ShellState> STREAM_CODEC = StreamCodec.of(
            (buf, state) -> {
                SyncRegistries.push(buf.registryAccess());
                try {
                    buf.writeNbt(state.writeNbt(new CompoundTag()));
                } finally {
                    SyncRegistries.pop();
                }
            },
            buf -> {
                SyncRegistries.push(buf.registryAccess());
                try {
                    return ShellState.fromNbt(buf.readNbt());
                } finally {
                    SyncRegistries.pop();
                }
            }
    );

    private static final NbtSerializerFactory<ShellState> NBT_SERIALIZER_FACTORY;

    private UUID uuid;
    private String name;
    private float progress;
    private DyeColor color;
    private boolean isArtificial;
    private boolean isVirtual;
    private boolean isTemporary;
    private boolean manualOnly;

    private UUID ownerUuid;
    private String ownerName;
    private String textureValue;
    private String textureSignature;
    private float health;
    private int gameMode;
    private SimpleInventory inventory;
    private ShellStateComponent component;

    private int foodLevel;
    private float saturationLevel;
    private float exhaustion;

    private int experienceLevel;
    private float experienceProgress;
    private int totalExperience;

    private Identifier world;
    private BlockPos pos;

    private final NbtSerializer<ShellState> serializer;

    public UUID getUuid() {
        return this.uuid;
    }

    @Nullable
    public String getName() {
        return this.name;
    }

    public void setName(@Nullable String name) {
        String filtered = name == null ? null : StringUtil.filterText(name).trim();
        this.name = filtered == null || filtered.isEmpty()
                ? null
                : StringUtil.truncateStringIfNecessary(filtered, MAX_NAME_LENGTH, false);
    }

    public boolean hasName() {
        return this.name != null;
    }

    public Component getDisplayName() {
        if (this.name != null) {
            return Component.literal(this.name);
        }
        return this.pos == null
                ? Component.empty()
                : Component.translatable("gui.neosync.shell_selector.position", this.pos.getX(), this.pos.getY(), this.pos.getZ());
    }

    public DyeColor getColor() {
        return this.color;
    }

    public void setColor(DyeColor color) {
        this.color = color;
    }

    public float getProgress() {
        return this.progress;
    }

    public void setProgress(float progress) {
        this.progress = Mth.clamp(progress, 0F, 1F);
    }

    public boolean isArtificial() {
        return this.isArtificial;
    }

    public boolean isVirtual() {
        return this.isVirtual;
    }

    public boolean isTemporary() {
        return this.isTemporary;
    }

    public boolean isManualOnly() {
        return this.manualOnly;
    }

    public void setManualOnly(boolean manualOnly) {
        this.manualOnly = manualOnly;
    }

    public UUID getOwnerUuid() {
        return this.ownerUuid;
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public String getTextureValue() {
        return this.textureValue;
    }

    public String getTextureSignature() {
        return this.textureSignature;
    }

    public float getHealth() {
        return this.health;
    }

    public int getGameMode() {
        return this.gameMode;
    }

    public SimpleInventory getInventory() {
        return this.inventory;
    }

    public ShellStateComponent getComponent() {
        return this.component;
    }

    public int getFoodLevel() {
        return this.foodLevel;
    }

    public float getSaturationLevel() {
        return this.saturationLevel;
    }

    public float getExhaustion() {
        return this.exhaustion;
    }

    public int getExperienceLevel() {
        return this.experienceLevel;
    }

    public float getExperienceProgress() {
        return this.experienceProgress;
    }

    public int getTotalExperience() {
        return this.totalExperience;
    }

    public Identifier getWorld() {
        return this.world;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    private ShellState() {
        this.serializer = NBT_SERIALIZER_FACTORY.create(this);
    }

    /**
     * Creates empty shell of the specified player.
     *
     * @param player The player.
     * @param pos Position of the shell.
     * @return Empty shell of the specified player.
     */
    public static ShellState empty(ServerPlayer player, BlockPos pos) {
        return empty(player, pos, null);
    }

    /**
     * Creates empty shell of the specified player.
     *
     * @param player The player.
     * @param pos Position of the shell.
     * @param color Color of the shell.
     * @return Empty shell of the specified player.
     */
    public static ShellState empty(ServerPlayer player, BlockPos pos, DyeColor color) {
        return create(player, pos, color, 0, true, false);
    }

    /**
     * Creates shell that is a full copy of the specified player.
     *
     * @param player The player.
     * @param pos Position of the shell.
     * @return Shell that is a full copy of the specified player.
     */
    public static ShellState of(ServerPlayer player, BlockPos pos) {
        return of(player, pos, null);
    }

    /**
     * Creates shell that is a full copy of the specified player.
     *
     * @param player The player.
     * @param pos Position of the shell.
     * @param color Color of the shell.
     * @return Shell that is a full copy of the specified player.
     */
    public static ShellState of(ServerPlayer player, BlockPos pos, DyeColor color) {
        return create(player, pos, color, 1, ((Shell)player).isArtificial(), true);
    }

    public static ShellState anchor(ServerPlayer player, Identifier worldId, BlockPos pos) {
        return anchor(player, worldId, pos, false);
    }

    public static ShellState anchor(ServerPlayer player, Identifier worldId, BlockPos pos, boolean temporary) {
        ShellState shell = create(player, pos, null, PROGRESS_DONE, true, false);
        shell.world = worldId;
        shell.isVirtual = true;
        shell.isTemporary = temporary;
        return shell;
    }

    /**
     * Creates shell from the nbt data.
     * @param nbt The nbt data.
     * @return Shell created from the nbt data.
     */
    public static ShellState fromNbt(CompoundTag nbt) {
        ShellState state = new ShellState();
        state.readNbt(nbt);
        return state;
    }

    private static ShellState create(ServerPlayer player, BlockPos pos, DyeColor color, float progress, boolean isArtificial, boolean copyPlayerState) {
        ShellState shell = new ShellState();

        shell.uuid = UUID.randomUUID();
        shell.progress = progress;
        shell.color = color;
        shell.isArtificial = isArtificial;

        shell.ownerUuid = player.getUUID();
        shell.ownerName = player.getName().getString();
        java.util.Collection<Property> texturesCol = player.getGameProfile().properties().get("textures");
        Property textures = texturesCol != null ? texturesCol.stream().findFirst().orElse(null) : null;
        if (textures != null) {
            shell.textureValue = textures.value();
            shell.textureSignature = textures.signature();
        }
        shell.gameMode = player.gameMode.getGameModeForPlayer().getId();
        shell.inventory = new SimpleInventory();
        shell.component = ShellStateComponent.empty();

        if (copyPlayerState) {
            shell.health = player.getHealth();
            shell.inventory.clone(player.getInventory());
            shell.component.clone(ShellStateComponent.of(player));

            shell.foodLevel = player.getFoodData().getFoodLevel();
            shell.saturationLevel = player.getFoodData().getSaturationLevel();
            shell.exhaustion = 0;

            shell.experienceLevel = player.experienceLevel;
            shell.experienceProgress = player.experienceProgress;
            shell.totalExperience = player.totalExperience;
        } else {
            shell.health = player.getMaxHealth();
            shell.foodLevel = 20;
            shell.saturationLevel = 5;
        }

        shell.world = WorldUtil.getId(player.level());
        shell.pos = pos;

        return shell;
    }


    public void dropInventory(ServerLevel world) {
        this.dropInventory(world, this.pos);
    }

    public void dropInventory(ServerLevel world, BlockPos pos) {
        Stream
                .of(this.inventory.main, this.inventory.armor, this.inventory.offHand, this.component.getItems())
                .flatMap(Collection::stream)
                .forEach(x -> this.dropItemStack(world, pos, x));
    }

    public void dropXp(ServerLevel world) {
        this.dropXp(world, this.pos);
    }

    public void dropXp(ServerLevel world, BlockPos pos) {
        int xp = Math.min(this.experienceLevel * 7, 100) + this.component.getXp();
        Vec3 vecPos = new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        ExperienceOrb.award(world, vecPos, xp);
    }

    public void drop(ServerLevel world) {
        this.drop(world, this.pos);
    }

    public void drop(ServerLevel world, BlockPos pos) {
        this.dropInventory(world, pos);
        this.dropXp(world, pos);
    }

    private void dropItemStack(Level world, BlockPos pos, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        ItemEntity item = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack);
        item.setPickUpDelay(40);
        if (world instanceof ServerLevel serverLevel) {
            Entity thrower = serverLevel.getEntity(this.getOwnerUuid());
            if (thrower != null) {
                item.setThrower(thrower);
            }
        }

        float h = world.getRandom().nextFloat() * 0.5F;
        float v = world.getRandom().nextFloat() * 2 * Radians.R_PI;
        item.setDeltaMovement(-Mth.sin(v) * h, 0.2, Mth.cos(v) * h);
        world.addFreshEntity(item);
    }


    public CompoundTag writeNbt(CompoundTag nbt) {
        return this.serializer.writeNbt(nbt);
    }

    public void readNbt(CompoundTag nbt) {
        this.serializer.readNbt(nbt);
    }


    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof ShellState state && Objects.equals(this.uuid, state.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.uuid);
    }


    static {
        NBT_SERIALIZER_FACTORY = new NbtSerializerFactoryBuilder<ShellState>()
                .add(UUID.class, "uuid", x -> x.uuid, (x, uuid) -> x.uuid = uuid)
                .add(String.class, "name", x -> x.name, (x, name) -> x.name = name)
                .add(Integer.class, "color", x -> x.color == null ? -1 : x.color.getId(), (x, color) -> x.color = color == -1 ? null : DyeColor.byId(color))
                .add(Float.class, "progress", x -> x.progress, (x, progress) -> x.progress = progress)
                .add(Boolean.class, "isArtificial", x -> x.isArtificial, (x, isArtificial) -> x.isArtificial = isArtificial)
                .add(Boolean.class, "isVirtual", x -> x.isVirtual, (x, isVirtual) -> x.isVirtual = isVirtual)
                .add(Boolean.class, "isTemporary", x -> x.isTemporary, (x, isTemporary) -> x.isTemporary = isTemporary != null && isTemporary)
                .add(Boolean.class, "manualOnly", x -> x.manualOnly, (x, manualOnly) -> x.manualOnly = manualOnly != null && manualOnly)

                .add(UUID.class, "ownerUuid", x -> x.ownerUuid, (x, ownerUuid) -> x.ownerUuid = ownerUuid)
                .add(String.class, "ownerName", x -> x.ownerName, (x, ownerName) -> x.ownerName = ownerName)
                .add(String.class, "textureValue", x -> x.textureValue, (x, v) -> x.textureValue = v)
                .add(String.class, "textureSignature", x -> x.textureSignature, (x, v) -> x.textureSignature = v)
                .add(Float.class, "health", x -> x.health, (x, health) -> x.health = health)
                .add(Integer.class, "gameMode", x -> x.gameMode, (x, gameMode) -> x.gameMode = gameMode)
                .add(ListTag.class, "inventory", x -> x.inventory.writeNbt(new ListTag()), (x, inventory) -> { x.inventory = new SimpleInventory(); if (inventory != null) { x.inventory.readNbt(inventory); } })
                .add(CompoundTag.class, "components", x -> x.component.writeNbt(new CompoundTag()), (x, component) -> { x.component = ShellStateComponent.empty(); if (component != null) { x.component.readNbt(component); } })

                .add(Integer.class, "foodLevel", x -> x.foodLevel, (x, foodLevel) -> x.foodLevel = foodLevel)
                .add(Float.class, "saturationLevel", x -> x.saturationLevel, (x, saturationLevel) -> x.saturationLevel = saturationLevel)
                .add(Float.class, "exhaustion", x -> x.exhaustion, (x, exhaustion) -> x.exhaustion = exhaustion)

                .add(Integer.class, "experienceLevel", x -> x.experienceLevel, (x, experienceLevel) -> x.experienceLevel = experienceLevel)
                .add(Float.class, "experienceProgress", x -> x.experienceProgress, (x, experienceProgress) -> x.experienceProgress = experienceProgress)
                .add(Integer.class, "totalExperience", x -> x.totalExperience, (x, totalExperience) -> x.totalExperience = totalExperience)

                .add(Identifier.class, "world", x -> x.world, (x, world) -> x.world = world)
                .add(BlockPos.class, "pos", x -> x.pos, (x, pos) -> x.pos = pos)
                .build();
    }
}