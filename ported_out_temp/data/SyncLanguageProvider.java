package com.breakinblocks.neosync.data;

import com.breakinblocks.neosync.NeoSync;
import com.breakinblocks.neosync.common.block.SyncBlocks;
import com.breakinblocks.neosync.common.item.SyncItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public final class SyncLanguageProvider extends LanguageProvider {
    public SyncLanguageProvider(PackOutput output) {
        super(output, NeoSync.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("creativetab.neosync", "NeoSync");

        add(SyncBlocks.SHELL_STORAGE.get(), "Shell Storage");
        add(SyncBlocks.SHELL_CONSTRUCTOR.get(), "Shell Constructor");
        add(SyncBlocks.TREADMILL.get(), "Treadmill");

        add(SyncItems.SYNC_CORE.get(), "Sync Core");

        add("death.attack.neosync.fingerstick", "%s died in the name of science");

        add("command.neosync.ghostshells.invalid_action", "Available actions are: sync, remove and repair");
        add("command.neosync.ghostshells.not_found", "%s has no information about the shell that should be located at %s");
        add("command.neosync.ghostshells.repaired", "Successfully repaired %s's shell at %s");
        add("command.neosync.ghostshells.failed", "%s's shell cannot be repaired at %s. There's no shell container at the given coords");
        add("command.neosync.ghostshells.removed", "Successfully removed shell (%2$s) from %1$s's radial menu");

        add("event.neosync.request.fail.invalid.shell", "The selected shell is not owned by the current player");
        add("event.neosync.request.fail.invalid.location.current", "The sync process cannot be started at the current location");
        add("event.neosync.request.fail.invalid.location.target", "The selected shell cannot be found");
        add("event.neosync.construction.fail.health", "You don't have enough health to perform this operation");
        add("event.neosync.construction.fail.occupied", "This shell container is already occupied");
        add("event.neosync.any.fail.undead", "This operation cannot be performed by an undead player");

        add("gui.neosync.default.cross_button.title", "Close");
        add("gui.neosync.shell_selector.title", "Shell Selector");
        add("gui.neosync.shell_selector.up.title", "Previous world");
        add("gui.neosync.shell_selector.down.title", "Next world");
        add("gui.neosync.shell_selector.left.title", "Previous page");
        add("gui.neosync.shell_selector.right.title", "Next page");
        add("gui.neosync.shell_selector.progress_percent", "%s%%");
        add("gui.neosync.shell_selector.position", "%s, %s, %s");
        add("gui.neosync.page_display.pagination", "%s / %s");

        add("jade.neosync.owner", "Owner: %s");
        add("jade.neosync.progress", "Building: %s");
        add("jade.neosync.color", "Color: %s");
        add("jade.neosync.empty", "Empty");
        add("jade.neosync.powered", "Powered");
        add("jade.neosync.unpowered", "Unpowered");
        add("jade.neosync.idle", "Idle");
        add("jade.neosync.overheated", "Overheated");

        add("jei.neosync.category.treadmill_energy", "Treadmill Energy Sources");
        add("jei.neosync.energy_per_tick", "%s FE/tick");
        add("jei.neosync.info.sync_core", "The Sync Core is the key component in every shell-related block. Combine it with gray concrete to craft a Shell Constructor or a Shell Storage.");
        add("jei.neosync.info.shell_constructor", "Place the Shell Constructor, supply it with energy via a Treadmill (or any FE source), then right-click it to begin constructing a new shell from your genetic sample. Warning: the sampling process deals damage.");
        add("jei.neosync.info.shell_storage", "Place a Shell Storage and supply it with redstone power (or FE). Walk into it when the doors are open to pull up the radial shell menu and transfer your mind into a stored shell.");
        add("jei.neosync.info.treadmill", "Two-block structure. Lure a pig, wolf, or other supported animal onto the front block and it will generate energy that neighbouring Shell Constructors / Storages will consume.");

        add("config.jade.plugin_neosync.shell_container", "Shell Container");
        add("config.jade.plugin_neosync.treadmill", "Treadmill");

        add("advancements.neosync.root.title", "One Mind, Many Bodies");
        add("advancements.neosync.root.description", "Obtain a Sync Core and begin your journey into serial immortality.");
        add("advancements.neosync.place_constructor.title", "For Science");
        add("advancements.neosync.place_constructor.description", "Place a Shell Constructor. The lab is open.");
        add("advancements.neosync.place_storage.title", "Soul Cage");
        add("advancements.neosync.place_storage.description", "Place a Shell Storage. A vacant body awaits.");
        add("advancements.neosync.place_treadmill.title", "Piggawatts");
        add("advancements.neosync.place_treadmill.description", "Place a Treadmill. Livestock-powered industry.");
        add("advancements.neosync.first_sync.title", "Out of Body Experience");
        add("advancements.neosync.first_sync.description", "Transfer your consciousness into a shell for the first time.");
        add("advancements.neosync.cross_dim_sync.title", "Astral Projection");
        add("advancements.neosync.cross_dim_sync.description", "Sync into a shell resting in a different dimension.");
    }
}
