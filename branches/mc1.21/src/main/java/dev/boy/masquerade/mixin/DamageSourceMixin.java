package dev.boy.masquerade.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(DamageSource.class)
abstract class DamageSourceMixin {
    private static final Set<String> ATTACKER_REQUIRED_MESSAGES = Set.of("player", "mob");

    @Inject(method = "getDeathMessage", at = @At("HEAD"), cancellable = true)
    private void masquerade$avoidIdentityCollision(LivingEntity killed, CallbackInfoReturnable<Text> callback) {
        DamageSource self = (DamageSource) (Object) this;
        if (!(killed instanceof PlayerEntity victim) || !(self.getAttacker() instanceof PlayerEntity killer)) {
            return;
        }
        if (victim.getUuid().equals(killer.getUuid())) {
            return;
        }
        if (!victim.getDisplayName().getString().equals(killer.getDisplayName().getString())) {
            return;
        }
        callback.setReturnValue(Text.translatable(genericKey(self), victim.getDisplayName()));
    }

    private static String genericKey(DamageSource source) {
        String msgId = source.getType().msgId();
        return ATTACKER_REQUIRED_MESSAGES.contains(msgId) ? "death.attack.generic" : "death.attack." + msgId;
    }
}
