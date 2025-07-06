package com.pwetutils.settings;

public class ModuleSettings {
    private static boolean nameMentionEnabled = true;
    private static boolean chatWarningsEnabled = true;
    private static boolean languageInputEnabled = true;
    private static boolean increaseChatLengthEnabled = true;
    private static boolean resourceTimerEnabled = true;
    private static boolean experienceCounterEnabled = true;
    private static boolean emoteConverterEnabled = true;

    public static boolean isNameMentionEnabled() {
        return nameMentionEnabled;
    }

    public static void setNameMentionEnabled(boolean enabled) {
        nameMentionEnabled = enabled;
    }

    public static boolean isChatWarningsEnabled() {
        return chatWarningsEnabled;
    }

    public static void setChatWarningsEnabled(boolean enabled) {
        chatWarningsEnabled = enabled;
    }

    public static boolean isLanguageInputEnabled() {
        return languageInputEnabled;
    }

    public static void setLanguageInputEnabled(boolean enabled) {
        languageInputEnabled = enabled;
    }

    public static boolean isIncreaseChatLengthEnabled() {
        return increaseChatLengthEnabled;
    }

    public static void setIncreaseChatLengthEnabled(boolean enabled) {
        increaseChatLengthEnabled = enabled;
    }

    public static boolean isResourceTimerEnabled() {
        return resourceTimerEnabled;
    }

    public static void setResourceTimerEnabled(boolean enabled) {
        resourceTimerEnabled = enabled;
    }

    public static boolean isExperienceCounterEnabled() {
        return experienceCounterEnabled;
    }

    public static void setExperienceCounterEnabled(boolean enabled) {
        experienceCounterEnabled = enabled;
    }

    public static boolean isEmoteConverterEnabled() {
        return emoteConverterEnabled;
    }

    public static void setEmoteConverterEnabled(boolean enabled) {
        emoteConverterEnabled = enabled;
    }
}