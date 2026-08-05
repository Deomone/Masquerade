package dev.boy.masquerade.events;

import dev.boy.masquerade.MasqueradeServer;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;

public final class VisibilityTracker {
    private final MasqueradeServer context;

    public VisibilityTracker(MasqueradeServer context) {
        this.context = context;
    }

    public void tick(ServerPlayerEntity player) {
        if (!context.config().get().invisibility().enabled()) {
            return;
        }

        boolean invisible = player.hasStatusEffect(StatusEffects.INVISIBILITY);
        if (invisible == context.disguises().isHidden(player.getUuid())) {
            return;
        }

        context.disguises().setHidden(player, invisible);
        refreshListing(player);
    }

    private void refreshListing(ServerPlayerEntity player) {
        if (!context.config().get().invisibility().hideFromPlayerList()) {
            return;
        }
        context.server().getPlayerManager().sendToAll(
                new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_LISTED, player));
    }
}
