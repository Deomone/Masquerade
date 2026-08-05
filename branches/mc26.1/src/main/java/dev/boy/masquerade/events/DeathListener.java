package dev.boy.masquerade.events;

import dev.boy.masquerade.MasqueradePlugin;
import dev.boy.masquerade.data.DisguiseIdentity;
import dev.boy.masquerade.util.PlayerHeads;
import dev.boy.masquerade.util.WeaponMatcher;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public final class DeathListener implements Listener {
    private final MasqueradePlugin plugin;

    public DeathListener(MasqueradePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        if (!plugin.config().enabled()) {
            return;
        }

        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        logDeath(victim, killer);
        avoidIdentityCollision(event, victim, killer);
        dropStolenHead(event, victim, killer);
        releaseDisguise(event, victim);
    }

    private void logDeath(Player victim, Player killer) {
        String victimName = plugin.identities().moderationName(victim);
        if (killer != null) {
            plugin.getLogger().info(plugin.identities().moderationName(killer) + " killed " + victimName);
        } else {
            plugin.getLogger().info(victimName + " died");
        }
    }

    private void avoidIdentityCollision(PlayerDeathEvent event, Player victim, Player killer) {
        if (killer == null || killer.getUniqueId().equals(victim.getUniqueId())) {
            return;
        }

        String victimVisible = plugin.disguises().visibleName(victim);
        if (!victimVisible.equals(plugin.disguises().visibleName(killer))) {
            return;
        }
        if (event.deathMessage() == null) {
            return;
        }

        event.deathMessage(Component.translatable("death.attack.generic", Component.text(victimVisible)));
    }

    private void dropStolenHead(PlayerDeathEvent event, Player victim, Player killer) {
        if (!plugin.config().headDrops().enabled() || killer == null) {
            return;
        }
        if (plugin.disguises().isDisguised(victim.getUniqueId()) || plugin.identities().isHidden(victim)) {
            return;
        }

        ItemStack weapon = killer.getInventory().getItemInMainHand();
        if (!WeaponMatcher.matches(weapon, plugin.config().headDrops())) {
            return;
        }

        plugin.disguises().originalIdentity(victim)
                .ifPresent(identity -> event.getDrops().add(PlayerHeads.create(identity)));
        plugin.getLogger().info(plugin.identities().moderationName(killer)
                + " beheaded " + plugin.disguises().realName(victim));
    }

    private void releaseDisguise(PlayerDeathEvent event, Player victim) {
        Optional<DisguiseIdentity> removed = plugin.disguises().clear(victim);
        if (removed.isPresent() && plugin.config().disguise().dropHeadOnDeath()) {
            event.getDrops().add(PlayerHeads.create(removed.get()));
        }
    }
}
