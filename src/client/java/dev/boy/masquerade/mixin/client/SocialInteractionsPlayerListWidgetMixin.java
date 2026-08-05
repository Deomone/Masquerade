package dev.boy.masquerade.mixin.client;

import dev.boy.masquerade.client.ClientDisguiseStore;
import dev.boy.masquerade.client.rendering.DisguiseSkinCache;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListWidget;
import net.minecraft.entity.player.SkinTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Mixin(SocialInteractionsPlayerListWidget.class)
abstract class SocialInteractionsPlayerListWidgetMixin {
    @ModifyArgs(
            method = "createListEntry",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screen/multiplayer/SocialInteractionsPlayerListEntry;<init>(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/gui/screen/multiplayer/SocialInteractionsScreen;Ljava/util/UUID;Ljava/lang/String;Ljava/util/function/Supplier;Z)V")
    )
    private void masquerade$applyDisguise(Args args) {
        UUID subject = args.get(2);
        ClientDisguiseStore store = ClientDisguiseStore.instance();

        store.plainNameOverride(subject).ifPresent(name -> args.set(3, name));

        Optional<SkinTextures> skin = store.skinIdentity(subject)
                .flatMap(identity -> DisguiseSkinCache.instance().skinFor(identity));
        if (skin.isPresent()) {
            Supplier<SkinTextures> supplier = skin::get;
            args.set(4, supplier);
        }
    }
}
