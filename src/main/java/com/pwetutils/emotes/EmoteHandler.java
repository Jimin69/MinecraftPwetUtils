package com.pwetutils.emotes;

import java.util.HashMap;
import java.util.Map;

public class EmoteHandler {
    private static boolean emotesEnabled = true;
    public static boolean areEmotesEnabled() {
        return emotesEnabled;
    }
    public static void setEmotesEnabled(boolean enabled) {
        emotesEnabled = enabled;
    }

    public static boolean isEmote(String text) {
        return EMOTES.containsKey(text);
    }

    private static final Map<String, String> EMOTES = new HashMap<>();

    static {
        EMOTES.put("<3", "❤");
        EMOTES.put(":star:", "✮");
        EMOTES.put(":yes:", "✔");
        EMOTES.put(":no:", "✖");
        EMOTES.put(":java:", "☕");
        EMOTES.put(":arrow:", "➜");
        EMOTES.put(":shrug:", "¯\\_(ツ)_/¯");
        EMOTES.put(":tableflip:", "(╯°□°）╯︵ ┻━┻");
        EMOTES.put("o/", "( ﾟ◡ﾟ)/");
        EMOTES.put(":totem:", "☉_☉");
        EMOTES.put(":typing:", "✎...");
        EMOTES.put(":maths:", "√(π+x)=L");
        EMOTES.put(":snail:", "@'-'");
        EMOTES.put(":thinking:", "(0.o?)");
        EMOTES.put(":gimme:", "༼つ◕_◕༽つ");
        EMOTES.put(":wizard:", "('-')⊃━☆ﾟ.*･｡ﾟ");
        EMOTES.put(":pvp:", "⚔");
        EMOTES.put(":peace:", "✌");
        EMOTES.put(":oof:", "OOF");
        EMOTES.put(":puffer:", "<('O')>");
        EMOTES.put("^-^", "^-^");
        EMOTES.put(":cute:", "(✿◠‿◠)");
        EMOTES.put(":yey:", "ヽ (◕◡◕) ﾉ");
        EMOTES.put(":cat:", "= ＾● ⋏ ●＾ =");
        EMOTES.put(":dab:", "<o/");
        EMOTES.put("^_^", "^_^");
        EMOTES.put(":sloth:", "(・⊝・)");
        EMOTES.put(":dj:", "ヽ(⌐■_■)ノ♬");
        EMOTES.put(":snow:", "☃");
        EMOTES.put("h/", "ヽ(^◇^*)/");
        EMOTES.put(":dog:", "(ᵔᴥᵔ)");
    }

    public static String processEmotes(String message) {
        String processed = message;

        processed = processed.replaceAll("<3(?![0-9A-Za-z])", "❤");
        processed = processed.replaceAll("(?<![0-9A-Za-z])o/(?![0-9A-Za-z])", "( ﾟ◡ﾟ)/");
        processed = processed.replaceAll("(?<![0-9A-Za-z])h/(?![0-9A-Za-z])", "ヽ(^◇^*)/");

        for (Map.Entry<String, String> emote : EMOTES.entrySet()) {
            String key = emote.getKey();
            if (!key.equals("<3") && !key.equals("o/") && !key.equals("h/")) {
                processed = processed.replace(key, emote.getValue());
            }
        }

        return processed;
    }
}