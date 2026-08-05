package dev.boy.masquerade.events;

import dev.boy.masquerade.MasqueradeServer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;

public final class VisibilityTracker {
    private final MasqueradeServer context;

    public VisibilityTracker(MasqueradeServer context) {
        this.context = context;
    }

    public void tick(ServerPlayer player) {
        if (!context.config().get().invisibility().enabled()) {
            return;
        }

        boolean invisible = player.hasEffect(MobEffects.INVISIBILITY);
        if (invisible == context.disguises().isHidden(player.getUUID())) {
            return;
        }

        context.disguises().setHidden(player, invisible);
        refreshListing(player);
    }

    private void refreshListing(ServerPlayer player) {
        if (!context.config().get().invisibility().hideFromPlayerList()) {
            return;
        }
        context.server().getPlayerList().broadcastAll(
                new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED, player));
    }
}
