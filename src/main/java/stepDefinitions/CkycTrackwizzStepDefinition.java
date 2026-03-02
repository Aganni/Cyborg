package stepDefinitions;

import base.BaseClass;
import constants.APIEndPoints;
import constants.Constants;
import helpers.DynamicDataClass;
import helpers.HelperMethods;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;


public class CkycTrackwizzStepDefinition extends BaseClass {

    public static String fileURL = null;

    @Given ( "KSF starts ckyc initiating for different appforms with Trackwizz" )
    public void ksfStartsCkycIntiating () throws Exception {
        log.info("KSF starts ckycInitiating");
        String uuidNumber = HelperMethods.doPostCkycFile("eywaUri", APIEndPoints.CKYC_INITIATE_API);
        log.info("KSF ends ckycInitiating with uuidNumber:  " + uuidNumber);
        Thread.sleep(5000);
    }

    @Then( "KSF hits batch status with UUID to check the reporting status" )
    public void ksfHitsBatchStatusWithUUID () throws Exception {
        String response = HelperMethods.doGetWithUuid( "eywaUri",(String) DynamicDataClass.getValue (Constants.UUID_NUMBER ),APIEndPoints.CKYC_BATCH_STATUS_API );
        log.info("Comparing the results..");
        HelperMethods.compareResults(response, "app_form_id", "Trackwizz");
        log.info("UUID status are matched");
    }

    @Then ( "KSF generates S3 URL for initiated appforms" )
    public void ksfHitsDateWiseApi ( ) throws Exception {
        fileURL = HelperMethods.doGetLatestS3UrlWithDate("eywaUri",APIEndPoints.CKYC_STATUS_API);
    }

    @Then ( "KSF validates CKYC reporting" )
    public void validateCKYCReporting( ) throws Exception {
        HelperMethods.downloadZipFile(fileURL);
    }

}
