package dev.boy.masquerade.disguise;

import dev.boy.masquerade.MasqueradePlugin;
import dev.boy.masquerade.data.DisguiseIdentity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class IdentityService {
    private final MasqueradePlugin plugin;
    private final DisguiseManager disguises;

    public IdentityService(MasqueradePlugin plugin, DisguiseManager disguises) {
        this.plugin = plugin;
        this.disguises = disguises;
    }

    public boolean isHidden(Player player) {
        return plugin.config().enabled()
                && plugin.config().invisibility().enabled()
                && disguises.isHidden(player.getUniqueId());
    }

    public Component displayName(Player player) {
        if (isHidden(player)) {
            return unknownName();
        }
        return Component.text(player.getName());
    }

    public Optional<Component> displayNameOverride(Player player) {
        return isHidden(player) ? Optional.of(unknownName()) : Optional.empty();
    }

    public Component unknownName() {
        return Component.text(plugin.config().invisibility().unknownName())
                .decorate(TextDecoration.OBFUSCATED);
    }

    public String moderationName(Player player) {
        String real = disguises.realName(player);
        if (isHidden(player)) {
            return "invisible " + real;
        }
        Optional<DisguiseIdentity> identity = disguises.get(player.getUniqueId());
        return identity.map(value -> real + " disguised as " + value.name()).orElse(real);
    }
}
