package dev.boy.masquerade;

import dev.boy.masquerade.api.PermissionResolver;
import dev.boy.masquerade.config.ConfigManager;
import dev.boy.masquerade.data.DisguiseStorage;
import dev.boy.masquerade.disguise.DisguiseManager;
import dev.boy.masquerade.disguise.MasqueradeIdentityResolver;
import dev.boy.masquerade.disguise.ProfileResolver;
import dev.boy.masquerade.networking.DisguiseSynchronizer;
import dev.boy.masquerade.util.MasqueradeLog;
import net.minecraft.server.MinecraftServer;

import java.util.Optional;

public final class MasqueradeServer {
    private final MinecraftServer server;
    private final ConfigManager config;
    private final PermissionResolver permissions;
    private final DisguiseSynchronizer synchronizer;
    private final DisguiseManager disguises;
    private final MasqueradeIdentityResolver identities;
    private final ProfileResolver profiles;

    public MasqueradeServer(MinecraftServer server, ConfigManager config, PermissionResolver permissions) {
        this.server = server;
        this.config = config;
        this.permissions = permissions;
        this.synchronizer = new DisguiseSynchronizer(server, config, permissions);
        this.disguises = new DisguiseManager(server, config, DisguiseStorage.forServer(server), synchronizer);
        this.identities = new MasqueradeIdentityResolver(config, disguises);
        this.profiles = new ProfileResolver(server);
    }

    public ProfileResolver profiles() {
        return profiles;
    }

    public void refreshPlaceholderSkin() {
        String name = config.get().invisibility().placeholderSkin();
        if (name == null || name.isBlank()) {
            server.execute(() -> disguises.setPlaceholder(Optional.empty()));
            return;
        }

        profiles.resolve(name).thenAccept(identity -> server.execute(() -> {
            if (identity.isEmpty()) {
                MasqueradeLog.warn("Could not resolve placeholder skin '{}', invisible players keep their own skin", name);
            }
            disguises.setPlaceholder(identity);
        }));
    }

    public MinecraftServer server() {
        return server;
    }

    public ConfigManager config() {
        return config;
    }

    public PermissionResolver permissions() {
        return permissions;
    }

    public DisguiseSynchronizer synchronizer() {
        return synchronizer;
    }

    public DisguiseManager disguises() {
        return disguises;
    }

    public MasqueradeIdentityResolver identities() {
        return identities;
    }
}
