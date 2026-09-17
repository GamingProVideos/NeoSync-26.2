package com.breakinblocks.neosync.compat.mekanism;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

final class MekanismRadiation {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Identifier ATTACHMENT_ID = Identifier.fromNamespaceAndPath("mekanism", "radiation");

    private static AttachmentType<?> attachment;
    private static boolean resolved;

    private MekanismRadiation() {}

    @Nullable
    static Double get(ServerPlayer player) {
        AttachmentType<?> type = attachment();
        if (type == null) {
            return null;
        }

        Object radiation = player.getExistingDataOrNull(type);
        return radiation instanceof Double dose ? dose : null;
    }

    @SuppressWarnings("unchecked")
    static void set(ServerPlayer player, @Nullable Double radiation) {
        AttachmentType<?> type = attachment();
        if (type == null) {
            return;
        }

        if (radiation == null) {
            player.removeData(type);
        } else if (player.getData(type) instanceof Double) {
            player.setData((AttachmentType<Double>)type, radiation);
        }
    }

    @Nullable
    private static synchronized AttachmentType<?> attachment() {
        if (!resolved) {
            resolved = true;
            attachment = NeoForgeRegistries.ATTACHMENT_TYPES.getValue(ATTACHMENT_ID);
            if (attachment == null) {
                LOGGER.warn("Mekanism is loaded but its {} attachment is missing; shells will keep the radiation of the body they replace", ATTACHMENT_ID);
            }
        }
        return attachment;
    }
}
