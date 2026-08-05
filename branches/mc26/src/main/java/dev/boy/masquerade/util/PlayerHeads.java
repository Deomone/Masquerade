package dev.boy.masquerade.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Optional;

public final class PlayerHeads {
    private PlayerHeads() {
    }

    public static boolean isPlayerHead(ItemStack stack) {
        return stack.is(Items.PLAYER_HEAD);
    }

    public static ItemStack create(GameProfile profile) {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        stack.set(DataComponents.PROFILE, ResolvableProfile.createResolved(profile));
        return stack;
    }

    public static Optional<ResolvableProfile> componentOf(ItemStack stack) {
        if (!isPlayerHead(stack)) {
            return Optional.empty();
        }
        return Optional.ofNullable(stack.get(DataComponents.PROFILE));
    }

    public static Optional<GameProfile> profileOf(ItemStack stack) {
        return componentOf(stack)
                .map(ResolvableProfile::partialProfile)
                .filter(profile -> profile != null && profile.name() != null && !profile.name().isEmpty());
    }
}
