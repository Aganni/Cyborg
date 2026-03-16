package helpers;

import constants.ApiMessages;
import constants.Constants;
import constants.JsonKeys;
import io.restassured.path.json.JsonPath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

import java.util.List;
import java.util.Map;

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

    public void validateFailureDetails(String expectedStatus,JsonPath response, String requestBody) {
        validateFailureDetails(response, requestBody, expectedStatus, ApiMessages.FIELDS_MISSING_MSG);
    }

    public void validateFailureDetails(JsonPath response, String requestBody, String expectedStatus,
            String expectedMessage) {
        log.info("KSF: Starting Consolidated Failure Validation...");
        JsonPath request = new JsonPath(requestBody);

        if (expectedMessage != null) {
            String actualMsg = response.getString(JsonKeys.MESSAGE);
            Assert.assertTrue(actualMsg.toLowerCase().contains(expectedMessage.toLowerCase()),
                    String.format("Message mismatch. Expected part: [%s], Actual: [%s]", expectedMessage, actualMsg));
        }

        List<Map<String, String>> actualErrors = response.getList("errors");
        List<String> expectedFields = session().getRemovedFields();

        // 2. Comprehensive Field Validation
        if (!expectedFields.isEmpty()) {
            log.info("Validating that all expected error fields {} are present", expectedFields);
            for (String fieldPath : expectedFields) {
                String fieldName = fieldPath.contains(".") ? fieldPath.substring(fieldPath.lastIndexOf(".") + 1)
                        : fieldPath;
                boolean found = false;
                if (actualErrors != null) {
                    for (Map<String, String> err : actualErrors) {
                        String errField = err.get("Field");
                        if (fieldName.equalsIgnoreCase(errField) || fieldPath.equalsIgnoreCase(errField)) {
                            found = true;
                            log.info("SUCCESS: Found error for field: {}", fieldPath);
                            // 3. Dynamic Message Validation for this specific error
                            validateDynamicErrorMessage(errField, err.get("Msg"), request);
                            break;
                        }
                    }
                }
                Assert.assertTrue(found, "Error for field '" + fieldPath + "' not found in response.");
            }
        } else if (actualErrors != null) {
            log.info("No specific expected fields found in session, validating all errors in response...");
            for (Map<String, String> err : actualErrors) {
                validateDynamicErrorMessage(err.get("Field"), err.get("Msg"), request);
            }
        }
        log.info("SUCCESS: Consolidated Failure Validation passed.");
    }

    private void validateDynamicErrorMessage(String actualField, String actualMsg, JsonPath request) {
        if (actualField == null || actualMsg == null)
            return;

        String msgLower = actualMsg.toLowerCase();

        // 1. Generic mandatory field check: Many fields return "This field is required"
        // when null or empty.
        if (msgLower.contains("required") || msgLower.contains("missing")) {
            log.info("SUCCESS: Confirmed mandatory field failure for [{}] with msg: [{}]", actualField, actualMsg);
            return;
        }

        // 2. Specific Dynamic Message Checks
        if (actualField.equalsIgnoreCase("LoanType")) {
            String sentValue = request.getString("loanType");
            String expectedSnippet = String.format(ApiMessages.ERR_LOAN_TYPE, sentValue);
            Assert.assertTrue(msgLower.contains(expectedSnippet.toLowerCase()),
                    String.format("Error Msg Mismatch for field %s!\nExpected snippet: [%s]\nActual: [%s]",
                            actualField, expectedSnippet, actualMsg));
        } else if (actualField.equalsIgnoreCase("Type") || actualField.toLowerCase().contains("kyc")) {
            String sentValue = request.getString("kyc.type");

            String sentNorm = sentValue == null ? "" : sentValue.toLowerCase().replace("'", "").replace("\"", "");

            boolean hasCanonicalPhrases = msgLower.contains("invalid kyc type")
                    && msgLower.contains("please check supported types");

            Assert.assertTrue(hasCanonicalPhrases,
                    String.format(
                            "Error Msg Mismatch for field %s! Expected message to contain canonical KYC failure phrase. Actual: [%s]",
                            actualField, actualMsg));

            if (sentNorm.length() > 0 && !(msgLower.contains(sentNorm) || msgLower.contains("'" + sentNorm + "'"))) {
                log.warn("KYC failure message didn't contain the sent KYC type [{}]. Actual message: {}", sentValue,
                        actualMsg);
            }
        } else {
            // Generic non-empty check for other fields
            Assert.assertTrue(actualMsg.length() > 0, "Empty error message for field: " + actualField);
        }
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

    public static void validateReplicationResponse(String expectedStatus, String listToValidate) {
        JsonPath response = session().getCurrentResponse();
        log.info("Validating Replication Response for list: {}", listToValidate);

        // 1. Basic Status Validation
        String actualStatus = response.getString("status");
        Assert.assertEquals(actualStatus, expectedStatus, "API Status Mismatch!");

        // 2. Get Source Data from Session to verify against Response
        String sourceAppFormId = (String) getValue("sourceAppFormId");
        Object sourceApplicantId = getValue("sourceApplicantId");

        // 3. Dynamic List Validation
        if ("success".equalsIgnoreCase(listToValidate)) {
            List<Map<String, Object>> successList = response.getList("success");
            Assert.assertNotNull(successList, "Expected 'success' list but it was null");
            Assert.assertFalse(successList.isEmpty(), "Success list is empty!");

            for (Map<String, Object> item : successList) {
                validateCommonFields(item, sourceAppFormId, sourceApplicantId);
                log.info("Successfully validated replication for AppFormId: {}", item.get("appFormId"));
            }

        } else if ("skipped".equalsIgnoreCase(listToValidate)) {
            List<Map<String, Object>> skippedList = response.getList("skipped");
            Assert.assertNotNull(skippedList, "Expected 'skipped' list but it was null");
            Assert.assertFalse(skippedList.isEmpty(), "Skipped list is empty!");

            String expectedReason = "A successful bureau pull already exists for provided target and bureau details";

            for (Map<String, Object> item : skippedList) {
                validateCommonFields(item, sourceAppFormId, sourceApplicantId);
                // Reason Validation
                Assert.assertEquals(item.get("reason"), expectedReason, "Skipped reason mismatch!");
                log.info("Validated SKIPPED replication for AppFormId: {} with correct reason.", item.get("appFormId"));
            }
        }
    }

    /**
     * Helper method to validate common fields across success and skipped lists
     */
    private static void validateCommonFields(Map<String, Object> item, String expectedAppId, Object expectedApplicantId) {
        Assert.assertNotNull(item.get("appFormId"), "appFormId is null in response");
        Assert.assertNotNull(item.get("applicantId"), "applicantId is null in response");
    }
}