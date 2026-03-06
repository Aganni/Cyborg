package orchestrator;

import base.BaseClass;
import constants.APIEndPoints;
import constants.Constants;
import constants.JsonKeys;
import helpers.CustomResponseValidator;
import helpers.HelperMethods;
import helpers.ResponseValidator;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;

import java.util.List;
import java.util.Map;

import static helpers.DynamicDataClass.getValue;
import static helpers.ReadDataFromJson.parseJsonToString;
import static helpers.ReadDataFromJson.updateJsonWithPath;

/**
 * This class encapsulates the workflow for pulling bureau reports.
 * It handles sending requests to the BureauEngine API, validating responses,
 * and verifying the content of the generated reports.
 */
public class BureauEngineOrchestrator extends BaseClass {

    private static final String CONSUMER_EXPERIAN = "ConsumerExperian";
    private static final String BUREAU_REPORT_BASE_URL = "src/main/resources/bureauPull/";

    public static void sendBureauPullRequest(String bureauVendor, String pullType, int statusCode) throws Exception {
        buildPayload(bureauVendor, pullType);
        sendRequest(bureauVendor, statusCode);
    }

    public static void buildPayload(String bureauVendor, String pullType) {
        String payloadPath = String.format("%s%sRequestBody.json", BUREAU_REPORT_BASE_URL, bureauVendor);

        try {
            String requestBody = parseJsonToString(payloadPath);

            if (bureauVendor.equalsIgnoreCase(CONSUMER_EXPERIAN)) {
                requestBody = updateJsonWithPath(requestBody, JsonKeys.REQ_BUREAU_PULL_TYPE, pullType);
            }
            // Inject WithdrawalId for the bureauPull at withdrawal level
            Object withdrawalId = getValue(Constants.WITHDRAWAL_ID);
            if (withdrawalId != null) {
                requestBody = injectWithdrawalId(requestBody, withdrawalId.toString());
            }
            session().setBureauPullPayload(requestBody);
            session().setCurrentPayload(requestBody);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build payload for: " + bureauVendor, e);
        }
    }

    public static void sendRequest(String bureauType, int statusCode) throws Exception {
        log.info("Hitting BureauEngine API for {} BureauPull", bureauType);
        String requestBody = session().getCurrentPayload();
        if (requestBody == null) {
            throw new RuntimeException("No active bureau payload found in session.");
        }

        JsonPath response = HelperMethods.doPostBureauPull(
                "bureauengine",
                APIEndPoints.BUREAU_ENGINE_BUREAUPULL,
                requestBody,
                getValue(Constants.APP_FORM_ID).toString(), (Integer) getValue(Constants.APPLICANT_ID), statusCode);

        log.info("Got response from BureauEngine API for {} BureauPull", bureauType);
        session().setBureauEngineResponse(response);
        session().setCurrentResponse(response);
        session().setLastStatusCode(statusCode);
    }

    public static void validateBureauEngineResponse(String expectedStatus) {
        JsonPath response = session().getBureauEngineResponse();

        // Initial Status Check
        if (!expectedStatus.equalsIgnoreCase(response.getString(JsonKeys.STATUS))) {
            String msg = String.format("FAIL: Expected %s but got %s. Message: %s",
                    expectedStatus, response.getString(JsonKeys.STATUS), response.getString(JsonKeys.MESSAGE));
            log.error(msg);
            Assert.fail(msg);
        }

        CustomResponseValidator customValidator = new CustomResponseValidator();
        if (expectedStatus.equalsIgnoreCase("Success")) {
            customValidator.validateBureauResponseSyncWithRequest(response, session().getCurrentPayload());
            ResponseValidator.assertNoNullValues(response);
        } else {
            customValidator.validateFailureDetails(response, session().getCurrentPayload());
        }
        log.info("KSF: BureauEngine Validation Successful. Final Response Content:\n{}", response.prettify());
    }

    private static String injectWithdrawalId(String body, String withdrawalId) {
        return body.substring(0, body.lastIndexOf("}")) + ",\"withdrawalId\":\"" + withdrawalId + "\"}";
    }

    public static String getCurrentPayload() {
        return session().getCurrentPayload();
    }

    public static void setCurrentPayload(String payload) {
        session().setCurrentPayload(payload);
    }

    public static void sendRequestWithSameIdentifiers(int expectedStatusCode) throws Exception {
        log.info("Sending DUPLICATE request to BureauEngine...");

        // If this scenario used the external API, repeat via the external endpoint
        if (session().getBureauExternalUploadPayload() != null) {
            log.info("Duplicate request: routing to external bureau endpoint (parse={})", session().isExternalParse());
            sendExternalRequest(expectedStatusCode);
        } else {
            // Normal bureau pull duplicate
            sendRequest("DuplicateRequest", expectedStatusCode);
        }
    }

    public static void verifyBureauRequestXml(String vendor, String docType, String xmlTag, String expectedValue)
            throws Exception {

        Integer lastCode = session().getLastStatusCode();
        if (lastCode != null && lastCode != 200) {
            log.info("Skipping XML tag <{}> validation for vendor {} — last API status was {} (not 200)",
                    xmlTag, vendor, lastCode);
            return;
        }

        getDocId(vendor, docType, "xml");

        // Step 3: Get pre-signed S3 URL
        String presignedUrl = HelperMethods.getFilePresignedUrl(session().getBureauDocId());

        // Step 4: Download the XML content
        session().setBureauDocContent(HelperMethods.downloadFileToString(presignedUrl));
        log.info("Downloaded content length: {}", session().getBureauDocContent().length());

        // Decode the content (handles SOAP envelopes, Base64, plain XML)
        session().setBureauDocContent(HelperMethods.decodeRawXml(session().getBureauDocContent()));

        // Step 5: Extract the XML tag value using regex
        HelperMethods.validateXmlTagWithValue(vendor, xmlTag, expectedValue, docType);
    }

    private static void getDocId(String vendor, String docType, String docFormat) throws Exception {
        // Step 1: Fetch all credit reports
        log.info("Fetching credit reports for appFormId: {}, applicantId: {}",
                getValue(Constants.APP_FORM_ID).toString(), getValue(Constants.APPLICANT_ID));
        JsonPath reportsResponse = HelperMethods.getCreditReports(getValue(Constants.APP_FORM_ID).toString(),
                (Integer) getValue(Constants.APPLICANT_ID));

        // Step 2: Find the matching report for this vendor
        List<Map<String, Object>> reports = reportsResponse.getList("reports");
        if (reports == null || reports.isEmpty()) {
            Assert.fail("No reports found for appFormId: " + getValue(Constants.APP_FORM_ID).toString());
        }

        for (Map<String, Object> report : reports) {
            if (vendor.equalsIgnoreCase((String) report.get("bureauName"))) {
                List<Map<String, Object>> creditReports = (List<Map<String, Object>>) report.get("creditReports");
                if (creditReports != null) {
                    for (Map<String, Object> doc : creditReports) {
                        if (docType.equalsIgnoreCase((String) doc.get("type"))
                                && docFormat.equalsIgnoreCase((String) doc.get("format"))) {
                            session().setBureauDocId(doc.get("id").toString());
                            break;
                        }
                    }
                }
                break;
            }
        }

        if (session().getBureauDocId() == null) {
            Assert.fail(String.format("No %s XML document found for vendor %s", docType, vendor));
        }
        log.info("Found {} XML document ID: {}", docType, session().getBureauDocId());
    }

    public static void buildExternalPullRequestPayload(String lpc, String pullType,
            String bureauVendor, boolean parse) throws Exception {

        String docId = parse
                ? Constants.BUREAU_DOC_ID_FOR_XML_PRE_SIGN_URL
                : Constants.BUREAU_DOC_ID_FOR_JSON_PRE_SIGN_URL;
        session().setExternalParse(parse);
        session().setBureauDocPresignedUrl(HelperMethods.getFilePresignedUrl(docId));

        String payloadPath = BUREAU_REPORT_BASE_URL + "ExternalApiPayload.json";
        String requestBody = parseJsonToString(payloadPath);

        // Inject identifiers and routing fields
        requestBody = updateJsonWithPath(requestBody, "lpc", lpc);
        requestBody = updateJsonWithPath(requestBody, JsonKeys.REQ_BUREAU_PULL_TYPE, pullType);
        requestBody = updateJsonWithPath(requestBody, JsonKeys.REQ_BUREAU_VENDOR, bureauVendor);
        requestBody = updateJsonWithPath(requestBody, "responseS3Url", session().getBureauDocPresignedUrl());

        if (parse) {
            org.json.JSONObject json = new org.json.JSONObject(requestBody);
            if (json.has("bureau")) {
                org.json.JSONObject bureau = json.getJSONObject("bureau");
                bureau.remove("score");
                json.put("bureau", bureau);
            }
            requestBody = json.toString();
        } else {
            org.json.JSONObject json = new org.json.JSONObject(requestBody);
            if (json.has("bureau") && json.getJSONObject("bureau").has("score")) {
                session().setExternalScoreSent(json.getJSONObject("bureau").getInt("score"));
            }
        }

        Object withdrawalId = getValue(Constants.WITHDRAWAL_ID);
        if (withdrawalId != null) {
            requestBody = injectWithdrawalId(requestBody, withdrawalId.toString());
        }
        session().setBureauExternalUploadPayload(requestBody);
        log.info("External pull payload built for vendor={}, parse={}, lpc={}: {}",
                bureauVendor, parse, lpc, requestBody);
    }

    public static void sendExternalRequest(int expectedStatusCode) throws Exception {
        if (session().getBureauExternalUploadPayload() == null) {
            throw new RuntimeException("No external bureau payload found. Call buildExternalPullRequestPayload first.");
        }
        boolean parse = session().isExternalParse();
        log.info("Hitting External BureauEngine API, parse={}", parse);

        io.restassured.RestAssured.config = io.restassured.RestAssured.config()
                .sslConfig(io.restassured.config.SSLConfig.sslConfig().with().relaxedHTTPSValidation());

        String rawResponse = io.restassured.RestAssured.given()
                .spec(HelperMethods.requestSpecifications())
                .baseUri(HelperMethods.initializeEnvironment("bureauengine"))
                .pathParam("appformId", getValue(Constants.APP_FORM_ID))
                .pathParam("applicantId", getValue(Constants.APPLICANT_ID))
                .queryParam("parse", parse)
                .contentType("application/json")
                .body(session().getBureauExternalUploadPayload())
                .when()
                .post(APIEndPoints.BUREAU_ENGINE_EXTERNAL_REPORT_UPLOAD)
                .then()
                .statusCode(expectedStatusCode)
                .extract().response().asString();

        JsonPath jsonResponse = new JsonPath(rawResponse);
        session().setExternalBureauResponse(jsonResponse);
        session().setBureauEngineResponse(jsonResponse);
        session().setCurrentResponse(jsonResponse);
        session().setLastStatusCode(expectedStatusCode);
        log.info("External BureauEngine response: {}", rawResponse);
    }

    public static void validateExternalBureauResponse(String expectedVendor) {
        JsonPath response = session().getExternalBureauResponse();
        if (response == null) {
            throw new IllegalStateException("External bureau response not found in session.");
        }

        Assert.assertEquals(response.getString(JsonKeys.STATUS), "Success",
                "External bureau status mismatch. Full response: " + response.prettify());
        Assert.assertTrue(response.getBoolean(JsonKeys.EXTERNAL),
                "Expected 'external' to be true in response.");

        String actualVendor = response.getString(JsonKeys.B_VENDOR);
        Assert.assertEquals(actualVendor, expectedVendor,
                String.format("Vendor mismatch: expected=%s, actual=%s", expectedVendor, actualVendor));

        Integer responseScore = response.get(JsonKeys.B_SCORE);
        Assert.assertNotNull(responseScore, "creditReports.score is null in external bureau response.");

        if (!session().isExternalParse()) {
            Integer sentScore = session().getExternalScoreSent();
            Assert.assertNotNull(sentScore, "externalScoreSent was not stored — check payload building.");
            Assert.assertEquals(responseScore, sentScore,
                    String.format("Score mismatch: sent=%d, got=%d", sentScore, responseScore));
            log.info("[External parse=false] Score validation passed: sent={}, got={}", sentScore, responseScore);
        } else {
            log.info("[External parse=true] Score from parsed XML: {}", responseScore);
        }
        log.info("External BureauEngine validation PASSED. Vendor={}, Score={}", actualVendor, responseScore);
    }
}
