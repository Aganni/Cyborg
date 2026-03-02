package testRunner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions
        (
                features = {"features/bureauEngine.feature"},
                glue = "stepDefinitions",
                monochrome = true,                                                                                                                           //  For readable format output	plugin = {"pretty","html:Reports/Report.html","json:Reports/Report.json","junit:Reports/Report.xml"},                                                              //type:folder/name.type
                plugin = {"com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:"}
        )

public class BureauEngineRunner extends AbstractTestNGCucumberTests {
}
