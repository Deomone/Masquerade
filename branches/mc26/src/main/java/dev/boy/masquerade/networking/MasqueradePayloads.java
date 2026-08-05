package dev.boy.masquerade.networking;

import dev.boy.masquerade.MasqueradeMod;
import dev.boy.masquerade.data.DisguiseIdentity;
import dev.boy.masquerade.data.DisguiseState;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

public final class MasqueradePayloads {
    private MasqueradePayloads() {
    }

    public record StateUpdate(DisguiseState state) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<StateUpdate> ID =
                new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MasqueradeMod.MOD_ID, "state_update"));
        public static final StreamCodec<RegistryFriendlyByteBuf, StateUpdate> CODEC = StreamCodec.composite(
                DisguiseState.PACKET_CODEC, StateUpdate::state,
                StateUpdate::new
        );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record StateSnapshot(String unknownName, Optional<DisguiseIdentity> placeholder,
                                List<DisguiseState> states) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<StateSnapshot> ID =
                new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MasqueradeMod.MOD_ID, "state_snapshot"));
        public static final StreamCodec<RegistryFriendlyByteBuf, StateSnapshot> CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, StateSnapshot::unknownName,
                ByteBufCodecs.optional(DisguiseIdentity.PACKET_CODEC), StateSnapshot::placeholder,
                DisguiseState.PACKET_CODEC.apply(ByteBufCodecs.list()), StateSnapshot::states,
                StateSnapshot::new
        );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(StateUpdate.ID, StateUpdate.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(StateSnapshot.ID, StateSnapshot.CODEC);
    }
}
