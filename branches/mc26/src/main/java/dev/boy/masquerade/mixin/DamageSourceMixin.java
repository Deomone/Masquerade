package dev.boy.masquerade.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(DamageSource.class)
abstract class DamageSourceMixin {
    private static final Set<String> ATTACKER_REQUIRED_MESSAGES = Set.of("player", "mob");

    @Inject(method = "getLocalizedDeathMessage", at = @At("HEAD"), cancellable = true)
    private void masquerade$avoidIdentityCollision(LivingEntity killed, CallbackInfoReturnable<Component> callback) {
        DamageSource self = (DamageSource) (Object) this;
        if (!(killed instanceof Player victim) || !(self.getEntity() instanceof Player killer)) {
            return;
        }
        if (victim.getUUID().equals(killer.getUUID())) {
            return;
        }
        if (!victim.getDisplayName().getString().equals(killer.getDisplayName().getString())) {
            return;
        }
        callback.setReturnValue(Component.translatable(genericKey(self), victim.getDisplayName()));
    }

    private static String genericKey(DamageSource source) {
        String msgId = source.type().msgId();
        return ATTACKER_REQUIRED_MESSAGES.contains(msgId) ? "death.attack.generic" : "death.attack." + msgId;
    }
}
