package dev.boy.masquerade.mixin;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.boy.masquerade.util.MessageTargets;
import net.minecraft.server.command.MessageCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;

@Mixin(MessageCommand.class)
abstract class MessageCommandMixin {
    @Redirect(
            method = "*",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/command/argument/EntityArgumentType;getPlayers(Lcom/mojang/brigadier/context/CommandContext;Ljava/lang/String;)Ljava/util/Collection;")
    )
    private static Collection<ServerPlayerEntity> masquerade$resolveByVisibleName(
            CommandContext<ServerCommandSource> context, String argument) throws CommandSyntaxException {
        return MessageTargets.resolve(context, argument);
    }
}
