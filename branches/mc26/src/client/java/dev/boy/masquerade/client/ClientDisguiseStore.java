package dev.boy.masquerade.client;

import dev.boy.masquerade.data.DisguiseIdentity;
import dev.boy.masquerade.data.DisguiseState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientDisguiseStore {
    private static final ClientDisguiseStore INSTANCE = new ClientDisguiseStore();

    private final Map<UUID, DisguiseState> states = new ConcurrentHashMap<>();

    private String unknownName = "Unknown";
    private Optional<DisguiseIdentity> placeholder = Optional.empty();

    private ClientDisguiseStore() {
    }

    public static ClientDisguiseStore instance() {
        return INSTANCE;
    }

    public void accept(DisguiseState state) {
        if (state.isEmpty()) {
            states.remove(state.player());
        } else {
            states.put(state.player(), state);
        }
    }

    public void replaceAll(List<DisguiseState> snapshot) {
        states.clear();
        for (DisguiseState state : snapshot) {
            accept(state);
        }
    }

    public void clear() {
        states.clear();
    }

    public Optional<DisguiseState> state(UUID player) {
        return Optional.ofNullable(states.get(player));
    }

    public Optional<DisguiseIdentity> identity(UUID player) {
        return state(player).flatMap(DisguiseState::identity);
    }

    public boolean isHidden(UUID player) {
        return state(player).map(DisguiseState::hidden).orElse(false);
    }

    public Optional<Component> displayNameOverride(Player player) {
        return displayNameOverride(player.getUUID());
    }

    public Optional<Component> displayNameOverride(UUID player) {
        DisguiseState state = states.get(player);
        if (state == null) {
            return Optional.empty();
        }

        Component base;
        if (state.hidden()) {
            base = Component.literal(unknownName).withStyle(ChatFormatting.OBFUSCATED);
        } else if (state.identity().isPresent()) {
            base = Component.literal(state.identity().get().name());
        } else {
            return Optional.empty();
        }

        Component result = base;
        if (state.revealName().isPresent()) {
            result = Component.empty().append(base)
                    .append(Component.literal(" (" + state.revealName().get() + ")").withStyle(ChatFormatting.GRAY));
        }
        return Optional.of(result);
    }

    public void setUnknownName(String value) {
        this.unknownName = value;
    }

    public String unknownName() {
        return unknownName;
    }

    public Optional<String> plainNameOverride(UUID player) {
        DisguiseState state = states.get(player);
        if (state == null) {
            return Optional.empty();
        }
        if (state.hidden()) {
            return Optional.of(ChatFormatting.OBFUSCATED + unknownName + ChatFormatting.RESET);
        }
        return state.identity().map(DisguiseIdentity::name);
    }

    public Optional<DisguiseIdentity> skinIdentity(UUID player) {
        DisguiseState state = states.get(player);
        if (state == null) {
            return Optional.empty();
        }
        return state.hidden() ? placeholder : state.identity();
    }

    public Optional<DisguiseIdentity> placeholder() {
        return placeholder;
    }

    public void setPlaceholder(Optional<DisguiseIdentity> value) {
        this.placeholder = value;
    }
}
