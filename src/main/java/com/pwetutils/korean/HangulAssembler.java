package com.pwetutils.korean;

public class HangulAssembler {
    private static final String CHO = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ";
    private static final String JUNG = "ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ";
    private static final String JONG = " ㄱㄲㄳㄴㄵㄶㄷㄹㄺㄻㄼㄽㄾㄿㅀㅁㅂㅄㅅㅆㅇㅈㅊㅋㅌㅍㅎ";

    private static int getChoFromKey(char key) {
        switch(key) {
            case 'r': return 0;  // ㄱ
            case 's': return 2;  // ㄴ
            case 'e': return 3;  // ㄷ
            case 'f': return 5;  // ㄹ
            case 'a': return 6;  // ㅁ
            case 'q': return 7;  // ㅂ
            case 't': return 9;  // ㅅ
            case 'd': return 11; // ㅇ
            case 'w': return 12; // ㅈ
            case 'c': return 14; // ㅊ
            case 'z': return 15; // ㅋ
            case 'x': return 16; // ㅌ
            case 'v': return 17; // ㅍ
            case 'g': return 18; // ㅎ
            case 'R': return 1;  // ㄲ
            case 'E': return 4;  // ㄸ
            case 'Q': return 8;  // ㅃ
            case 'T': return 10; // ㅆ
            case 'W': return 13; // ㅉ
        }
        return -1;
    }

    private static int getJungFromKey(char key) {
        switch(key) {
            case 'k': return 0;  // ㅏ
            case 'o': return 1;  // ㅐ
            case 'i': return 2;  // ㅑ
            case 'O': return 3;  // ㅒ
            case 'j': return 4;  // ㅓ
            case 'p': return 5;  // ㅔ
            case 'u': return 6;  // ㅕ
            case 'P': return 7;  // ㅖ
            case 'h': return 8;  // ㅗ
            case 'y': return 12; // ㅛ
            case 'n': return 13; // ㅜ
            case 'b': return 17; // ㅠ
            case 'm': return 18; // ㅡ
            case 'l': return 20; // ㅣ
        }
        return -1;
    }

    private static int getJongFromKey(char key) {
        switch(key) {
            case 'r': return 1;  // ㄱ
            case 'R': return 2;  // ㄲ
            case 's': return 4;  // ㄴ
            case 'e': return 7;  // ㄷ
            case 'f': return 8;  // ㄹ
            case 'a': return 16; // ㅁ
            case 'q': return 17; // ㅂ
            case 't': return 19; // ㅅ
            case 'T': return 20; // ㅆ
            case 'd': return 21; // ㅇ
            case 'w': return 22; // ㅈ
            case 'c': return 23; // ㅊ
            case 'z': return 24; // ㅋ
            case 'x': return 25; // ㅌ
            case 'v': return 26; // ㅍ
            case 'g': return 27; // ㅎ

            case 'Q': return 3;  // ㄳ
            case 'W': return 5;  // ㄵ
            case 'E': return 6;  // ㄶ
        }
        return -1;
    }

    public static int combineVowels(int jung1, int jung2) {
        // ㅗ (8) combinations
        if (jung1 == 8) {
            if (jung2 == 0) return 9;   // ㅗ + ㅏ = ㅘ
            if (jung2 == 1) return 10;  // ㅗ + ㅐ = ㅙ
            if (jung2 == 20) return 11; // ㅗ + ㅣ = ㅚ
        }
        // ㅜ (13) combinations
        if (jung1 == 13) {
            if (jung2 == 4) return 14;  // ㅜ + ㅓ = ㅝ
            if (jung2 == 5) return 15;  // ㅜ + ㅔ = ㅞ
            if (jung2 == 20) return 16; // ㅜ + ㅣ = ㅟ
        }
        // ㅡ (18) combinations
        if (jung1 == 18 && jung2 == 20) return 19; // ㅡ + ㅣ = ㅢ

        return -1;
    }

    public static int combineJong(int jong1, int jong2) {
        // ㄱ (1) combinations
        if (jong1 == 1 && jong2 == 19) return 3;   // ㄱ + ㅅ = ㄳ
        // ㄴ (4) combinations
        if (jong1 == 4) {
            if (jong2 == 22) return 5;  // ㄴ + ㅈ = ㄵ
            if (jong2 == 27) return 6;  // ㄴ + ㅎ = ㄶ
        }
        // ㄹ (8) combinations
        if (jong1 == 8) {
            if (jong2 == 1) return 9;   // ㄹ + ㄱ = ㄺ
            if (jong2 == 16) return 10; // ㄹ + ㅁ = ㄻ
            if (jong2 == 17) return 11; // ㄹ + ㅂ = ㄼ
            if (jong2 == 19) return 12; // ㄹ + ㅅ = ㄽ
            if (jong2 == 25) return 13; // ㄹ + ㅌ = ㄾ
            if (jong2 == 26) return 14; // ㄹ + ㅍ = ㄿ
            if (jong2 == 27) return 15; // ㄹ + ㅎ = ㅀ
        }
        // ㅂ (17) combinations
        if (jong1 == 17 && jong2 == 19) return 18; // ㅂ + ㅅ = ㅄ

        return -1;
    }

    public static int decomposeComplexJong(int jongIdx) {
        switch(jongIdx) {
            case 3: return 1;   // ㄳ -> ㄱ
            case 5: return 4;   // ㄵ -> ㄴ
            case 6: return 4;   // ㄶ -> ㄴ
            case 9: return 8;   // ㄺ -> ㄹ
            case 10: return 8;  // ㄻ -> ㄹ
            case 11: return 8;  // ㄼ -> ㄹ
            case 12: return 8;  // ㄽ -> ㄹ
            case 13: return 8;  // ㄾ -> ㄹ
            case 14: return 8;  // ㄿ -> ㄹ
            case 15: return 8;  // ㅀ -> ㄹ
            case 18: return 17; // ㅄ -> ㅂ
        }
        return -1;
    }

    public static Hangul decompose(char korean) {
        Hangul h = new Hangul();
        if (korean >= 0xAC00 && korean <= 0xD7A3) {
            int code = korean - 0xAC00;
            h.jong = code % 28;
            h.jung = ((code - h.jong) / 28) % 21;
            h.cho = (((code - h.jong) / 28) - h.jung) / 21;
            if (h.jong == 0) h.jong = -1;
        }
        return h;
    }

    // Jong to Cho mapping (which jong can become which cho)
    private static final int[] JONG_TO_CHO = {
            -1, 0, 1, -1, 2, -1, -1, 3, 5, -1, -1, -1, -1, -1, -1, -1,
            6, 7, -1, 9, 10, 11, 12, 14, 15, 16, 17, 18
    };

    public static class Hangul {
        public int cho = -1;
        public int jung = -1;
        public int jong = -1;

        public char toChar() {
            if (cho != -1 && jung != -1) {
                return (char)(0xAC00 + (cho * 21 + jung) * 28 + Math.max(jong, 0));
            }
            return 0;
        }

        public void clear() {
            cho = jung = jong = -1;
        }
    }

    public static char convertKeyToKorean(char key) {
        int idx = getChoFromKey(key);
        if (idx != -1) return CHO.charAt(idx);

        idx = getJungFromKey(key);
        if (idx != -1) return JUNG.charAt(idx);

        idx = getJongFromKey(key);
        if (idx > 0) return JONG.charAt(idx);

        return 0;
    }

    public static int getChoIndex(char c) {
        return getChoFromKey(c);
    }

    public static int getJungIndex(char c) {
        return getJungFromKey(c);
    }

    public static int getJongIndex(char c) {
        return getJongFromKey(c);
    }

    public static int jongToCho(int jongIdx) {
        if (jongIdx > 0 && jongIdx < JONG_TO_CHO.length) {
            return JONG_TO_CHO[jongIdx];
        }
        return -1;
    }
}