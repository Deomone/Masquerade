package dev.boy.masquerade.data;

import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Uuids;

import java.util.Optional;
import java.util.UUID;

public record DisguiseState(UUID player, Optional<DisguiseIdentity> identity, boolean hidden, Optional<String> revealName) {
    public static final PacketCodec<io.netty.buffer.ByteBuf, DisguiseState> PACKET_CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, DisguiseState::player,
            PacketCodecs.optional(DisguiseIdentity.PACKET_CODEC), DisguiseState::identity,
            PacketCodecs.BOOLEAN, DisguiseState::hidden,
            PacketCodecs.optional(PacketCodecs.STRING), DisguiseState::revealName,
            DisguiseState::new
    );

    public static DisguiseState cleared(UUID player) {
        return new DisguiseState(player, Optional.empty(), false, Optional.empty());
    }

    public boolean isEmpty() {
        return identity.isEmpty() && !hidden;
    }
}
