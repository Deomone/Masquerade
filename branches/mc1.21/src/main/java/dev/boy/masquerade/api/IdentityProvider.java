package dev.boy.masquerade.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.Optional;

@FunctionalInterface
public interface IdentityProvider {
    Optional<Text> displayNameOverride(PlayerEntity player);
}
