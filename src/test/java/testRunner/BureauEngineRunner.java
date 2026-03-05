package testRunner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions
        (
                features = {"features/negativeScenarios.feature"},
                glue = "stepDefinitions",
                monochrome = true,                                                                                                                           //  For readable format output	plugin = {"pretty","html:Reports/Report.html","json:Reports/Report.json","junit:Reports/Report.xml"},                                                              //type:folder/name.type
                plugin = {"com.aventstack.extentreports.cucumber.adapter."}
        )

public class BureauEngineRunner extends AbstractTestNGCucumberTests {
}
