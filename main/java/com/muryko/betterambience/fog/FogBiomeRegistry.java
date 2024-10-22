package com.muryko.betterambience.fog;

import com.google.gson.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FogBiomeRegistry {
    private static final Map<RegistryKey<Biome>, FogProperties> BIOME_FOG_MAP = new HashMap<>();
    private static final Gson GSON = new Gson();
    private static final String CONFIG_FILE_PATH = "config/betterambience.json";
    private static float fogTransitionGlobal = 2.0f; // Tempo padrão em segundos
    private static float fogTransitionBiome = 2.0f; // Tempo padrão em segundos
    private static float fogTransitionEvent = 2.0f; // Tempo padrão em segundos

    public static void initialize() {
        File configDir = new File("config");
        if (!configDir.exists()) {
            boolean created = configDir.mkdirs();
            if (created) {
                System.out.println("Diretório 'config' criado.");
            } else {
                System.err.println("Falha ao criar o diretório 'config'.");
            }
        }

        File configFile = new File(CONFIG_FILE_PATH);
        if (!configFile.exists()) {
            createDefaultConfig(configFile);
        }
        loadConfig(configFile);
    }

    // Método para criar configuração padrão
    private static void createDefaultConfig(File configFile) {
        JsonObject root = new JsonObject();
        root.addProperty("fogTransitionGlobal", 2.0);

        JsonObject biomes = new JsonObject();

        // Adiciona as configurações padrão de neblina para o bioma plains
        JsonObject plains = new JsonObject();
        plains.addProperty("fogTransitionBiome", 3.0);
        JsonArray plainsEvents = new JsonArray();

        JsonObject morningFog = new JsonObject();
        morningFog.addProperty("name", "MorningFog");
        morningFog.addProperty("startTime", 0);
        morningFog.addProperty("endTime", 6000);
        morningFog.addProperty("fogTransitionEvent", 2.0); // Valor corrigido para 2.0
        morningFog.addProperty("fogStart", 10.0);
        morningFog.addProperty("fogEnd", 60.0);
        morningFog.add("fogColor", GSON.toJsonTree(new float[]{0.7f, 0.8f, 0.9f}));
        plainsEvents.add(morningFog);

        JsonObject eveningFog = new JsonObject();
        eveningFog.addProperty("name", "EveningFog");
        eveningFog.addProperty("startTime", 18000);
        eveningFog.addProperty("endTime", 24000);
        eveningFog.addProperty("fogTransitionEvent", 2.0);
        eveningFog.addProperty("fogStart", 15.0);
        eveningFog.addProperty("fogEnd", 90.0);
        eveningFog.add("fogColor", GSON.toJsonTree(new float[]{1.0f, 0.3f, 0.4f}));
        plainsEvents.add(eveningFog);
        plains.add("events", plainsEvents);

        // Adiciona as configurações padrão de neblina para o bioma plains
        JsonObject plainsDefault = new JsonObject();
        plainsDefault.addProperty("fogStartDay", 10.0);
        plainsDefault.addProperty("fogEndDay", 60.0);
        plainsDefault.add("fogColorDay", GSON.toJsonTree(new float[]{1.0f, 0.0f, 1.0f}));

        plainsDefault.addProperty("fogStartNight", 15.0);
        plainsDefault.addProperty("fogEndNight", 90.0);
        plainsDefault.add("fogColorNight", GSON.toJsonTree(new float[]{1.0f, 0.3f, 0.4f}));

        plainsDefault.addProperty("fogStartRain", 5.0);
        plainsDefault.addProperty("fogEndRain", 40.0);
        plainsDefault.add("fogColorRain", GSON.toJsonTree(new float[]{0.4f, 0.5f, 1.0f}));

        plainsDefault.addProperty("fogStartStorm", 5.0);
        plainsDefault.addProperty("fogEndStorm", 80.0);
        plainsDefault.add("fogColorStorm", GSON.toJsonTree(new float[]{1.0f, 1.0f, 0.2f}));
        plains.add("default", plainsDefault);
        biomes.add("minecraft:plains", plains);

        // Adiciona configuração padrão para o bioma desert
        JsonObject desert = new JsonObject();
        desert.addProperty("fogTransitionBiome", 3.0);
        JsonArray desertEvents = new JsonArray();
        desert.add("events", desertEvents);
        JsonObject desertDefault = new JsonObject();
        desertDefault.addProperty("fogStartDay", 20.0);
        desertDefault.addProperty("fogEndDay", 150.0);
        desertDefault.add("fogColorDay", GSON.toJsonTree(new float[]{1.0f, 0.9f, 0.5f}));

        desertDefault.addProperty("fogStartRain", 0.0);
        desertDefault.addProperty("fogEndRain", 0.0);
        desertDefault.add("fogColorRain", GSON.toJsonTree(new float[]{1.0f, 0.8f, 0.3f}));

        desertDefault.addProperty("fogStartStorm", 0.0);
        desertDefault.addProperty("fogEndStorm", 0.0);
        desertDefault.add("fogColorStorm", GSON.toJsonTree(new float[]{1.0f, 0.8f, 0.3f}));
        desert.add("default", desertDefault);
        biomes.add("minecraft:desert", desert);

        root.add("biomes", biomes);
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(root, writer);
            System.out.println("Arquivo 'betterambience.json' criado com valores padrão.");
        } catch (IOException e) {
            System.err.println("Erro ao criar configuração: " + e.getMessage());
        }
    }

    public static void loadConfig(File configFile) {
        BIOME_FOG_MAP.clear(); // Limpa as propriedades anteriores
        try (FileReader reader = new FileReader(configFile)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            // Lê a configuração global
            fogTransitionGlobal = root.get("fogTransitionGlobal").getAsFloat();
            fogTransitionBiome = root.get("fogTransitionBiome").getAsFloat();
            fogTransitionEvent = root.get("fogTransitionEvent").getAsFloat();

            JsonObject biomes = root.getAsJsonObject("biomes");
            for (Map.Entry<String, JsonElement> entry : biomes.entrySet()) {
                String biomeId = entry.getKey();
                JsonObject biomeConfig = entry.getValue().getAsJsonObject();

                // Lê a transição de neblina do bioma
                float biomeTransition = biomeConfig.get("fogTransitionBiome").getAsFloat();

                // Lê eventos
                List<FogProperties.FogEvent> events = new ArrayList<>();
                if (biomeConfig.has("events")) {
                    JsonArray eventsArray = biomeConfig.getAsJsonArray("events");
                    for (JsonElement eventElement : eventsArray) {
                        JsonObject eventConfig = eventElement.getAsJsonObject();
                        String eventName = eventConfig.get("name").getAsString();
                        long eventStartTime = eventConfig.get("startTime").getAsLong();
                        long eventEndTime = eventConfig.get("endTime").getAsLong();
                        FogProperties.FogSetting fogSetting = new FogProperties.FogSetting(
                                eventConfig.get("fogStart").getAsFloat(),
                                eventConfig.get("fogEnd").getAsFloat(),
                                GSON.fromJson(eventConfig.get("fogColor"), float[].class)
                        );

                        // Lê a transição de neblina do evento
                        float fogTransitionEvent = eventConfig.get("fogTransitionEvent").getAsFloat();

                        events.add(new FogProperties.FogEvent(eventName, eventStartTime, eventEndTime, fogSetting, fogTransitionEvent));
                    }
                }

                // Lê as configurações padrão de neblina do bioma
                JsonObject defaultConfig = biomeConfig.getAsJsonObject("default");
                FogProperties.FogSetting daySetting = new FogProperties.FogSetting(
                        defaultConfig.get("fogStartDay").getAsFloat(),
                        defaultConfig.get("fogEndDay").getAsFloat(),
                        GSON.fromJson(defaultConfig.get("fogColorDay"), float[].class)
                );

                FogProperties.FogSetting nightSetting = new FogProperties.FogSetting(
                        defaultConfig.has("fogStartNight") ? defaultConfig.get("fogStartNight").getAsFloat() : daySetting.getFogStart(),
                        defaultConfig.has("fogEndNight") ? defaultConfig.get("fogEndNight").getAsFloat() : daySetting.getFogEnd(),
                        defaultConfig.has("fogColorNight") ? GSON.fromJson(defaultConfig.get("fogColorNight"), float[].class) : daySetting.getFogColor()
                );

                FogProperties.FogSetting rainSetting = new FogProperties.FogSetting(
                        defaultConfig.get("fogStartRain").getAsFloat(),
                        defaultConfig.get("fogEndRain").getAsFloat(),
                        GSON.fromJson(defaultConfig.get("fogColorRain"), float[].class)
                );

                FogProperties.FogSetting stormSetting = new FogProperties.FogSetting(
                        defaultConfig.get("fogStartStorm").getAsFloat(),
                        defaultConfig.get("fogEndStorm").getAsFloat(),
                        GSON.fromJson(defaultConfig.get("fogColorStorm"), float[].class)
                );

                FogProperties properties = new FogProperties(daySetting, nightSetting, rainSetting, stormSetting, events, biomeTransition);

                // Armazena as propriedades de neblina para o bioma
                RegistryKey<Biome> biomeKey = RegistryKey.of(RegistryKeys.BIOME, Identifier.of(biomeId));
                BIOME_FOG_MAP.put(biomeKey, properties);
            }

            System.out.println("Configuração carregada com sucesso a partir de " + CONFIG_FILE_PATH);
        } catch (IOException | JsonParseException e) {
            System.err.println("Erro ao carregar configuração: " + e.getMessage());
        }
    }

    public static void reloadConfig() {
        try {
            File configFile = new File(CONFIG_FILE_PATH);
            loadConfig(configFile);
            System.out.println("Configuração de neblina recarregada com sucesso.");
        } catch (Exception e) {
            System.err.println("Erro ao recarregar a configuração de neblina: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static float getFogTransitionGlobal() {
        return fogTransitionGlobal;
    }

    public static float getFogTransitionBiome() {
        return fogTransitionBiome;
    }

    public static float getFogTransitionEvent() {
        return fogTransitionEvent;
    }

    public static FogProperties getFogProperties(RegistryKey<Biome> biomeKey) {
        FogProperties properties = BIOME_FOG_MAP.get(biomeKey);
        if (properties == null) {
            System.out.println("Nenhuma configuração de neblina encontrada para o bioma: " + biomeKey.getValue());
            return getDefaultFogProperties();
        }
        return properties;
    }

    private static FogProperties getDefaultFogProperties() {
        // Defina suas configurações padrão aqui
        FogProperties.FogSetting day = new FogProperties.FogSetting(10.0f, 60.0f, new float[]{0.5f, 0.5f, 0.5f});
        FogProperties.FogSetting night = new FogProperties.FogSetting(15.0f, 90.0f, new float[]{0.3f, 0.3f, 0.3f});
        FogProperties.FogSetting rain = new FogProperties.FogSetting(5.0f, 40.0f, new float[]{0.4f, 0.4f, 0.4f});
        FogProperties.FogSetting storm = new FogProperties.FogSetting(5.0f, 80.0f, new float[]{0.6f, 0.6f, 0.6f});
        List<FogProperties.FogEvent> events = new ArrayList<>();
        return new FogProperties(day, night, rain, storm, events, fogTransitionBiome);
    }
}
