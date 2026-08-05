package dev.boy.masquerade.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public interface IdentityResolver {
    boolean isHidden(Player player);

    Optional<Component> displayNameOverride(Player player);

    Component displayName(Player player);

    String moderationName(Player player);
}
