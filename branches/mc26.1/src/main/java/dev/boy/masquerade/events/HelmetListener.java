package dev.boy.masquerade.events;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import dev.boy.masquerade.MasqueradePlugin;
import dev.boy.masquerade.data.DisguiseIdentity;
import dev.boy.masquerade.util.PlayerHeads;
import dev.boy.masquerade.util.Scheduling;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public final class HelmetListener implements Listener {
    private final MasqueradePlugin plugin;

    public HelmetListener(MasqueradePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onArmorChange(PlayerArmorChangeEvent event) {
        if (!plugin.config().enabled() || !plugin.config().disguise().enabled()) {
            return;
        }
        if (event.getSlotType() != PlayerArmorChangeEvent.SlotType.HEAD) {
            return;
        }

        ItemStack helmet = event.getNewItem();
        if (!PlayerHeads.isPlayerHead(helmet)) {
            return;
        }

        Optional<DisguiseIdentity> identity = PlayerHeads.identityOf(helmet);
        if (identity.isEmpty()) {
            return;
        }

        Player player = event.getPlayer();
        DisguiseIdentity owner = identity.get();
        if (!plugin.config().disguise().allowSelfDisguise() && owner.uuid().equals(player.getUniqueId())) {
            return;
        }

        Scheduling.entity(plugin, player, () -> activate(player, owner));
    }

    private void activate(Player player, DisguiseIdentity owner) {
        ItemStack helmet = player.getInventory().getHelmet();
        if (!PlayerHeads.isPlayerHead(helmet)) {
            return;
        }

        if (helmet.getAmount() > 1) {
            ItemStack remainder = helmet.clone();
            remainder.setAmount(helmet.getAmount() - 1);
            giveBack(player, remainder);
        }
        player.getInventory().setHelmet(null);

        plugin.disguises().clear(player)
                .ifPresent(previous -> giveBack(player, PlayerHeads.create(previous)));
        plugin.disguises().apply(player, owner);
        player.updateInventory();
    }

    private void giveBack(Player player, ItemStack stack) {
        for (ItemStack leftover : player.getInventory().addItem(stack).values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }
    }
}
