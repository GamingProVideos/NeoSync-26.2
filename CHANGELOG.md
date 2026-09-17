# NeoSync Changelog

## 1.7.0

### Added

- Manual Shell Storage, a storage whose shell is never chosen automatically when you die. Craft it from a Shell Storage and a lever. Walk into it to sync by hand like any other storage.
- A new config option, autoSyncOnDeath, that turns off automatic syncing on death for the whole server. Dying then shows the normal death screen and the player respawns as usual, with all shells still available.

### Fixed

- The camera flight when syncing into a body should now be smoother.

## 1.6.3

### Fixed

- Dying in an artificial body now respects the death message and mob forgiveness game rules.

## 1.6.2

### Fixed

- Dying in an artificial body now fires `LivingDeathEvent`. The death handler cancelled `ServerPlayer.die` at HEAD, ahead of NeoForge's hook, so grave and death-tracking mods never saw the death and the dropped inventory went to the ground uncaptured. A mod that cancels the event now keeps the body alive, as it does for an ordinary death.

## 1.6.1

### Added

- `/neosync purge <players> [dimension]` (op 2) deletes a player's shells, both the player-side entry and the block-side copy. Contained bodies drop their inventory and XP at the block. Omit the dimension to wipe every shell; a player left with none stops counting as artificial.

### Fixed

- Syncing out of a body that is not inside a Shell Storage or Constructor no longer throws server-side. The response packet wrapped a null stored shell in `Optional.of`; the client already handled an absent one. The sync itself had completed, so the visible symptom was a stale shell list and leftover entries in the selector.

## 1.6.0

### Added

- Optional Mekanism integration. Radiation is now stored per shell instead of following the player. A body left behind in a shell storage keeps the dose it absorbed, a shell out of the constructor starts clean, and an anchor respawn starts clean too. Reading and writing goes through Mekanism's radiation attachment, so no compile-time dependency is needed and the integration stays inactive when Mekanism is absent.

## 1.5.3

### Added

- isActive() reports whether the current server-thread call stack is inside a NeoSync sync teleport. Mods that police dimension changes can use it to recognise and permit sync travel without guessing from timing. Covers both the direct sync path and syncs completed through death and respawn.

### Fixed

- A sync whose target world does not exist now logs a warning instead of failing with no trace.

## 1.5.2

### Fixed

- A sync whose teleport is refused (by another mod cancelling the dimension change) no longer half-completes. Previously the player's current body was stored into the shell container, complete with a copy of their inventory, and the target shell was consumed before the teleport was attempted; a refused teleport then left the player where they stood with their items, a duplicate of that inventory inside the stored shell, and the target shell gone. The teleport now runs first, the target shell is only consumed after it succeeds, and a failed sync rolls back the stored shell and shows the standard failure message. Applies to both live syncs and syncs completed through death and respawn.

## 1.5.1

### Fixed

- Shell Storage, Shell Constructor, and their Zero Point variants no longer drop two items when broken. The block is two blocks tall and both halves rolled the same loot table; loot is now gated to the lower half, matching how vanilla handles doors. This also stops the stray item drop when breaking the bottom half in creative mode.
- Breaking the bottom half of a shell container no longer leaves the top half floating; the two halves are linked again after the 26.1 port dropped that handling.

## 1.5.0

- Shell rename support, retrimming, anchor and inventory fixes.
