package config;

import com.microsoft.playwright.Page;

public abstract class BasePage {
    protected final Page page;
    protected final String BASE_URL;

    protected BasePage(Page page) {
        this.page = page;
        this.BASE_URL = Config.baseUrl();
    }

    public Page getPage() { return page; }
}
