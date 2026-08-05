package dev.boy.masquerade.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import dev.boy.masquerade.MasqueradeServer;
import dev.boy.masquerade.MasqueradeServices;
import dev.boy.masquerade.config.ConfigManager;
import dev.boy.masquerade.config.MasqueradeConfig;
import dev.boy.masquerade.data.DisguiseIdentity;
import dev.boy.masquerade.disguise.PermissionResolvers;
import dev.boy.masquerade.util.PlayerHeads;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class MasqueradeCommand {
    private MasqueradeCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, ConfigManager config) {
        dispatcher.register(CommandManager.literal("masquerade")
                .then(CommandManager.literal("remove")
                        .executes(MasqueradeCommand::removeOwn))
                .then(CommandManager.literal("set")
                        .requires(source -> isAdmin(source, config))
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.argument("identity", StringArgumentType.word())
                                        .suggests(MasqueradeCommand::suggestPlayerNames)
                                        .executes(MasqueradeCommand::setDisguise))))
                .then(CommandManager.literal("clear")
                        .requires(source -> isAdmin(source, config))
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(MasqueradeCommand::clearOther)))
                .then(CommandManager.literal("list")
                        .requires(source -> canReveal(source, config))
                        .executes(MasqueradeCommand::list))
                .then(CommandManager.literal("reload")
                        .requires(source -> isAdmin(source, config))
                        .executes(context -> reload(context, config))));
    }

    private static boolean isAdmin(ServerCommandSource source, ConfigManager config) {
        MasqueradeConfig.Permissions settings = config.get().permissions();
        return MasqueradeServices.get()
                .map(context -> context.permissions().has(source, settings.adminNode(), settings.adminFallbackLevel()))
                .orElseGet(() -> PermissionResolvers.hasLevel(source, settings.adminFallbackLevel()));
    }

    private static boolean canReveal(ServerCommandSource source, ConfigManager config) {
        MasqueradeConfig.Permissions settings = config.get().permissions();
        return MasqueradeServices.get()
                .map(context -> context.permissions().has(source, settings.revealNode(), settings.revealFallbackLevel()))
                .orElseGet(() -> PermissionResolvers.hasLevel(source, settings.revealFallbackLevel()));
    }

    private static int removeOwn(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        MasqueradeServer server = requireContext(context);

        Optional<DisguiseIdentity> removed = server.disguises().clear(player);
        if (removed.isEmpty()) {
            context.getSource().sendFeedback(() -> Text.translatable("masquerade.command.not_disguised"), false);
            return 0;
        }

        player.getInventory().offerOrDrop(PlayerHeads.create(removed.get().toGameProfile()));
        context.getSource().sendFeedback(
                () -> Text.translatable("masquerade.command.removed", removed.get().name()), false);
        return 1;
    }

    private static CompletableFuture<Suggestions> suggestPlayerNames(CommandContext<ServerCommandSource> context,
                                                                    SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(context.getSource().getPlayerNames(), builder);
    }

    private static int setDisguise(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
        String requested = StringArgumentType.getString(context, "identity");
        MasqueradeServer server = requireContext(context);
        ServerCommandSource source = context.getSource();

        server.profiles().resolve(requested).thenAccept(identity -> server.server().execute(() -> {
            if (identity.isEmpty()) {
                source.sendError(Text.translatable("masquerade.command.unknown_identity", requested));
                return;
            }
            ServerPlayerEntity online = server.server().getPlayerManager().getPlayer(target.getUuid());
            if (online == null) {
                return;
            }
            server.disguises().apply(online, identity.get());
            source.sendFeedback(() -> Text.translatable("masquerade.command.applied",
                    online.getGameProfile().getName(), identity.get().name()), true);
        }));
        return 1;
    }

    private static int clearOther(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
        MasqueradeServer server = requireContext(context);

        Optional<DisguiseIdentity> removed = server.disguises().clear(target);
        if (removed.isEmpty()) {
            context.getSource().sendFeedback(() -> Text.translatable("masquerade.command.not_disguised"), false);
            return 0;
        }

        target.getInventory().offerOrDrop(PlayerHeads.create(removed.get().toGameProfile()));
        context.getSource().sendFeedback(() -> Text.translatable("masquerade.command.cleared",
                target.getGameProfile().getName()), true);
        return 1;
    }

    private static int list(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        MasqueradeServer server = requireContext(context);
        Map<UUID, DisguiseIdentity> active = server.disguises().active();

        if (active.isEmpty()) {
            context.getSource().sendFeedback(() -> Text.translatable("masquerade.command.list.empty"), false);
            return 0;
        }

        context.getSource().sendFeedback(() -> Text.translatable("masquerade.command.list.header", active.size())
                .formatted(Formatting.GOLD), false);
        for (Map.Entry<UUID, DisguiseIdentity> entry : active.entrySet()) {
            ServerPlayerEntity online = server.server().getPlayerManager().getPlayer(entry.getKey());
            String realName = online != null ? online.getGameProfile().getName() : entry.getKey().toString();
            String suffix = online == null ? " (offline)" : "";
            context.getSource().sendFeedback(() -> Text.translatable("masquerade.command.list.entry",
                    realName + suffix, entry.getValue().name()), false);
        }
        return active.size();
    }

    private static int reload(CommandContext<ServerCommandSource> context, ConfigManager config) {
        config.reload();
        MasqueradeServices.get().ifPresent(server -> {
            server.profiles().invalidate();
            for (ServerPlayerEntity player : server.server().getPlayerManager().getPlayerList()) {
                server.disguises().sendSnapshot(player);
            }
            server.refreshPlaceholderSkin();
        });
        context.getSource().sendFeedback(() -> Text.translatable("masquerade.command.reloaded"), true);
        return 1;
    }

    private static MasqueradeServer requireContext(CommandContext<ServerCommandSource> context)
            throws CommandSyntaxException {
        return MasqueradeServices.get().orElseThrow(() ->
                new CommandSyntaxException(new com.mojang.brigadier.exceptions.SimpleCommandExceptionType(
                        Text.translatable("masquerade.command.unavailable")), Text.translatable("masquerade.command.unavailable")));
    }
}
