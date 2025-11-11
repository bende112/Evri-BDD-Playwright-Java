package pages;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class SendFlowPage {
    private final Page page;

    public SendFlowPage(Page page) { this.page = page; }

    public void progressToWhatsInside() {
        dismissConsentIfPresent();
        ensureHomeAndSendTab();

        fillFromPostcode("SW1A 1AA");
        fillToPostcode("LS1 4DY");
        selectWeight("Under 1kg");

        clickSendAParcel();

        waitForCourierQuote();
        chooseFirstServiceAndContinue();
        clickIfVisible(btnByRoleName("^Continue$|^Next$|^Proceed$"));
        clickIfVisible(btnByRoleName("^Continue$|^Next$|^Proceed$"));

        waitForWhatsInsideOrFail();
    }

    private Locator fromPostcode() {
        return page
                .getByLabel(Pattern.compile("^from postcode\\*?$", Pattern.CASE_INSENSITIVE))
                .or(page.getByPlaceholder(Pattern.compile("^from postcode", Pattern.CASE_INSENSITIVE)))
                .or(page.locator("input[name*='from' i][name*='post' i], input[id*='from' i][id*='post' i]"))
                .first();
    }

    private Locator toPostcode() {
        return page
                .getByLabel(Pattern.compile("^to postcode\\*?$", Pattern.CASE_INSENSITIVE))
                .or(page.getByPlaceholder(Pattern.compile("^to postcode", Pattern.CASE_INSENSITIVE)))
                .or(page.locator("input[name*='to' i][name*='post' i], input[id*='to' i][id*='post' i]"))
                .first();
    }

    private Locator weightControl() {
        return page
                .getByLabel(Pattern.compile("^weight \\(kg\\)$", Pattern.CASE_INSENSITIVE))
                .or(page.locator("select[name*='weight' i], select[id*='weight' i]"))
                .or(page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions()
                        .setName(Pattern.compile("^weight", Pattern.CASE_INSENSITIVE))))
                .first();
    }

    private Locator sendWidgetForm() {
        Locator base = toPostcode();
        if (base.count() > 0) {
            Locator form = base.locator("xpath=ancestor::form[1]");
            if (form.count() > 0) return form.first();
        }
        return page.locator("form").first();
    }

    private Locator sendAParcelCta() {
        Locator form = sendWidgetForm();
        Locator byTypeSubmit = form.locator("button[type='submit'], input[type='submit']");
        Locator byText = form.locator(":is(button,[role='button']):has-text('Send a parcel')");
        if (byTypeSubmit.count() > 0) return byTypeSubmit.first();
        if (byText.count() > 0) return byText.first();
        return page.locator(":is(button,[role='button'],input[type='submit']):has-text('Send a parcel')").first();
    }

    private void fillFromPostcode(String value) {
        Locator f = fromPostcode();
        assertThat(f).isVisible();
        safeFillAndBlur(f, value);
    }

    private void fillToPostcode(String value) {
        Locator t = toPostcode();
        assertThat(t).isVisible();
        safeFillAndBlur(t, value);
    }

    private void selectWeight(String visibleLabel) {
        Locator ctrl = weightControl();
        assertThat(ctrl).isVisible();
        ctrl.scrollIntoViewIfNeeded();

        if ("SELECT".equalsIgnoreCase(tagName(ctrl))) {
            try {
                ctrl.selectOption(new SelectOption().setLabel(visibleLabel));
            } catch (Throwable ignore) {
                try {
                    Locator options = ctrl.locator("option");
                    for (int i = 0; i < options.count(); i++) {
                        String txt = options.nth(i).innerText().trim();
                        if (!txt.equalsIgnoreCase("Please select") && !txt.isBlank()) {
                            ctrl.selectOption(new SelectOption().setLabel(txt));
                            break;
                        }
                    }
                } catch (Throwable ignored) {}
            }
            blur(ctrl);
            return;
        }

        try {
            ctrl.click();
            Locator opt = page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions()
                    .setName(Pattern.compile("^\\s*" + Pattern.quote(visibleLabel) + "\\s*$", Pattern.CASE_INSENSITIVE)));
            if (opt.count() > 0) {
                opt.first().click();
                blur(ctrl);
                return;
            }
        } catch (Throwable ignored) {}

        try {
            ctrl.click();
            page.keyboard().press("ArrowDown");
            page.keyboard().press("Enter");
            blur(ctrl);
        } catch (Throwable ignored) {}
    }

    private void clickSendAParcel() {
        Locator btn = sendAParcelCta();

        btn.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.ATTACHED).setTimeout(10000));
        btn.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE).setTimeout(10000));

        page.waitForCondition(() -> isEnabled(btn), new Page.WaitForConditionOptions().setTimeout(12000));

        btn.click(new Locator.ClickOptions().setForce(true));
    }

    private boolean isEnabled(Locator btn) {
        try {
            if (btn.isDisabled()) return false;
        } catch (Throwable ignored) {}
        try {
            String aria = attr(btn, "aria-disabled");
            if ("true".equalsIgnoreCase(aria)) return false;
        } catch (Throwable ignored) {}
        try {
            String cls = attr(btn, "class");
            if (cls != null && cls.matches(".*\\bdisabled\\b.*")) return false;
        } catch (Throwable ignored) {}
        return true;
    }

    private void waitForCourierQuote() {
        page.waitForURL(Pattern.compile("/courier-quote", Pattern.CASE_INSENSITIVE),
                new Page.WaitForURLOptions().setTimeout(20000));
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    private void chooseFirstServiceAndContinue() {
        Locator radios = page.getByRole(AriaRole.RADIO);
        for (int i = 0; i < radios.count(); i++) {
            Locator r = radios.nth(i);
            if (r.isVisible()) {
                try { r.check(new Locator.CheckOptions().setForce(true)); break; }
                catch (Throwable ignored) {}
            }
        }
        Locator choose = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
                .setName(Pattern.compile("^select$|^choose$|^continue$", Pattern.CASE_INSENSITIVE)));
        if (choose.count() > 0) clickIfVisible(choose.first());
        clickIfVisible(btnByRoleName("^Continue$|^Next$"));
    }

    private void waitForWhatsInsideOrFail() {
        Locator heading = page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName(Pattern.compile("what.?s inside your parcel", Pattern.CASE_INSENSITIVE)));
        Locator contents = page.getByLabel(Pattern.compile("parcel\\s*contents|what.?s inside", Pattern.CASE_INSENSITIVE))
                .or(page.getByPlaceholder(Pattern.compile("what.?s inside", Pattern.CASE_INSENSITIVE)))
                .or(page.locator("textarea[name*='content' i], input[name*='content' i]"));

        try {
            heading.first().waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE).setTimeout(20000));
            return;
        } catch (Throwable ignored) {}

        try {
            contents.first().waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE).setTimeout(20000));
            return;
        } catch (Throwable ignored) {}

        throw new AssertionError("Failed to reach \"What's inside your parcel\" screen. URL: " + page.url());
    }

    private void ensureHomeAndSendTab() {
        if (!page.url().matches("(?i)^https?://(www\\.)?evri\\.com/?$")) {
            page.navigate("https://www.evri.com/");
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        }
        dismissConsentIfPresent();
        Locator sendTab = page.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName(Pattern.compile("^send$", Pattern.CASE_INSENSITIVE)));
        if (sendTab.count() > 0) sendTab.first().click();
    }

    private void clickIfVisible(Locator loc) {
        try {
            if (loc != null && loc.count() > 0) {
                Locator first = loc.first();
                first.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
                first.click(new Locator.ClickOptions().setForce(true));
            }
        } catch (Throwable ignored) {}
    }

    private Locator btnByRoleName(String pattern) {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
                .setName(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)));
    }

    private void safeFillAndBlur(Locator input, String value) {
        input.scrollIntoViewIfNeeded();
        input.click(new Locator.ClickOptions().setForce(true));
        input.fill("");
        input.type(value, new Locator.TypeOptions().setDelay(10));
        blur(input);
    }

    private void blur(Locator input) {
        try { input.evaluate("el => el.blur()"); } catch (Throwable ignored) {}
        try { page.keyboard().press("Tab"); } catch (Throwable ignored) {}
    }

    private String tagName(Locator loc) {
        try { return loc.evaluate("el => el.tagName").toString(); } catch (Throwable t) { return ""; }
    }

    private String attr(Locator loc, String name) {
        try { return loc.getAttribute(name); } catch (Throwable t) { return null; }
    }

    private void dismissConsentIfPresent() {
        try {
            Locator oneTrust = page.locator("#onetrust-accept-btn-handler");
            if (oneTrust.count() > 0 && oneTrust.first().isVisible()) {
                oneTrust.first().click();
                page.waitForTimeout(120);
                return;
            }
        } catch (Throwable ignored) {}

        try {
            Locator accept = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(
                    Pattern.compile("accept( all)?|agree|allow all|i accept", Pattern.CASE_INSENSITIVE)));
            if (accept.count() > 0 && accept.first().isVisible()) {
                accept.first().click();
                page.waitForTimeout(120);
            }
        } catch (Throwable ignored) {}
    }
}
