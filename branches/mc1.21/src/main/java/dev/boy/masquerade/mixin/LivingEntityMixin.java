package dev.boy.masquerade.mixin;

import dev.boy.masquerade.MasqueradeServices;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {
    @Shadow
    protected abstract void clearPotionSwirls();

    @Inject(method = "updatePotionVisibility", at = @At("TAIL"))
    private void masquerade$hideInvisibilityParticles(CallbackInfo callback) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof PlayerEntity)) {
            return;
        }
        if (!self.hasStatusEffect(StatusEffects.INVISIBILITY)) {
            return;
        }
        boolean enabled = MasqueradeServices.get()
                .map(context -> context.config().get().enabled()
                        && context.config().get().invisibility().enabled()
                        && context.config().get().invisibility().hideParticles())
                .orElse(false);
        if (enabled) {
            clearPotionSwirls();
        }
    }
}
