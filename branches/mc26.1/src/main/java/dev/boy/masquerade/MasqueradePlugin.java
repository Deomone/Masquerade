package dev.boy.masquerade;

import dev.boy.masquerade.command.MasqueradeCommand;
import dev.boy.masquerade.config.PluginConfig;
import dev.boy.masquerade.data.DisguiseStorage;
import dev.boy.masquerade.disguise.DisguiseManager;
import dev.boy.masquerade.disguise.IdentityService;
import dev.boy.masquerade.disguise.ProfileCache;
import dev.boy.masquerade.events.ConnectionListener;
import dev.boy.masquerade.events.DeathListener;
import dev.boy.masquerade.events.HelmetListener;
import dev.boy.masquerade.events.InvisibilityListener;
import dev.boy.masquerade.events.PrivateMessageListener;
import dev.boy.masquerade.util.Scheduling;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Optional;

public final class MasqueradePlugin extends JavaPlugin {
    private static final long SAVE_INTERVAL_TICKS = 20L * 60L;
    private static final long VISIBILITY_INTERVAL_TICKS = 20L;

    private PluginConfig config;
    private DisguiseManager disguises;
    private IdentityService identities;
    private ProfileCache profiles;
    private InvisibilityListener invisibility;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = PluginConfig.from(getConfig());

        profiles = new ProfileCache(this);
        disguises = new DisguiseManager(this, new DisguiseStorage(new File(getDataFolder(), "disguises.yml"), getLogger()));
        identities = new IdentityService(this, disguises);
        invisibility = new InvisibilityListener(this);

        disguises.loadFromDisk();

        getServer().getPluginManager().registerEvents(new ConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new HelmetListener(this), this);
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(invisibility, this);
        getServer().getPluginManager().registerEvents(new PrivateMessageListener(this), this);

        PluginCommand command = getCommand("masquerade");
        if (command != null) {
            MasqueradeCommand executor = new MasqueradeCommand(this);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }

        Scheduling.globalTimer(this, this::tickVisibility, VISIBILITY_INTERVAL_TICKS, VISIBILITY_INTERVAL_TICKS);
        Scheduling.globalTimer(this, disguises::saveIfDirty, SAVE_INTERVAL_TICKS, SAVE_INTERVAL_TICKS);

        refreshPlaceholderSkin();
        getLogger().info("Masquerade ready with " + disguises.active().size() + " stored disguises");
    }

    @Override
    public void onDisable() {
        if (disguises == null) {
            return;
        }

        for (Player player : getServer().getOnlinePlayers()) {
            disguises.restoreOriginalProfile(player);
        }
        disguises.saveToDisk();
    }

    private void tickVisibility() {
        if (!config.enabled() || !config.invisibility().enabled()) {
            return;
        }
        for (Player player : getServer().getOnlinePlayers()) {
            Scheduling.entity(this, player, () -> {
                invisibility.stripParticles(player);
                invisibility.syncVisibility(player);
            });
        }
    }

    public void reloadSettings() {
        reloadConfig();
        config = PluginConfig.from(getConfig());
        profiles.invalidate();
        refreshPlaceholderSkin();
        disguises.refreshAll();
    }

    public void refreshPlaceholderSkin() {
        String name = config.invisibility().placeholderSkin();
        if (name == null || name.isBlank()) {
            disguises.setPlaceholder(Optional.empty());
            return;
        }

        profiles.resolve(name).thenAccept(identity -> Scheduling.global(this, () -> {
            if (identity.isEmpty()) {
                getLogger().warning("Could not resolve placeholder skin '" + name
                        + "', invisible players keep their own skin");
            }
            disguises.setPlaceholder(identity);
        }));
    }

    public PluginConfig config() {
        return config;
    }

    public DisguiseManager disguises() {
        return disguises;
    }

    public IdentityService identities() {
        return identities;
    }

    public ProfileCache profiles() {
        return profiles;
    }
}
