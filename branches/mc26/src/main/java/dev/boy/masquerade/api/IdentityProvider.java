package dev.boy.masquerade.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;

import java.util.Optional;

@FunctionalInterface
public interface IdentityProvider {
    Optional<Component> displayNameOverride(Player player);
}
