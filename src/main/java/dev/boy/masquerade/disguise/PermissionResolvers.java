package dev.boy.masquerade.disguise;

import dev.boy.masquerade.api.PermissionResolver;
import dev.boy.masquerade.util.MasqueradeLog;
import net.minecraft.server.command.ServerCommandSource;

public final class PermissionResolvers {
    private static final boolean NAMED_PERMISSIONS_AVAILABLE = detect();

    private PermissionResolvers() {
    }

    public static PermissionResolver create() {
        if (NAMED_PERMISSIONS_AVAILABLE) {
            MasqueradeLog.logger().info("Using named permission nodes");
            return new ModernPermissionResolver();
        }
        MasqueradeLog.logger().info("Named permissions unavailable on this Minecraft version, using operator levels");
        return new LegacyPermissionResolver();
    }

    public static boolean hasLevel(ServerCommandSource source, int level) {
        return NAMED_PERMISSIONS_AVAILABLE
                ? ModernPermissionResolver.hasLevel(source, level)
                : LegacyPermissionResolver.hasLevel(source, level);
    }

    private static boolean detect() {
        try {
            Class.forName("net.minecraft.command.permission.PermissionPredicate");
            ServerCommandSource.class.getMethod("getPermissions");
            return true;
        } catch (ClassNotFoundException | NoSuchMethodException exception) {
            return false;
        }
    }
}
