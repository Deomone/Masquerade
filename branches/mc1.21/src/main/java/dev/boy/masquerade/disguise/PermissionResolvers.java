package dev.boy.masquerade.disguise;

import dev.boy.masquerade.api.PermissionResolver;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public final class PermissionResolvers {
    private PermissionResolvers() {
    }

    public static PermissionResolver create() {
        return new LevelPermissionResolver();
    }

    public static boolean hasLevel(ServerCommandSource source, int level) {
        return source.hasPermissionLevel(level);
    }

    private static final class LevelPermissionResolver implements PermissionResolver {
        @Override
        public boolean has(ServerPlayerEntity player, String node, int fallbackLevel) {
            return player.hasPermissionLevel(fallbackLevel);
        }

        @Override
        public boolean has(ServerCommandSource source, String node, int fallbackLevel) {
            return source.hasPermissionLevel(fallbackLevel);
        }
    }
}
