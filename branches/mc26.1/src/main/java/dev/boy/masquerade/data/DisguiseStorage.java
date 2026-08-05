package dev.boy.masquerade.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DisguiseStorage {
    private final File file;
    private final Logger logger;

    public DisguiseStorage(File file, Logger logger) {
        this.file = file;
        this.logger = logger;
    }

    public Map<UUID, DisguiseIdentity> load() {
        Map<UUID, DisguiseIdentity> loaded = new HashMap<>();
        if (!file.exists()) {
            return loaded;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (String key : yaml.getKeys(false)) {
            ConfigurationSection section = yaml.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            try {
                UUID wearer = UUID.fromString(key);
                UUID owner = UUID.fromString(section.getString("uuid", ""));
                String name = section.getString("name", "");
                if (name.isEmpty()) {
                    continue;
                }
                loaded.put(wearer, new DisguiseIdentity(owner, name, readProperties(section)));
            } catch (IllegalArgumentException exception) {
                logger.warning("Skipping malformed disguise entry " + key);
            }
        }
        return loaded;
    }

    private List<DisguiseIdentity.StoredProperty> readProperties(ConfigurationSection section) {
        List<DisguiseIdentity.StoredProperty> properties = new ArrayList<>();
        List<Map<?, ?>> raw = section.getMapList("properties");
        for (Map<?, ?> entry : raw) {
            Object name = entry.get("name");
            Object value = entry.get("value");
            Object signature = entry.get("signature");
            if (name != null && value != null) {
                properties.add(new DisguiseIdentity.StoredProperty(
                        name.toString(), value.toString(), signature == null ? null : signature.toString()));
            }
        }
        return properties;
    }

    public void save(Map<UUID, DisguiseIdentity> disguises) {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<UUID, DisguiseIdentity> entry : disguises.entrySet()) {
            DisguiseIdentity identity = entry.getValue();
            ConfigurationSection section = yaml.createSection(entry.getKey().toString());
            section.set("uuid", identity.uuid().toString());
            section.set("name", identity.name());

            List<Map<String, String>> properties = new ArrayList<>();
            for (DisguiseIdentity.StoredProperty property : identity.properties()) {
                Map<String, String> serialized = new HashMap<>();
                serialized.put("name", property.name());
                serialized.put("value", property.value());
                if (property.signature() != null) {
                    serialized.put("signature", property.signature());
                }
                properties.add(serialized);
            }
            section.set("properties", properties);
        }

        try {
            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            yaml.save(file);
        } catch (IOException exception) {
            logger.log(Level.SEVERE, "Failed to write disguises.yml", exception);
        }
    }
}
