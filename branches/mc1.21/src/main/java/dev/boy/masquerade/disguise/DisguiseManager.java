package dev.boy.masquerade.disguise;

import dev.boy.masquerade.api.DisguiseService;
import dev.boy.masquerade.config.ConfigManager;
import dev.boy.masquerade.data.DisguiseIdentity;
import dev.boy.masquerade.data.DisguiseState;
import dev.boy.masquerade.data.DisguiseStorage;
import dev.boy.masquerade.networking.DisguiseSynchronizer;
import dev.boy.masquerade.util.MasqueradeLog;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class DisguiseManager implements DisguiseService {
    private final Map<UUID, DisguiseIdentity> disguises = new HashMap<>();
    private final Set<UUID> hidden = new HashSet<>();

    private final MinecraftServer server;
    private final ConfigManager config;
    private final DisguiseStorage storage;
    private final DisguiseSynchronizer synchronizer;

    private boolean dirty;
    private Optional<DisguiseIdentity> placeholder = Optional.empty();

    public DisguiseManager(MinecraftServer server, ConfigManager config, DisguiseStorage storage, DisguiseSynchronizer synchronizer) {
        this.server = server;
        this.config = config;
        this.storage = storage;
        this.synchronizer = synchronizer;
    }

    public void loadFromDisk() {
        disguises.clear();
        if (config.get().disguise().persistAcrossSessions()) {
            disguises.putAll(storage.load());
            MasqueradeLog.debug(config, "Loaded {} persisted disguises", disguises.size());
        }
        dirty = false;
    }

    public void saveToDisk() {
        if (!config.get().disguise().persistAcrossSessions()) {
            return;
        }
        storage.save(Map.copyOf(disguises));
        dirty = false;
    }

    public void saveIfDirty() {
        if (dirty) {
            saveToDisk();
        }
    }

    @Override
    public Optional<DisguiseIdentity> get(UUID playerUuid) {
        return Optional.ofNullable(disguises.get(playerUuid));
    }

    @Override
    public boolean isDisguised(UUID playerUuid) {
        return disguises.containsKey(playerUuid);
    }

    @Override
    public Map<UUID, DisguiseIdentity> active() {
        return Collections.unmodifiableMap(disguises);
    }

    @Override
    public void apply(ServerPlayerEntity player, DisguiseIdentity identity) {
        disguises.put(player.getUuid(), identity);
        dirty = true;
        MasqueradeLog.info("{} is now disguised as {}", player.getGameProfile().getName(), identity.name());
        broadcast(player);
    }

    @Override
    public Optional<DisguiseIdentity> clear(ServerPlayerEntity player) {
        DisguiseIdentity removed = disguises.remove(player.getUuid());
        if (removed == null) {
            return Optional.empty();
        }
        dirty = true;
        MasqueradeLog.info("{} is no longer disguised as {}", player.getGameProfile().getName(), removed.name());
        broadcast(player);
        return Optional.of(removed);
    }

    public Optional<DisguiseIdentity> clearOffline(UUID playerUuid) {
        DisguiseIdentity removed = disguises.remove(playerUuid);
        if (removed != null) {
            dirty = true;
        }
        return Optional.ofNullable(removed);
    }

    public boolean isHidden(UUID playerUuid) {
        return hidden.contains(playerUuid);
    }

    public void setHidden(ServerPlayerEntity player, boolean value) {
        boolean changed = value ? hidden.add(player.getUuid()) : hidden.remove(player.getUuid());
        if (changed) {
            MasqueradeLog.debug(config, "{} visibility changed, hidden={}", player.getGameProfile().getName(), value);
            broadcast(player);
        }
    }

    public void forget(UUID playerUuid) {
        hidden.remove(playerUuid);
    }

    public String visibleName(ServerPlayerEntity player) {
        String real = player.getGameProfile().getName();
        if (!config.get().enabled()) {
            return real;
        }
        if (config.get().invisibility().enabled() && hidden.contains(player.getUuid())) {
            return config.get().invisibility().unknownName();
        }
        if (!config.get().disguise().enabled()) {
            return real;
        }
        DisguiseIdentity identity = disguises.get(player.getUuid());
        return identity == null ? real : identity.name();
    }

    public Optional<ServerPlayerEntity> findByVisibleName(String name) {
        ServerPlayerEntity fallback = null;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (!visibleName(player).equalsIgnoreCase(name)) {
                continue;
            }
            if (player.getGameProfile().getName().equalsIgnoreCase(name)) {
                return Optional.of(player);
            }
            if (fallback == null) {
                fallback = player;
            }
        }
        return Optional.ofNullable(fallback);
    }

    public boolean isHiddenBehindOtherName(ServerPlayerEntity player) {
        return !visibleName(player).equalsIgnoreCase(player.getGameProfile().getName());
    }

    public DisguiseState stateFor(ServerPlayerEntity player, boolean reveal) {
        if (!config.get().enabled()) {
            return DisguiseState.cleared(player.getUuid());
        }
        return new DisguiseState(
                player.getUuid(),
                get(player.getUuid()),
                isHidden(player.getUuid()),
                reveal ? Optional.of(player.getGameProfile().getName()) : Optional.empty()
        );
    }

    public void broadcast(ServerPlayerEntity player) {
        synchronizer.broadcast(stateFor(player, false), stateFor(player, true));
    }

    public Optional<DisguiseIdentity> placeholder() {
        return placeholder;
    }

    public void setPlaceholder(Optional<DisguiseIdentity> identity) {
        this.placeholder = identity;
        for (ServerPlayerEntity viewer : server.getPlayerManager().getPlayerList()) {
            sendSnapshot(viewer);
        }
    }

    public void sendSnapshot(ServerPlayerEntity viewer) {
        boolean reveal = synchronizer.canReveal(viewer);
        List<DisguiseState> states = new ArrayList<>();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            DisguiseState state = stateFor(player, reveal);
            if (!state.isEmpty()) {
                states.add(state);
            }
        }
        synchronizer.sendSnapshot(viewer, states, config.get().enabled() ? placeholder : Optional.empty());
    }
}
