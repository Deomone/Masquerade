package dev.boy.masquerade.data;

import com.mojang.authlib.properties.Property;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.Optional;

public record ProfileProperty(String name, String value, Optional<String> signature) {
    public static final Codec<ProfileProperty> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(ProfileProperty::name),
            Codec.STRING.fieldOf("value").forGetter(ProfileProperty::value),
            Codec.STRING.optionalFieldOf("signature").forGetter(ProfileProperty::signature)
    ).apply(instance, ProfileProperty::new));

    public static final StreamCodec<io.netty.buffer.ByteBuf, ProfileProperty> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ProfileProperty::name,
            ByteBufCodecs.STRING_UTF8, ProfileProperty::value,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), ProfileProperty::signature,
            ProfileProperty::new
    );

    public static ProfileProperty of(Property property) {
        return new ProfileProperty(property.name(), property.value(), Optional.ofNullable(property.signature()));
    }

    public Property toProperty() {
        return new Property(name, value, signature.orElse(null));
    }
}
