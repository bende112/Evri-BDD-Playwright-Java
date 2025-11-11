//package pages;
//
//import com.microsoft.playwright.Locator;
//import com.microsoft.playwright.Page;
//import com.microsoft.playwright.options.AriaRole;
//
//import java.util.regex.Pattern;
//
//import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
//
//public class SendWidgetPage {
//    private final Page page;
//
//    public SendWidgetPage(Page page) { this.page = page; }
//
//    // ---------- Locators (robust, regex-capable) ----------
//
//    /** H1 that should read something like "Evri parcel size and weight guide". */
//    private Locator guideHeadingRole() {
//        return page.getByRole(
//                AriaRole.HEADING,
//                new Page.GetByRoleOptions()
//                        .setName(Pattern.compile("parcel\\s+size\\s+and\\s+weight(\\s+guide)?", Pattern.CASE_INSENSITIVE))
//                        .setLevel(1)
//        );
//    }
//
//    /** Fallback to raw h1 if the ARIA role lookup is finicky. */
//    private Locator guideHeadingCss() { return page.locator("h1"); }
//
//    private Locator panelWeightLimits() {
//        return page.getByRole(
//                AriaRole.HEADING,
//                new Page.GetByRoleOptions()
//                        .setName(Pattern.compile("evri\\s+parcel\\s+weight\\s+limits", Pattern.CASE_INSENSITIVE))
//                        .setLevel(2)
//        );
//    }
//
//    private Locator panelPostable() {
//        return page.getByRole(
//                AriaRole.HEADING,
//                new Page.GetByRoleOptions()
//                        .setName(Pattern.compile("postable\\s+parcel\\s*&\\s*large\\s+letter\\s+delivery", Pattern.CASE_INSENSITIVE))
//                        .setLevel(2)
//        );
//    }
//
//    private Locator panelLargeParcels() {
//        return page.getByRole(
//                AriaRole.HEADING,
//                new Page.GetByRoleOptions()
//                        .setName(Pattern.compile("sending\\s+large\\s+parcels", Pattern.CASE_INSENSITIVE))
//                        .setLevel(2)
//        );
//    }
//
//    // ---------- Assertions ----------
//
//    /** Assert we’re on the guide page by validating the heading (role first, css fallback). */
//    public void assertOnGuidePage() {
//        // role-based first
//        if (guideHeadingRole().count() > 0) {
//            assertThat(guideHeadingRole().first()).isVisible();
//            return;
//        }
//        // css fallback: accept common variations
//        assertThat(guideHeadingCss().first()).isVisible();
//        String text = guideHeadingCss().first().innerText().toLowerCase();
//        String simplified = text.replaceAll("[^a-z& ]", " ").replaceAll("\\s+", " ").trim();
//        boolean ok =
//                simplified.contains("parcel size and weight guide")
//                        || simplified.contains("parcel size and weight")
//                        || simplified.contains("size and weight guide");
//        if (!ok) {
//            throw new AssertionError("Unexpected H1 text: " + text);
//        }
//    }
//
//    /** Assert the three info panels are present and visible (scroll to reveal if lazy loaded). */
//    public void assertInfoPanelsVisible() {
//        // scroll to avoid lazy-loading surprises
//        panelWeightLimits().scrollIntoViewIfNeeded();
//        panelPostable().scrollIntoViewIfNeeded();
//        panelLargeParcels().scrollIntoViewIfNeeded();
//
//        assertThat(panelWeightLimits().first()).isVisible();
//        assertThat(panelPostable().first()).isVisible();
//        assertThat(panelLargeParcels().first()).isVisible();
//    }
//}
//package pages;
//
//import com.microsoft.playwright.Locator;
//import com.microsoft.playwright.Page;
//import com.microsoft.playwright.options.AriaRole;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.regex.Pattern;
//
//import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
//
//public class SendWidgetPage {
//    private final Page page;
//
//    public SendWidgetPage(Page page) { this.page = page; }
//
//    // ---------- guide detection ----------
//
//    public boolean looksLikeGuide() {
//        String url = page.url().toLowerCase();
//        if (url.contains("size-and-weight")) return true;
//
//        Locator h1 = page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setLevel(1))
//                .or(page.locator("h1"));
//        if (h1.count() > 0 && h1.first().isVisible()) {
//            String t = simplify(safeText(h1.first()));
//            return t.contains("size and weight");
//        }
//        return false;
//    }
//
//    public void assertOnGuidePage() {
//        if (looksLikeGuide()) return;
//        String h1 = "(none)";
//        try {
//            Locator h1Loc = page.locator("h1").first();
//            if (h1Loc.count() > 0 && h1Loc.isVisible()) h1 = h1Loc.innerText();
//        } catch (Throwable ignored) {}
//        throw new AssertionError("Unexpected page for guide.\nURL: " + page.url() + "\nH1: " + h1);
//    }
//
//    // ---------- patterns we want to see ----------
//
//    private static final List<Pattern> WEIGHT_LIMITS = Arrays.asList(
//            Pattern.compile("\\bevri\\b.*\\bweight\\b.*\\blimits?\\b", Pattern.CASE_INSENSITIVE),
//            Pattern.compile("\\bweight\\b.*\\blimits?\\b", Pattern.CASE_INSENSITIVE),
//            Pattern.compile("\\bmaximum\\b.*\\bweight\\b", Pattern.CASE_INSENSITIVE)
//    );
//
//    private static final List<Pattern> POSTABLE_LARGE_LETTER = Arrays.asList(
//            Pattern.compile("\\bpostable\\b.*\\bparcel\\b", Pattern.CASE_INSENSITIVE),
//            Pattern.compile("\\blarge\\b.*\\bletter\\b", Pattern.CASE_INSENSITIVE),
//            Pattern.compile("\\bpostable\\b", Pattern.CASE_INSENSITIVE)
//    );
//
//    private static final List<Pattern> SENDING_LARGE_PARCELS = Arrays.asList(
//            Pattern.compile("\\bsending\\b.*\\blarge\\b.*\\bparcels?\\b", Pattern.CASE_INSENSITIVE),
//            Pattern.compile("\\blarge\\b.*\\bparcels?\\b", Pattern.CASE_INSENSITIVE),
//            Pattern.compile("\\boversize|bulky\\b", Pattern.CASE_INSENSITIVE)
//    );
//
//    // ---------- expand / reveal helpers ----------
//
//    /** Try to expand accordions, summaries, generic “Expand/Show/More” buttons, etc. */
//    private void expandAllSectionsBestEffort() {
//        // <details><summary>...</summary>...</details>
//        Locator summaries = page.locator("details > summary");
//        int sCount = Math.min(summaries.count(), 15);
//        for (int i = 0; i < sCount; i++) {
//            Locator s = summaries.nth(i);
//            if (safeVisible(s)) {
//                try { s.click(); } catch (Throwable ignored) {}
//            }
//        }
//
//        // Buttons that sound like “expand/show/open/read more”
//        Pattern expandNames = Pattern.compile("expand|show|open|read\\s*more|view\\s*more|see\\s*more",
//                Pattern.CASE_INSENSITIVE);
//
//        Locator buttons = page.getByRole(AriaRole.BUTTON,
//                new Page.GetByRoleOptions().setName(expandNames));
//        int bCount = Math.min(buttons.count(), 15);
//        for (int i = 0; i < bCount; i++) {
//            Locator b = buttons.nth(i);
//            if (!safeVisible(b)) continue;
//            // Skip cookie/consent buttons accidentally matched
//            String txt = simplify(safeText(b));
//            if (txt.contains("accept") || txt.contains("agree")) continue;
//            try { b.click(); } catch (Throwable ignored) {}
//        }
//
//        // Elements with common accordion attributes
//        Locator accordionTriggers = page.locator(
//                "[data-accordion-trigger], [aria-expanded='false'][aria-controls], [data-component*='accordion' i] button"
//        );
//        int aCount = Math.min(accordionTriggers.count(), 15);
//        for (int i = 0; i < aCount; i++) {
//            Locator a = accordionTriggers.nth(i);
//            if (!safeVisible(a)) continue;
//            try { a.click(); } catch (Throwable ignored) {}
//        }
//
//        // Give the DOM a tick to render lazy content
//        page.waitForTimeout(200);
//    }
//
//    // ---------- tolerant matching across the page ----------
//
//    private boolean pageContainsAny(List<Pattern> patterns) {
//        // Prefer role=headings first (h2/h3/any heading)
//        Locator h2 = page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setLevel(2));
//        if (locatorHasAnyMatch(h2, patterns)) return true;
//
//        Locator h3 = page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setLevel(3));
//        if (locatorHasAnyMatch(h3, patterns)) return true;
//
//        Locator anyHeading = page.getByRole(AriaRole.HEADING);
//        if (locatorHasAnyMatch(anyHeading, patterns)) return true;
//
//        // Then, any visible text node (cards, list items, accordion content)
//        for (Pattern p : patterns) {
//            Locator t = page.getByText(p);
//            int n = Math.min(t.count(), 30);
//            for (int i = 0; i < n; i++) {
//                if (safeVisible(t.nth(i))) return true;
//            }
//        }
//        return false;
//    }
//
//    private boolean locatorHasAnyMatch(Locator loc, List<Pattern> patterns) {
//        int n = Math.min(loc.count(), 40);
//        for (int i = 0; i < n; i++) {
//            Locator el = loc.nth(i);
//            if (!safeVisible(el)) continue;
//            String txt = simplify(safeText(el));
//            for (Pattern p : patterns) {
//                if (p.matcher(txt).find()) return true;
//            }
//        }
//        return false;
//    }
//
//    // ---------- public assertions ----------
//
//    public void assertInfoPanelsVisible() {
//        assertThat(page.locator("body")).isVisible();
//
//        // best effort to reveal content
//        expandAllSectionsBestEffort();
//
//        boolean weight = pageContainsAny(WEIGHT_LIMITS);
//        boolean postable = pageContainsAny(POSTABLE_LARGE_LETTER);
//        boolean large = pageContainsAny(SENDING_LARGE_PARCELS);
//
//        if (weight && postable && large) return;
//
//        // As a last resort, scroll the whole page in chunks to trigger lazy loads, then try again
//        for (int y = 200; y <= 2000 && !(weight && postable && large); y += 200) {
//            try { page.mouse().wheel(0, y); } catch (Throwable ignored) {}
//            page.waitForTimeout(120);
//            if (!weight)   weight   = pageContainsAny(WEIGHT_LIMITS);
//            if (!postable) postable = pageContainsAny(POSTABLE_LARGE_LETTER);
//            if (!large)    large    = pageContainsAny(SENDING_LARGE_PARCELS);
//        }
//
//        if (weight && postable && large) return;
//
//        StringBuilder sb = new StringBuilder("Expected three info panels, but missing:");
//        if (!weight) sb.append(" [weight limits]");
//        if (!postable) sb.append(" [postable / large letter]");
//        if (!large) sb.append(" [sending large parcels]");
//        sb.append("\nURL: ").append(page.url());
//        throw new AssertionError(sb.toString());
//    }
//
//    // ---------- utils ----------
//    private static boolean safeVisible(Locator l) {
//        try { return l.isVisible(); } catch (Throwable t) { return false; }
//    }
//    private static String safeText(Locator l) {
//        try { return l.innerText(); } catch (Throwable t) { return ""; }
//    }
//    private static String simplify(String s) {
//        if (s == null) return "";
//        return s.toLowerCase()
//                .replaceAll("[^a-z0-9& ]", " ")
//                .replaceAll("\\s+", " ")
//                .trim();
//    }
//}


package pages;

import java.util.regex.Pattern;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Represents the "Send widget" and the "Parcel size and weight guide" page.
 * Clean, flat structure — all locators and helpers are grouped clearly.
 */
public class SendWidgetPage {
    private final Page page;

    // --- Locators ---
    private final Locator sendWidgetLink;
    private final Locator headingOnGuidePage;
    private final Locator panelWeightLimits;
    private final Locator panelPostableLargeLetter;
    private final Locator panelSendingLargeParcels;
    private final Locator acceptCookiesButton;

    // --- Constructor ---
    public SendWidgetPage(Page page) {
        this.page = page;

        this.sendWidgetLink = page.getByRole(
                AriaRole.LINK,
                new Page.GetByRoleOptions().setName(
                        "Parcel size and weight"
                ).setExact(false)
        );

        this.headingOnGuidePage = page.getByRole(
                AriaRole.HEADING,
                new Page.GetByRoleOptions()
                        .setName("Evri parcel size and weight guide")
                        .setLevel(1)
        );

        this.panelWeightLimits = page.getByRole(
                AriaRole.HEADING,
                new Page.GetByRoleOptions()
                        .setName("Evri parcel weight limits")
                        .setLevel(2)
        );

        this.panelPostableLargeLetter = page.getByRole(
                AriaRole.HEADING,
                new Page.GetByRoleOptions()
                        .setName("Postable parcel & large letter delivery")
                        .setLevel(2)
        );

        this.panelSendingLargeParcels = page.getByRole(
                AriaRole.HEADING,
                new Page.GetByRoleOptions()
                        .setName("Sending large parcels")
                        .setLevel(2)
        );

        this.acceptCookiesButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Accept All Cookies")
        ).or(page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Accept all"))
        );
    }


    public void navigateTo(String url) {
        page.navigate(url);
        acceptCookiesIfPresent();
    }

    public void clickParcelSizeAndWeight() {
        acceptCookiesIfPresent();
        assertThat(sendWidgetLink).isVisible();
        sendWidgetLink.first().click();
    }

    public void clickParcelSizeAndWeightFromSendWidget() {
        Locator container = sendWidgetContainer();

        Locator linkInWidget =
                container.getByLabel("Parcel size and weight guide",
                                new Locator.GetByLabelOptions().setExact(true))
                        .or(container.getByRole(AriaRole.LINK,
                                new Locator.GetByRoleOptions().setName(
                                        Pattern.compile("^\\s*Parcel size and weight( guide)?\\s*$", CASE_INSENSITIVE))));

        Locator link = linkInWidget;

        if (link.count() == 0) {
            link = page.locator("a[href*='/parcel-size-and-weight-guide']")
                    .filter(new Locator.FilterOptions().setHasNot(page.locator("footer")));
        }

        link.first().click();
    }


    /** Waits for and returns the H1 guide heading text. */
    public String getGuideHeading() {
        assertThat(headingOnGuidePage).isVisible();
        return headingOnGuidePage.first().innerText().trim();
    }

    public void assertInfoPanelsVisible() {
        panelWeightLimits.scrollIntoViewIfNeeded();
        panelPostableLargeLetter.scrollIntoViewIfNeeded();
        panelSendingLargeParcels.scrollIntoViewIfNeeded();

        assertThat(panelWeightLimits).isVisible();
        assertThat(panelPostableLargeLetter).isVisible();
        assertThat(panelSendingLargeParcels).isVisible();
    }

    public void assertOnGuidePage() {
        try {
            Locator heading = page.getByRole(
                    AriaRole.HEADING,
                    new Page.GetByRoleOptions().setLevel(1)
            );

            heading.first().waitFor(new Locator.WaitForOptions().setTimeout(10_000));

            String h1 = heading.first().innerText().trim().toLowerCase();
            String url = page.url().toLowerCase();

            boolean looksRight =
                    h1.contains("size and weight")
                            || url.contains("size-and-weight")
                            || url.contains("/parcel-size-and-weight-guide");

            if (!looksRight) {
                throw new AssertionError("Unexpected H1 text: " + h1 + "\nURL: " + url);
            }
        } catch (Throwable t) {
            throw new AssertionError("Failed to verify guide page: " + t.getMessage(), t);
        }
    }

    private Locator sendWidgetContainer() {
        return page.locator("[data-test-id='send-widget'], [data-testid='send-widget']")
                .or(page.locator("section:has(:text('Send a parcel'))"))
                .first();
    }

    private void acceptCookiesIfPresent() {
        try {
            if (acceptCookiesButton.count() > 0 && acceptCookiesButton.first().isVisible()) {
                acceptCookiesButton.first().click();
                page.waitForTimeout(150);
            }
        } catch (Throwable ignored) {
        }
    }
}
