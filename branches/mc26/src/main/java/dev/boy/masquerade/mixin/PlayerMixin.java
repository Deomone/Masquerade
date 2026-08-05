package dev.boy.masquerade.mixin;

import dev.boy.masquerade.MasqueradeIdentity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
abstract class PlayerMixin {
    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
    private void masquerade$overrideDisplayName(CallbackInfoReturnable<Component> callback) {
        MasqueradeIdentity.displayNameOverride((Player) (Object) this)
                .ifPresent(callback::setReturnValue);
    }
}
