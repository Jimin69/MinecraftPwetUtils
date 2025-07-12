package com.pwetutils.listener;

public class GameStateTracker {
    private static boolean inBedwars = true;
    private static boolean gametypeDetected = false;

    public static void setGametype(String gametype) {
        gametypeDetected = true;
        inBedwars = gametype.equalsIgnoreCase("BEDWARS");
    }

    public static void reset() {
        inBedwars = true;
        gametypeDetected = false;
    }

    public static boolean shouldShowOverlays() {
        return !gametypeDetected || inBedwars;
    }
}