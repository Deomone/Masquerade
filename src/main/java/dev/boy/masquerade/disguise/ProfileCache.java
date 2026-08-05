package dev.boy.masquerade.disguise;

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.boy.masquerade.MasqueradePlugin;
import dev.boy.masquerade.data.DisguiseIdentity;
import dev.boy.masquerade.util.Scheduling;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class ProfileCache {
    private final MasqueradePlugin plugin;
    private final Map<String, DisguiseIdentity> cache = new ConcurrentHashMap<>();

    public ProfileCache(MasqueradePlugin plugin) {
        this.plugin = plugin;
    }

    public Optional<DisguiseIdentity> online(String name) {
        Player direct = Bukkit.getPlayerExact(name);
        Player player = direct != null ? direct : plugin.disguises().findByRealName(name).orElse(null);
        if (player == null) {
            return Optional.empty();
        }

        Optional<DisguiseIdentity> original = plugin.disguises().originalIdentity(player);
        return original.isPresent() ? original : Optional.of(DisguiseIdentity.of(player.getPlayerProfile()));
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

        CompletableFuture<Optional<DisguiseIdentity>> future = new CompletableFuture<>();
        Scheduling.async(plugin, () -> future.complete(fetch(name)));
        return future;
    }

    private Optional<DisguiseIdentity> fetch(String name) {
        try {
            PlayerProfile profile = Bukkit.createProfile(name);
            profile.complete(true, true);
            if (!profile.hasTextures()) {
                PlayerProfile updated = profile.update().join();
                if (updated != null && updated.hasTextures()) {
                    profile = updated;
                }
            }

            if (profile.getId() == null || profile.getName() == null || profile.getName().isEmpty()) {
                plugin.getLogger().warning("Could not resolve a profile named " + name);
                return Optional.empty();
            }
            if (!profile.hasTextures()) {
                plugin.getLogger().warning("Profile " + profile.getName()
                        + " resolved without a skin, the disguise will use the default skin");
            }

            DisguiseIdentity identity = DisguiseIdentity.of(profile);
            cache.put(key(name), identity);
            return Optional.of(identity);
        } catch (RuntimeException exception) {
            plugin.getLogger().warning("Could not resolve profile for " + name + ": " + exception.getMessage());
            return Optional.empty();
        }
    }

    public void invalidate() {
        cache.clear();
    }

    private static String key(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
