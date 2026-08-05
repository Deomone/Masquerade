package dev.boy.masquerade.events;

import dev.boy.masquerade.MasqueradeServer;
import dev.boy.masquerade.data.DisguiseIdentity;
import dev.boy.masquerade.util.MasqueradeLog;
import dev.boy.masquerade.util.PlayerHeads;
import dev.boy.masquerade.util.WeaponMatcher;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;

public final class DeathHandler {
    private final MasqueradeServer context;

    public DeathHandler(MasqueradeServer context) {
        this.context = context;
    }

    public void onDeath(LivingEntity entity, DamageSource source) {
        if (!(entity instanceof ServerPlayer victim) || !context.config().get().enabled()) {
            return;
        }

        logDeath(victim, source);
        dropStolenHead(victim, source);
        releaseDisguise(victim);
    }

    private void logDeath(ServerPlayer victim, DamageSource source) {
        String victimName = context.identities().moderationName(victim);
        if (source.getEntity() instanceof ServerPlayer killer) {
            MasqueradeLog.logger().info("{} killed {}", context.identities().moderationName(killer), victimName);
        } else {
            MasqueradeLog.logger().info("{} died ({})", victimName, source.getMsgId());
        }
    }

    private void dropStolenHead(ServerPlayer victim, DamageSource source) {
        if (!context.config().get().headDrops().enabled()) {
            return;
        }
        if (!(source.getEntity() instanceof ServerPlayer killer)) {
            return;
        }
        if (context.disguises().isDisguised(victim.getUUID()) || context.identities().isHidden(victim)) {
            return;
        }

        ItemStack weapon = source.getWeaponItem();
        if (weapon == null || weapon.isEmpty()) {
            weapon = killer.getMainHandItem();
        }
        if (!WeaponMatcher.matches(weapon, context.config().get().headDrops())) {
            return;
        }

        ItemStack head = PlayerHeads.create(victim.getGameProfile());
        if (victim.level() instanceof ServerLevel world) {
            victim.spawnAtLocation(world, head);
        }
        MasqueradeLog.info("{} beheaded {}",
                context.identities().moderationName(killer), victim.getGameProfile().name());
    }

    private void releaseDisguise(ServerPlayer victim) {
        Optional<DisguiseIdentity> removed = context.disguises().clear(victim);
        if (removed.isEmpty() || !context.config().get().disguise().dropHeadOnDeath()) {
            return;
        }

        ItemStack head = PlayerHeads.create(removed.get().toGameProfile());
        if (victim.level() instanceof ServerLevel world) {
            victim.spawnAtLocation(world, head);
        }
    }
}
