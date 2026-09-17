![Logo](media/logo.png)

# NeoSync

[![GitHub license](https://img.shields.io/github/license/pawjwp/sync-fabric.svg?cacheSeconds=36000)](LICENSE)

> One mind. Many bodies.

NeoSync provides *shells*; clones of the player, each with their own inventory, experience, and gamemode, that you can transfer your consciousness into. This is a **NeoForge 1.21.1** port of the Fabric reimplementation by [Kir_Antipov](https://github.com/Kir-Antipov/sync-fabric), of the original [Sync](https://github.com/iChun/Sync) mod by [iChun](https://github.com/iChun).

----

## How to play

1. Craft a **shell constructor** and place it.
2. Right-click it with an empty hand to provide a genetic sample.
   > ⚠️ With default config this will **kill you**. 20 HP (40 in hardcore). Eat a golden apple for more health, hold a totem of undying, or enable `warnPlayerInsteadOfKilling` in the config.
3. Power the constructor: place a **treadmill** touching any side of it, lure a **pig** or **wolf** onto the front block, and piggawatts flow.

   ![Working shell constructor](media/shell_constructor-showcase.png)

   > A comparator on the constructor tracks build progress, which is also displayed in Jade.

4. Once the shell is built, craft a **shell storage**, place it, and supply redstone power (or FE from any tech mod).
5. When the storage doors open, walk in. A radial menu appears with your shells:

   ![Menu example](media/menu-showcase.png)

6. Pick a shell. Sync.

## Notes

- Right-click a shell storage with **dye** to color-code it.
- Syncing works cross-dimensional (custom dimensions supported).
- If you die in a shell, you auto-sync back to your original body, or to a random remaining shell if the original is gone. Shell deaths **don't** count towards your death counter. The dead shell's inventory drops at its location — grave mods like Simple Tombs will capture it (see [Mod integration](#mod-integration) for caveats).
- Hoppers connected to a shell storage can equip or unequip armor/tools on the stored shell.
- Shell storage needs continuous power to keep its shell alive (configurable); accepts redstone and/or FE.
- Comparator output from a shell container reports either *build progress* or *inventory fullness*. Right-click the container with a **wrench** (stick by default) to toggle.
- Shell containers drop themselves when mined — any pickaxe works, no silk touch needed.

## Config

Config file: `config/neosync-common.toml`. Key options:

| Key | Default | Effect |
| --- | --- | --- |
| `enableInstantShellConstruction` | `false` | Instant shell builds in creative |
| `warnPlayerInsteadOfKilling` | `false` | Don't kill low-HP players on fingerstick |
| `fingerstickDamage` / `hardcoreFingerstickDamage` | `20` / `40` | HP consumed per shell |
| `shellConstructorCapacity` | `256000` | FE needed for a full shell |
| `shellStorageCapacity` / `shellStorageConsumption` | `320` / `16` | FE buffer + per-tick drain keeping a shell alive |
| `shellStorageAcceptsRedstone` | `true` | Accept raw redstone as power |
| `shellStorageMaxUnpoweredLifespan` | `20` | Ticks a storage keeps its shell alive without power |
| `energyMap` | chicken=2, pig=16, player=20, wolf=22, villager=25, creeper=80, enderman=160 | FE/tick per entity on a treadmill |
| `syncPriority` | `NATURAL` | Which shell to pick on death. Values: `NATURAL`, `NEAREST`, or any dye color |
| `wrench` | `minecraft:stick` | Item that cycles a container's comparator output type |

## Commands

All commands are listed under `/neosync`. All of it needs gamemaster permission; `anchor` and `ghostshells` are also available to the host in single player.

### `/neosync select [<targets>]`

Opens the shell radial menu wherever the player is standing, with no shell storage needed, and lets them sync straight into any finished shell. With no argument it targets the sender. Players with no finished shell are skipped with a message. Returns the number of menus opened.

Standing in an empty shell storage still works the normal way: the body stays in the storage with everything it was carrying, and the player wakes up in the shell they picked.

Away from a shell storage there is no container to leave the body in, so the player is killed and moved into the shell they picked once they respawn. The death is an ordinary one, which means grave mods keep the items and xp, and a normal death drop happens when there is no grave mod. A player who is already dead can pick a shell too, from the death screen: nothing happens until they respawn, and then they arrive in that shell instead of at their spawn point. This is what makes a single use anchor cover exactly one death.

### `/neosync anchor set <targets> <dimension> <x y z> [<temporary>]`

Gives each target a respawn anchor at the given spot. An anchor behaves like a shell in the radial menu, but there is no block involved: syncing to it creates a fresh clone with full health and an empty inventory at those coordinates. Setting a second anchor at the same spot in the same dimension replaces the first.

Pass `true` for `temporary` to make it single use. The anchor is removed the instant the player syncs into it, so it covers exactly one death. Omit the argument (or pass `false`) for a permanent anchor. Returns the number of players given an anchor.

### `/neosync anchor ensure <targets> <dimension> <x y z>`

Same as `set ... true`, but only acts on players who currently have nothing to sync into, meaning no finished shell and no existing anchor. Players who still have somewhere to go are left alone. It is safe to run repeatedly, so it works well on a login hook or a timer to keep players from being stranded. Returns the number of players actually given an anchor, which is `0` when everyone already had a shell.

### `/neosync anchor list <targets>`

Lists each target's anchors, marking the single-use ones. Returns the total anchor count across all targets.

### `/neosync anchor remove <targets> [<dimension> <x y z>]`

Removes anchors from the targets. With coordinates, only the anchor at that spot goes; without them, all of the target's anchors go. Returns the number removed.

### `/neosync ghostshells <sync|remove|repair> <targets> [<x y z>]`

Cleans up shells that show in a player's menu but no longer exist in the world, usually after a shell storage was destroyed or a chunk was rolled back. Anchors are never touched. Give coordinates to act on one shell, or leave them off to sweep every shell the player has.

- `sync` repairs what it can and deletes the rest.
- `repair` repairs what it can and reports the rest without deleting anything.
- `remove` deletes ghost shells without attempting a repair.

Note that the sweep without coordinates also marks every one of that player's shells as fully built, so any shell still under construction finishes immediately.

## Mod integration

- **[JEI](https://www.curseforge.com/minecraft/mc-mods/jei)**. info descriptions on each sync block explaining the flow, plus a *Treadmill Energy Sources* category listing every entity the treadmill accepts and its FE/tick output (driven by `energyMap`).
- **[Jade](https://www.curseforge.com/minecraft/mc-mods/jade)**. crosshair tooltip for shell constructor / storage / treadmill showing owner, build progress, color, powered state, and energy level.

Both are optional; NeoSync runs fine without them.

### Grave / death-handling mods (Simple Tombs, etc.)

NeoSync coexists with grave mods, but only the parts that hook `LivingDropsEvent` work in the cross-shell death path:

- **Original-body death** uses the vanilla death flow. `LivingDeathEvent`, `LivingDropsEvent`, and `PlayerRespawnEvent` all fire normally, so grave mods behave exactly as they would without NeoSync.
- **Shell death with another shell available** is intercepted by NeoSync: vanilla `die()` is cancelled and the player auto-syncs into the next shell. `LivingDropsEvent` still fires (so the grave is placed at the dead shell's position with its full inventory), but `LivingDeathEvent` and `PlayerRespawnEvent` do **not** fire.
- **Syncing away from a shell storage** is a plain vanilla death followed by a move on respawn, so all three events fire and grave mods behave exactly as they would on any other death.

For Simple Tombs specifically:

- Graves are placed correctly at the dead shell's location and hold its full inventory. walk to the grave to retrieve.
- The `KEEPPARTS` option (hotbar/armor soulbinding) does **not** carry across a cross-shell auto-sync; those items go into the grave with everything else. If you want consistent behavior across both death paths, set `KEEPPARTS=NONE` in the Simple Tombs config and rely on the grave for everything.
- The grave key (if `KEYGIVEN=true`) lands in the grave alongside the rest of the loot.

If you need to request specific integration with other mods or graves feel free to reach out.

## License

MIT. Code by [Kir_Antipov](https://github.com/Kir-Antipov); NeoForge 1.21.1 port by BreakinBlocks. Original concept by [iChun](https://github.com/iChun). See [LICENSE](LICENSE) for details.
