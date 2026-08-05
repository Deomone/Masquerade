package dev.boy.masquerade.networking;

import dev.boy.masquerade.MasqueradeMod;
import dev.boy.masquerade.data.DisguiseIdentity;
import dev.boy.masquerade.data.DisguiseState;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

public final class MasqueradePayloads {
    private MasqueradePayloads() {
    }

    public record StateUpdate(DisguiseState state) implements CustomPayload {
        public static final CustomPayload.Id<StateUpdate> ID =
                new CustomPayload.Id<>(Identifier.of(MasqueradeMod.MOD_ID, "state_update"));
        public static final PacketCodec<RegistryByteBuf, StateUpdate> CODEC = PacketCodec.tuple(
                DisguiseState.PACKET_CODEC, StateUpdate::state,
                StateUpdate::new
        );

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record StateSnapshot(String unknownName, Optional<DisguiseIdentity> placeholder,
                                List<DisguiseState> states) implements CustomPayload {
        public static final CustomPayload.Id<StateSnapshot> ID =
                new CustomPayload.Id<>(Identifier.of(MasqueradeMod.MOD_ID, "state_snapshot"));
        public static final PacketCodec<RegistryByteBuf, StateSnapshot> CODEC = PacketCodec.tuple(
                PacketCodecs.STRING, StateSnapshot::unknownName,
                PacketCodecs.optional(DisguiseIdentity.PACKET_CODEC), StateSnapshot::placeholder,
                DisguiseState.PACKET_CODEC.collect(PacketCodecs.toList()), StateSnapshot::states,
                StateSnapshot::new
        );

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(StateUpdate.ID, StateUpdate.CODEC);
        PayloadTypeRegistry.playS2C().register(StateSnapshot.ID, StateSnapshot.CODEC);
    }
}
