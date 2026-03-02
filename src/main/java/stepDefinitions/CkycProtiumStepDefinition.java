package stepDefinitions;

import base.BaseClass;
import constants.APIEndPoints;
import constants.Constants;
import helpers.DynamicDataClass;
import helpers.HelperMethods;
import helpers.ReadDataFromJson;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CkycProtiumStepDefinition extends BaseClass {

    @Given("KSF starts ckyc initiating for different appforms with Protium")
    public void ksfStartsCkycInitiatingForDifferentAppformsWithProtium() throws Exception{
        log.info("KSF starts ckycInitiating with Protium");
        String requestId = HelperMethods.doPostCkycFileProtium("protiumUri", APIEndPoints.CKYC_FILE_REPORTING_API);
        log.info("KSF ends ckycInitiating with requestId:  " + requestId);
        Thread.sleep(5000);
    }
    @Then("KSF hits batch status with Request ID to check the reporting status")
    public void ksfHitsBatchStatusWithRequestIdToCheckTheReportingStatus() throws Exception {
        String response = HelperMethods.doGetWithRequestId( "protiumUri",(String) DynamicDataClass.getValue (Constants.REQUEST_ID ),APIEndPoints.CKYC_FILE_STATUS_API );
        log.info("Comparing the results..");
        Object fileReportingDetailsList = ReadDataFromJson.readFileReportingDetailsList(response);
        HelperMethods.compareResults(fileReportingDetailsList.toString(),"appFormId", "Protium");
        log.info("Status are matched");
    }

}
