package dev.boy.masquerade.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import dev.boy.masquerade.MasqueradeServer;
import dev.boy.masquerade.MasqueradeServices;
import dev.boy.masquerade.config.ConfigManager;
import dev.boy.masquerade.config.MasqueradeConfig;
import dev.boy.masquerade.data.DisguiseIdentity;
import dev.boy.masquerade.disguise.PermissionResolvers;
import dev.boy.masquerade.util.PlayerHeads;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class MasqueradeCommand {
    private MasqueradeCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, ConfigManager config) {
        dispatcher.register(Commands.literal("masquerade")
                .then(Commands.literal("remove")
                        .executes(MasqueradeCommand::removeOwn))
                .then(Commands.literal("set")
                        .requires(source -> isAdmin(source, config))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("identity", StringArgumentType.word())
                                        .suggests(MasqueradeCommand::suggestPlayerNames)
                                        .executes(MasqueradeCommand::setDisguise))))
                .then(Commands.literal("clear")
                        .requires(source -> isAdmin(source, config))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(MasqueradeCommand::clearOther)))
                .then(Commands.literal("list")
                        .requires(source -> canReveal(source, config))
                        .executes(MasqueradeCommand::list))
                .then(Commands.literal("reload")
                        .requires(source -> isAdmin(source, config))
                        .executes(context -> reload(context, config))));
    }

    private static boolean isAdmin(CommandSourceStack source, ConfigManager config) {
        MasqueradeConfig.Permissions settings = config.get().permissions();
        return MasqueradeServices.get()
                .map(context -> context.permissions().has(source, settings.adminNode(), settings.adminFallbackLevel()))
                .orElseGet(() -> PermissionResolvers.hasLevel(source, settings.adminFallbackLevel()));
    }

    private static boolean canReveal(CommandSourceStack source, ConfigManager config) {
        MasqueradeConfig.Permissions settings = config.get().permissions();
        return MasqueradeServices.get()
                .map(context -> context.permissions().has(source, settings.revealNode(), settings.revealFallbackLevel()))
                .orElseGet(() -> PermissionResolvers.hasLevel(source, settings.revealFallbackLevel()));
    }

    private static int removeOwn(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        MasqueradeServer server = requireContext(context);

        Optional<DisguiseIdentity> removed = server.disguises().clear(player);
        if (removed.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.translatable("masquerade.command.not_disguised"), false);
            return 0;
        }

        player.getInventory().placeItemBackInInventory(PlayerHeads.create(removed.get().toGameProfile()));
        context.getSource().sendSuccess(
                () -> Component.translatable("masquerade.command.removed", removed.get().name()), false);
        return 1;
    }

    private static CompletableFuture<Suggestions> suggestPlayerNames(CommandContext<CommandSourceStack> context,
                                                                    SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(context.getSource().getOnlinePlayerNames(), builder);
    }

    private static int setDisguise(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        String requested = StringArgumentType.getString(context, "identity");
        MasqueradeServer server = requireContext(context);
        CommandSourceStack source = context.getSource();

        server.profiles().resolve(requested).thenAccept(identity -> server.server().execute(() -> {
            if (identity.isEmpty()) {
                source.sendFailure(Component.translatable("masquerade.command.unknown_identity", requested));
                return;
            }
            ServerPlayer online = server.server().getPlayerList().getPlayer(target.getUUID());
            if (online == null) {
                return;
            }
            server.disguises().apply(online, identity.get());
            source.sendSuccess(() -> Component.translatable("masquerade.command.applied",
                    online.getGameProfile().name(), identity.get().name()), true);
        }));
        return 1;
    }

    private static int clearOther(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        MasqueradeServer server = requireContext(context);

        Optional<DisguiseIdentity> removed = server.disguises().clear(target);
        if (removed.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.translatable("masquerade.command.not_disguised"), false);
            return 0;
        }

        target.getInventory().placeItemBackInInventory(PlayerHeads.create(removed.get().toGameProfile()));
        context.getSource().sendSuccess(() -> Component.translatable("masquerade.command.cleared",
                target.getGameProfile().name()), true);
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        MasqueradeServer server = requireContext(context);
        Map<UUID, DisguiseIdentity> active = server.disguises().active();

        if (active.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.translatable("masquerade.command.list.empty"), false);
            return 0;
        }

        context.getSource().sendSuccess(() -> Component.translatable("masquerade.command.list.header", active.size())
                .withStyle(ChatFormatting.GOLD), false);
        for (Map.Entry<UUID, DisguiseIdentity> entry : active.entrySet()) {
            ServerPlayer online = server.server().getPlayerList().getPlayer(entry.getKey());
            String realName = online != null ? online.getGameProfile().name() : entry.getKey().toString();
            String suffix = online == null ? " (offline)" : "";
            context.getSource().sendSuccess(() -> Component.translatable("masquerade.command.list.entry",
                    realName + suffix, entry.getValue().name()), false);
        }
        return active.size();
    }

    private static int reload(CommandContext<CommandSourceStack> context, ConfigManager config) {
        config.reload();
        MasqueradeServices.get().ifPresent(server -> {
            server.profiles().invalidate();
            for (ServerPlayer player : server.server().getPlayerList().getPlayers()) {
                server.disguises().sendSnapshot(player);
            }
            server.refreshPlaceholderSkin();
        });
        context.getSource().sendSuccess(() -> Component.translatable("masquerade.command.reloaded"), true);
        return 1;
    }

    private static MasqueradeServer requireContext(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        return MasqueradeServices.get().orElseThrow(() ->
                new CommandSyntaxException(new com.mojang.brigadier.exceptions.SimpleCommandExceptionType(
                        Component.translatable("masquerade.command.unavailable")), Component.translatable("masquerade.command.unavailable")));
    }
}
