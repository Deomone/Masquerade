package dev.boy.masquerade.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.Optional;

public interface IdentityResolver {
    boolean isHidden(PlayerEntity player);

    Optional<Text> displayNameOverride(PlayerEntity player);

    Text displayName(PlayerEntity player);

    String moderationName(PlayerEntity player);
}
