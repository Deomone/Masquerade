package dev.boy.masquerade.disguise;

import dev.boy.masquerade.api.PermissionResolver;
import net.minecraft.commands.CommandSourceStack;

public final class PermissionResolvers {
    private PermissionResolvers() {
    }

    public static PermissionResolver create() {
        return new ModernPermissionResolver();
    }

    public static boolean hasLevel(CommandSourceStack source, int level) {
        return ModernPermissionResolver.hasLevel(source, level);
    }
}
