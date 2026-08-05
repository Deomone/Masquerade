package dev.boy.masquerade;

import dev.boy.masquerade.api.IdentityProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public final class MasqueradeIdentity {
    private static volatile IdentityProvider serverProvider;
    private static volatile IdentityProvider clientProvider;

    private MasqueradeIdentity() {
    }

    public static void installServer(IdentityProvider provider) {
        serverProvider = provider;
    }

    public static void installClient(IdentityProvider provider) {
        clientProvider = provider;
    }

    public static void uninstallServer() {
        serverProvider = null;
    }

    public static Optional<Component> displayNameOverride(Player player) {
        IdentityProvider provider = player.level().isClientSide() ? clientProvider : serverProvider;
        return provider == null ? Optional.empty() : provider.displayNameOverride(player);
    }
}
