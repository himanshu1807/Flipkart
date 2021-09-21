import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import com.opencsv.CSVWriter;

public class Flipkart {
	public static WebDriver driver;
	public static Actions act;
	public static String xpathFilters;
	public static FileWriter outputfile;
	public static CSVWriter writer;
	public static JSONObject jsonObject = new JSONObject();
	public static int filterCount; 

	public static void main(String[] args) throws IOException, Exception {

		ReadExcel.ReadExcel(args[0]);
		filterCount = ReadExcel.filterCount;

		LaunchFlipKartAndSearch();		
		
		takeSnapShot(driver, ReadExcel.screenshotPath);

		xpathFilters = "//*[@class='_1KOcBL']";
				
		maxPrice(ReadExcel.MaxPrice);
		
		for (int i=1; i<=filterCount; i++) {
			String filter = ReadExcel.filterCriteria.get(i).toString();
			ReadExcel.splitString(filter);
			String filterName = ReadExcel.filterName1;
			String filterValue = ReadExcel.filterValue1;
			Filter(filterName, filterValue, xpathFilters);
			Thread.sleep(1000);;
		}
		
//		Thread.sleep(2000);
//		
//		Filter(ReadExcel.filterName1, ReadExcel.filterValue1, xpathFilters);
//	
//		
//		Thread.sleep(2000);
//		
//		Filter(ReadExcel.filterName2, ReadExcel.filterValue2, xpathFilters);
//		
//		
//		Thread.sleep(2000);
		
		
		searchResult();
		
		Thread.sleep(5000);
		driver.quit();
		
	}
	//MAX price
	public static void maxPrice(String price) {
		WebElement maxPrice = driver.findElement(By.xpath("(//*[@class='_2YxCDZ'])[2]")); // Price range
		Select select = new Select(maxPrice);
		select.selectByValue(ReadExcel.MaxPrice);
	}
	
	//Launch flipkart and search mobile
	public static void LaunchFlipKartAndSearch() throws InterruptedException {
		System.setProperty("webdriver.chrome.driver", ReadExcel.chromedriverPath);
		driver = new ChromeDriver();
		driver.get(ReadExcel.url);
		driver.manage().window().maximize();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

		Thread.sleep(2000);
		act = new Actions(driver);
		act.sendKeys(Keys.ESCAPE);

		WebElement closePopUp = driver.findElement(By.xpath("/html/body/div[2]/div/div/button"));
		closePopUp.click();

		//select mobile from auto suggestion
		WebElement searchBar = driver.findElement(By.name("q"));
		searchBar.sendKeys(ReadExcel.mobileBrand);
		Thread.sleep(2000);
		driver.findElement(By.xpath("//*[contains(@href,'suggestionId')]")).click(); // select mobiles from auto  suggest

		Thread.sleep(1000);
		WebElement logo = driver.findElement(By.xpath("//a[@href='/']"));
		act.moveToElement(logo).build().perform();
	}
	
	// filter search result based on parameter provided in Input Sheet
	public static void Filter(String filterName,String filterValue, String filter) {
		try {
			String xpathFilterName = filter + "/..//div[text()='"+filterName+"']";
			String xpathFilterValuesVisible = filter + "/..//div[text()='"+filterName+"']/../..//div[2]";
			String xpathFilterValue = xpathFilterValuesVisible + "/..//div[@title='"+filterValue+"']";
			try {
				WebElement filterValueisDisplayed = driver.findElement(By.xpath(xpathFilterValuesVisible));
				Thread.sleep(1000);
				driver.findElement(By.xpath(xpathFilterValue)).click();
			}
			catch(Exception e) {
				WebElement FilterName = driver.findElement(By.xpath(xpathFilterName));
				act.moveToElement(FilterName).build().perform();
				FilterName.click();
				Thread.sleep(1000);
				driver.findElement(By.xpath(xpathFilterValue)).click();
			}
			
			
			Thread.sleep(2000);
			driver.findElement(By.xpath(xpathFilterName)).click();
			System.out.println(filterValue + " " + filterName + " selected");
		}

		catch (Exception e) {
			System.out.println("no such element");
		}
	}
	
	public static void takeSnapShot(WebDriver webdriver,String fileWithPath) throws Exception{

        //Convert web driver object to TakeScreenshot

        TakesScreenshot scrShot =((TakesScreenshot)webdriver);
        //Call getScreenshotAs method to create image file
        File SrcFile=scrShot.getScreenshotAs(OutputType.FILE);
        //Move image file to new destination
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");  
        LocalDateTime now = LocalDateTime.now(); 
        String date = dtf.format(now);
        String fileName = date.replace(":", "-");
        System.out.println(dtf.format(now));  
        File DestFile=new File(fileWithPath+"\\"+ ReadExcel.mobileBrand+" search "+fileName+".png");
        //Copy file at destination
        FileUtils.copyFile(SrcFile, DestFile);

    }
	
	//store searched mobiles into json file
	@SuppressWarnings("unchecked")
	public static void searchResult() throws InterruptedException, IOException {
		try {
			File file = new File(ReadExcel.outPutFile);
			outputfile = new FileWriter(file);
					
		String searchResult = "//*[@class='_1YokD2 _3Mn1Gg'][contains(@style,'flex-grow')]";
		WebElement mobileSearchResult = driver.findElement(By.xpath(searchResult));
		List<WebElement> mobiles = mobileSearchResult.findElements(By.tagName("a"));
		int num = mobiles.size();
		System.out.println("search results count: " + num);

		String [] MobileList = new String[num];
		 for(int i =2;i<(num-1);i++) {
			 Thread.sleep(1000);
			 String xpathMobileName = searchResult + "/div[" + i + "]/div/div/div/a/div[2]/div/div";
			 String xpathMobilePrice = searchResult + "/div[" + i + "]/div/div/div/a/div[2]/div[2]/div/div/div";
			 WebElement mobileName = driver.findElement(By.xpath(xpathMobileName));
			 WebElement mobilePrice = driver.findElement(By.xpath(xpathMobilePrice));
			 String s = mobileName.getText();
			 String mobileName1 = s.replace(",", "");
			 String s1 = mobilePrice.getText();
			 String price = s1.replaceAll("[^0-9]", "");
			 System.out.println(s+" "+price);
			 jsonObject.put(mobileName1, price);
			 
			 MobileList[i]=s;
		 }
		}
		catch(Exception e) {
			System.out.println("End of List");
		}
		JSONObject mobileObject = new JSONObject(); 
		mobileObject.put("mobile", jsonObject);
		
		JSONArray mobileList = new JSONArray();
		mobileList.add(mobileObject);
		mobileList.add(jsonObject);
		outputfile.write(mobileList.toJSONString());
		outputfile.close();
		System.out.println("JSON file created: "+jsonObject);;
		
	}
}