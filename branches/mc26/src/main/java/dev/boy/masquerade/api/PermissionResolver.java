package dev.boy.masquerade.api;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public interface PermissionResolver {
    boolean has(ServerPlayer player, String node, int fallbackLevel);

    boolean has(CommandSourceStack source, String node, int fallbackLevel);
}
