package dev.boy.masquerade.data;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.core.UUIDUtil;

import java.util.Optional;
import java.util.UUID;

public record DisguiseState(UUID player, Optional<DisguiseIdentity> identity, boolean hidden, Optional<String> revealName) {
    public static final StreamCodec<io.netty.buffer.ByteBuf, DisguiseState> PACKET_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, DisguiseState::player,
            ByteBufCodecs.optional(DisguiseIdentity.PACKET_CODEC), DisguiseState::identity,
            ByteBufCodecs.BOOL, DisguiseState::hidden,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), DisguiseState::revealName,
            DisguiseState::new
    );

    public static DisguiseState cleared(UUID player) {
        return new DisguiseState(player, Optional.empty(), false, Optional.empty());
    }

    public boolean isEmpty() {
        return identity.isEmpty() && !hidden;
    }
}
