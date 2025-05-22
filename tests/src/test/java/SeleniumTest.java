//SeleniumTestPassword8!
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SeleniumTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @Before
    public void setup() throws MalformedURLException {
        ChromeOptions options = new ChromeOptions();
        driver = new RemoteWebDriver(new URL("http://selenium:4444/wd/hub"), options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, 10);
    }

    private WebElement waitAndFind(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private void doLogin() {
        System.out.println("Navigating to the login page...");
        driver.get("https://www.transfermarkt.us/profil/login");

        // Accept cookies if the popup is shown
        try {
            System.out.println("Checking for cookie consent popup...");
            WebElement cookieButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("onetrust-accept-btn-handler")));
            cookieButton.click();
            System.out.println("Cookie consent accepted.");
        } catch (TimeoutException e) {
            System.out.println("No cookie consent popup found.");
        }

        try {
            // Enter username
            System.out.println("Locating username field...");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("LoginForm[username]"))).sendKeys("Kristof");
            System.out.println("Username entered.");

            // Enter password
            System.out.println("Locating password field...");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("LoginForm[password]"))).sendKeys("SeleniumTestPassword8!");
            System.out.println("Password entered.");

            // Check and handle iframe if blocking
            try {
                wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.cssSelector("iframe[src*='privacy-mgmt']")));
                System.out.println("Iframe detected, switching to iframe...");
                WebElement consentButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[title='Accept']")));
                consentButton.click();
                System.out.println("Iframe consent accepted.");
                driver.switchTo().defaultContent();
            } catch (TimeoutException iframeException) {
                System.out.println("No blocking iframe found.");
                driver.switchTo().defaultContent();
            }

            // Use JavaScript to click the login button if standard click fails
            System.out.println("Locating login button...");
            WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='submit']")));
            try {
                loginBtn.click();
                System.out.println("Login button clicked.");
            } catch (Exception clickException) {
                System.out.println("Standard click failed, attempting JavaScript click...");
                ((RemoteWebDriver) driver).executeScript("arguments[0].click();", loginBtn);
                System.out.println("Login button clicked via JavaScript.");
            }

            System.out.println("Login successful.");

        } catch (Exception e) {
            System.out.println("An error occurred during the login process: " + e.getMessage());
            throw e;
        }
    }



    @Test
    public void testLogin() {
        doLogin();
        try {
            // Locate the "My profile" button using XPath
            WebElement profileButton = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[@title='My profile']"))
            );
            assertTrue("Login failed: 'My profile' button not visible.", profileButton.isDisplayed());
            System.out.println("Login successful: 'My profile' button is visible.");
        } catch (TimeoutException e) {
            System.out.println("Login failed: 'My profile' button not found.");
            throw e;
        }
    }


    @Test
    public void testSearchPlayer() {
        doLogin();
        String playerName = "Iker Casillas";
        waitAndFind(By.name("query")).sendKeys(playerName + Keys.ENTER);
        By playerLink = By.xpath("//a[@title='" + playerName + "']");
        WebElement link = wait.until(ExpectedConditions.visibilityOfElementLocated(playerLink));
        assertTrue("Player link not found on search results page.", link.isDisplayed());
    }

    @Test
    public void testLogout() throws Exception {
        doLogin();

        try {
            // Explicit wait for stability
            Thread.sleep(2000);

            // Handle obstructing iframe explicitly by checking and dismissing
            List<WebElement> iframes = driver.findElements(By.tagName("iframe"));
            for (WebElement iframe : iframes) {
                try {
                    driver.switchTo().frame(iframe);
                    List<WebElement> consentButtons = driver.findElements(By.xpath("//button[contains(text(),'Accept')]"));
                    if (!consentButtons.isEmpty()) {
                        consentButtons.get(0).click();
                        System.out.println("Iframe consent dismissed.");
                    }
                    driver.switchTo().defaultContent();
                } catch (Exception e) {
                    driver.switchTo().defaultContent();
                }
            }

            // Click "My Profile" using XPath for reliability
            WebElement profileButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@title='My profile']")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", profileButton);
            System.out.println("'My Profile' clicked using XPath and JavaScript.");

            Thread.sleep(1000);

            // Click logout button via XPath to ensure correct element
            WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//form[contains(@class, 'logout')]//button[@type='submit']")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", logoutButton);
            System.out.println("Logout button clicked via XPath and JavaScript.");

            // Wait and verify logout success by checking for 'Log in' button presence
            boolean isLoginButtonPresent = !driver.findElements(By.xpath("//button[@title='Log in']")).isEmpty();
            assertTrue("Logout was unsuccessful: 'Log in' button not visible.", isLoginButtonPresent);
            System.out.println("Successfully logged out.");

        } catch (Exception e) {
            System.out.println("Logout test failed: " + e.getMessage());
            throw e;
        }
    }


    @Test
    public void testMarketValuesPage() {
        // Navigate to the Market Values page
        driver.get("https://www.transfermarkt.us/navigation/marktwerte");

        // Expected title of the Market Values page

        String expectedTitle = "MARKET VALUES | Transfermarkt";

        // Assert that the page title matches the expected title
        assertEquals(expectedTitle, driver.getTitle());

        // Locate the "Most valuable players in the world" link
        WebElement mostValuablePlayersLink = waitAndFind(
            By.xpath("//a[contains(text(),'Most valuable clubs')]")
        );

        // Assert that the link is displayed
        assertTrue(mostValuablePlayersLink.isDisplayed());
    }


    @After
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
