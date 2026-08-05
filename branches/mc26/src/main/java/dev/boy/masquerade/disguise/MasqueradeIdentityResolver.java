package dev.boy.masquerade.disguise;

import dev.boy.masquerade.api.IdentityResolver;
import dev.boy.masquerade.config.ConfigManager;
import dev.boy.masquerade.data.DisguiseIdentity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.Optional;

public final class MasqueradeIdentityResolver implements IdentityResolver {
    private final ConfigManager config;
    private final DisguiseManager disguises;

    public MasqueradeIdentityResolver(ConfigManager config, DisguiseManager disguises) {
        this.config = config;
        this.disguises = disguises;
    }

    @Override
    public boolean isHidden(Player player) {
        return config.get().enabled()
                && config.get().invisibility().enabled()
                && disguises.isHidden(player.getUUID());
    }

    @Override
    public Optional<Component> displayNameOverride(Player player) {
        if (!config.get().enabled()) {
            return Optional.empty();
        }
        if (isHidden(player)) {
            return Optional.of(unknownName());
        }
        if (!config.get().disguise().enabled()) {
            return Optional.empty();
        }
        return disguises.get(player.getUUID()).map(identity -> Component.literal(identity.name()));
    }

    @Override
    public Component displayName(Player player) {
        return displayNameOverride(player).orElseGet(player::getName);
    }

    @Override
    public String moderationName(Player player) {
        String real = player.getGameProfile().name();
        if (isHidden(player)) {
            return "invisible " + real;
        }
        Optional<DisguiseIdentity> identity = disguises.get(player.getUUID());
        return identity.map(value -> real + " disguised as " + value.name()).orElse(real);
    }

    public Component unknownName() {
        return Component.literal(config.get().invisibility().unknownName()).withStyle(ChatFormatting.OBFUSCATED);
    }
}
