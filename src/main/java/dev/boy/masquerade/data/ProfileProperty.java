package dev.boy.masquerade.data;

import com.mojang.authlib.properties.Property;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.Optional;

public record ProfileProperty(String name, String value, Optional<String> signature) {
    public static final Codec<ProfileProperty> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(ProfileProperty::name),
            Codec.STRING.fieldOf("value").forGetter(ProfileProperty::value),
            Codec.STRING.optionalFieldOf("signature").forGetter(ProfileProperty::signature)
    ).apply(instance, ProfileProperty::new));

    public static final PacketCodec<io.netty.buffer.ByteBuf, ProfileProperty> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, ProfileProperty::name,
            PacketCodecs.STRING, ProfileProperty::value,
            PacketCodecs.optional(PacketCodecs.STRING), ProfileProperty::signature,
            ProfileProperty::new
    );

    public static ProfileProperty of(Property property) {
        return new ProfileProperty(property.name(), property.value(), Optional.ofNullable(property.signature()));
    }

    public Property toProperty() {
        return new Property(name, value, signature.orElse(null));
    }
}
