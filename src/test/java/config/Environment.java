package config;

public enum Environment {
    PROD("https://www.evri.com"),
    TEST("https://www.test.evri.com"),
    DEV("https://www.dev.evri.com");

    private final String baseUrl;
    Environment(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    public String baseUrl() {
        return baseUrl;
    }
}
