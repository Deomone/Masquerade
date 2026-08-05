package dev.boy.masquerade.mixin.client;

import com.mojang.authlib.GameProfile;
import dev.boy.masquerade.client.ClientDisguiseStore;
import dev.boy.masquerade.client.rendering.DisguiseSkinCache;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListEntry.class)
abstract class PlayerListEntryMixin {
    @Shadow
    public abstract GameProfile getProfile();

    @Inject(method = "getSkinTextures", at = @At("HEAD"), cancellable = true)
    private void masquerade$replaceSkin(CallbackInfoReturnable<SkinTextures> callback) {
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

    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
    private void masquerade$replaceListName(CallbackInfoReturnable<Text> callback) {
        ClientDisguiseStore.instance().displayNameOverride(getProfile().id())
                .ifPresent(callback::setReturnValue);
    }
}
