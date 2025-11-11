package support;
import com.microsoft.playwright.*;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import config.Environment;

public class Hooks {
    private final World world;

    public Hooks(World world) { this.world = world; }

    public static String getBaseURL() {
        String envProp = System.getProperty("env", "PROD").toUpperCase();
        Environment env;
        try {
            env = Environment.valueOf(envProp);
        } catch (IllegalArgumentException e) {
            env = Environment.PROD; // fallback
        }
        String url = env.baseUrl();
        System.out.println("[Hooks] Using baseUrl: " + url);
        return url;
    }

    @Before
    public void setUp() {
        world.playwright = Playwright.create();

        // Detect browser name from system property
        String browserName = System.getProperty("browser", "chrome").toLowerCase();
        System.out.println("[Hooks] Starting tests on browser: " + browserName);

        BrowserType browserType;
        switch (browserName) {
            case "firefox":
                browserType = world.playwright.firefox();
                break;
            case "safari":
            case "webkit":
                browserType = world.playwright.webkit();
                break;
            case "chrome":
            case "chromium":
            default:
                browserType = world.playwright.chromium();
                break;
        }

        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(false)   // set true for CI/headless runs
                .setSlowMo(200);      // slow down interactions for visibility

        world.browser = browserType.launch(launchOptions);
        world.context = world.browser.newContext();
        world.page = world.context.newPage();
    }

    @After
    public void tearDown() {
        if (world.context != null) world.context.close();
        if (world.browser != null) world.browser.close();
        if (world.playwright != null) world.playwright.close();
    }
}







