package dev.boy.masquerade.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public record PluginConfig(
        boolean enabled,
        HeadDrops headDrops,
        Disguise disguise,
        Invisibility invisibility,
        boolean debugLogging
) {
    public record HeadDrops(boolean enabled, boolean requireSwordOrAxe, List<String> weaponNames) {
    }

    public record Disguise(boolean enabled, boolean persistAcrossSessions, boolean dropHeadOnDeath,
                           boolean allowSelfDisguise) {
    }

    public record Invisibility(boolean enabled, boolean hideParticles,
                               String unknownName, String placeholderSkin) {
    }

    public static PluginConfig from(FileConfiguration source) {
        return new PluginConfig(
                source.getBoolean("enabled", true),
                new HeadDrops(
                        source.getBoolean("head-drops.enabled", true),
                        source.getBoolean("head-drops.require-sword-or-axe", true),
                        List.copyOf(source.getStringList("head-drops.weapon-names").isEmpty()
                                ? List.of("Beheader", "The Beheader")
                                : source.getStringList("head-drops.weapon-names"))
                ),
                new Disguise(
                        source.getBoolean("disguise.enabled", true),
                        source.getBoolean("disguise.persist-across-sessions", true),
                        source.getBoolean("disguise.drop-head-on-death", true),
                        source.getBoolean("disguise.allow-self-disguise", true)
                ),
                new Invisibility(
                        source.getBoolean("invisibility.enabled", true),
                        source.getBoolean("invisibility.hide-particles", true),
                        source.getString("invisibility.unknown-name", "Unknown"),
                        source.getString("invisibility.placeholder-skin", "MHF_Question")
                ),
                source.getBoolean("debug-logging", false)
        );
    }
}
