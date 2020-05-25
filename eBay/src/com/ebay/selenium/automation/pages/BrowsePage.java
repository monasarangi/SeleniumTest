package com.ebay.selenium.automation.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.LoadableComponent;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BrowsePage extends LoadableComponent<BrowsePage> {
	public WebDriver driver;
	public WebElement ebayLogo;
	public WebElement nextPagination;
	public List<WebElement> allItemAnchorElements;
	public static final String URL = "https://www.ebay.com/b/Rolex-Wristwatches/31387/bn_2989578";

	public BrowsePage(WebDriver driver) {
		this.driver = driver;

	}

	public WebElement getEbayLogo() {
		return driver.findElement(By.id("gh-la")); //Find ebay logo identifier
	}

	public WebElement getNextPagination() {
		return driver.findElement(By.cssSelector(".ebayui-pagination > a[rel='next']")); //Find pagination > arrow
	}

	public List<WebElement> getAllItemAnchorElements() {
		return driver.findElements(By.cssSelector("div.container a.s-item__link")); //Find All item links
	}

	@Override
	protected void load() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void isLoaded() throws Error {
		if (!isElementDisplayed(driver, By.cssSelector("div.srp-controls__row-cells"))) {
			throw new WebDriverException("Browse Page is not successfully loaded");
		}
	}

	public static boolean isElementDisplayed(WebDriver driver, By element) {
		try {
			new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(element));
		} catch (WebDriverException ex) {
			return false;
		}
		return true;
	}

}
