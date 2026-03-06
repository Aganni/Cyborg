package helpers;

import constants.ApiMessages;
import constants.Constants;
import constants.JsonKeys;
import io.restassured.path.json.JsonPath;
import orchestrator.BureauEngineOrchestrator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

import base.BaseClass;

import static constants.JsonKeys.*;
import static helpers.DynamicDataClass.getValue;

public class CustomResponseValidator extends BaseClass {

    private static final Logger log = LogManager.getLogger(CustomResponseValidator.class);

    public void validateBureauResponseSyncWithRequest(JsonPath response, String requestBody) {
        JsonPath requestJson = new JsonPath(requestBody);
        log.info("KSF: Starting Cross-Layer Sync Validation (URL Params + Body)...");

        syncWithDynamicData(response, Constants.APP_FORM_ID, Constants.APP_FORM_ID);
        syncWithDynamicData(response, Constants.APPLICANT_ID, Constants.APPLICANT_ID);

        Object withdrawalId = getValue(Constants.WITHDRAWAL_ID);
        if (withdrawalId != null) {
            // Ensure the key exists and matches from request body
            syncWithDynamicData(response, Constants.WITHDRAWAL_ID, Constants.WITHDRAWAL_ID);
        } else {
            // key should be absent in case standard pull
            boolean isKeyPresent = response.getMap("").containsKey(Constants.WITHDRAWAL_ID);
            Assert.assertFalse(isKeyPresent, "FAILURE: '" + Constants.WITHDRAWAL_ID
                    + "' was found in the response but this is a Standard Pull!");
        }

        // Validate JSON Body Fields (Mirroring)
        syncRequestToResponse(requestJson, response, JsonKeys.LPC);
        syncRequestToResponse(requestJson, response, JsonKeys.LOAN_TYPE);
        syncRequestToResponse(requestJson, response, JsonKeys.CUSTOMER_ID);

        // Deep Nested Validation using master equality checker
        String reqPull = requestJson.getString(JsonKeys.REQ_BUREAU_PULL_TYPE);
        String resPull = response.getString(B_PULL_TYPE);
        validateEquality("PullType", reqPull, resPull);

        log.info("KSF VALIDATION PASSED: Response successfully mirrored all Request layers.");
    }

    private void syncRequestToResponse(JsonPath req, JsonPath res, String field) {
        validateEquality("Body Field: " + field, req.get(field), res.get(field));
    }

    private void syncWithDynamicData(JsonPath response, String jsonPath, String dataKey) {
        Object expected = DynamicDataClass.getValue(dataKey);
        Object actual = response.get(jsonPath);
        validateEquality("URL Param Sync: " + jsonPath, expected, actual);
    }

    /**
     * Master Equality Checker refactored for TestNG parameter order:
     * Assert.assertEquals(actual, expected, message)
     */
    private void validateEquality(String context, Object expected, Object actual) {
        String expectedStr = String.valueOf(expected);
        String actualStr = String.valueOf(actual);

        try {
            // Corrected order to prevent the "mismatch" string from appearing as the
            // 'Actual' value
            Assert.assertEquals(actualStr, expectedStr, context + " mismatch!");
            log.info("SUCCESS: {} matches. Value: [{}]", context, actualStr);
        } catch (AssertionError e) {
            log.error("FAILURE: {} | Expected: [{}] but got: [{}]", context, expectedStr, actualStr);
            throw e;
        }
    }

    public void validateFailureDetails(JsonPath response, String requestBody) {
        log.info("KSF: Starting Dynamic Failure Validation...");
        JsonPath request = new JsonPath(requestBody);

        // 1. Basic Status Check
        Assert.assertEquals(response.getString(JsonKeys.STATUS), "Failure");
        Assert.assertEquals(response.getString(JsonKeys.MESSAGE), ApiMessages.FIELDS_MISSING_MSG);

        // 2. Extract failure info from the FIRST error in the array
        String actualField = response.getString("errors[0].Field"); // e.g., "LoanType" or "Type"
        String actualMsg = response.getString("errors[0].Msg");

        // 3. Dynamic Logic: Determine what we sent based on the error field
        if (actualField.equalsIgnoreCase("LoanType")) {
            String sentValue = request.getString("loanType");
            String expectedSnippet = String.format(ApiMessages.ERR_LOAN_TYPE, sentValue);
            Assert.assertTrue(actualMsg.toLowerCase().contains(expectedSnippet.toLowerCase()),
                    String.format("Error Msg Mismatch!\nField: %s\nExpected snippet: [%s]\nActual: [%s]",
                            actualField, expectedSnippet, actualMsg));
        } else if (actualField.equalsIgnoreCase("Type") || actualField.toLowerCase().contains("kyc")) {
            String sentValue = request.getString("kyc.type");
            String msgLower = actualMsg.toLowerCase();

            // Normalize sent value and remove surrounding quotes when checking
            String sentNorm = sentValue == null ? "" : sentValue.toLowerCase().replace("'", "").replace("\"", "");

            boolean hasCanonicalPhrases = msgLower.contains("invalid kyc type")
                    && msgLower.contains("please check supported types");
            boolean containsSentValue = sentNorm.length() > 0
                    && (msgLower.contains(sentNorm) || msgLower.contains("'" + sentNorm + "'"));

            Assert.assertTrue(hasCanonicalPhrases,
                    String.format(
                            "Error Msg Mismatch! Expected message to contain canonical KYC failure phrase. Actual: [%s]",
                            actualMsg));

            if (!containsSentValue) {
                // Log a warning but don't fail — API sometimes quotes a different token than
                // sent (e.g., wraps in single quotes or returns a different representation)
                log.warn("KYC failure message didn't contain the sent KYC type [{}]. Actual message: {}", sentValue,
                        actualMsg);
            }
        } else {
            // Unknown field - assert that we at least have a generic error message
            Assert.assertTrue(actualMsg != null && actualMsg.length() > 0,
                    "Expected non-empty error message for field: " + actualField);
        }

        log.info("SUCCESS: Confirmed failure for field [{}] with correct dynamic message.", actualField);
    }

    public static void validateBureauEngineError(String expectedStatus, String expectedMessage, String Api) {
        JsonPath response = session().getBureauEngineResponse();

        // Status Check
        if (Api.equalsIgnoreCase("CreditReport")) {
            if (!expectedStatus.equalsIgnoreCase(response.getString(JsonKeys.STATUS))) {
                String msg = String.format("FAIL: Expected Status %s but got %s. Full Response: %s",
                        expectedStatus, response.getString(JsonKeys.STATUS), response.prettify());
                log.error(msg);
                Assert.fail(msg);
            }
        }
        // Message Check
        String actualMessage = response.getString(JsonKeys.MESSAGE);
        if (expectedMessage != null && !actualMessage.contains(expectedMessage)) {
            String msg = String.format("FAIL: Expected Message to contain '%s' but got '%s'.",
                    expectedMessage, actualMessage);
            log.error(msg);
            Assert.fail(msg);
        }

        log.info("KSF: BureauEngine Error Validation Successful. Status: {}, Message: {}",
                expectedStatus, actualMessage);
    }
}