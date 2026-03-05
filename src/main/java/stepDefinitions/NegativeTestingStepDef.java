package stepDefinitions;

import base.BaseClass;
import helpers.ReadDataFromJson;
import helpers.JsonMetadataReader;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.path.json.JsonPath;
import orchestrator.BureauEngineOrchestrator;
import org.testng.Assert;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class NegativeTestingStepDef extends BaseClass {

    @When("KSF remove all mandatory fields from the payload based on {string}")
    public void ksfRemoveAllMandatoryFields(String metadataFile) {
        String currentPayload = BureauEngineOrchestrator.getCurrentPayload();
        if (currentPayload == null) {
            throw new RuntimeException("No active payload found to remove fields from.");
        }

        List<JsonMetadataReader.FieldMetadata> metadata = JsonMetadataReader.readMetadata(metadataFile);
        List<String> removed = new ArrayList<>();

        for (JsonMetadataReader.FieldMetadata field : metadata) {
            if (field.isMandatory()) {
                String scenario = field.getTestScenario() != null ? field.getTestScenario().toUpperCase() : "REMOVE";
                log.info("Processing mandatory field: {} with scenario: {}", field.getField(), scenario);

                if ("REMOVE".equals(scenario)) {
                    currentPayload = ReadDataFromJson.removeJsonPath(currentPayload, field.getField());
                } else if ("EMPTY".equals(scenario)) {
                    currentPayload = ReadDataFromJson.updateJsonWithPath(currentPayload, field.getField(), "");
                }
                removed.add(field.getField());
            }
        }

        BureauEngineOrchestrator.setCurrentPayload(currentPayload);
        session().setRemovedFields(removed);
    }

    @Then("Validate BureauEngine error response for missing mandatory fields")
    public void validateErrorResponseForMissingFields() {
        JsonPath response = BureauEngineOrchestrator.getBureauEngineResponse();
        List<String> expectedFields = session().getRemovedFields();

        // Extract errors from response
        List<Map<String, String>> actualErrors = response.getList("errors");
        Assert.assertNotNull(actualErrors, "Errors array is missing in response");

        log.info("Validating {} expected missing fields", expectedFields.size());

        for (String expected : expectedFields) {
            boolean found = false;
            // The path might be nested (e.g., kyc.phone), but the error might just say
            // "Phone"
            // We'll perform a smart matching: check if the expected path ends with the
            // error field, case-insensitive
            String expectedLeaf = expected.contains(".") ? expected.substring(expected.lastIndexOf(".") + 1) : expected;

            for (Map<String, String> error : actualErrors) {
                String errorField = error.get("Field");
                String errorMsg = error.get("Msg");

                if (errorField.equalsIgnoreCase(expectedLeaf) || errorField.equalsIgnoreCase(expected)) {
                    found = true;
                    Assert.assertTrue(
                            errorMsg.toLowerCase().contains("required") || errorMsg.toLowerCase().contains("missing"),
                            String.format("Error message for field '%s' does not indicate requiredness: %s", expected,
                                    errorMsg));
                    break;
                }
            }
            Assert.assertTrue(found,
                    "Expected error for field '" + expected + "' was not found in response: " + response.prettify());
        }

        log.info("Successfully validated all missing mandatory fields.");
    }

    @Then("Validate BureauEngine error response status {string} and message {string}")
    public void validateErrorStatusAndMessage(String status, String message) {
        JsonPath response = BureauEngineOrchestrator.getBureauEngineResponse();
        Assert.assertEquals(response.getString("status"), status, "Status mismatch");
        Assert.assertTrue(response.getString("message").toLowerCase().contains(message.toLowerCase()),
                String.format("Message mismatch. Expected part: '%s', Actual: '%s'", message,
                        response.getString("message")));
    }
}
