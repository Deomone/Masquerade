package dev.boy.masquerade.disguise;

import dev.boy.masquerade.api.PermissionResolver;
import dev.boy.masquerade.util.MasqueradeLog;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

final class ModernPermissionResolver implements PermissionResolver {
    private final Map<String, Optional<Permission>> nodeCache = new ConcurrentHashMap<>();

    @Override
    public boolean has(ServerPlayer player, String node, int fallbackLevel) {
        return check(player.permissions(), node, fallbackLevel);
    }

    @Override
    public boolean has(CommandSourceStack source, String node, int fallbackLevel) {
        return check(source.permissions(), node, fallbackLevel);
    }

    static boolean hasLevel(CommandSourceStack source, int level) {
        return source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(level)));
    }

    private boolean check(PermissionSet permissions, String node, int fallbackLevel) {
        Optional<Permission> atom = nodeCache.computeIfAbsent(node, ModernPermissionResolver::parse);
        if (atom.isPresent() && permissions.hasPermission(atom.get())) {
            return true;
        }
        return permissions.hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(fallbackLevel)));
    }

    private static Optional<Permission> parse(String node) {
        Identifier identifier = Identifier.tryParse(node);
        if (identifier == null) {
            MasqueradeLog.warn("Permission node '{}' is not a valid identifier, using operator level only", node);
            return Optional.empty();
        }
        return Optional.of(Permission.Atom.create(identifier));
    }
}
