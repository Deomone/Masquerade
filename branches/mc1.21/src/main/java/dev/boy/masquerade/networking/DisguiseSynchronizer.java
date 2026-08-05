package dev.boy.masquerade.networking;

import dev.boy.masquerade.api.PermissionResolver;
import dev.boy.masquerade.config.ConfigManager;
import dev.boy.masquerade.config.MasqueradeConfig;
import dev.boy.masquerade.data.DisguiseIdentity;
import dev.boy.masquerade.data.DisguiseState;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.Optional;

public final class DisguiseSynchronizer {
    private final MinecraftServer server;
    private final ConfigManager config;
    private final PermissionResolver permissions;

    public DisguiseSynchronizer(MinecraftServer server, ConfigManager config, PermissionResolver permissions) {
        this.server = server;
        this.config = config;
        this.permissions = permissions;
    }

    public boolean canReveal(ServerPlayerEntity viewer) {
        MasqueradeConfig.Permissions settings = config.get().permissions();
        return permissions.has(viewer, settings.revealNode(), settings.revealFallbackLevel());
    }

    public void broadcast(DisguiseState plain, DisguiseState revealed) {
        for (ServerPlayerEntity viewer : server.getPlayerManager().getPlayerList()) {
            sendUpdate(viewer, canReveal(viewer) ? revealed : plain);
        }
    }

    public void sendUpdate(ServerPlayerEntity viewer, DisguiseState state) {
        if (ServerPlayNetworking.canSend(viewer, MasqueradePayloads.StateUpdate.ID)) {
            ServerPlayNetworking.send(viewer, new MasqueradePayloads.StateUpdate(state));
        }
    }

    public void sendSnapshot(ServerPlayerEntity viewer, List<DisguiseState> states,
                             Optional<DisguiseIdentity> placeholder) {
        if (ServerPlayNetworking.canSend(viewer, MasqueradePayloads.StateSnapshot.ID)) {
            String unknownName = config.get().invisibility().unknownName();
            ServerPlayNetworking.send(viewer,
                    new MasqueradePayloads.StateSnapshot(unknownName, placeholder, states));
        }
    }
}
