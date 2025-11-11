package steps;

import com.microsoft.playwright.Page;
import io.cucumber.java.en.*;
import pages.SendFlowPage;
import pages.WhatsInsidePage;
import support.World;

public class ProhibitedItemsSteps {
    private final World world;
    private final Page page;
    private final WhatsInsidePage whatsInside;

    public ProhibitedItemsSteps(World world) {
        this.world = world;
        this.page = world.page;
        this.whatsInside = new WhatsInsidePage(page);
    }

    @And("I fill in some send parcel details and progress to the \"What's inside your parcel\" screen")
    public void progressToWhatsInside() {
        new SendFlowPage(world.page).progressToWhatsInside();
        whatsInside.waitForScreen();
    }

    @When("I enter 'gun' into the 'Parcel contents' field")
    public void enterGun() {
        whatsInside.enterContents("gun");
    }

    @And("I enter '100' into the 'How much is it worth' field")
    public void enterValue() {
        whatsInside.enterDeclaredValue("100");
    }

    @Then("I see an error message is displayed under the 'Parcel contents' field with text 'Parcel contents are prohibited")
    public void assertProhibitedError() {
        whatsInside.assertProhibitedErrorVisible();
    }

    @And("the 'Continue' button is disabled")
    public void assertContinueDisabled() {
        whatsInside.assertContinueDisabled();
    }
}
