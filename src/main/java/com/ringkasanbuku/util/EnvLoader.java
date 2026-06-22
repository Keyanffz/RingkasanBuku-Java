package com.ringkasanbuku.util;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvLoader {
    private static final Dotenv dotenv = Dotenv.configure()
            .directory("src/main/resources")
            .filename(".env")
            .ignoreIfMissing()
            .load();

    public static String get(String key) {
        return dotenv.get(key);
    }

    public static java.util.List<String> getKeys(String prefix) {
        java.util.List<String> keys = new java.util.ArrayList<>();
        String bareKey = get(prefix);
        if (bareKey != null && !bareKey.isBlank()) {
            keys.add(bareKey);
        }
        int i = 1;
        while (true) {
            String val = get(prefix + "_" + i);
            if (val != null && !val.isBlank()) {
                keys.add(val);
                i++;
            } else {
                break;
            }
        }
        return keys;
    }
}
