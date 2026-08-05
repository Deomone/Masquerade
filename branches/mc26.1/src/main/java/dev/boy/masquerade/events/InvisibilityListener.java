package dev.boy.masquerade.events;

import dev.boy.masquerade.MasqueradePlugin;
import dev.boy.masquerade.util.Scheduling;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class InvisibilityListener implements Listener {
    private final MasqueradePlugin plugin;

    public InvisibilityListener(MasqueradePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (!plugin.config().enabled() || !plugin.config().invisibility().enabled()) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!isInvisibility(event)) {
            return;
        }

        Scheduling.entity(plugin, player, () -> {
            stripParticles(player);
            syncVisibility(player);
        });
    }

    public void syncVisibility(Player player) {
        boolean invisible = player.hasPotionEffect(PotionEffectType.INVISIBILITY);
        plugin.disguises().setHidden(player, invisible);
    }

    public void stripParticles(Player player) {
        if (!plugin.config().invisibility().hideParticles()) {
            return;
        }

        PotionEffect active = player.getPotionEffect(PotionEffectType.INVISIBILITY);
        if (active == null || !active.hasParticles()) {
            return;
        }

        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, active.getDuration(),
                active.getAmplifier(), active.isAmbient(), false, active.hasIcon()));
    }

    private boolean isInvisibility(EntityPotionEffectEvent event) {
        return (event.getNewEffect() != null && event.getNewEffect().getType().equals(PotionEffectType.INVISIBILITY))
                || (event.getOldEffect() != null && event.getOldEffect().getType().equals(PotionEffectType.INVISIBILITY));
    }
}
