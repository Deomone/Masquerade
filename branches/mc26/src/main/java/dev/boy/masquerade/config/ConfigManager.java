package dev.boy.masquerade.config;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

public final class ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("Masquerade/Config");
    private static final Gson READER = new Gson();
    private static final Gson WRITER = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .create();

    private final Path path;
    private final AtomicReference<MasqueradeConfig> current = new AtomicReference<>(MasqueradeConfig.DEFAULT);

    public ConfigManager(Path path) {
        this.path = path;
    }

    public static ConfigManager createDefault() {
        return new ConfigManager(FabricLoader.getInstance().getConfigDir().resolve("masquerade.json"));
    }

    public MasqueradeConfig get() {
        return current.get();
    }

    public void load() {
        current.set(read());
        write(current.get());
    }

    public void reload() {
        load();
    }

    private MasqueradeConfig read() {
        if (!Files.exists(path)) {
            return MasqueradeConfig.DEFAULT;
        }

        try {
            String raw = Files.readString(path, StandardCharsets.UTF_8);
            if (raw.isBlank()) {
                LOGGER.warn("masquerade.json is empty, rewriting it with defaults");
                return MasqueradeConfig.DEFAULT;
            }

            JsonElement json = READER.fromJson(raw, JsonElement.class);
            return MasqueradeConfig.CODEC.parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(error -> LOGGER.warn("Malformed masquerade.json: {}", error))
                    .orElse(MasqueradeConfig.DEFAULT);
        } catch (IOException | RuntimeException exception) {
            LOGGER.error("Failed to read masquerade.json, falling back to defaults", exception);
            return MasqueradeConfig.DEFAULT;
        }
    }

    private void write(MasqueradeConfig config) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, WRITER.toJson(config) + System.lineSeparator(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            LOGGER.error("Failed to write masquerade.json", exception);
        }
    }
}
