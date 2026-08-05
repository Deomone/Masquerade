package dev.boy.masquerade.util;

import dev.boy.masquerade.config.MasqueradeConfig;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;

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
        return weapon.isIn(ItemTags.SWORDS) || weapon.isIn(ItemTags.AXES);
    }

    private static boolean hasConfiguredName(ItemStack weapon, List<String> allowedNames) {
        Text customName = weapon.get(DataComponentTypes.CUSTOM_NAME);
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
