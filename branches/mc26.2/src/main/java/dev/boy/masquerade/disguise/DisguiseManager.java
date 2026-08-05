package dev.boy.masquerade.disguise;

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.boy.masquerade.MasqueradePlugin;
import dev.boy.masquerade.data.DisguiseIdentity;
import dev.boy.masquerade.data.DisguiseStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class DisguiseManager {
    private final Map<UUID, DisguiseIdentity> disguises = new HashMap<>();
    private final Map<UUID, DisguiseIdentity> originals = new HashMap<>();
    private final Map<UUID, String> realNames = new HashMap<>();
    private final Set<UUID> hidden = new HashSet<>();

    private final MasqueradePlugin plugin;
    private final DisguiseStorage storage;

    private Optional<DisguiseIdentity> placeholder = Optional.empty();
    private boolean dirty;

    public DisguiseManager(MasqueradePlugin plugin, DisguiseStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    public void loadFromDisk() {
        disguises.clear();
        if (plugin.config().disguise().persistAcrossSessions()) {
            disguises.putAll(storage.load());
        }
        dirty = false;
    }

    public void saveToDisk() {
        if (plugin.config().disguise().persistAcrossSessions()) {
            storage.save(Map.copyOf(disguises));
        }
        dirty = false;
    }

    public void saveIfDirty() {
        if (dirty) {
            saveToDisk();
        }
    }

    public void remember(Player player) {
        UUID uuid = player.getUniqueId();
        realNames.put(uuid, player.getName());
        originals.putIfAbsent(uuid, DisguiseIdentity.of(player.getPlayerProfile()));
    }

    public void forget(UUID uuid) {
        hidden.remove(uuid);
        realNames.remove(uuid);
        originals.remove(uuid);
    }

    public String realName(Player player) {
        return realNames.getOrDefault(player.getUniqueId(), player.getName());
    }

    public Optional<Player> findByVisibleName(String name) {
        Player fallback = null;
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!visibleName(player).equalsIgnoreCase(name)) {
                continue;
            }
            if (realName(player).equalsIgnoreCase(name)) {
                return Optional.of(player);
            }
            if (fallback == null) {
                fallback = player;
            }
        }
        return Optional.ofNullable(fallback);
    }

    public Optional<Player> findByRealName(String name) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (realName(player).equalsIgnoreCase(name)) {
                return Optional.of(player);
            }
        }
        return Optional.empty();
    }

    public Optional<DisguiseIdentity> originalIdentity(Player player) {
        return Optional.ofNullable(originals.get(player.getUniqueId()));
    }

    public String visibleName(Player player) {
        PlayerProfile profile = player.getPlayerProfile();
        return profile.getName() == null ? player.getName() : profile.getName();
    }

    public Optional<DisguiseIdentity> get(UUID uuid) {
        return Optional.ofNullable(disguises.get(uuid));
    }

    public boolean isDisguised(UUID uuid) {
        return disguises.containsKey(uuid);
    }

    public Map<UUID, DisguiseIdentity> active() {
        return Collections.unmodifiableMap(disguises);
    }

    public boolean isHidden(UUID uuid) {
        return hidden.contains(uuid);
    }

    public Optional<DisguiseIdentity> placeholder() {
        return placeholder;
    }

    public void setPlaceholder(Optional<DisguiseIdentity> identity) {
        this.placeholder = identity;
        refreshAll();
    }

    public void apply(Player player, DisguiseIdentity identity) {
        disguises.put(player.getUniqueId(), identity);
        dirty = true;
        plugin.getLogger().info(realName(player) + " is now disguised as " + identity.name());
        refresh(player);
    }

    public Optional<DisguiseIdentity> clear(Player player) {
        DisguiseIdentity removed = disguises.remove(player.getUniqueId());
        if (removed == null) {
            return Optional.empty();
        }
        dirty = true;
        plugin.getLogger().info(realName(player) + " is no longer disguised as " + removed.name());
        refresh(player);
        return Optional.of(removed);
    }

    public Optional<DisguiseIdentity> clearOffline(UUID uuid) {
        DisguiseIdentity removed = disguises.remove(uuid);
        if (removed != null) {
            dirty = true;
        }
        return Optional.ofNullable(removed);
    }

    public void setHidden(Player player, boolean value) {
        boolean changed = value ? hidden.add(player.getUniqueId()) : hidden.remove(player.getUniqueId());
        if (changed) {
            refresh(player);
        }
    }

    public void restoreOriginalProfile(Player player) {
        DisguiseIdentity original = originals.get(player.getUniqueId());
        if (original == null) {
            return;
        }

        player.setPlayerProfile(original.toProfile(player.getUniqueId(), original.name()));
        player.displayName(Component.text(original.name()));
        player.playerListName(null);
    }

    public void refreshAll() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            refresh(player);
        }
    }

    public void refresh(Player player) {
        UUID uuid = player.getUniqueId();
        DisguiseIdentity original = originals.get(uuid);
        if (original == null) {
            return;
        }

        boolean masked = plugin.config().enabled();
        boolean invisible = masked && plugin.config().invisibility().enabled() && hidden.contains(uuid);
        DisguiseIdentity disguise = masked && plugin.config().disguise().enabled() ? disguises.get(uuid) : null;

        DisguiseIdentity source;
        String displayName;
        if (invisible) {
            source = placeholder.orElse(original);
            displayName = trimName(plugin.config().invisibility().unknownName());
        } else if (disguise != null) {
            source = disguise;
            displayName = disguise.name();
        } else {
            source = original;
            displayName = original.name();
        }

        PlayerProfile profile = source.toProfile(uuid, displayName);
        player.setPlayerProfile(profile);

        Component visible = visibleComponent(invisible, displayName);
        player.playerListName(invisible ? visible : null);
        player.displayName(visible);
    }

    private Component visibleComponent(boolean invisible, String displayName) {
        Component name = Component.text(displayName);
        return invisible ? name.decorate(TextDecoration.OBFUSCATED) : name;
    }

    private static String trimName(String value) {
        String cleaned = value == null || value.isBlank() ? "Unknown" : value;
        return cleaned.length() > 16 ? cleaned.substring(0, 16) : cleaned;
    }
}
