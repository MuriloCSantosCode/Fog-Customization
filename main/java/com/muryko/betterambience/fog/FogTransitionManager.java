package com.muryko.betterambience.fog;

import net.minecraft.client.MinecraftClient;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;

public class FogTransitionManager {

    // Variáveis para armazenar configurações de neblina
    private static float[] currentFogSettings = new float[4]; // {density, colorR, colorG, colorB}
    private static long lastUpdateTime = 0;

    // Método público para aplicar a neblina
    public static void applyFog(float[] fogSettings, float transitionTime, float duration) {
        if (fogSettings == null || fogSettings.length < 4) {
            throw new IllegalArgumentException("fogSettings must contain at least 4 values: density, colorR, colorG, colorB.");
        }

        // Atualiza as configurações de neblina atuais
        System.arraycopy(fogSettings, 0, currentFogSettings, 0, fogSettings.length);

        // Obtenha o cliente e o mundo atual
        MinecraftClient client = MinecraftClient.getInstance();
        World world = client.world;

        if (world != null) {
            // Aplique a lógica de transição de neblina
            long currentTime = System.currentTimeMillis();
            float alpha = Math.min(1.0f, (currentTime - lastUpdateTime) / (transitionTime * 1000)); // Interpolação suave

            // Aplique a neblina ao mundo
            float newDensity = interpolateFogDensity(currentFogSettings[0], alpha);
            float newColorR = interpolateFogColor(currentFogSettings[1], alpha);
            float newColorG = interpolateFogColor(currentFogSettings[2], alpha);
            float newColorB = interpolateFogColor(currentFogSettings[3], alpha);

            // Aplique as configurações de neblina ao cliente
            client.options.fogDistance = newDensity; // Exemplo de como aplicar a densidade de neblina
            client.options.fogColorR = newColorR;
            client.options.fogColorG = newColorG;
            client.options.fogColorB = newColorB;

            lastUpdateTime = currentTime; // Atualize o tempo da última aplicação
        }
    }

    // Método para iniciar a transição global
    public static void startGlobalTransition(FogProperties.FogSetting fogSetting, float transitionTime) {
        float[] fogSettings = convertFogSettingToArray(fogSetting);
        applyFog(fogSettings, transitionTime, 0); // 0 como duração se não for necessário
    }

    // Método para iniciar a transição de evento
    public static void startEventTransition(FogProperties.FogSetting fogSetting, float transitionTime) {
        float[] fogSettings = convertFogSettingToArray(fogSetting);
        applyFog(fogSettings, transitionTime, 0);
    }

    // Método para iniciar a transição de bioma
    public static void startBiomeTransition(FogProperties.FogSetting fogSetting, float transitionTime) {
        float[] fogSettings = convertFogSettingToArray(fogSetting);
        applyFog(fogSettings, transitionTime, 0);
    }

    // Método auxiliar para converter a configuração de neblina em um array
    private static float[] convertFogSettingToArray(FogProperties.FogSetting fogSetting) {
        // Exemplo de conversão, ajuste conforme suas necessidades
        return new float[] {
            fogSetting.getDensity(),
            fogSetting.getColorR(),
            fogSetting.getColorG(),
            fogSetting.getColorB()
        };
    }

    // Métodos auxiliares para interpolação
    private static float interpolateFogDensity(float targetDensity, float alpha) {
        // Interpolação simples para a densidade
        return currentFogSettings[0] * (1 - alpha) + targetDensity * alpha;
    }

    private static float interpolateFogColor(float targetColor, float alpha) {
        // Interpolação simples para a cor
        return currentFogSettings[1] * (1 - alpha) + targetColor * alpha;
    }
}
