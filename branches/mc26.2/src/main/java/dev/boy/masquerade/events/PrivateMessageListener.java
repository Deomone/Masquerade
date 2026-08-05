package dev.boy.masquerade.events;

import dev.boy.masquerade.MasqueradePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public final class PrivateMessageListener implements Listener {
    private static final Set<String> MESSAGE_COMMANDS = Set.of("msg", "tell", "w", "whisper");

    private final MasqueradePlugin plugin;

    public PrivateMessageListener(MasqueradePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!plugin.config().enabled()) {
            return;
        }

        String[] parts = event.getMessage().split(" ", 3);
        if (parts.length < 2 || !isMessageCommand(parts[0])) {
            return;
        }

        String requested = parts[1];
        if (requested.startsWith("@")) {
            return;
        }

        Optional<Player> masked = plugin.disguises().findByVisibleName(requested);
        if (masked.isPresent()) {
            event.setMessage(rewrite(parts, plugin.disguises().realName(masked.get())));
            return;
        }

        Optional<Player> real = plugin.disguises().findByRealName(requested);
        if (real.isPresent() && isHiddenIdentity(real.get())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Component
                    .translatable("argument.entity.notfound.player")
                    .color(NamedTextColor.RED));
        }
    }

    private boolean isHiddenIdentity(Player player) {
        return !plugin.disguises().visibleName(player).equalsIgnoreCase(plugin.disguises().realName(player));
    }

    private boolean isMessageCommand(String token) {
        String command = token.startsWith("/") ? token.substring(1) : token;
        int colon = command.indexOf(':');
        if (colon >= 0) {
            command = command.substring(colon + 1);
        }
        return MESSAGE_COMMANDS.contains(command.toLowerCase(Locale.ROOT));
    }

    private String rewrite(String[] parts, String realName) {
        StringBuilder rebuilt = new StringBuilder(parts[0]).append(' ').append(realName);
        if (parts.length > 2) {
            rebuilt.append(' ').append(parts[2]);
        }
        return rebuilt.toString();
    }
}
