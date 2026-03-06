package stepDefinitions;

import base.BaseClass;
import constants.Constants;

import helpers.CustomResponseValidator;
import helpers.DynamicDataClass;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import orchestrator.BureauEngineOrchestrator;

import static helpers.DynamicDataClass.getValue;
import static helpers.DynamicDataClass.setValue;
import static helpers.ReadDataFromJson.updateJsonWithPath;

public class BureauEngineStepDefiniton extends BaseClass {

    @Before(order = 0)
    public void setupScenario(Scenario scenario) {
        dynamicData.DynamicDataClass.resetConstants();
        log.info("-------------------------------------------------------");
        log.info("Scenario Started: {}", scenario.getName());
        log.info("Thread ID: {}", Thread.currentThread().getId());
        log.info("-------------------------------------------------------");
    }

    @After(order = 0)
    public synchronized void testData(Scenario scenario) {
        scenario.log("AppForm ID : " + getValue(Constants.APP_FORM_ID));
        log.info("-------------------------------------------------------");
        scenario.log("ApplicantId ID : " + getValue(Constants.APPLICANT_ID));
        dynamicData.DynamicDataClass.resetConstants();
    }

    // Use an instance of the orchestrator instead of calling static methods
    private final BureauEngineOrchestrator bureauOrchestrator = new BureauEngineOrchestrator();

    @Given("KSF generate unique stateless identifiers for the request")
    public void ksfGenerateUniqueStatelessIdentifiersForTheTransaction() {
        setValue(Constants.APP_FORM_ID, "KSF-AD-" + System.currentTimeMillis());
        setValue(Constants.APPLICANT_ID, (int) (Math.random() * 9000000) + 1000000);

        log.info("Generated stateless IDs - AppFormId: {}, ApplicantId: {}", getValue(Constants.APP_FORM_ID),
                getValue(Constants.APPLICANT_ID));
    }

    @When("KSF hit the BureauEngine Api for {string} BureauPull with {string} payload expecting {int}")
    public void BureauEngineBreauPull(String bureauVendor, String pullType, int statusCode) throws Exception {
        bureauOrchestrator.sendBureauPullRequest(bureauVendor, pullType, statusCode);
    }

    @Then("Validate BureauEngine response having status {string} for {string}")
    public void validateBureauEngineResponse(String Status, String bureauVendor) {
        bureauOrchestrator.validateBureauEngineResponse(Status);
    }

    @And("KSF set withdrawalId as {string} for re-pull")
    public void ksfSetWithdrawalIdAsForRePull(String withdrawalId) {
        DynamicDataClass.setValue(Constants.WITHDRAWAL_ID, withdrawalId);
        log.info("Setting withdrawalId : {}", getValue(Constants.WITHDRAWAL_ID));
    }

    @When("KSF update payload path {string} with value {string}")
    public void ksfUpdatePayloadPathWithValue(String path, String value) {
        String currentPayload = bureauOrchestrator.getCurrentPayload();
        if (currentPayload == null) {
            throw new RuntimeException("No active payload found. Ensure a scenario step has initialized the request.");
        }
        String updatedPayload = updateJsonWithPath(currentPayload, path, value);
        session().setCurrentPayload(updatedPayload);
        log.info("Updated payload path '{}' with value '{}'", path, value);
    }

    @Then("Validate BureauEngine error response {string} and message constant {string} for {string} Api")
    public void validateBureauEngineErrorResponseAndMessageConstant(String expectedStatus, String messageConstant, String Api) {
        String expectedMessage = getMessageFromConstant(messageConstant);
        CustomResponseValidator.validateBureauEngineError(expectedStatus, expectedMessage,Api);
    }

    private String getMessageFromConstant(String constantName) {
        try {
            java.lang.reflect.Field field = constants.ApiMessages.class.getField(constantName);
            return (String) field.get(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to find error message constant: " + constantName, e);
        }
    }

    @And("KSF hit the BureauEngine Api again with same identifiers expecting {int}")
    public void ksfHitTheBureauEngineApiAgainWithSameIdentifiersExpecting(int expectedStatusCode) throws Exception {
        bureauOrchestrator.sendRequestWithSameIdentifiers(expectedStatusCode);
    }

    @When("KSF hit BureauEngine for {string} with PullType {string}, KycType {string} and value {string} with {int}")
    public void ksfHitBureauEngineForWithPullTypeKycTypeAndValue(String vendor, String pullType, String kycType,
            String kycValue, int statusCode) throws Exception {
        bureauOrchestrator.buildPayload(vendor, pullType);

        String currentPayload = bureauOrchestrator.getCurrentPayload();
        currentPayload = updateJsonWithPath(currentPayload, "kyc.type", kycType);

        String uniqueKycValue = kycValue;
        int randomNum = 1000 + (int) (Math.random() * 9000);
        if (kycValue.length() > 4) {
            uniqueKycValue = kycValue.substring(0, kycValue.length() - 4) + randomNum;
        } else {
            uniqueKycValue = kycValue + randomNum;
        }
        currentPayload = updateJsonWithPath(currentPayload, "kyc.value", uniqueKycValue);
        session().setCurrentPayload(currentPayload);
        bureauOrchestrator.sendRequest(vendor, statusCode);
    }

    @Given("KSF prepare bureau request for {string} with {string}")
    public void ksfPrepareBureauRequestForWith(String vendor, String pullType) {
        bureauOrchestrator.buildPayload(vendor, pullType);
        log.info("Prepared payload for {} {}", vendor, pullType);
    }

    @Given("KSF hit the BureauEngine Api with prepared payload expecting {int}")
    public void ksfHitTheBureauEngineApiWithPreparedPayloadExpecting(int statusCode) throws Exception {
        bureauOrchestrator.sendRequest("PreparedRequest", statusCode);
    }

    @Then("KSF verify the {string} bureau {string} XML has {string} as {string}")
    public void ksfVerifyTheBureauXmlHasTagAs(String vendor, String docType, String xmlTag, String expectedValue)
            throws Exception {
        bureauOrchestrator.verifyBureauRequestXml(vendor, docType, xmlTag, expectedValue);
    }

    // ── External Bureau API Steps ────────────────────────────────────────────────

    @And("KSF generate {string} payload for external api with {string} for {string} and parse {string}")
    public void ksfGeneratePayloadForExternalApiWithPullTypeForVendorAndParse(
            String lpc, String pullType, String vendor, String parseStr) throws Exception {
        boolean parse = Boolean.parseBoolean(parseStr);
        log.info("Building external bureau payload: vendor={}, pullType={}, lpc={}, parse={}", vendor, pullType, lpc,
                parse);
        bureauOrchestrator.buildExternalPullRequestPayload(lpc, pullType, vendor, parse);
    }

    @When("KSF hit the external BureauEngine Api expecting {int}")
    public void ksfHitTheExternalBureauEngineApiExpecting(int expectedStatusCode) throws Exception {
        bureauOrchestrator.sendExternalRequest(expectedStatusCode);
    }

    @Then("KSF validate external response score for vendor {string}")
    public void ksfValidateExternalResponseScoreForVendor(String vendor) {
        bureauOrchestrator.validateExternalBureauResponse(vendor);
    }
}
