package dev.boy.masquerade.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record MasqueradeConfig(
        boolean enabled,
        HeadDrops headDrops,
        Disguise disguise,
        Invisibility invisibility,
        Permissions permissions,
        boolean debugLogging
) {
    public static final MasqueradeConfig DEFAULT = new MasqueradeConfig(
            true,
            HeadDrops.DEFAULT,
            Disguise.DEFAULT,
            Invisibility.DEFAULT,
            Permissions.DEFAULT,
            false
    );

    public static final Codec<MasqueradeConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(MasqueradeConfig::enabled),
            HeadDrops.CODEC.optionalFieldOf("head_drops", HeadDrops.DEFAULT).forGetter(MasqueradeConfig::headDrops),
            Disguise.CODEC.optionalFieldOf("disguise", Disguise.DEFAULT).forGetter(MasqueradeConfig::disguise),
            Invisibility.CODEC.optionalFieldOf("invisibility", Invisibility.DEFAULT).forGetter(MasqueradeConfig::invisibility),
            Permissions.CODEC.optionalFieldOf("permissions", Permissions.DEFAULT).forGetter(MasqueradeConfig::permissions),
            Codec.BOOL.optionalFieldOf("debug_logging", false).forGetter(MasqueradeConfig::debugLogging)
    ).apply(instance, MasqueradeConfig::new));

    public record HeadDrops(boolean enabled, boolean requireSwordOrAxe, List<String> weaponNames) {
        public static final HeadDrops DEFAULT = new HeadDrops(true, true, List.of("Beheader", "The Beheader"));

        public static final Codec<HeadDrops> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("enabled", true).forGetter(HeadDrops::enabled),
                Codec.BOOL.optionalFieldOf("require_sword_or_axe", true).forGetter(HeadDrops::requireSwordOrAxe),
                Codec.STRING.listOf().optionalFieldOf("weapon_names", List.of("Beheader", "The Beheader")).forGetter(HeadDrops::weaponNames)
        ).apply(instance, HeadDrops::new));

        public HeadDrops {
            weaponNames = List.copyOf(weaponNames);
        }
    }

    public record Disguise(boolean enabled, boolean persistAcrossSessions, boolean dropHeadOnDeath, boolean allowSelfDisguise) {
        public static final Disguise DEFAULT = new Disguise(true, true, true, true);

        public static final Codec<Disguise> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("enabled", true).forGetter(Disguise::enabled),
                Codec.BOOL.optionalFieldOf("persist_across_sessions", true).forGetter(Disguise::persistAcrossSessions),
                Codec.BOOL.optionalFieldOf("drop_head_on_death", true).forGetter(Disguise::dropHeadOnDeath),
                Codec.BOOL.optionalFieldOf("allow_self_disguise", true).forGetter(Disguise::allowSelfDisguise)
        ).apply(instance, Disguise::new));
    }

    public record Invisibility(boolean enabled, boolean hideParticles, boolean hideFromPlayerList, String unknownName,
                               String placeholderSkin) {
        public static final Invisibility DEFAULT = new Invisibility(true, true, true, "Unknown", "MHF_Question");

        public static final Codec<Invisibility> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("enabled", true).forGetter(Invisibility::enabled),
                Codec.BOOL.optionalFieldOf("hide_particles", true).forGetter(Invisibility::hideParticles),
                Codec.BOOL.optionalFieldOf("hide_from_player_list", true).forGetter(Invisibility::hideFromPlayerList),
                Codec.STRING.optionalFieldOf("unknown_name", "Unknown").forGetter(Invisibility::unknownName),
                Codec.STRING.optionalFieldOf("placeholder_skin", "MHF_Question").forGetter(Invisibility::placeholderSkin)
        ).apply(instance, Invisibility::new));
    }

    public record Permissions(String adminNode, String revealNode, int adminFallbackLevel, int revealFallbackLevel) {
        public static final Permissions DEFAULT = new Permissions("masquerade:admin", "masquerade:reveal", 2, 2);

        public static final Codec<Permissions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.optionalFieldOf("admin_node", "masquerade:admin").forGetter(Permissions::adminNode),
                Codec.STRING.optionalFieldOf("reveal_node", "masquerade:reveal").forGetter(Permissions::revealNode),
                Codec.INT.optionalFieldOf("admin_fallback_level", 2).forGetter(Permissions::adminFallbackLevel),
                Codec.INT.optionalFieldOf("reveal_fallback_level", 2).forGetter(Permissions::revealFallbackLevel)
        ).apply(instance, Permissions::new));
    }
}
