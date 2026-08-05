package dev.boy.masquerade.mixin;

import dev.boy.masquerade.MasqueradeServices;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {
    @Shadow
    protected abstract void removeEffectParticles();

    @Inject(method = "updateInvisibilityStatus", at = @At("TAIL"))
    private void masquerade$hideInvisibilityParticles(CallbackInfo callback) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player)) {
            return;
        }
        if (!self.hasEffect(MobEffects.INVISIBILITY)) {
            return;
        }
        boolean enabled = MasqueradeServices.get()
                .map(context -> context.config().get().enabled()
                        && context.config().get().invisibility().enabled()
                        && context.config().get().invisibility().hideParticles())
                .orElse(false);
        if (enabled) {
            removeEffectParticles();
        }
    }
}
