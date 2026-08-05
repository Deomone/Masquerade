package dev.boy.masquerade.mixin;

import dev.boy.masquerade.MasqueradeServices;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
abstract class ServerPlayerEntityMixin {
    @Inject(method = "allowsServerListing", at = @At("HEAD"), cancellable = true)
    private void masquerade$hideInvisibleFromPlayerList(CallbackInfoReturnable<Boolean> callback) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        MasqueradeServices.get().ifPresent(context -> {
            if (context.config().get().enabled()
                    && context.config().get().invisibility().enabled()
                    && context.config().get().invisibility().hideFromPlayerList()
                    && context.disguises().isHidden(self.getUuid())) {
                callback.setReturnValue(false);
            }
        });
    }
}
