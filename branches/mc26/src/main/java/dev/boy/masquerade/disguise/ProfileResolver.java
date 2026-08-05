package dev.boy.masquerade.disguise;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileResult;
import dev.boy.masquerade.data.DisguiseIdentity;
import dev.boy.masquerade.util.MasqueradeLog;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class ProfileResolver {
    private final MinecraftServer server;
    private final Map<String, DisguiseIdentity> cache = new ConcurrentHashMap<>();

    public ProfileResolver(MinecraftServer server) {
        this.server = server;
    }

    public Optional<DisguiseIdentity> online(String name) {
        ServerPlayer player = server.getPlayerList().getPlayer(name);
        return player == null ? Optional.empty() : Optional.of(DisguiseIdentity.of(player.getGameProfile()));
    }

    public CompletableFuture<Optional<DisguiseIdentity>> resolve(String name) {
        Optional<DisguiseIdentity> live = online(name);
        if (live.isPresent()) {
            return CompletableFuture.completedFuture(live);
        }

        DisguiseIdentity cached = cache.get(key(name));
        if (cached != null) {
            return CompletableFuture.completedFuture(Optional.of(cached));
        }

        return CompletableFuture.supplyAsync(() -> fetch(name), Util.ioPool());
    }

    private Optional<DisguiseIdentity> fetch(String name) {
        Optional<GameProfile> base = server.services().profileResolver().fetchByName(name);
        if (base.isEmpty()) {
            return Optional.empty();
        }

        GameProfile profile = base.get();
        if (profile.properties().isEmpty()) {
            profile = withTextures(profile);
        }

        DisguiseIdentity identity = DisguiseIdentity.of(profile);
        cache.put(key(name), identity);
        return Optional.of(identity);
    }

    private GameProfile withTextures(GameProfile profile) {
        try {
            ProfileResult result = server.services().sessionService().fetchProfile(profile.id(), true);
            if (result != null && result.profile() != null) {
                return result.profile();
            }
        } catch (RuntimeException exception) {
            MasqueradeLog.warn("Could not fetch skin for {}, disguise will use the default skin", profile.name());
        }
        return profile;
    }

    public void invalidate() {
        cache.clear();
    }

    private static String key(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
