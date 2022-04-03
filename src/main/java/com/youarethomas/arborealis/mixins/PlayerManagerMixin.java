package com.youarethomas.arborealis.mixins;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.util.ArborealisConstants;
import com.youarethomas.arborealis.util.RuneManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Inject(method = "onPlayerConnect(Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;<init>(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;)V", shift = At.Shift.AFTER))
    public void arborealisClientGetRunes(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        if (player.networkHandler != null) {
            Arborealis.LOGGER.info(String.format("%s runes synced to %s!", RuneManager.getRuneCount(), player.getEntityName()));

            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeCollection(RuneManager.getRunes(), (packet, rune) -> {
                packet.writeString(rune.settings.id);
                packet.writeString(rune.name);
                packet.writeString(rune.colour);
                packet.writeString(rune.catalyst.toString());
                packet.writeInt(rune.lifeForce);
                packet.writeIntArray(rune.shape);
            });

            ServerPlayNetworking.send(player, ArborealisConstants.CLIENT_RUNE_PUSH, buf);
        }

    }
}
