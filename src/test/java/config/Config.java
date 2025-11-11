package config;

import java.util.Locale;

public final class Config {
    private Config() {}

    private static Environment current = null;

    public static synchronized Environment env() {
        if (current != null) return current;

        // Priority 1: system property -Denv=TEST/DEV/PROD
        String fromSysProp = System.getProperty("env");
        if (fromSysProp != null && !fromSysProp.isBlank()) {
            current = parse(fromSysProp);
            return current;
        }

        // Default: PROD
        current = Environment.PROD;
        return current;
    }

    public static synchronized void setEnv(Environment env) {
        current = env; // allows Hooks to override from scenario tag
    }

    public static String baseUrl() {
        return trimTrailingSlash(env().baseUrl());
    }

    private static Environment parse(String raw) {
        try {
            return Environment.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            System.out.println("[Config] Unknown env '" + raw + "'; falling back to PROD");
            return Environment.PROD;
        }
    }

    private static String trimTrailingSlash(String s) {
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }
}
