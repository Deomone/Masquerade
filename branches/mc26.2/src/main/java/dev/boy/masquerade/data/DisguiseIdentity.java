package dev.boy.masquerade.data;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public record DisguiseIdentity(UUID uuid, String name, List<StoredProperty> properties) {
    public record StoredProperty(String name, String value, String signature) {
        public ProfileProperty toProperty() {
            return new ProfileProperty(name, value, signature);
        }
    }

    public DisguiseIdentity {
        properties = List.copyOf(properties);
    }

    public static DisguiseIdentity of(PlayerProfile profile) {
        List<StoredProperty> collected = new ArrayList<>();
        for (ProfileProperty property : profile.getProperties()) {
            collected.add(new StoredProperty(property.getName(), property.getValue(), property.getSignature()));
        }
        return new DisguiseIdentity(profile.getId(), profile.getName(), collected);
    }

    public PlayerProfile toProfile() {
        return toProfile(uuid, name);
    }

    public PlayerProfile toProfile(UUID targetUuid, String targetName) {
        PlayerProfile profile = Bukkit.createProfileExact(targetUuid, targetName);
        profile.setProperties(propertyObjects());
        return profile;
    }

    public Collection<ProfileProperty> propertyObjects() {
        List<ProfileProperty> converted = new ArrayList<>(properties.size());
        for (StoredProperty property : properties) {
            converted.add(property.toProperty());
        }
        return converted;
    }

    public boolean hasTextures() {
        for (StoredProperty property : properties) {
            if (property.name().equals("textures")) {
                return true;
            }
        }
        return false;
    }
}
