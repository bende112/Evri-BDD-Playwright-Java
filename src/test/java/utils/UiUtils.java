package utils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class UiUtils {
    public static void dismissCookies(Page page) {
        // Accept main cookie banner
        Locator accept = page.locator(
                "#onetrust-accept-btn-handler, " +
                        "button:has-text('Accept all'), " +
                        "button:has-text('Allow all'), " +
                        "[aria-label='Accept all']"
        );
        if (accept.count() > 0 && accept.first().isVisible()) accept.first().click();

        // Close the cookie “vendor/search” dialog if it pops
        Locator vendorSearch = page.locator("#vendor-search-handler, input[aria-label='Cookie list search']");
        if (vendorSearch.count() > 0 && vendorSearch.first().isVisible()) {
            Locator close = page.locator("button[aria-label='Close'], .ot-close-icon, .ot-close-button");
            if (close.count() > 0 && close.first().isVisible()) close.first().click();
            else page.keyboard().press("Escape");
        }
    }
}
