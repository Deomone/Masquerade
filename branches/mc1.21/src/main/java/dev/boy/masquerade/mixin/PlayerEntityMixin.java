package dev.boy.masquerade.mixin;

import dev.boy.masquerade.MasqueradeIdentity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
abstract class PlayerEntityMixin {
    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
    private void masquerade$overrideDisplayName(CallbackInfoReturnable<Text> callback) {
        MasqueradeIdentity.displayNameOverride((PlayerEntity) (Object) this)
                .ifPresent(callback::setReturnValue);
    }
}
