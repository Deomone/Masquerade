package dev.boy.masquerade.util;

import dev.boy.masquerade.config.MasqueradeConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class WeaponMatcher {
    private WeaponMatcher() {
    }

    public static boolean matches(ItemStack weapon, MasqueradeConfig.HeadDrops settings) {
        if (weapon == null || weapon.isEmpty()) {
            return false;
        }
        if (settings.requireSwordOrAxe() && !isSwordOrAxe(weapon)) {
            return false;
        }
        return hasConfiguredName(weapon, settings.weaponNames());
    }

    public static boolean isSwordOrAxe(ItemStack weapon) {
        return weapon.is(ItemTags.SWORDS) || weapon.is(ItemTags.AXES);
    }

    private static boolean hasConfiguredName(ItemStack weapon, List<String> allowedNames) {
        Component customName = weapon.get(DataComponents.CUSTOM_NAME);
        if (customName == null) {
            return false;
        }
        String plain = customName.getString().trim();
        for (String allowed : allowedNames) {
            if (plain.equalsIgnoreCase(allowed.trim())) {
                return true;
            }
        }
        return false;
    }
}
