package testRunner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(features = { "features" }, glue = {
        "stepDefinitions" }, monochrome = true, plugin = { "pretty", "html:target/cucumber-reports.html",
                "json:target/cucumber-reports.json" })
public class BureauEngineRunner extends AbstractTestNGCucumberTests {
}
