package dev.boy.masquerade.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.Optional;

public final class PlayerHeads {
    private PlayerHeads() {
    }

    public static boolean isPlayerHead(ItemStack stack) {
        return stack.isOf(Items.PLAYER_HEAD);
    }

    public static ItemStack create(GameProfile profile) {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        stack.set(DataComponentTypes.PROFILE, new ProfileComponent(profile));
        return stack;
    }

    public static Optional<ProfileComponent> componentOf(ItemStack stack) {
        if (!isPlayerHead(stack)) {
            return Optional.empty();
        }
        return Optional.ofNullable(stack.get(DataComponentTypes.PROFILE));
    }

    public static Optional<GameProfile> profileOf(ItemStack stack) {
        return componentOf(stack)
                .map(ProfileComponent::gameProfile)
                .filter(profile -> profile != null && profile.getName() != null && !profile.getName().isEmpty());
    }
}
