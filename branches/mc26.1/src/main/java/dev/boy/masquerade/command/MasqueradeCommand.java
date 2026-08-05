package dev.boy.masquerade.command;

import dev.boy.masquerade.MasqueradePlugin;
import dev.boy.masquerade.data.DisguiseIdentity;
import dev.boy.masquerade.util.PlayerHeads;
import dev.boy.masquerade.util.Scheduling;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class MasqueradeCommand implements CommandExecutor, TabCompleter {
    private static final String ADMIN_NODE = "masquerade.admin";
    private static final String REVEAL_NODE = "masquerade.reveal";

    private final MasqueradePlugin plugin;

    public MasqueradeCommand(MasqueradePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("/masquerade <remove|set|clear|list|reload>", NamedTextColor.GRAY));
            return true;
        }

        return switch (args[0].toLowerCase()) {
            case "remove" -> removeOwn(sender);
            case "set" -> setDisguise(sender, args);
            case "clear" -> clearOther(sender, args);
            case "list" -> list(sender);
            case "reload" -> reload(sender);
            default -> {
                sender.sendMessage(Component.text("Unknown subcommand", NamedTextColor.RED));
                yield true;
            }
        };
    }

    private boolean removeOwn(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can remove their own disguise", NamedTextColor.RED));
            return true;
        }

        Optional<DisguiseIdentity> removed = plugin.disguises().clear(player);
        if (removed.isEmpty()) {
            player.sendMessage(Component.text("No active disguise", NamedTextColor.GRAY));
            return true;
        }

        giveBack(player, PlayerHeads.create(removed.get()));
        player.sendMessage(Component.text("Disguise removed, you were " + removed.get().name(), NamedTextColor.GREEN));
        return true;
    }

    private boolean setDisguise(CommandSender sender, String[] args) {
        if (!sender.hasPermission(ADMIN_NODE)) {
            sender.sendMessage(Component.text("No permission", NamedTextColor.RED));
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(Component.text("/masquerade set <player> <identity>", NamedTextColor.GRAY));
            return true;
        }

        Player target = resolveTarget(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("Player " + args[1] + " is not online", NamedTextColor.RED));
            return true;
        }

        String requested = args[2];
        plugin.profiles().resolve(requested).thenAccept(identity -> Scheduling.entity(plugin, target, () -> {
            if (identity.isEmpty()) {
                sender.sendMessage(Component.text("Could not find a profile named " + requested, NamedTextColor.RED));
                return;
            }
            plugin.disguises().apply(target, identity.get());
            sender.sendMessage(Component.text(plugin.disguises().realName(target)
                    + " is now disguised as " + identity.get().name(), NamedTextColor.GREEN));
        }));
        return true;
    }

    private boolean clearOther(CommandSender sender, String[] args) {
        if (!sender.hasPermission(ADMIN_NODE)) {
            sender.sendMessage(Component.text("No permission", NamedTextColor.RED));
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(Component.text("/masquerade clear <player>", NamedTextColor.GRAY));
            return true;
        }

        Player target = resolveTarget(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("Player " + args[1] + " is not online", NamedTextColor.RED));
            return true;
        }

        Optional<DisguiseIdentity> removed = plugin.disguises().clear(target);
        if (removed.isEmpty()) {
            sender.sendMessage(Component.text("No active disguise", NamedTextColor.GRAY));
            return true;
        }

        giveBack(target, PlayerHeads.create(removed.get()));
        sender.sendMessage(Component.text("Disguise cleared for "
                + plugin.disguises().realName(target), NamedTextColor.GREEN));
        return true;
    }

    private boolean list(CommandSender sender) {
        if (!sender.hasPermission(REVEAL_NODE)) {
            sender.sendMessage(Component.text("No permission", NamedTextColor.RED));
            return true;
        }

        Map<UUID, DisguiseIdentity> active = plugin.disguises().active();
        if (active.isEmpty()) {
            sender.sendMessage(Component.text("Nobody is disguised right now", NamedTextColor.GRAY));
            return true;
        }

        sender.sendMessage(Component.text("Active disguises: " + active.size(), NamedTextColor.GOLD));
        for (Map.Entry<UUID, DisguiseIdentity> entry : active.entrySet()) {
            Player online = Bukkit.getPlayer(entry.getKey());
            String real = online == null ? entry.getKey() + " (offline)" : plugin.disguises().realName(online);
            sender.sendMessage(Component.text(real + " -> " + entry.getValue().name(), NamedTextColor.YELLOW));
        }
        return true;
    }

    private boolean reload(CommandSender sender) {
        if (!sender.hasPermission(ADMIN_NODE)) {
            sender.sendMessage(Component.text("No permission", NamedTextColor.RED));
            return true;
        }

        plugin.reloadSettings();
        sender.sendMessage(Component.text("Masquerade configuration reloaded", NamedTextColor.GREEN));
        return true;
    }

    private Player resolveTarget(String name) {
        Player direct = Bukkit.getPlayerExact(name);
        return direct != null ? direct : plugin.disguises().findByRealName(name).orElse(null);
    }

    private void giveBack(Player player, ItemStack stack) {
        for (ItemStack leftover : player.getInventory().addItem(stack).values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            options.add("remove");
            if (sender.hasPermission(REVEAL_NODE)) {
                options.add("list");
            }
            if (sender.hasPermission(ADMIN_NODE)) {
                options.add("set");
                options.add("clear");
                options.add("reload");
            }
        } else if (args.length >= 2 && sender.hasPermission(ADMIN_NODE)
                && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("clear"))) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                options.add(plugin.disguises().realName(player));
            }
        }

        String prefix = args[args.length - 1].toLowerCase();
        options.removeIf(option -> !option.toLowerCase().startsWith(prefix));
        options.sort(Comparator.naturalOrder());
        return options;
    }
}
