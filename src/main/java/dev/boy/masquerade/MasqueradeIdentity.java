package dev.boy.masquerade;

import dev.boy.masquerade.api.IdentityProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

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

    public static Optional<Text> displayNameOverride(PlayerEntity player) {
        IdentityProvider provider = player.getEntityWorld().isClient() ? clientProvider : serverProvider;
        return provider == null ? Optional.empty() : provider.displayNameOverride(player);
    }
}
