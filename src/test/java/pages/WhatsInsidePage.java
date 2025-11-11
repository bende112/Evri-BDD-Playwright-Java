package pages;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class WhatsInsidePage {
    private final Page page;

    public WhatsInsidePage(Page page) {
        this.page = page;
    }

    private Locator headingWhatsInside() {
        return page.getByRole(
                AriaRole.HEADING,
                new Page.GetByRoleOptions().setName(
                        Pattern.compile("what.?s inside your parcel", Pattern.CASE_INSENSITIVE)
                )
        );
    }

    private Locator contentsField() {
        return page
                .getByLabel(Pattern.compile("^parcel\\s*contents$|what.?s inside", Pattern.CASE_INSENSITIVE))
                .or(page.getByPlaceholder(Pattern.compile("what.?s inside", Pattern.CASE_INSENSITIVE)))
                .or(page.locator("textarea[name*='content' i], input[name*='content' i], textarea[id*='content' i], input[id*='content' i]"))
                .or(page.locator("textarea, input[type='text']")).first();
    }

    private Locator valueField() {
        return page
                .getByLabel(Pattern.compile("how (much|value)|what.?s it worth|value of parcel|worth", Pattern.CASE_INSENSITIVE))
                .or(page.getByPlaceholder(Pattern.compile("how (much|value)|worth", Pattern.CASE_INSENSITIVE)))
                .or(page.locator("input[name*='value' i], input[id*='value' i], input[name*='worth' i], input[id*='worth' i], input[type='number']"))
                .first();
    }

    private Locator continueButton() {
        return page
                .getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(
                        Pattern.compile("^continue$|^next$", Pattern.CASE_INSENSITIVE)))
                .or(page.locator("button:has-text('Continue'), [type='submit']")).first();
    }

    private Locator contentsError() {
        Locator byText = page.getByText(
                Pattern.compile("parcel\\s*contents.*prohibited|prohibited item", Pattern.CASE_INSENSITIVE));

        Locator field = contentsField();
        Locator aria = page.locator("#" + safeAttr(field, "aria-describedby"));
        return byText.or(aria);
    }

    public void waitForScreen() {
        dismissConsentIfPresent();

        try {
            headingWhatsInside()
                    .first()
                    .waitFor(new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.VISIBLE)
                            .setTimeout(8000));
            return;
        } catch (Throwable ignored) {}

        contentsField()
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(8000));
    }

    public void enterContents(String text) {
        waitForScreen();
        Locator box = contentsField();
        assertThat(box).isVisible();
        box.click();
        box.fill(text);
    }

    public void enterDeclaredValue(String value) {
        Locator val = valueField();
        assertThat(val).isVisible();
        val.click();
        val.fill(value);
    }

    public void clickContinue() {
        Locator btn = continueButton();
        assertThat(btn).isVisible();
        btn.click();
    }

    public void assertProhibitedErrorVisible() {
        assertThat(contentsError().first()).isVisible();
    }

    public void assertContinueDisabled() {
        Locator btn = continueButton();
        boolean disabled = false;
        try {
            if (btn.isDisabled()) disabled = true; // native disabled
            String aria = safeAttr(btn, "aria-disabled");
            String cls  = safeAttr(btn, "class");
            if ("true".equalsIgnoreCase(aria)) disabled = true;
            if (cls != null && cls.matches(".*\\bdisabled\\b.*")) disabled = true;
        } catch (Throwable ignored) {}

        if (!disabled) {
            assertProhibitedErrorVisible();
        }
    }

    private String safeAttr(Locator loc, String name) {
        try {
            String v = loc.getAttribute(name);
            return v == null ? "" : v;
        } catch (Throwable t) {
            return "";
        }
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
            Locator accept = page.getByRole(
                    AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName(
                            Pattern.compile("accept( all)?|agree|allow all|i accept", Pattern.CASE_INSENSITIVE)));
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
                FrameLocator fl = page.frameLocator(frameSel).nth(i);
                Locator btn = fl.getByRole(
                        AriaRole.BUTTON,
                        new FrameLocator.GetByRoleOptions().setName(
                                Pattern.compile("accept( all)?|agree|allow all|i accept", Pattern.CASE_INSENSITIVE)));
                if (btn.count() > 0 && btn.first().isVisible()) {
                    btn.first().click();
                    page.waitForTimeout(120);
                    return;
                }
            }
        } catch (Throwable ignored) {}
    }
}
