package com.muryko.betterambience;

import com.muryko.betterambience.fog.FogBiomeRegistry;
import com.muryko.betterambience.fog.FogTransitionManager;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MurykosBetterAmbienceClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Inicializa o registro da neblina
        FogBiomeRegistry.initialize();

        // Registre outros eventos necessários, como renderização ou atualização do cliente.
    }

    public static void updateFog() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null) {
            BlockPos playerPos = client.player.getBlockPos();
            World world = client.world;

            // Use o FogTransitionManager para aplicar neblina com base na posição do jogador
            FogTransitionManager.applyFog(world, playerPos);
        }
    }
}
