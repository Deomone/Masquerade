package dev.boy.masquerade.api;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PermissionResolver {
    boolean has(ServerPlayerEntity player, String node, int fallbackLevel);

    boolean has(ServerCommandSource source, String node, int fallbackLevel);
}
