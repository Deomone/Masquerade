package dev.boy.masquerade.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

public final class Scheduling {
    private Scheduling() {
    }

    public static void global(Plugin plugin, Runnable action) {
        Bukkit.getGlobalRegionScheduler().execute(plugin, action);
    }

    public static void globalTimer(Plugin plugin, Runnable action, long delayTicks, long periodTicks) {
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, task -> action.run(), delayTicks, periodTicks);
    }

    public static void entity(Plugin plugin, Entity entity, Runnable action) {
        entity.getScheduler().run(plugin, task -> action.run(), null);
    }

    public static void async(Plugin plugin, Runnable action) {
        Bukkit.getAsyncScheduler().runNow(plugin, task -> action.run());
    }

    public static void asyncDelayed(Plugin plugin, Runnable action, long delayMillis) {
        Bukkit.getAsyncScheduler().runDelayed(plugin, task -> action.run(), delayMillis, TimeUnit.MILLISECONDS);
    }
}
