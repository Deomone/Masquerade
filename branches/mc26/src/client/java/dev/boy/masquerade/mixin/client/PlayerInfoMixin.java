package dev.boy.masquerade.mixin.client;

import com.mojang.authlib.GameProfile;
import dev.boy.masquerade.client.ClientDisguiseStore;
import dev.boy.masquerade.client.rendering.DisguiseSkinCache;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInfo.class)
abstract class PlayerInfoMixin {
    @Shadow
    public abstract GameProfile getProfile();

    @Inject(method = "getSkin", at = @At("HEAD"), cancellable = true)
    private void masquerade$replaceSkin(CallbackInfoReturnable<PlayerSkin> callback) {
        if (ClientDisguiseStore.instance().isHidden(getProfile().id())) {
            ClientDisguiseStore.instance().placeholder()
                    .flatMap(identity -> DisguiseSkinCache.instance().skinFor(identity))
                    .ifPresent(callback::setReturnValue);
            return;
        }
        ClientDisguiseStore.instance().identity(getProfile().id())
                .flatMap(identity -> DisguiseSkinCache.instance().skinFor(identity))
                .ifPresent(callback::setReturnValue);
    }

    @Inject(method = "getTabListDisplayName", at = @At("HEAD"), cancellable = true)
    private void masquerade$replaceListName(CallbackInfoReturnable<Component> callback) {
        ClientDisguiseStore.instance().displayNameOverride(getProfile().id())
                .ifPresent(callback::setReturnValue);
    }
}
