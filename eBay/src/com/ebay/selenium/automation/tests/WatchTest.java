package com.ebay.selenium.automation.tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ebay.selenium.automation.pages.BrowsePage;

public class WatchTest {

	public BrowsePage browsePage;
	public static WebDriver driver;
	public WebDriverWait webDriverWait;

	Map<String, Integer> map = new HashMap<String, Integer>();

	private static final int MAX_ITEM_COUNT = 250;

	@BeforeClass
	public void setUp() {
		// Set chromedriver.exe path
		System.setProperty("webdriver.chrome.driver", "/Users/m0s04ai/Documents/chromedriver");
		driver = new ChromeDriver();
		browsePage = new BrowsePage(driver);
		webDriverWait = new WebDriverWait(driver, 15);
	}

	/**
	 * Loads browse page And verify eBay logo on top of the page
	 */
	@Test(groups = "WatchTest", priority = 1, alwaysRun = true)
	public void verifyEbayLogo() {
		driver.get(BrowsePage.URL);
		driver.manage().window().maximize();
		Assert.assertTrue(browsePage.getEbayLogo().isDisplayed());
		Reporter.log("Verified eBay Log Displayed on Page", true);
	}

	/**
	 * Get All item links from browse pages using pagination. Check if the item
	 * has link to ViewItem(ebay.com/itm) , product(ebay.com/p/) or category
	 * page(ebay.com/c/).Get View Per Hour from each of the pages and ignore if
	 * any of the page has no "View Per Hour"
	 *
	 * Add page URL and no of views per hour to Map<String, Integer> map = new
	 * HashMap<String, Integer>();
	 */
	@Test(groups = "WatchTest", priority = 1, alwaysRun = true)
	public void printUrlWithViews() {
		int pagination = 1;
		int totalItems = 0;
		boolean isNextPageEnabled = true;

		do {
			driver.get(BrowsePage.URL + "?_pgn=" + pagination);
			webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("gh-la")));
			driver.manage().window().maximize();

			String paginationDisabled = browsePage.getNextPagination().getAttribute("aria-disabled");

			isNextPageEnabled = paginationDisabled == null || !paginationDisabled.equals("true");
			List<WebElement> allItemAnchorElements = browsePage.getAllItemAnchorElements();
			int size = allItemAnchorElements.size();
			if ((totalItems + size) > MAX_ITEM_COUNT) {
				allItemAnchorElements = allItemAnchorElements.subList(0, MAX_ITEM_COUNT - totalItems);
			}

			totalItems += allItemAnchorElements.size();
			System.out.println("");
			List<String> itemUrls = getItemUrls(allItemAnchorElements);

			for (String viewItemUrl : itemUrls) {
				driver.get(viewItemUrl);
				// Check the URL
				Assert.assertTrue(driver.getCurrentUrl().contains("ebay.com/itm")
						|| driver.getCurrentUrl().contains("ebay.com/p/")
						|| driver.getCurrentUrl().contains("ebay.com/c/"));

				try {
					WebElement viewPerHourFromPage = null;
					if (driver.getCurrentUrl().contains("ebay.com/itm")) {
						viewPerHourFromPage = driver.findElement(By.id("vi_notification_new"));
					} else if (driver.getCurrentUrl().contains("ebay.com/p/")) {
						viewPerHourFromPage = driver.findElement(By.className("banner-status"));
					}

					if (viewPerHourFromPage != null) {
						addViewsToMap(viewPerHourFromPage, viewItemUrl);
					}

				} catch (NoSuchElementException ex) {
					// Handle exception If no views present in the page
				}
			}

			pagination += 1;
		} while (totalItems < MAX_ITEM_COUNT && isNextPageEnabled);
		Reporter.log("Total number of items checked ---- " + totalItems);
		Reporter.log("Items with views ----" + map.size(), true);
		Set<String> set = map.keySet();
		for (String element : set) {
			Reporter.log(element + " ----views----" + map.get(element));
		}
	}

	/**
	 * This method depends on previous method printUrlWIthViews as teh preevious
	 * method is adding urls & no of views to Map Sort the map by value and
	 * print top 5 values
	 */
	@Test(groups = "WatchTest", dependsOnMethods = "printUrlWIthViews", priority = 1, alwaysRun = true)
	public void printTopFiveWatches() {
		Map<String, Integer> sortedmap = sortMapByValue();
		Set<String> set = sortedmap.keySet();
		int count = 0;
		Reporter.log("Top 5 Watches with Highest Views below ********* ", true);
		for (String element : set) {
			Reporter.log(element + " ---- has Number of Views ----  " + sortedmap.get(element));
			count++;
			if (count == 5) {
				break;
			}
		}

	}

	/**
	 * 
	 * @param allItemAnchorElements
	 * @return List of Item anchor links
	 */
	private List<String> getItemUrls(List<WebElement> allItemAnchorElements) {
		List<String> itemUrls = new ArrayList<String>();
		for (WebElement url : allItemAnchorElements) {
			itemUrls.add(url.getAttribute("href"));

		}
		return itemUrls;
	}

	/**
	 * 
	 * @param viewPerHourPage
	 * @param viewItemUrl
	 *            Extract integer value from <<# viewed per hour>> e.g. 20
	 *            viewed per hour Add Url and views to Map
	 */
	private void addViewsToMap(WebElement viewPerHourPage, String viewItemUrl) {
		String viewPerHourText;
		Integer noOfView = null;

		if (viewPerHourPage.isDisplayed()) {

			// Get text from view per hour element
			viewPerHourText = viewPerHourPage.getText();
			System.out.println(viewItemUrl + "----" + viewPerHourText);
			// Ignore if teh text is Last one else extract the integer value
			// from it
			if (!viewPerHourText.equalsIgnoreCase("Last one!")) {
				String viewCount = viewPerHourText.split(" ")[0];
				noOfView = Integer.parseInt(viewCount);
				map.put(viewItemUrl, noOfView);
			}
		}
	}

	/**
	 * Sort map by value
	 * 
	 * @return sorted Map by Value
	 */
	private Map<String, Integer> sortMapByValue() {
		List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(map.entrySet());

		// Sort the list
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		// put data from sorted list to hashmap
		HashMap<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		for (Map.Entry<String, Integer> aa : list) {
			sortedMap.put(aa.getKey(), aa.getValue());
		}
		return sortedMap;
	}

	/**
	 * Quit driver or close driver by calling driver.close()
	 */
	@AfterClass
	public void tearDown() {
		driver.quit();
	}

}
