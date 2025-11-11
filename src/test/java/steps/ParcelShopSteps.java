package steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;

import pages.HomePage;
import pages.ParcelShopFinderPage;
import support.World;

public class ParcelShopSteps {

    private final World world;
    private final HomePage homePage;
    private final ParcelShopFinderPage finder;

    public ParcelShopSteps(World world) {
        this.world = world;
        this.homePage = new HomePage(world.page);
        this.finder = new ParcelShopFinderPage(world.page);
    }

    @And("I select the 'Find a ParcelShop' link from the 'ParcelShops' menu")
    public void openFinder() {
        homePage.goToParcelShopFinder();
    }

    @When("I search for 'Edinburgh'")
    public void searchEdinburgh() {
        finder.searchFor("Edinburgh");
    }

    @Then("I see only Parcelshops and Lockers with postcodes starting with 'EH' in the list")
    public void assertEHPostcodes() {
        finder.assertAllPostcodesStartWith("EH");
    }

    @And("I select 'ParcelShops' from the filters")
    public void filterParcelShops() {
        finder.selectParcelShopsOnly();
    }

    @Then("I see only Parcelshops with postcodes starting with 'EH' in the list")
    public void assertEHPostcodesShopsOnly() {
        finder.assertAllPostcodesStartWith("EH");
    }

    @And("I select 'Packageless ParcelShops' from the filters")
    public void filterPackageless() {
        finder.selectPackageless();
    }

    @And("I select 'Print labels' from the filters")
    public void filterPrintLabels() {
        finder.selectPrintLabels();
    }

    @And("I select 'Next day drop off' from the filters")
    public void filterNextDay() {
        finder.selectNextDayDropOff();
    }

    @Then("I see only Lockers in the list")
    public void assertOnlyLockers() {
        finder.assertAllResultsAreLockers();
    }
}
