package dev.boy.masquerade.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.boy.masquerade.data.DisguiseIdentity;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Optional;

public final class PlayerHeads {
    private PlayerHeads() {
    }

    public static boolean isPlayerHead(ItemStack stack) {
        return stack != null && stack.getType() == Material.PLAYER_HEAD;
    }

    public static ItemStack create(DisguiseIdentity identity) {
        return create(identity.toProfile());
    }

    public static ItemStack create(PlayerProfile profile) {
        ItemStack stack = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) stack.getItemMeta();
        meta.setPlayerProfile(profile);
        stack.setItemMeta(meta);
        return stack;
    }

    public static Optional<DisguiseIdentity> identityOf(ItemStack stack) {
        if (!isPlayerHead(stack) || !(stack.getItemMeta() instanceof SkullMeta meta)) {
            return Optional.empty();
        }

        PlayerProfile profile = meta.getPlayerProfile();
        if (profile == null || profile.getId() == null || profile.getName() == null || profile.getName().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(DisguiseIdentity.of(profile));
    }
}
