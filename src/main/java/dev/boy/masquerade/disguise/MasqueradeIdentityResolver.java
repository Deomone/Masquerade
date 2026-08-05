package dev.boy.masquerade.disguise;

import dev.boy.masquerade.api.IdentityResolver;
import dev.boy.masquerade.config.ConfigManager;
import dev.boy.masquerade.data.DisguiseIdentity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Optional;

public final class MasqueradeIdentityResolver implements IdentityResolver {
    private final ConfigManager config;
    private final DisguiseManager disguises;

    public MasqueradeIdentityResolver(ConfigManager config, DisguiseManager disguises) {
        this.config = config;
        this.disguises = disguises;
    }

    @Override
    public boolean isHidden(PlayerEntity player) {
        return config.get().enabled()
                && config.get().invisibility().enabled()
                && disguises.isHidden(player.getUuid());
    }

    @Override
    public Optional<Text> displayNameOverride(PlayerEntity player) {
        if (!config.get().enabled()) {
            return Optional.empty();
        }
        if (isHidden(player)) {
            return Optional.of(unknownName());
        }
        if (!config.get().disguise().enabled()) {
            return Optional.empty();
        }
        return disguises.get(player.getUuid()).map(identity -> Text.literal(identity.name()));
    }

    @Override
    public Text displayName(PlayerEntity player) {
        return displayNameOverride(player).orElseGet(player::getName);
    }

    @Override
    public String moderationName(PlayerEntity player) {
        String real = player.getGameProfile().name();
        if (isHidden(player)) {
            return "invisible " + real;
        }
        Optional<DisguiseIdentity> identity = disguises.get(player.getUuid());
        return identity.map(value -> real + " disguised as " + value.name()).orElse(real);
    }

    public Text unknownName() {
        return Text.literal(config.get().invisibility().unknownName()).formatted(Formatting.OBFUSCATED);
    }
}
