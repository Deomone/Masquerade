package dev.boy.masquerade.disguise;

import dev.boy.masquerade.api.PermissionResolver;
import dev.boy.masquerade.util.MasqueradeLog;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

final class LegacyPermissionResolver implements PermissionResolver {
    private static final MethodHandle SOURCE_HAS_LEVEL = findSourceHandle();
    private static final MethodHandle PLAYER_LEVEL = findPlayerHandle();

    @Override
    public boolean has(ServerPlayerEntity player, String node, int fallbackLevel) {
        if (PLAYER_LEVEL != null) {
            try {
                return (int) PLAYER_LEVEL.invoke(player) >= fallbackLevel;
            } catch (Throwable throwable) {
                MasqueradeLog.warn("Legacy permission lookup failed for {}", player.getGameProfile().name());
            }
        }
        return false;
    }

    @Override
    public boolean has(ServerCommandSource source, String node, int fallbackLevel) {
        return hasLevel(source, fallbackLevel);
    }

    static boolean hasLevel(ServerCommandSource source, int level) {
        if (SOURCE_HAS_LEVEL != null) {
            try {
                return (boolean) SOURCE_HAS_LEVEL.invoke(source, level);
            } catch (Throwable throwable) {
                MasqueradeLog.warn("Legacy permission lookup failed for command source");
            }
        }
        return false;
    }

    private static MethodHandle findSourceHandle() {
        try {
            return MethodHandles.publicLookup().findVirtual(ServerCommandSource.class, "hasPermissionLevel",
                    MethodType.methodType(boolean.class, int.class));
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }

    private static MethodHandle findPlayerHandle() {
        try {
            return MethodHandles.publicLookup().findVirtual(ServerPlayerEntity.class, "getPermissionLevel",
                    MethodType.methodType(int.class));
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }
}
