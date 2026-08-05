package dev.boy.masquerade.events;

import dev.boy.masquerade.MasqueradeServer;
import dev.boy.masquerade.data.DisguiseIdentity;
import dev.boy.masquerade.util.MasqueradeLog;
import dev.boy.masquerade.util.PlayerHeads;
import dev.boy.masquerade.util.WeaponMatcher;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Optional;

public final class DeathHandler {
    private final MasqueradeServer context;

    public DeathHandler(MasqueradeServer context) {
        this.context = context;
    }

    public void onDeath(LivingEntity entity, DamageSource source) {
        if (!(entity instanceof ServerPlayerEntity victim) || !context.config().get().enabled()) {
            return;
        }

        logDeath(victim, source);
        dropStolenHead(victim, source);
        releaseDisguise(victim);
    }

    private void logDeath(ServerPlayerEntity victim, DamageSource source) {
        String victimName = context.identities().moderationName(victim);
        if (source.getAttacker() instanceof ServerPlayerEntity killer) {
            MasqueradeLog.logger().info("{} killed {}", context.identities().moderationName(killer), victimName);
        } else {
            MasqueradeLog.logger().info("{} died ({})", victimName, source.getName());
        }
    }

    private void dropStolenHead(ServerPlayerEntity victim, DamageSource source) {
        if (!context.config().get().headDrops().enabled()) {
            return;
        }
        if (!(source.getAttacker() instanceof ServerPlayerEntity killer)) {
            return;
        }
        if (context.disguises().isDisguised(victim.getUuid()) || context.identities().isHidden(victim)) {
            return;
        }

        ItemStack weapon = source.getWeaponStack();
        if (weapon == null || weapon.isEmpty()) {
            weapon = killer.getMainHandStack();
        }
        if (!WeaponMatcher.matches(weapon, context.config().get().headDrops())) {
            return;
        }

        ItemStack head = PlayerHeads.create(victim.getGameProfile());
        if (victim.getEntityWorld() instanceof ServerWorld world) {
            victim.dropStack(world, head);
        }
        MasqueradeLog.info("{} beheaded {}",
                context.identities().moderationName(killer), victim.getGameProfile().name());
    }

    private void releaseDisguise(ServerPlayerEntity victim) {
        Optional<DisguiseIdentity> removed = context.disguises().clear(victim);
        if (removed.isEmpty() || !context.config().get().disguise().dropHeadOnDeath()) {
            return;
        }

        ItemStack head = PlayerHeads.create(removed.get().toGameProfile());
        if (victim.getEntityWorld() instanceof ServerWorld world) {
            victim.dropStack(world, head);
        }
    }
}
