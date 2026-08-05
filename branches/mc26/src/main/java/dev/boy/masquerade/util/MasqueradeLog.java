package dev.boy.masquerade.util;

import dev.boy.masquerade.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MasqueradeLog {
    private static final Logger LOGGER = LoggerFactory.getLogger("Masquerade");

    private MasqueradeLog() {
    }

    public static Logger logger() {
        return LOGGER;
    }

    public static void info(String message, Object... args) {
        LOGGER.info(message, args);
    }

    public static void warn(String message, Object... args) {
        LOGGER.warn(message, args);
    }

    public static void error(String message, Throwable throwable) {
        LOGGER.error(message, throwable);
    }

    public static void debug(ConfigManager config, String message, Object... args) {
        if (config.get().debugLogging()) {
            LOGGER.info("[debug] " + message, args);
        }
    }
}
