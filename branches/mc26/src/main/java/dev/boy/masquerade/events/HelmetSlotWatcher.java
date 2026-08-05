package dev.boy.masquerade.events;

import com.mojang.authlib.GameProfile;
import dev.boy.masquerade.MasqueradeServer;
import dev.boy.masquerade.data.DisguiseIdentity;
import dev.boy.masquerade.util.PlayerHeads;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public final class HelmetSlotWatcher {
    private final MasqueradeServer context;

    public HelmetSlotWatcher(MasqueradeServer context) {
        this.context = context;
    }

    public void tick(ServerPlayer player) {
        if (!context.config().get().disguise().enabled()) {
            return;
        }

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!PlayerHeads.isPlayerHead(helmet)) {
            return;
        }

        Optional<GameProfile> profile = PlayerHeads.profileOf(helmet);
        if (profile.isEmpty()) {
            return;
        }

        GameProfile owner = profile.get();
        if (!context.config().get().disguise().allowSelfDisguise() && owner.id().equals(player.getUUID())) {
            return;
        }

        takeHeadFromSlot(player, helmet);
        returnPreviousDisguise(player);
        context.disguises().apply(player, DisguiseIdentity.of(owner));
        resyncInventory(player);
    }

    private void resyncInventory(ServerPlayer player) {
        player.getInventory().setChanged();
        player.containerMenu.sendAllDataToRemote();
    }

    private void takeHeadFromSlot(ServerPlayer player, ItemStack helmet) {
        if (helmet.getCount() > 1) {
            ItemStack remainder = helmet.copy();
            remainder.shrink(1);
            player.getInventory().placeItemBackInInventory(remainder);
        }
        player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
    }

    private void returnPreviousDisguise(ServerPlayer player) {
        context.disguises().clear(player)
                .ifPresent(previous -> player.getInventory().placeItemBackInInventory(PlayerHeads.create(previous.toGameProfile())));
    }
}
