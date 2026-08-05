package dev.boy.masquerade.api;

import dev.boy.masquerade.data.DisguiseIdentity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface DisguiseService {
    Optional<DisguiseIdentity> get(UUID playerUuid);

    boolean isDisguised(UUID playerUuid);

    Map<UUID, DisguiseIdentity> active();

    void apply(ServerPlayerEntity player, DisguiseIdentity identity);

    Optional<DisguiseIdentity> clear(ServerPlayerEntity player);
}
