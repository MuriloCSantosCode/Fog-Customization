package com.muryko.betterambience.command;

import com.muryko.betterambience.fog.FogBiomeRegistry;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class BetterAmbienceCommands {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("reloadFog")
                .executes(context -> {
                    return reloadFog();
                }));
        });
    }

    private static int reloadFog() {
        try {
            // Recarregar as configurações da neblina
            FogBiomeRegistry.reloadConfig();  // Certifique-se de que esse método existe e recarrega as configurações corretamente

            MinecraftClient client = MinecraftClient.getInstance();
            client.player.sendMessage(Text.literal("Configuração de neblina recarregada com sucesso!"), false);

            // Forçar recalcular a neblina
            forceFogRecalculation();

            // Aplicar a atualização imediata da neblina
            applyImmediateFogUpdate();

            // Atualizar a neblina com a nova configuração
            client.execute(() -> {
                if (client.world != null && client.player != null) {
                    FogRendererHelper.forceUpdateFog(client.world, client.player.getBlockPos());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            MinecraftClient.getInstance().player.sendMessage(Text.literal("Ocorreu um erro ao tentar recarregar a neblina."), false);
        }
        return 1;
    }

    private static void applyImmediateFogUpdate() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            FogRendererHelper.applyFog(client.world, client.player.getBlockPos());
        }
    }

    private static void forceFogRecalculation() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.execute(() -> {
                try {
                    client.worldRenderer.reload();  // Recarrega o renderizador do mundo
                    applyImmediateFogUpdate();  // Aplica as atualizações imediatamente
                    System.out.println("Forçando recalculação da neblina com base nas novas configurações.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else {
            System.err.println("Erro: Jogador ou cliente não está disponível para forçar a recalculação da neblina.");
        }
    }
}
