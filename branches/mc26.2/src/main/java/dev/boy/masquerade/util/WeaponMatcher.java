package dev.boy.masquerade.util;

import dev.boy.masquerade.config.PluginConfig;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class WeaponMatcher {
    private WeaponMatcher() {
    }

    public static boolean matches(ItemStack weapon, PluginConfig.HeadDrops settings) {
        if (weapon == null || weapon.getType().isAir()) {
            return false;
        }
        if (settings.requireSwordOrAxe() && !isSwordOrAxe(weapon)) {
            return false;
        }
        return hasConfiguredName(weapon, settings.weaponNames());
    }

    public static boolean isSwordOrAxe(ItemStack weapon) {
        return Tag.ITEMS_SWORDS.isTagged(weapon.getType()) || Tag.ITEMS_AXES.isTagged(weapon.getType());
    }

    private static boolean hasConfiguredName(ItemStack weapon, List<String> allowedNames) {
        if (!weapon.hasItemMeta() || !weapon.getItemMeta().hasDisplayName()) {
            return false;
        }

        String plain = PlainTextComponentSerializer.plainText()
                .serialize(weapon.getItemMeta().displayName())
                .trim();
        for (String allowed : allowedNames) {
            if (plain.equalsIgnoreCase(allowed.trim())) {
                return true;
            }
        }
        return false;
    }
}
