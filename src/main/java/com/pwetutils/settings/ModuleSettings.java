package com.pwetutils.settings;

public class ModuleSettings {
    private static boolean nameMentionEnabled = true;
    private static boolean chatWarningsEnabled = true;
    private static boolean languageInputEnabled = true;

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
}