package dev.boy.masquerade.mixin;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.boy.masquerade.util.MessageTargets;
import net.minecraft.server.commands.MsgCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;

@Mixin(MsgCommand.class)
abstract class MessageCommandMixin {
    @Redirect(
            method = "*",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/commands/arguments/EntityArgument;getPlayers(Lcom/mojang/brigadier/context/CommandContext;Ljava/lang/String;)Ljava/util/Collection;")
    )
    private static Collection<ServerPlayer> masquerade$resolveByVisibleName(
            CommandContext<CommandSourceStack> context, String argument) throws CommandSyntaxException {
        return MessageTargets.resolve(context, argument);
    }
}
