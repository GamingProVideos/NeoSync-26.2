package com.breakinblocks.neosync.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import org.joml.Matrix3x2f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import com.breakinblocks.neosync.api.event.PlayerSyncEvents;
import com.breakinblocks.neosync.api.networking.ShellRenamePacket;
import com.breakinblocks.neosync.api.shell.ClientShell;
import com.breakinblocks.neosync.api.shell.Shell;
import com.breakinblocks.neosync.api.shell.ShellState;
import com.breakinblocks.neosync.client.gui.hud.HudController;
import com.breakinblocks.neosync.client.gui.render.AnnulusSectorRenderState;
import com.breakinblocks.neosync.client.utils.render.ColorUtil;
import com.breakinblocks.neosync.common.block.entity.ShellEntity;
import com.breakinblocks.neosync.common.utils.IdentifierUtil;
import com.breakinblocks.neosync.common.utils.math.Radians;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ShellSelectorGUI extends Screen {
    private static final Component TITLE = Component.translatable("gui.neosync.shell_selector.title");
    private static final Component RENAME_TITLE = Component.translatable("gui.neosync.shell_selector.rename.title");
    private static final Component RENAME_HINT = Component.translatable("gui.neosync.shell_selector.rename.hint");
    private static final Component RENAME_PROMPT = Component.translatable("gui.neosync.shell_selector.rename.prompt");
    private static final Component RENAME_PLACEHOLDER = Component.translatable("gui.neosync.shell_selector.rename.placeholder");
    private static final int SHELLS_PER_PAGE = 8;
    private static final float MENU_RADIUS = 0.3F;
    private static final float HOLLOW_RATIO = 0.6F;
    private static final float BORDER_RATIO = 0.06F;
    private static final float SECTOR_GAP = 0.02F;
    private static final int BACKGROUND_COLOR = ColorUtil.fromDyeColor(DyeColor.BLACK, 0.3F);
    private static final int SECTOR_COLOR = ColorUtil.fromDyeColor(DyeColor.BLACK, 0.6F);
    private static final int SECTOR_HOVERED_COLOR = ColorUtil.fromDyeColor(DyeColor.BLACK, 0.8F);
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int DISABLED_TEXT_COLOR = 0xFFAAAAAA;
    private static final int ARROW_HIT_SIZE = 22;

    private final Runnable onCloseCallback;
    private final Runnable onRemovedCallback;
    private final Map<UUID, ShellEntity> shellEntities = new HashMap<>();

    private EditBox nameField;
    private ShellState renaming;

    private List<Identifier> worlds = List.of();
    private List<ShellState> worldShells = List.of();
    private List<ShellState> page = List.of();
    private int worldIndex;
    private int pageIndex;
    private int pageCount = 1;
    private boolean wasClosed;

    private float centerX;
    private float centerY;
    private float outerRadius;
    private float innerRadius;
    private float borderRadius;

    public ShellSelectorGUI(Runnable onCloseCallback, Runnable onRemovedCallback) {
        super(TITLE);
        this.onCloseCallback = onCloseCallback;
        this.onRemovedCallback = onRemovedCallback;
    }

    @Override
    protected void init() {
        LocalPlayer player = this.minecraft == null ? null : this.minecraft.player;
        if (player == null) {
            this.onClose();
            return;
        }

        List<ShellState> shells = ((Shell) player).getAvailableShellStates().collect(Collectors.toList());
        this.worlds = shells.stream()
                .map(ShellState::getWorld)
                .distinct()
                .sorted(Comparator.comparing(Identifier::toString))
                .collect(Collectors.toList());

        if (this.worlds.isEmpty()) {
            this.worlds = List.of(player.level().dimension().identifier());
        }

        int preferred = this.worlds.indexOf(player.level().dimension().identifier());
        this.worldIndex = preferred < 0 ? 0 : preferred;

        this.centerX = this.width / 2F;
        this.centerY = this.height / 2F;
        this.outerRadius = this.height * MENU_RADIUS;
        this.innerRadius = this.outerRadius * HOLLOW_RATIO;
        this.borderRadius = this.outerRadius - (this.outerRadius - this.innerRadius) * BORDER_RATIO;

        this.selectWorld(this.worldIndex);
        HudController.hide();
    }

    private void selectWorld(int index) {
        this.worldIndex = Math.floorMod(index, this.worlds.size());
        Identifier world = this.worlds.get(this.worldIndex);
        LocalPlayer player = this.minecraft == null ? null : this.minecraft.player;
        List<ShellState> shells = player == null
                ? List.of()
                : ((Shell) player).getAvailableShellStates()
                        .filter(s -> s.getWorld().equals(world))
                        .sorted(Comparator.comparing(s -> s.getUuid().toString()))
                        .collect(Collectors.toList());

        this.worldShells = shells;
        this.pageCount = Math.max(1, (shells.size() + SHELLS_PER_PAGE - 1) / SHELLS_PER_PAGE);
        this.selectPage(0);
    }

    private void selectPage(int index) {
        this.pageIndex = Math.floorMod(index, this.pageCount);
        int from = this.pageIndex * SHELLS_PER_PAGE;
        int to = Math.min(from + SHELLS_PER_PAGE, this.worldShells.size());
        this.page = from >= to ? List.of() : new ArrayList<>(this.worldShells.subList(from, to));
    }

    /**
     * Rebuild the wheel from the player's current client shell data. This is called
     * after shell network updates so removed clones disappear without reopening the menu.
     */
    public void refreshShells() {
        LocalPlayer player = this.minecraft == null ? null : this.minecraft.player;
        if (player == null) {
            return;
        }

        Identifier selectedWorld = this.worlds.isEmpty()
                ? player.level().dimension().identifier()
                : this.worlds.get(Math.min(this.worldIndex, this.worlds.size() - 1));
        int selectedPage = this.pageIndex;

        List<ShellState> shells = ((Shell) player).getAvailableShellStates()
                .collect(Collectors.toList());

        List<Identifier> newWorlds = shells.stream()
                .map(ShellState::getWorld)
                .distinct()
                .sorted(Comparator.comparing(Identifier::toString))
                .collect(Collectors.toList());

        if (newWorlds.isEmpty()) {
            newWorlds = List.of(player.level().dimension().identifier());
        }
        this.worlds = newWorlds;

        int newWorldIndex = this.worlds.indexOf(selectedWorld);
        if (newWorldIndex < 0) {
            newWorldIndex = this.worlds.indexOf(player.level().dimension().identifier());
        }
        this.worldIndex = newWorldIndex < 0 ? 0 : newWorldIndex;

        Identifier world = this.worlds.get(this.worldIndex);
        this.worldShells = shells.stream()
                .filter(shell -> shell.getWorld().equals(world))
                .sorted(Comparator.comparing(shell -> shell.getUuid().toString()))
                .collect(Collectors.toList());

        this.pageCount = Math.max(1, (this.worldShells.size() + SHELLS_PER_PAGE - 1) / SHELLS_PER_PAGE);
        this.pageIndex = Mth.clamp(selectedPage, 0, this.pageCount - 1);
        int from = this.pageIndex * SHELLS_PER_PAGE;
        int to = Math.min(from + SHELLS_PER_PAGE, this.worldShells.size());
        this.page = from >= to ? List.of() : new ArrayList<>(this.worldShells.subList(from, to));

        // Do not keep preview entities for shells that no longer exist.
        var liveIds = shells.stream().map(ShellState::getUuid).collect(Collectors.toSet());
        this.shellEntities.keySet().removeIf(uuid -> !liveIds.contains(uuid));

        if (this.renaming != null && !liveIds.contains(this.renaming.getUuid())) {
            this.endRename();
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, BACKGROUND_COLOR);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        int count = Math.max(1, this.page.size());
        int hovered = this.sectorAt(mouseX, mouseY);
        Matrix3x2f pose = new Matrix3x2f(graphics.pose());

        for (int i = 0; i < count; ++i) {
            ShellState shell = i < this.page.size() ? this.page.get(i) : null;
            float from = this.sectorStart(i, count);
            float to = from + this.sectorSpan(count);
            boolean isHovered = shell != null && i == hovered;

            graphics.submitGuiElementRenderState(new AnnulusSectorRenderState(pose, this.centerX, this.centerY,
                    this.innerRadius, this.outerRadius, from, to,
                    isHovered ? SECTOR_HOVERED_COLOR : SECTOR_COLOR, graphics.peekScissorStack()));

            if (shell != null) {
                DyeColor color = shell.getColor() == null ? DyeColor.WHITE : shell.getColor();
                graphics.submitGuiElementRenderState(new AnnulusSectorRenderState(pose, this.centerX, this.centerY,
                        this.borderRadius, this.outerRadius, from, to,
                        ColorUtil.fromDyeColor(color, isHovered ? 1F : 0.75F), graphics.peekScissorStack()));
            }
        }

        for (int i = 0; i < this.page.size(); ++i) {
            this.extractShell(graphics, this.page.get(i), i, count);
        }

        this.extractCenter(graphics, mouseX, mouseY, hovered);
        this.extractArrows(graphics, mouseX, mouseY);
    }

    private void extractShell(GuiGraphicsExtractor graphics, ShellState shell, int index, int count) {
        float angle = this.sectorStart(index, count) + this.sectorSpan(count) / 2F;
        float bandRadius = (this.innerRadius + this.outerRadius) / 2F;
        float shellX = this.centerX + Mth.cos(angle) * bandRadius;
        float shellY = this.centerY + Mth.sin(angle) * bandRadius;
        float half = (this.outerRadius - this.innerRadius) / 2F;

        EntityRenderState renderState = this.extractShellRenderState(shell);
        if (renderState != null) {
            graphics.entity(renderState, half * 0.75F,
                    new Vector3f(0F, renderState.boundingBoxHeight / 2F, 0F),
                    new Quaternionf().rotateZ(Radians.R_PI),
                    null,
                    Mth.floor(shellX - half), Mth.floor(shellY - half),
                    Mth.ceil(shellX + half), Mth.ceil(shellY + half));
        }

        float labelRadius = this.outerRadius - this.font.lineHeight;
        int labelX = Mth.floor(this.centerX + Mth.cos(angle) * labelRadius);
        int labelY = Mth.floor(this.centerY + Mth.sin(angle) * labelRadius - this.font.lineHeight / 2F);

        if (shell.getProgress() < ShellState.PROGRESS_DONE) {
            Component progress = Component.translatable("gui.neosync.shell_selector.progress_percent",
                    Mth.floor(shell.getProgress() * 100F));
            graphics.centeredText(this.font, progress, labelX, labelY - this.font.lineHeight, 0xFFFF5555);
        } else if (shell.isManualOnly()) {
            Component manual = Component.translatable("gui.neosync.shell_selector.manual");
            graphics.centeredText(this.font, manual, labelX, labelY - this.font.lineHeight, DISABLED_TEXT_COLOR);
        }

        float chord = 2F * labelRadius * Mth.sin(Math.min(this.sectorSpan(count), Radians.R_PI) / 2F);
        int maxWidth = Mth.floor(Math.min(chord, this.outerRadius));
        graphics.centeredText(this.font, this.truncate(shell.getDisplayName().getString(), maxWidth),
                labelX, labelY, TEXT_COLOR);
    }

    private String truncate(String text, int maxWidth) {
        if (this.font.width(text) <= maxWidth) {
            return text;
        }
        return this.font.plainSubstrByWidth(text, Math.max(0, maxWidth - this.font.width("..."))) + "...";
    }

    private EntityRenderState extractShellRenderState(ShellState shell) {
        ShellEntity entity = this.shellEntities.computeIfAbsent(shell.getUuid(), uuid -> new ShellEntity(shell));
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? super ShellEntity, ?> renderer = dispatcher.getRenderer(entity);
        EntityRenderState renderState = renderer.createRenderState(entity, 1F);
        renderState.shadowPieces.clear();
        renderState.outlineColor = 0;
        if (renderState instanceof LivingEntityRenderState living) {
            living.bodyRot = 180F;
            living.yRot = 0F;
            living.xRot = 0F;
            living.boundingBoxWidth = living.boundingBoxWidth / living.scale;
            living.boundingBoxHeight = living.boundingBoxHeight / living.scale;
            living.scale = 1F;
        }
        return renderState;
    }

    private void extractCenter(GuiGraphicsExtractor graphics, int mouseX, int mouseY, int hovered) {
        int cx = Mth.floor(this.centerX);
        int cy = Mth.floor(this.centerY);
        int line = this.font.lineHeight + 3;

        if (this.renaming != null) {
            int fieldHalf = this.nameField == null ? 0 : this.nameField.getHeight() / 2;
            graphics.centeredText(this.font, RENAME_TITLE, cx, cy - fieldHalf - line, TEXT_COLOR);
            graphics.centeredText(this.font, RENAME_HINT, cx, cy + fieldHalf + line - this.font.lineHeight, DISABLED_TEXT_COLOR);
            return;
        }

        Component world = IdentifierUtil.prettifyAsText(this.worlds.get(this.worldIndex));
        graphics.centeredText(this.font, world, cx, cy - line - line / 2, TEXT_COLOR);

        Component pagination = Component.translatable("gui.neosync.page_display.pagination", this.pageIndex + 1, this.pageCount);
        graphics.centeredText(this.font, pagination, cx, cy - line / 2, this.pageCount > 1 ? TEXT_COLOR : DISABLED_TEXT_COLOR);

        boolean overCenter = this.isOverCenter(mouseX, mouseY);
        Component close = Component.translatable("gui.neosync.default.cross_button.title");
        graphics.centeredText(this.font, close, cx, cy + line / 2, overCenter ? 0xFFFF5555 : DISABLED_TEXT_COLOR);

        if (hovered >= 0 && hovered < this.page.size()) {
            ShellState shell = this.page.get(hovered);
            graphics.centeredText(this.font, shell.getDisplayName(), cx, cy + line + line / 2, TEXT_COLOR);
            graphics.centeredText(this.font, RENAME_PROMPT, cx, cy + line * 2 + line / 2, DISABLED_TEXT_COLOR);
        }
    }

    private void extractArrows(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (this.pageCount > 1) {
            this.extractArrow(graphics, "<", this.centerX - this.outerRadius - ARROW_HIT_SIZE, this.centerY, mouseX, mouseY);
            this.extractArrow(graphics, ">", this.centerX + this.outerRadius + ARROW_HIT_SIZE, this.centerY, mouseX, mouseY);
        }
        if (this.worlds.size() > 1) {
            this.extractArrow(graphics, "^", this.centerX, this.centerY - this.outerRadius - ARROW_HIT_SIZE, mouseX, mouseY);
            this.extractArrow(graphics, "v", this.centerX, this.centerY + this.outerRadius + ARROW_HIT_SIZE, mouseX, mouseY);
        }
    }

    private void extractArrow(GuiGraphicsExtractor graphics, String glyph, float x, float y, int mouseX, int mouseY) {
        boolean isHovered = isInside(mouseX, mouseY, x, y, ARROW_HIT_SIZE);
        graphics.centeredText(this.font, glyph, Mth.floor(x), Mth.floor(y - this.font.lineHeight / 2F),
                isHovered ? TEXT_COLOR : DISABLED_TEXT_COLOR);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();

        if (this.renaming != null) {
            if (this.nameField != null && this.nameField.isMouseOver(mouseX, mouseY)) {
                return super.mouseClicked(event, doubleClick);
            }
            this.commitRename();
            return true;
        }

        if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT) {
            int index = this.sectorAt(mouseX, mouseY);
            if (index >= 0 && index < this.page.size()) {
                this.beginRename(this.page.get(index));
                return true;
            }
        }

        if (this.pageCount > 1) {
            if (isInside(mouseX, mouseY, this.centerX - this.outerRadius - ARROW_HIT_SIZE, this.centerY, ARROW_HIT_SIZE)) {
                this.selectPage(this.pageIndex - 1);
                return true;
            }
            if (isInside(mouseX, mouseY, this.centerX + this.outerRadius + ARROW_HIT_SIZE, this.centerY, ARROW_HIT_SIZE)) {
                this.selectPage(this.pageIndex + 1);
                return true;
            }
        }

        if (this.worlds.size() > 1) {
            if (isInside(mouseX, mouseY, this.centerX, this.centerY - this.outerRadius - ARROW_HIT_SIZE, ARROW_HIT_SIZE)) {
                this.selectWorld(this.worldIndex - 1);
                return true;
            }
            if (isInside(mouseX, mouseY, this.centerX, this.centerY + this.outerRadius + ARROW_HIT_SIZE, ARROW_HIT_SIZE)) {
                this.selectWorld(this.worldIndex + 1);
                return true;
            }
        }

        if (this.isOverCenter(mouseX, mouseY)) {
            this.onClose();
            return true;
        }

        int index = this.sectorAt(mouseX, mouseY);
        if (index >= 0 && index < this.page.size()) {
            this.selectShell(this.page.get(index));
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.renaming != null) {
            if (event.isEscape()) {
                this.endRename();
                return true;
            }
            if (event.isConfirmation()) {
                this.commitRename();
                return true;
            }
        }
        return super.keyPressed(event);
    }

    private void beginRename(ShellState shell) {
        this.renaming = shell;

        int width = Mth.floor(this.innerRadius * 1.3F);
        int height = this.font.lineHeight + 8;
        this.nameField = new EditBox(this.font, Mth.floor(this.centerX - width / 2F), Mth.floor(this.centerY - height / 2F),
                width, height, RENAME_TITLE);
        this.nameField.setMaxLength(ShellState.MAX_NAME_LENGTH);
        this.nameField.setHint(RENAME_PLACEHOLDER);
        this.nameField.setValue(shell.getName() == null ? "" : shell.getName());
        this.nameField.moveCursorToEnd(false);

        this.addRenderableWidget(this.nameField);
        this.setFocused(this.nameField);
        this.nameField.setFocused(true);
    }

    private void commitRename() {
        ShellState shell = this.renaming;
        String name = this.nameField == null ? "" : this.nameField.getValue().trim();
        this.endRename();

        if (shell == null) {
            return;
        }
        shell.setName(name);
        new ShellRenamePacket(shell, name).send();
    }

    private void endRename() {
        if (this.nameField != null) {
            this.removeWidget(this.nameField);
            this.nameField = null;
        }
        this.renaming = null;
        this.setFocused(null);
    }

    private void selectShell(ShellState shell) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || shell.getProgress() < ShellState.PROGRESS_DONE) {
            return;
        }

        PlayerSyncEvents.SyncFailureReason failureReason = ((ClientShell) client.player).beginSync(shell);
        if (failureReason != null) {
            this.onClose();
            client.player.sendSystemMessage(failureReason.toText());
        }
    }

    private int sectorAt(double mouseX, double mouseY) {
        int count = this.page.size();
        if (count == 0) {
            return -1;
        }

        double dx = mouseX - this.centerX;
        double dy = mouseY - this.centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance < this.innerRadius || distance > this.outerRadius) {
            return -1;
        }

        float span = Radians.R_2_PI / count;
        double angle = Math.atan2(dy, dx);
        for (int i = 0; i < count; ++i) {
            double delta = wrapRadians(angle - this.sectorCenter(i, count));
            if (Math.abs(delta) <= span / 2F - SECTOR_GAP / 2F) {
                return i;
            }
        }
        return -1;
    }

    private static double wrapRadians(double angle) {
        double wrapped = angle % Radians.R_2_PI;
        if (wrapped >= Radians.R_PI) {
            wrapped -= Radians.R_2_PI;
        }
        if (wrapped < -Radians.R_PI) {
            wrapped += Radians.R_2_PI;
        }
        return wrapped;
    }

    private boolean isOverCenter(double mouseX, double mouseY) {
        double dx = mouseX - this.centerX;
        double dy = mouseY - this.centerY;
        return dx * dx + dy * dy < this.innerRadius * this.innerRadius;
    }

    private float sectorCenter(int index, int count) {
        return -Radians.R_PI_2 + Radians.R_2_PI * index / count;
    }

    private float sectorStart(int index, int count) {
        return this.sectorCenter(index, count) - this.sectorSpan(count) / 2F - SECTOR_GAP / 2F;
    }

    private float sectorSpan(int count) {
        return Radians.R_2_PI / count - (count > 1 ? SECTOR_GAP : 0F);
    }

    private static boolean isInside(double mouseX, double mouseY, float x, float y, int size) {
        return Math.abs(mouseX - x) <= size / 2F && Math.abs(mouseY - y) <= size / 2F;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        HudController.restore();
        this.wasClosed = true;
        if (this.onCloseCallback != null) {
            this.onCloseCallback.run();
        }
        super.onClose();
    }

    @Override
    public void removed() {
        super.removed();
        this.shellEntities.clear();
        if (!this.wasClosed && this.onRemovedCallback != null) {
            this.onRemovedCallback.run();
        }
    }
}
