package dev.boy.masquerade.events;

import dev.boy.masquerade.MasqueradePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public final class ConnectionListener implements Listener {
    private final MasqueradePlugin plugin;

    public ConnectionListener(MasqueradePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.disguises().remember(player);
        plugin.disguises().refresh(player);

        if (plugin.config().enabled() && event.joinMessage() != null) {
            event.joinMessage(Component
                    .translatable("multiplayer.player.joined", player.displayName())
                    .color(NamedTextColor.YELLOW));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.disguises().restoreOriginalProfile(player);

        if (!plugin.config().disguise().persistAcrossSessions()) {
            plugin.disguises().clearOffline(player.getUniqueId());
        }
        plugin.disguises().forget(player.getUniqueId());
        plugin.disguises().saveIfDirty();
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        plugin.disguises().refresh(event.getPlayer());
    }
}
