package dev.boy.masquerade.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

public final class DisguiseStorage {
    private static final Logger LOGGER = LoggerFactory.getLogger("Masquerade/Storage");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Codec<Map<UUID, DisguiseIdentity>> CODEC =
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, DisguiseIdentity.CODEC);

    private final Path file;

    public DisguiseStorage(Path file) {
        this.file = file;
    }

    public static DisguiseStorage forServer(MinecraftServer server) {
        return new DisguiseStorage(server.getWorldPath(LevelResource.ROOT).resolve("data").resolve("masquerade.json"));
    }

    public Map<UUID, DisguiseIdentity> load() {
        if (!Files.exists(file)) {
            return Map.of();
        }

        try {
            String raw = Files.readString(file, StandardCharsets.UTF_8);
            JsonElement json = GSON.fromJson(raw, JsonElement.class);
            return CODEC.parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(error -> LOGGER.warn("Malformed disguise storage: {}", error))
                    .orElse(Map.of());
        } catch (IOException | RuntimeException exception) {
            LOGGER.error("Failed to read disguise storage", exception);
            return Map.of();
        }
    }

    public void save(Map<UUID, DisguiseIdentity> disguises) {
        CODEC.encodeStart(JsonOps.INSTANCE, disguises)
                .resultOrPartial(error -> LOGGER.error("Failed to encode disguise storage: {}", error))
                .ifPresent(json -> {
                    try {
                        Files.createDirectories(file.getParent());
                        Files.writeString(file, GSON.toJson(json), StandardCharsets.UTF_8);
                    } catch (IOException exception) {
                        LOGGER.error("Failed to write disguise storage", exception);
                    }
                });
    }
}
