package dev.boy.masquerade.client.rendering;

import dev.boy.masquerade.data.DisguiseIdentity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.PlayerSkin;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class DisguiseSkinCache {
    private static final DisguiseSkinCache INSTANCE = new DisguiseSkinCache();

    private final Map<UUID, Supplier<PlayerSkin>> suppliers = new ConcurrentHashMap<>();

    private DisguiseSkinCache() {
    }

    public static DisguiseSkinCache instance() {
        return INSTANCE;
    }

    public Optional<PlayerSkin> skinFor(DisguiseIdentity identity) {
        if (!identity.hasTextures()) {
            return Optional.empty();
        }
        Supplier<PlayerSkin> supplier = suppliers.computeIfAbsent(identity.uuid(), uuid ->
                Minecraft.getInstance().getSkinManager().createLookup(identity.toGameProfile(), false));
        return Optional.ofNullable(supplier.get());
    }

    public void clear() {
        suppliers.clear();
    }
}
