package pages;

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.Assertions;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class ParcelShopFinderPage {
    private final Page page;

    public ParcelShopFinderPage(Page page) { this.page = page; }

    /** The real finder search input (excludes cookie vendor search input). */
    private Locator searchInput() {
        String css =
                "input[type='text']:not(#vendor-search-handler)[data-test-id='psf-input'], " +
                        "input[type='text']:not(#vendor-search-handler)[placeholder*='Postcode' i], " +
                        "input[type='text']:not(#vendor-search-handler)[placeholder*='City' i], " +
                        "input[type='text']:not(#vendor-search-handler)[placeholder*='County' i], " +
                        "input[type='search']:not(#vendor-search-handler)[placeholder*='Postcode' i], " +
                        "input[type='search']:not(#vendor-search-handler)[placeholder*='City' i], " +
                        "input[type='search']:not(#vendor-search-handler)[placeholder*='County' i]";
        return page.locator(css).first();
    }

    /** Any visible results signal: list cards, list items, map markers, etc. */
    private Locator finderResultsAny() {
        return page.locator(
                // Typical card/test-ids used by the finder
                "[data-test-id^='psf-'][data-test-id*='-info-'], " +   // info cards
                        "[data-testid='location-card'], " +                    // generic card
                        "[data-test*='location-card'], " +
                        "[role='listitem'], article[data-test-id*='psf-'], " +
                        // Containers that imply results are present
                        "[data-testid='psf-list-results'] li, " +
                        // Map markers
                        "[data-test-id^='map-marker-']"
        );
    }

    /** Cards / list items only (used by assertions). */
    private Locator resultCards() {
        return page.locator(
                "[data-test-id^='psf-'][data-test-id*='-info-'], " +
                        "[data-testid='location-card'], " +
                        "[data-test*='location-card'], " +
                        "[role='listitem'], " +
                        "article[data-test-id*='psf-']"
        );
    }

    /** Obvious Search/Find/Go control if present. */
    private Locator searchButton() {
        Locator byRole =
                page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions()
                                .setName(Pattern.compile("^search$|^find$|^go$", Pattern.CASE_INSENSITIVE)));
        Locator byTestId = page.locator("[data-test-id='psf-search-button'], button[data-test-id*='search']");
        return byRole.or(byTestId);
    }

    private Locator suggestionsListbox() {
        return page.getByRole(AriaRole.LISTBOX)
                .or(page.locator("[role='listbox'], ul[role='listbox']"));
    }

    private Locator firstSuggestion() {
        return suggestionsListbox().getByRole(AriaRole.OPTION).first()
                .or(page.locator("[role='option']").first());
    }

    private Locator loadingSpinner() {
        return page.locator("[aria-busy='true'], [data-testid*='loading'], [class*='loading']");
    }

    public void searchFor(String query) {
        dismissCookiesBestEffort();

        assertThat(searchInput()).isVisible();

        searchInput().click();
        searchInput().fill("");
        searchInput().type(query);

        boolean suggestionPicked = pickSuggestionIfPresent(1200);
        if (!suggestionPicked) {
            if (searchButton().count() > 0 && searchButton().first().isVisible()) {
                searchButton().first().click();
            } else {
                searchInput().press("Enter");
            }
        }

        waitForResultsUi(12_000);
    }

    private boolean pickSuggestionIfPresent(int timeoutMs) {
        try {
            suggestionsListbox().first().waitFor(
                    new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(timeoutMs)
            );
            if (firstSuggestion().count() > 0 && firstSuggestion().first().isVisible()) {
                firstSuggestion().first().click();
                return true;
            }
        } catch (Throwable ignored) { /* no suggestions – fine */ }
        return false;
    }

    private void waitForResultsUi(int timeoutMs) {
        long end = System.currentTimeMillis() + timeoutMs;

        while (System.currentTimeMillis() < end) {
            try {
                if (loadingSpinner().count() > 0 && loadingSpinner().first().isVisible()) {
                    page.waitForTimeout(150);
                }
            } catch (PlaywrightException ignored) {}

            try {
                if (finderResultsAny().count() > 0 && finderResultsAny().first().isVisible()) return;
            } catch (PlaywrightException ignored) {}

            page.waitForTimeout(150);
        }

        try {
            resultCards().first().waitFor(
                    new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(2000)
            );
            return;
        } catch (Throwable ignored) {}

        Assertions.fail("No results UI became visible after search (waited ~" + (timeoutMs / 1000) + "s).");
    }

    private void enableFilter(Locator checkbox) {
        assertThat(checkbox).isVisible();
        if (!checkbox.isChecked()) checkbox.check();
        assertThat(resultCards().first()).isVisible();
    }

    public void selectParcelShopsOnly() {
        enableFilter(page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setName("ParcelShops")));
    }
    public void selectPackageless() {
        enableFilter(page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setName("Packageless ParcelShops")));
    }
    public void selectLockers() {
        enableFilter(page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setName("Lockers")));
    }
    public void selectPrintLabels() {
        enableFilter(page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setName("Print labels")));
    }
    public void selectNextDayDropOff() {
        enableFilter(page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setName("Next day drop off")));
    }

    private List<String> visiblePostcodePrefixes() {
        Pattern outward = Pattern.compile("\\b([A-Z]{1,2})\\d", Pattern.CASE_INSENSITIVE);
        int count = Math.min(resultCards().count(), 20); // cap to keep it snappy
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> safeInnerText(resultCards().nth(i)))
                .map(text -> {
                    Matcher m = outward.matcher(text);
                    return m.find() ? m.group(1).toUpperCase() : "";
                })
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private String safeInnerText(Locator loc) {
        try { return loc.innerText(); } catch (Throwable t) { return ""; }
    }

    public void assertAllPostcodesStartWith(String expectedPrefix) {
        List<String> prefixes = visiblePostcodePrefixes();
        Assertions.assertFalse(prefixes.isEmpty(), "No results returned to validate.");
        for (String pc : prefixes) {
            Assertions.assertTrue(
                    pc.equalsIgnoreCase(expectedPrefix),
                    "Postcode prefix '" + pc + "' did not match expected '" + expectedPrefix + "'"
            );
        }
    }

    public void assertAllResultsAreLockers() {
        int count = resultCards().count();
        for (int i = 0; i < count; i++) {
            Assertions.assertTrue(
                    resultCards().nth(i)
                            .getByText("Locker", new Locator.GetByTextOptions().setExact(true))
                            .first().isVisible(),
                    "Result card #" + (i + 1) + " is not marked as Locker."
            );
        }
    }

    private void dismissCookiesBestEffort() {
        try {
            Locator oneTrust = page.locator("#onetrust-accept-btn-handler");
            if (oneTrust.count() > 0 && oneTrust.first().isVisible()) {
                oneTrust.first().click();
                page.waitForTimeout(120);
                return;
            }
        } catch (Throwable ignored) {}

        try {
            Locator accept = page.getByRole(
                    AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName(
                            Pattern.compile("accept( all)?|agree|allow all|i accept", Pattern.CASE_INSENSITIVE)
                    )
            );
            if (accept.count() > 0 && accept.first().isVisible()) {
                accept.first().click();
                page.waitForTimeout(120);
                return;
            }
        } catch (Throwable ignored) {}

        try {
            String frameSel = "iframe[id*='sp_'], iframe[id*='sp-message'], iframe[title*='privacy' i], iframe[src*='consent' i]";
            int n = page.locator(frameSel).count();
            for (int i = 0; i < n; i++) {
                try {
                    FrameLocator fl = page.frameLocator(frameSel).nth(i);
                    Locator btn = fl.getByRole(
                            AriaRole.BUTTON,
                            new FrameLocator.GetByRoleOptions().setName(
                                    Pattern.compile("accept( all)?|agree|allow all|i accept", Pattern.CASE_INSENSITIVE)
                            )
                    );
                    if (btn.count() > 0 && btn.first().isVisible()) {
                        btn.first().click();
                        page.waitForTimeout(120);
                        return;
                    }
                } catch (Throwable ignoredInner) {}
            }
        } catch (Throwable ignoredOuter) {}
    }
}


