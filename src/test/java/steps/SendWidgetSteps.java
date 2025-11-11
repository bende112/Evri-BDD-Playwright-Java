package steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pages.SendWidgetPage;
import support.World;

public class SendWidgetSteps {
    private final World world;
    private final SendWidgetPage sendWidgetPage;

    public SendWidgetSteps(World world) {
        this.world = world;
        this.sendWidgetPage = new SendWidgetPage(world.page);
    }

    @When("I select the 'Parcel size and weight' link from the Send widget")
    public void iSelectParcelSizeAndWeightFromSendWidget() {
        sendWidgetPage.clickParcelSizeAndWeightFromSendWidget();
    }

    @Then("the heading changes to 'Evri parcel size and weight guide'")
    public void headingChanges() {
        sendWidgetPage.assertOnGuidePage();
    }

    @Then("the three information panels 'Evri parcel weight limits', 'Postable parcel & large letter delivery' and 'Sending large parcels' are displayed below.")
    public void infoPanelsDisplayed() {
        sendWidgetPage.assertInfoPanelsVisible();
    }
}
