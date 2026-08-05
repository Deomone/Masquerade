package dev.boy.masquerade.events;

import dev.boy.masquerade.MasqueradeServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class MasqueradeTicker {
    private static final int SAVE_INTERVAL_TICKS = 20 * 60;

    private final MasqueradeServer context;
    private final HelmetSlotWatcher helmets;
    private final VisibilityTracker visibility;

    private int sinceSave;

    public MasqueradeTicker(MasqueradeServer context, HelmetSlotWatcher helmets, VisibilityTracker visibility) {
        this.context = context;
        this.helmets = helmets;
        this.visibility = visibility;
    }

    public void tick() {
        if (!context.config().get().enabled()) {
            return;
        }

        for (ServerPlayerEntity player : context.server().getPlayerManager().getPlayerList()) {
            helmets.tick(player);
            visibility.tick(player);
        }

        if (++sinceSave >= SAVE_INTERVAL_TICKS) {
            sinceSave = 0;
            context.disguises().saveIfDirty();
        }
    }
}
