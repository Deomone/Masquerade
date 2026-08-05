package dev.boy.masquerade.mixin;

import dev.boy.masquerade.MasqueradeServices;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
abstract class ServerPlayerMixin {
    @Inject(method = "allowsListing", at = @At("HEAD"), cancellable = true)
    private void masquerade$hideInvisibleFromPlayerList(CallbackInfoReturnable<Boolean> callback) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        MasqueradeServices.get().ifPresent(context -> {
            if (context.config().get().enabled()
                    && context.config().get().invisibility().enabled()
                    && context.config().get().invisibility().hideFromPlayerList()
                    && context.disguises().isHidden(self.getUUID())) {
                callback.setReturnValue(false);
            }
        });
    }
}
