package dev.boy.masquerade;

import dev.boy.masquerade.disguise.MasqueradeIdentityResolver;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.Optional;

public final class MasqueradeServices {
    private static volatile MasqueradeServer current;

    private MasqueradeServices() {
    }

    public static void install(MasqueradeServer server) {
        current = server;
    }

    public static void uninstall() {
        current = null;
    }

    public static Optional<MasqueradeServer> get() {
        return Optional.ofNullable(current);
    }

    public static Optional<Text> displayNameOverride(PlayerEntity player) {
        MasqueradeServer server = current;
        if (server == null) {
            return Optional.empty();
        }
        return server.identities().displayNameOverride(player);
    }

    public static Optional<MasqueradeIdentityResolver> identities() {
        MasqueradeServer server = current;
        return server == null ? Optional.empty() : Optional.of(server.identities());
    }
}
