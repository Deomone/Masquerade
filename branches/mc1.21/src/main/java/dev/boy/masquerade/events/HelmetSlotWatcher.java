package dev.boy.masquerade.events;

import com.mojang.authlib.GameProfile;
import dev.boy.masquerade.MasqueradeServer;
import dev.boy.masquerade.data.DisguiseIdentity;
import dev.boy.masquerade.util.PlayerHeads;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

public final class HelmetSlotWatcher {
    private final MasqueradeServer context;

    public HelmetSlotWatcher(MasqueradeServer context) {
        this.context = context;
    }

    public void tick(ServerPlayerEntity player) {
        if (!context.config().get().disguise().enabled()) {
            return;
        }

        ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
        if (!PlayerHeads.isPlayerHead(helmet)) {
            return;
        }

        Optional<GameProfile> profile = PlayerHeads.profileOf(helmet);
        if (profile.isEmpty()) {
            return;
        }

        GameProfile owner = profile.get();
        if (!context.config().get().disguise().allowSelfDisguise() && owner.getId().equals(player.getUuid())) {
            return;
        }

        takeHeadFromSlot(player, helmet);
        returnPreviousDisguise(player);
        context.disguises().apply(player, DisguiseIdentity.of(owner));
        resyncInventory(player);
    }

    private void takeHeadFromSlot(ServerPlayerEntity player, ItemStack helmet) {
        if (helmet.getCount() > 1) {
            ItemStack remainder = helmet.copy();
            remainder.decrement(1);
            player.getInventory().offerOrDrop(remainder);
        }
        player.equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);
    }

    private void resyncInventory(ServerPlayerEntity player) {
        player.getInventory().markDirty();
        player.currentScreenHandler.syncState();
    }

    private void returnPreviousDisguise(ServerPlayerEntity player) {
        context.disguises().clear(player)
                .ifPresent(previous -> player.getInventory().offerOrDrop(PlayerHeads.create(previous.toGameProfile())));
    }
}
