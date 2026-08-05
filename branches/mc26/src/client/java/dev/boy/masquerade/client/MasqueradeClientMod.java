package dev.boy.masquerade.client;

import dev.boy.masquerade.MasqueradeIdentity;
import dev.boy.masquerade.client.rendering.DisguiseSkinCache;
import dev.boy.masquerade.networking.MasqueradePayloads;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class MasqueradeClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MasqueradeIdentity.installClient(ClientDisguiseStore.instance()::displayNameOverride);

        ClientPlayNetworking.registerGlobalReceiver(MasqueradePayloads.StateUpdate.ID, (payload, context) ->
                context.client().execute(() -> ClientDisguiseStore.instance().accept(payload.state())));

        ClientPlayNetworking.registerGlobalReceiver(MasqueradePayloads.StateSnapshot.ID, (payload, context) ->
                context.client().execute(() -> {
                    ClientDisguiseStore.instance().setUnknownName(payload.unknownName());
                    ClientDisguiseStore.instance().setPlaceholder(payload.placeholder());
                    ClientDisguiseStore.instance().replaceAll(payload.states());
                }));

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ClientDisguiseStore.instance().clear();
            DisguiseSkinCache.instance().clear();
        });
    }
}
