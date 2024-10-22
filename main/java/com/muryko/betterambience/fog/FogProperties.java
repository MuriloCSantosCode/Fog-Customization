package com.muryko.betterambience.fog;

import java.util.List;

public class FogProperties {
    private final FogSetting day;
    private final FogSetting night;
    private final FogSetting rain;
    private final FogSetting storm;
    private final List<FogEvent> events;
    private final float fogTransitionBiome; // Transição específica do bioma
    public FogProperties(FogSetting day, FogSetting night, FogSetting rain, FogSetting storm, List<FogEvent> events, float fogTransitionBiome) {
        this.day = day;
        this.night = night;
        this.rain = rain;
        this.storm = storm;
        this.events = events;
        this.fogTransitionBiome = fogTransitionBiome;
    }
    public FogSetting getDaySetting() {
        return day;
    }
    public FogSetting getNightSetting() {
        return night != null ? night : day;
    }
    public FogSetting getRainSetting() {
        return rain != null ? rain : getDaySetting();
    }
    public FogSetting getStormSetting() {
        return storm != null ? storm : getRainSetting();
    }
    public List<FogEvent> getEvents() {
        return events;
    }
    public float getFogTransitionBiome() {
        return fogTransitionBiome;
    }
    public static class FogSetting {
        private final float fogStart;
        private final float fogEnd;
        private final float[] fogColor;
        public FogSetting(float fogStart, float fogEnd, float[] fogColor) {
            this.fogStart = fogStart;
            this.fogEnd = fogEnd;
            this.fogColor = fogColor;
        }
        public float getFogStart() {
            return fogStart;
        }
        public float getFogEnd() {
            return fogEnd;
        }
        public float[] getFogColor() {
            return fogColor;
        }
    }
    public static class FogEvent {
        private final String name;
        private final long startTime;
        private final long endTime;
        private final FogSetting fogSetting;
        private final float fogTransitionEvent;
        public FogEvent(String name, long startTime, long endTime, FogSetting fogSetting, float fogTransitionEvent) {
            this.name = name;
            this.startTime = startTime;
            this.endTime = endTime;
            this.fogSetting = fogSetting;
            this.fogTransitionEvent = fogTransitionEvent;
        }
        public String getName() {
            return name;
        }
        public long getStartTime() {
            return startTime;
        }
        public long getEndTime() {
            return endTime;
        }
        public FogSetting getFogSetting() {
            return fogSetting;
        }
        public float getFogTransitionEvent() {
            return fogTransitionEvent;
        }
        public boolean isActive(long currentTime) {
            if (startTime < endTime) {
                return currentTime >= startTime && currentTime < endTime;
            } else {
                // Caso o evento passe da meia-noite (endTime < startTime)
                return currentTime >= startTime || currentTime < endTime;
            }
        }
    }
}
