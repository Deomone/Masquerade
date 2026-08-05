package dev.boy.masquerade.client.rendering;

import dev.boy.masquerade.data.DisguiseIdentity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.SkinTextures;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class DisguiseSkinCache {
    private static final DisguiseSkinCache INSTANCE = new DisguiseSkinCache();

    private final Map<UUID, Supplier<SkinTextures>> suppliers = new ConcurrentHashMap<>();

    private DisguiseSkinCache() {
    }

    public static DisguiseSkinCache instance() {
        return INSTANCE;
    }

    public Optional<SkinTextures> skinFor(DisguiseIdentity identity) {
        if (!identity.hasTextures()) {
            return Optional.empty();
        }
        Supplier<SkinTextures> supplier = suppliers.computeIfAbsent(identity.uuid(), uuid ->
                MinecraftClient.getInstance().getSkinProvider().getSkinTexturesSupplier(identity.toGameProfile()));
        return Optional.ofNullable(supplier.get());
    }

    public void clear() {
        suppliers.clear();
    }
}
