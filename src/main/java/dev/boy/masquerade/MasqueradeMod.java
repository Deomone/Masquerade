package dev.boy.masquerade;

import dev.boy.masquerade.command.MasqueradeCommand;
import dev.boy.masquerade.config.ConfigManager;
import dev.boy.masquerade.disguise.PermissionResolvers;
import dev.boy.masquerade.events.DeathHandler;
import dev.boy.masquerade.events.HelmetSlotWatcher;
import dev.boy.masquerade.events.MasqueradeTicker;
import dev.boy.masquerade.events.VisibilityTracker;
import dev.boy.masquerade.networking.MasqueradePayloads;
import dev.boy.masquerade.util.MasqueradeLog;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import java.util.concurrent.atomic.AtomicReference;

public final class MasqueradeMod implements ModInitializer {
    public static final String MOD_ID = "masquerade";

    private final ConfigManager config = ConfigManager.createDefault();
    private final AtomicReference<MasqueradeTicker> ticker = new AtomicReference<>();
    private final AtomicReference<DeathHandler> deaths = new AtomicReference<>();

    @Override
    public void onInitialize() {
        config.load();
        MasqueradePayloads.register();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            MasqueradeServer context = new MasqueradeServer(server, config, PermissionResolvers.create());
            context.disguises().loadFromDisk();
            MasqueradeServices.install(context);
            MasqueradeIdentity.installServer(context.identities()::displayNameOverride);

            deaths.set(new DeathHandler(context));
            ticker.set(new MasqueradeTicker(context,
                    new HelmetSlotWatcher(context),
                    new VisibilityTracker(context)));

            context.refreshPlaceholderSkin();
            MasqueradeLog.logger().info("Masquerade ready with {} stored disguises", context.disguises().active().size());
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server ->
                MasqueradeServices.get().ifPresent(context -> context.disguises().saveToDisk()));

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            MasqueradeIdentity.uninstallServer();
            MasqueradeServices.uninstall();
            ticker.set(null);
            deaths.set(null);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            MasqueradeTicker current = ticker.get();
            if (current != null) {
                current.tick();
            }
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            DeathHandler handler = deaths.get();
            if (handler != null) {
                handler.onDeath(entity, source);
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                MasqueradeServices.get().ifPresent(context -> {
                    context.disguises().sendSnapshot(handler.getPlayer());
                    context.disguises().broadcast(handler.getPlayer());
                }));

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                MasqueradeServices.get().ifPresent(context -> {
                    java.util.UUID uuid = handler.getPlayer().getUuid();
                    context.disguises().forget(uuid);
                    if (!config.get().disguise().persistAcrossSessions()) {
                        context.disguises().clearOffline(uuid);
                    }
                }));

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
                MasqueradeServices.get().ifPresent(context -> {
                    context.disguises().sendSnapshot(newPlayer);
                    context.disguises().broadcast(newPlayer);
                }));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                MasqueradeCommand.register(dispatcher, config));

        MasqueradeLog.logger().info("Masquerade initialized");
    }
}
