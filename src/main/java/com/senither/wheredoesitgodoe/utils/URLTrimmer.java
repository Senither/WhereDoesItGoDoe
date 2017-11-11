package com.senither.wheredoesitgodoe.utils;

public class URLTrimmer {

    private static final char[] specialCharacters = new char[]{
            '<', '>', ' ', '.', ',', ':', ';', '\'', '\\', '/', '[', ']', '(', ')'
    };

    public static String trim(String string) {
        if (string.length() == 0) {
            return string;
        }
        
        for (char character : specialCharacters) {
            if (string.charAt(0) == character) {
                return trim(string.substring(1, string.length()));
            }

            if (string.charAt(string.length() - 1) == character) {
                return trim(string.substring(0, string.length() - 1));
            }
        }
        return string;
    }
}
