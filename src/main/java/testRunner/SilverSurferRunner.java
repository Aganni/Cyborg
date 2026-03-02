package testRunner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions
        (
                features = {"features/silverSurfer.feature"},
                glue = "stepDefinitions",
                monochrome = true,                                                                                                                           //  For readable format output	plugin = {"pretty","html:Reports/Report.html","json:Reports/Report.json","junit:Reports/Report.xml"},                                                              //type:folder/name.type
                plugin = {"com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:"}
        )
public class SilverSurferRunner extends AbstractTestNGCucumberTests {

}
