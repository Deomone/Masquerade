package dev.boy.masquerade.data;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.core.UUIDUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record DisguiseIdentity(UUID uuid, String name, List<ProfileProperty> properties) {
    public static final Codec<DisguiseIdentity> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.STRING_CODEC.fieldOf("uuid").forGetter(DisguiseIdentity::uuid),
            Codec.STRING.fieldOf("name").forGetter(DisguiseIdentity::name),
            ProfileProperty.CODEC.listOf().optionalFieldOf("properties", List.of()).forGetter(DisguiseIdentity::properties)
    ).apply(instance, DisguiseIdentity::new));

    public static final StreamCodec<io.netty.buffer.ByteBuf, DisguiseIdentity> PACKET_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, DisguiseIdentity::uuid,
            ByteBufCodecs.STRING_UTF8, DisguiseIdentity::name,
            ProfileProperty.PACKET_CODEC.apply(ByteBufCodecs.list()), DisguiseIdentity::properties,
            DisguiseIdentity::new
    );

    public DisguiseIdentity {
        properties = List.copyOf(properties);
    }

    public static DisguiseIdentity of(GameProfile profile) {
        List<ProfileProperty> collected = new ArrayList<>();
        for (Property property : profile.properties().values()) {
            collected.add(ProfileProperty.of(property));
        }
        return new DisguiseIdentity(profile.id(), profile.name(), collected);
    }

    public GameProfile toGameProfile() {
        return toGameProfile(uuid);
    }

    public GameProfile toGameProfile(UUID overrideUuid) {
        Multimap<String, Property> collected = LinkedHashMultimap.create();
        for (ProfileProperty property : properties) {
            collected.put(property.name(), property.toProperty());
        }
        return new GameProfile(overrideUuid, name, new PropertyMap(collected));
    }

    public boolean hasTextures() {
        for (ProfileProperty property : properties) {
            if (property.name().equals("textures")) {
                return true;
            }
        }
        return false;
    }
}
