package dev.boy.masquerade.disguise;

import dev.boy.masquerade.api.PermissionResolver;
import dev.boy.masquerade.util.MasqueradeLog;
import net.minecraft.command.permission.Permission;
import net.minecraft.command.permission.PermissionLevel;
import net.minecraft.command.permission.PermissionPredicate;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

final class ModernPermissionResolver implements PermissionResolver {
    private final Map<String, Optional<Permission>> nodeCache = new ConcurrentHashMap<>();

    @Override
    public boolean has(ServerPlayerEntity player, String node, int fallbackLevel) {
        return check(player.getPermissions(), node, fallbackLevel);
    }

    @Override
    public boolean has(ServerCommandSource source, String node, int fallbackLevel) {
        return check(source.getPermissions(), node, fallbackLevel);
    }

    static boolean hasLevel(ServerCommandSource source, int level) {
        return source.getPermissions().hasPermission(new Permission.Level(PermissionLevel.fromLevel(level)));
    }

    private boolean check(PermissionPredicate permissions, String node, int fallbackLevel) {
        Optional<Permission> atom = nodeCache.computeIfAbsent(node, ModernPermissionResolver::parse);
        if (atom.isPresent() && permissions.hasPermission(atom.get())) {
            return true;
        }
        return permissions.hasPermission(new Permission.Level(PermissionLevel.fromLevel(fallbackLevel)));
    }

    private static Optional<Permission> parse(String node) {
        Identifier identifier = Identifier.tryParse(node);
        if (identifier == null) {
            MasqueradeLog.warn("Permission node '{}' is not a valid identifier, using operator level only", node);
            return Optional.empty();
        }
        return Optional.of(Permission.Atom.of(identifier));
    }
}
