package dev.boy.masquerade.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.boy.masquerade.MasqueradeServer;
import dev.boy.masquerade.MasqueradeServices;
import dev.boy.masquerade.disguise.DisguiseManager;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class MessageTargets {
    private MessageTargets() {
    }

    public static Collection<ServerPlayer> resolve(CommandContext<CommandSourceStack> context, String argument)
            throws CommandSyntaxException {
        Optional<String> requested = requestedName(context);
        Optional<MasqueradeServer> server = MasqueradeServices.get();
        if (requested.isEmpty() || server.isEmpty()) {
            return EntityArgument.getPlayers(context, argument);
        }

        DisguiseManager disguises = server.get().disguises();
        String name = requested.get();

        Optional<ServerPlayer> masked = disguises.findByVisibleName(name);
        if (masked.isPresent()) {
            return List.of(masked.get());
        }

        ServerPlayer real = server.get().server().getPlayerList().getPlayerByName(name);
        if (real != null && disguises.isHiddenBehindOtherName(real)) {
            throw EntityArgument.NO_PLAYERS_FOUND.create();
        }

        return EntityArgument.getPlayers(context, argument);
    }

    private static Optional<String> requestedName(CommandContext<CommandSourceStack> context) {
        String input = context.getInput();
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }

        String trimmed = input.startsWith("/") ? input.substring(1) : input;
        String[] parts = trimmed.split("\\s+", 3);
        if (parts.length < 2) {
            return Optional.empty();
        }

        String target = parts[1];
        return target.startsWith("@") ? Optional.empty() : Optional.of(target);
    }
}
