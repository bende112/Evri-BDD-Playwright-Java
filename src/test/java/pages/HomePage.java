 /// second working file
package pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;

import config.BasePage;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;


public class HomePage extends BasePage {
    public Locator clickSizeAndWeightGuide;
    public Locator openParcelShopFinder;
    public Locator sendMenuLinkHeader;
    public Locator sendMenuLinkGlobal;

    private final Locator header;
    private final Locator burgerMenuButton;

    public Locator header() { return header; }

    public HomePage(BasePage basePage) {
        super(basePage.getPage());
        this.header = page.locator("header").first();
        this.burgerMenuButton = page.locator("[data-test-id='mobile-menu-open-toggle']");
        initLocators();
    }

    public HomePage(Page page) {
        super(page);
        this.header = page.locator("header").first();
        this.burgerMenuButton = page.locator("[data-test-id='mobile-menu-open-toggle']");
        initLocators();
    }

    private void initLocators() {
        sendMenuLinkHeader = header.locator("[data-test-id='link-group-send']")
                .or(header.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Send").setExact(true)));
        sendMenuLinkGlobal = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Send").setExact(true));

        clickSizeAndWeightGuide = header
                .locator("[data-test-id='secondary-sublinks-0-size-and-weight-guide']")
                .or(page.locator("a[href*='/parcel-size-and-weight-guide']"));

        openParcelShopFinder = header
                .locator("[data-test-id*='find-a-parcelshop']")
                .or(page.locator("a[href*='/find-a-parcelshop']"));
    }


    public void openHome() {
        page.navigate(BASE_URL);
        page.waitForLoadState();
        dismissCookies();
    }

    public void goToHome() {
        openHome();
    }

    public void clickParcelSizeAndWeightLink() {
        ensureMainNavOpen();
        dismissCookies();

        //  Hover “Send” to reveal sub links
        hoverIfExists(sendMenuLinkHeader);
        page.waitForTimeout(150);

        Locator candidates =
                clickSizeAndWeightGuide
                        .or(page.getByRole(
                                AriaRole.LINK,
                                new Page.GetByRoleOptions().setName(
                                        Pattern.compile("parcel\\s*size.*weight", Pattern.CASE_INSENSITIVE))))
                        .or(page.locator("a[href*='size'][href*='weight']"));

        int total = candidates.count();
        for (int i = 0; i < total; i++) {
            Locator link = candidates.nth(i);
            if (!link.isVisible()) continue;

            String before = page.url();
            link.click();
            page.waitForLoadState();
            page.waitForTimeout(250);

            if (ensureOnSizeAndWeightPage(6000)) return;

            String after = page.url();
            boolean went404 = after.contains("404");
            if (went404 || after.equals(before)) {
                page.goBack(new Page.GoBackOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

                page.waitForTimeout(200);
            } else {
                page.goBack(new Page.GoBackOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));


                page.waitForTimeout(200);
            }
        }

        throw new AssertionError("Could not reach 'Parcel size and weight guide' by clicking in-page links. URL: " + page.url());
    }

    public void goToSizeAndWeightGuide() {
        if (ensureOnSizeAndWeightPage(1000)) return;

        clickParcelSizeAndWeightLink();

        if (!ensureOnSizeAndWeightPage(4000)) {
            throw new AssertionError("Failed to confirm 'Parcel size and weight guide' after navigation. URL: " + page.url());
        }
    }

    public void goToParcelShopFinder() {
        ensureMainNavOpen();

        Locator anyFinderLink = page.getByRole(
                AriaRole.LINK,
                new Page.GetByRoleOptions().setName(Pattern.compile("Find a ParcelShop", Pattern.CASE_INSENSITIVE))
        ).or(page.locator("a[href*='/find-a-parcelshop']"));

        if (clickIfVisible(anyFinderLink) && ensureOnFinderPage(4000)) return;

        Locator dropdownRoots = header.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Drop off or pick up").setExact(true))
                .or(header.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("ParcelShops").setExact(true)))
                .or(sendMenuLinkHeader)
                .or(sendMenuLinkGlobal);

        hoverIfExists(dropdownRoots);
        if (clickIfVisible(openParcelShopFinder) && ensureOnFinderPage(4000)) return;

        List<String> candidates = Arrays.asList(
                "/find-a-parcelshop",
                "/parcel-shops/find-a-parcelshop",
                "/parcel-shops"
        );
        for (String path : candidates) {
            String url = fixPath(BASE_URL, path);
            System.out.println("[Finder fallback] navigating to: " + url);
            page.navigate(url);
            page.waitForLoadState();
            if (ensureOnFinderPage(4000)) return;
        }

        throw new AssertionError("Could not reach ParcelShop Finder. Current URL: " + page.url());
    }


    private String fixPath(String base, String path) {
        if (base.endsWith("/") && path.startsWith("/")) return base + path.substring(1);
        if (!base.endsWith("/") && !path.startsWith("/")) return base + "/" + path;
        return base + path;
    }

    private void ensureMainNavOpen() {
        try {
            if (burgerMenuButton.count() > 0 && burgerMenuButton.first().isVisible()) {
                burgerMenuButton.first().click();
                page.waitForTimeout(200);
            }
        } catch (PlaywrightException ignored) {}
    }

    private void hoverIfExists(Locator loc) {
        try {
            if (loc != null && loc.count() > 0 && loc.first().isVisible()) {
                loc.first().hover(new Locator.HoverOptions().setForce(true));
                page.waitForTimeout(200);
            }
        } catch (PlaywrightException ignored) {}
    }

    private boolean clickIfVisible(Locator loc) {
        try {
            if (loc == null || loc.count() == 0) return false;
            Locator first = loc.first();
            try {
                first.waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(1500));
            } catch (PlaywrightException ignored) {}
            if (first.isVisible()) {
                first.click();
                return true;
            }
        } catch (PlaywrightException ignored) {}
        return false;
    }

    /**
     * “Size & weight guide” page detector: prefers H1 content, falls back to URL.
     */
    private boolean ensureOnSizeAndWeightPage(int timeoutMs) {
        long end = System.currentTimeMillis() + timeoutMs;

        while (System.currentTimeMillis() < end) {
            if (looksLike404()) return false;

            Locator h1 = page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setLevel(1));
            if (h1.count() > 0 && h1.first().isVisible()) {
                String text = h1.first().innerText().trim();
                if (text.matches("(?i).*size.*weight.*guide.*") || text.matches("(?i).*parcel size.*weight.*")) {
                    return true;
                }
            }

            String u = page.url().toLowerCase();
            if (u.contains("size") && u.contains("weight") && !u.contains("404")) {
                return true;
            }

            page.waitForTimeout(150);
        }
        return false;
    }

    private boolean ensureOnFinderPage(int timeoutMs) {
        long end = System.currentTimeMillis() + timeoutMs;

        while (System.currentTimeMillis() < end) {
            if (looksLike404()) return false;

            Locator textbox = page.getByRole(AriaRole.TEXTBOX)
                    .or(page.locator("input[placeholder*='postcode' i], input[placeholder*='city' i]"));
            if (textbox.count() > 0 && textbox.first().isVisible()) return true;

            Locator results = page.locator("[data-testid='location-card'], [role='listitem'], article, [data-testid='map']");
            if (results.count() > 0 && results.first().isVisible()) return true;

            if (page.url().contains("find-a-parcelshop") || page.url().contains("parcel-shops")) {
                page.waitForTimeout(200);
            } else {
                break;
            }
        }
        return false;
    }

    private boolean looksLike404() {
        try {
            if (page.title() != null && page.title().matches("(?i).*404.*|.*not found.*")) return true;
            Locator notFound = page.getByText(Pattern.compile("404|page not found|we can't find", Pattern.CASE_INSENSITIVE));
            return notFound.count() > 0 && notFound.first().isVisible();
        } catch (Throwable ignored) { return false; }
    }

    /** Best-effort cookie/CMP dismissor */
    private void dismissCookies() {
        // OneTrust
        try {
            Locator oneTrust = page.locator("#onetrust-accept-btn-handler");
            if (oneTrust.count() > 0 && oneTrust.first().isVisible()) {
                oneTrust.first().click();
                page.waitForTimeout(150);
                return;
            }
        } catch (PlaywrightException ignored) {}

        // Simple “Accept/Agree” on-page
        try {
            Locator acceptOnPage = page.getByRole(
                    AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName(
                            Pattern.compile("accept( all)?|agree|i accept|allow all", Pattern.CASE_INSENSITIVE)
                    )
            );
            if (acceptOnPage.count() > 0 && acceptOnPage.first().isVisible()) {
                acceptOnPage.first().click();
                page.waitForTimeout(150);
                return;
            }
        } catch (PlaywrightException ignored) {}

        String frameSel = "iframe[id*='sp_'], iframe[id*='sp-message'], iframe[title*='privacy' i], iframe[src*='consent' i]";
        try {
            int n = page.locator(frameSel).count();
            for (int i = 0; i < n; i++) {
                try {
                    FrameLocator fl = page.frameLocator(frameSel).nth(i);
                    Locator btn = fl.getByRole(
                            AriaRole.BUTTON,
                            new FrameLocator.GetByRoleOptions().setName(
                                    Pattern.compile("accept( all)?|agree|i accept|allow all", Pattern.CASE_INSENSITIVE)
                            )
                    );
                    if (btn.count() > 0 && btn.first().isVisible()) {
                        btn.first().click();
                        page.waitForTimeout(150);
                        return;
                    }
                } catch (PlaywrightException ignoredInner) {}
            }
        } catch (PlaywrightException ignoredOuter) {}
    }
}
