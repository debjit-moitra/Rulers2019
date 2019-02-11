package com.cerner.sfautomation.testsuite.payerrules;

import org.testng.annotations.Test;

import com.cerner.sfautomation.common.CommonTask;
import com.cerner.sfautomation.framework.SoftAssert;
import com.cerner.sfautomation.utility.PropertyUtility;
import com.cerner.sfautomation.utility.ReportResult;

import org.testng.annotations.BeforeTest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterTest;

public class ECVTest {

	private static final TimeUnit SECONDS = null;
	private static final String currentDateTime = null;
	private static WebDriver driver;
	private static Properties prop; 
	StringBuilder sb;
	String smokeTestSubject = "";
	String releaseAndEnv = "";
	String[] stepName = new String[24];
	String[] stepDescription = new String[24];
	String[] stepResult = new String[24];
	String[] stepComment = new String[24];
	String overAllTestResult = "Passed";
	String buildLabel = "";
	String targetBuildLabel = "";
	String targetBuildType = "";
	String sentFrom = "";
	String sentTo = "";
	String mailServer = "";
	String ieDriverPath = "";
	String htmlResultDirectory = "";
	String headerText = null;
	String appURL;
	Calendar docGenInstance = Calendar.getInstance();
	static Logger logger = LogManager.getLogger(ECVTest.class);
	String igoneProtectedMode="true";
	WebDriverWait wait;
	int noOfRecs=0;
	int totalEdits=0;
	int totalExceptions=0;
	
	@BeforeTest
	public void doSetup()
	{
		  Properties prop = new Properties();
			InputStream input1 = null;
			
			try {
				input1 = ECVTest.class.getClassLoader().getResourceAsStream("conf/ecvTest.properties");
				prop.load(input1);

				ieDriverPath = prop.getProperty("driverPath");
				sentFrom = prop.getProperty("sentFromEmail");
				sentTo = prop.getProperty("sentToEmail");
				mailServer = prop.getProperty("mailServer");
				htmlResultDirectory = prop.getProperty("htmlResultDirectory");
				
				igoneProtectedMode=prop.getProperty("igoneProtectedMode");
				appURL=prop.getProperty("appURL");
				
				smokeTestSubject=prop.getProperty("smokeTestSubject");
				releaseAndEnv=prop.getProperty("releaseAndEnv");
				logger.info("URL: "+appURL);
				logger.info("HTML Result Directory: "+htmlResultDirectory);
				logger.info("Mail Server: "+mailServer);
				logger.info("Distribution List: "+sentTo);
				logger.info("BuildType:"+targetBuildType+" Build Number:"+targetBuildLabel);

			} catch (IOException ie) {
				logger.error("Error reading test.properties file");
				ie.printStackTrace();
			} finally {
				if(input1!=null){
					  try{
						  input1.close();
					  }catch (IOException ie){
						  ie.printStackTrace();
					  }
					  
				}
			}
			
			

			File file = new File(ieDriverPath);
			System.setProperty("webdriver.ie.driver", file.getAbsolutePath());
					
			
			Calendar now = Calendar.getInstance();
			String month = String.valueOf(now.get(Calendar.MONTH) + 1);
			String day = String.valueOf(now.get(Calendar.DATE));
			if (day.length() == 1)
				day = 0 + day;
			String year = String.valueOf(now.get(Calendar.YEAR));
			String hour = String.valueOf(now.get(Calendar.HOUR_OF_DAY));
			if (hour.length() == 1)
				hour = 0 + hour;
			String min = String.valueOf(now.get(Calendar.MINUTE));
			if (min.length() == 1)
				min = 0 + min;
			
			for (int i = 0; i <= 2; i++) {
				stepName[i] = "";
				stepDescription[i] = "";
				stepResult[i] = "";
				stepComment[i] = "";
			}
			stepName[0] = "Launch and login to the ECV";
			stepDescription[0] = "Launch and login to the ECV tool using the URL";

			stepName[1] = "Run all the data in the ECV tool";
			stepDescription[1] = "All the claims are processed through ECV tool";
			
			stepName[2] = "Compare the results with the previous run";
			stepDescription[2] = "Compares for any new edits in the UI";
			
			try{
		    	  if(igoneProtectedMode.equalsIgnoreCase("true")){
		    	      DesiredCapabilities ieCaps=DesiredCapabilities.internetExplorer();
		    	      ieCaps.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
		    	      ieCaps.setCapability(InternetExplorerDriver.INITIAL_BROWSER_URL, "about:blank");
		    	      ieCaps.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, true);
		    	      driver = new InternetExplorerDriver(ieCaps);
		    	      logger.info("Starting IEDriver ignoring protected mode settings");
		    	      }
		    	  else{
		    		  DesiredCapabilities ieCaps=DesiredCapabilities.internetExplorer();
		    	      ieCaps.setCapability(InternetExplorerDriver.INITIAL_BROWSER_URL, "about:blank");
		    	      ieCaps.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, true);
		    	      driver = new InternetExplorerDriver(ieCaps);
		    	      logger.info("Starting IEDriver without ignoring protected mode settings");	    		  
		    	  }
		    	  
		    	  driver.findElement(By.tagName("html")).sendKeys(Keys.chord(Keys.CONTROL, "0"));
			      logger.info("Launched browser with initial URL title:"+driver.getTitle().toString());
			      driver.manage().timeouts().implicitlyWait(90, TimeUnit.SECONDS);
			      
			}
			catch(WebDriverException e){
			  	  logger.info("Opened IE driver");
			  	     	 
			    }
	}


	
	
	@Test(priority=1)
	public void openECV()
	{
			
		System.out.println(" Opening ECV Tool ...");
		wait = new WebDriverWait(driver, 400);
	      driver.get(appURL); 
	      logger.info("Initial URL Title - "+driver.getTitle());
	      stepResult[0] = "Passed";
		  
	      
	      if(driver.getTitle().contains("Certificate Error")){
	    	  logger.warn("Certificate error accessing the URL");
	    	  driver.get("javascript:document.getElementById('overridelink').click()");
	    	  logger.warn("Accepted certificate error to continue");
	      }

	    stepResult[0] = "Passed";
		System.out.println("logged in.. looking for the rows");

		WebElement LoginButton = driver.findElement(By.id("ctlIccButton2Button__"));
	    LoginButton.click();
	    
	    System.out.println(" clicked...");
	    driver.get(appURL);
	    System.out.println("Refreshed the page");
	    driver.switchTo().defaultContent();
	    wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//table[@class='GWTTable']")));
	    System.out.println("Found the webElements");
	    String xpathStringforStatus ="//table[@class='GWTTable-Grid']/tbody/tr[@class='GWTTable-Row GWTTable-hand']/td[5]/div";
	    WebElement ecvStatus1= driver.findElement(By.xpath(xpathStringforStatus));
	    String Status1= ecvStatus1.getText();
	    WebElement ecvStatus2=driver.findElement(By.xpath("//table[@class='GWTTable-Grid']/tbody/tr[2]/td[5]/div"));
	    String Status2=ecvStatus2.getText();
	    WebElement ecvStatus3=driver.findElement(By.xpath("//table[@class='GWTTable-Grid']/tbody/tr[3]/td[5]/div"));
	    String Status3=ecvStatus3.getText();
	    
	    while((!(Status1.equalsIgnoreCase("END"))) ||(!(Status2.equalsIgnoreCase("END")))||(!(Status3.equalsIgnoreCase("END"))))
	    {
	    	try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    	driver.get(appURL);
	    	driver.switchTo().defaultContent();
	    	wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//table[@class='GWTTable']")));
	    	String xpathStringforStatusCurrent ="//table[@class='GWTTable-Grid']/tbody/tr[@class='GWTTable-Row GWTTable-hand']/td[5]/div";
		    WebElement ecvCurrentStatus1= driver.findElement(By.xpath(xpathStringforStatus));
		    String CurrentStatus1= ecvCurrentStatus1.getText();
		    WebElement ecvCurrentStatus2=driver.findElement(By.xpath("//table[@class='GWTTable-Grid']/tbody/tr[2]/td[5]/div"));
		    String CurrentStatus2=ecvCurrentStatus2.getText();
		    WebElement ecvCurrentStatus3=driver.findElement(By.xpath("//table[@class='GWTTable-Grid']/tbody/tr[3]/td[5]/div"));
		    String CurrentStatus3=ecvCurrentStatus3.getText();
		    Status1=CurrentStatus1;
		    System.out.println(Status1);
		    Status2=CurrentStatus2;
		    System.out.println(Status2);
		    Status3=CurrentStatus3;
		    System.out.println(Status3);
	    }
	    
	    if((Status1.equalsIgnoreCase("END"))&& (Status2.equalsIgnoreCase("END"))&&(Status3.equalsIgnoreCase("END"))){
	    	stepResult[1] = "Passed";
			stepComment[1] = "Status: END";
	          
	      }
	      else if((Status1.equalsIgnoreCase("ERR"))&&(Status2.equalsIgnoreCase("ERR"))&&(Status3.equalsIgnoreCase("ERR"))){
	    	  stepResult[1] = "Passed";
			  stepComment[1] = "Status: ERR";	    	  
	       }
	    
	    System.out.println("Executed all the recs");	 
	    driver.switchTo().defaultContent();
	    wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//table[@class='gwt-TabLayoutPanelContent']/tbody/tr[2]/td/table[@class='CUI-Table']/tbody/tr[1]/td[1]/div")));
	    WebElement nofOfRecs=driver.findElement(By.xpath("//table[@class='gwt-TabLayoutPanelContent']/tbody/tr[2]/td/table[@class='CUI-Table']/tbody/tr[1]/td[1]/div"));
		
		String noOfRecValue=nofOfRecs.getText();
		System.out.println(noOfRecValue);
		String[] parts = noOfRecValue.split(" ");
		String lastWord = parts[parts.length - 1];
		System.out.println(lastWord);
		noOfRecs=Integer.parseInt(lastWord);
		System.out.println(noOfRecs);
		 
	  
	    int noOfRows=0;
	    noOfRows= noOfRecs/150;
	    if((noOfRecs%150) !=0)
	    {
	    	noOfRows=noOfRows+1;
	    }
	    
	    System.out.println(noOfRows);
	    driver.switchTo().defaultContent();
	    List <WebElement> executedRow= new ArrayList<WebElement>();
	    for (int i=0;i< noOfRows;i++){
	    	int row= i+1;
	    	wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//table[@class='GWTTable']")));
	    	String xpathforrows="//table[@class='GWTTable-Grid']/tbody/tr["+row+"]/td[5]/div";
	    	System.out.println("Xpath:"+xpathforrows);
	    	try{  
	    		executedRow.add(driver.findElement(By.xpath(xpathforrows))); 
	    		System.out.println("Row added");
	        }catch(Throwable t){  
	            System.out.println(t);  
	        } 
	    	
	    	
	    }

	    driver.switchTo().defaultContent();
    	for(int i=0; i<executedRow.size();i++)
		{
			wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//table[@class='GWTTable']")));
	    	String xpathforEdits="//table[@class='GWTTable-Grid']/tbody/tr["+(i+1)+"]/td[7]/div";
	    	String xpathforException="//table[@class='GWTTable-Grid']/tbody/tr["+(i+1)+"]/td[8]/div";
	    	WebElement currentRowEdits=driver.findElement(By.xpath(xpathforEdits));
	    	WebElement currentRowExceptions=driver.findElement(By.xpath(xpathforException));
	    	totalEdits=totalEdits+Integer.parseInt(currentRowEdits.getText());
	    	totalExceptions= totalExceptions+Integer.parseInt(currentRowExceptions.getText());
	    	System.out.println("No of Edits : "+totalEdits+" No of Exceptions : "+totalExceptions);
    		stepResult[2] = "Passed";
			stepComment[2] = "No of Edits : "+totalEdits+" No of Exceptions : "+totalExceptions;
	    	
		}
	}
	


	@AfterTest
	public void ExitSetup()
	{
		SoftAssert softAssert = new SoftAssert();
		try {
			sb = new StringBuilder();
			sb.append("<html><body>");
			sb.append("<table border=\"0\" cellpadding=\"3\" cellspacing=\"0\" width=\"60%\">");
			sb.append("<tr><td>");
			sb.append("The ECV tool Tests have been executed with the following results:");
			sb.append("</td></tr>");

			sb.append("<tr><td>");
			sb.append("<b>Date/Time: </b>" + currentDateTime);
			sb.append("</td></tr>");

			sb.append("<tr><td>");
			sb.append("<b>Release/Environment:</b>");
			sb.append(releaseAndEnv);
			sb.append("</td></tr>");


			sb.append("<tr><td>");
			sb.append("<b>URL: </b>" + appURL);
			sb.append("</td></tr>");
			sb.append("<tr><td></td></tr>");
			sb.append("</table>");

			sb.append("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\" width=\"60%\">");
			sb.append("<tr>");
			sb.append("<th width=\"15%\">Scripts Executed</td>");
			sb.append("<th width=\"30%\">Description</th>");
			sb.append("<th width=\"10%\">Results</th>");
			sb.append("<th width=\"20%\">Comments/Data</th>");

			for (int i = 0; i <= 2; i++) {
				if (stepResult[i].equals(""))
					overAllTestResult = "Not Complete";
				else
					overAllTestResult = "Passed";
			}

			for (int i = 0; i <= 2; i++) {
				// Add results to report
				sb.append("<tr>");
				sb.append("<td>" + stepName[i] + "</td>");
				sb.append("<td>" + stepDescription[i] + "</td>");
				if (stepResult[i].equals("")) {
					overAllTestResult = "Failed";
					sb.append("<td style='color:red'>" + stepResult[i] + "</td>");
				} else
					overAllTestResult = "Passed";
					sb.append("<td style='color:blue'>" + stepResult[i] + "</td>");
				sb.append("<td>" + stepComment[i] + "</td>");
				sb.append("</tr>");

			}
			smokeTestSubject = smokeTestSubject + "--" + overAllTestResult;
			System.out.println(sb.toString());
			File file = new File(htmlResultDirectory + "/ECVTestResult.html");
			File file1 = new File(htmlResultDirectory + "/ECVTest.txt");

			StringBuilder sb1 = new StringBuilder();
			sb1.append(" ");
			sb1.append(overAllTestResult);

			BufferedWriter writer1 = new BufferedWriter(new FileWriter(file1));
			writer1.write(sb1.toString());
			writer1.flush();
			writer1.close();

			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(sb.toString());
			writer.flush();
			writer.close();
			
			
			// Take screenshot of app in case of failure
			if (!overAllTestResult.equals("Passed")) {
				System.out.println("About to take screenshot");
				DateFormat df = new SimpleDateFormat("MM-dd-yyyy_hh-mm-ss");
				String failedScreenshotFileName = "ScreenShot" + df.format(new Date()) + ".png";
				File file3 = new File(htmlResultDirectory + "/failedTests/" + failedScreenshotFileName);
				File image = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
				FileUtils.copyFile(image, file3);
				System.out.println("Screenshot taken");
				softAssert.assertEquals(overAllTestResult, "Passed", "Verify overall test result");				

			}

			if (sentTo.contains("@") && sentTo.contains("."))
				ReportResult.sendMail(mailServer, sentFrom, sentTo, smokeTestSubject, sb);
			System.out.println("Able to send mail:");
		} catch (Exception e) {
			System.out.println("Unable to mail test result. Error is:");
			e.printStackTrace();
		}
		
		softAssert.assertAll();
	}
}

		
	
	

