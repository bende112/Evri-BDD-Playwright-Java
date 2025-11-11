package steps;

import com.microsoft.playwright.Page;
import io.cucumber.java.en.Given;
import pages.HomePage;
import support.World;

public class CommonSteps {
    private final Page page;
    private final HomePage homePage;

    public CommonSteps(World world) {
        this.page = world.page;
        this.homePage = new HomePage(this.page);
    }

    @Given("I am on the evri.com homepage")
    public void iAmOnHome() {
        homePage.openHome(); // includes cookie dismiss in your HomePage
    }
}
