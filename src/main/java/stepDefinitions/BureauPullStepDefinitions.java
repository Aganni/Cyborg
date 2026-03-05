package stepDefinitions;

import base.BaseClass;
import helpers.CsvMetadataReader;
import helpers.ReadDataFromJson;
import helpers.ResponseValidator;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.path.json.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orchestrator.BureauEngineOrchestrator;
import org.testng.Assert;

import java.util.List;

public class BureauPullStepDefinitions extends BaseClass {

    private static final Logger log = LoggerFactory.getLogger(BureauPullStepDefinitions.class);

    @When("KSF update all mandatory fields to empty string in payload based on {string}")
    public void updateMandatoryFieldsToEmpty(String csvFile) {
        String currentPayload = BureauEngineOrchestrator.getCurrentPayload();
        List<CsvMetadataReader.FieldMetadata> metadata = CsvMetadataReader.readMetadata(csvFile);

        for (CsvMetadataReader.FieldMetadata field : metadata) {
            if (field.isMandatory()) {
                if ("Integer".equalsIgnoreCase(field.getExpectedType())) {
                    log.info("Updating mandatory integer field {} to 0 (type-aware empty)", field.getField());
                    currentPayload = ReadDataFromJson.updateJsonWithPath(currentPayload, field.getField(), 0);
                } else {
                    log.info("Updating mandatory field {} to empty string", field.getField());
                    currentPayload = ReadDataFromJson.updateJsonWithPath(currentPayload, field.getField(), "");
                }
            }
        }
        BureauEngineOrchestrator.setCurrentPayload(currentPayload);
    }

    @When("KSF update all mandatory fields to null in payload based on {string}")
    public void updateMandatoryFieldsToNull(String csvFile) {
        String currentPayload = BureauEngineOrchestrator.getCurrentPayload();
        List<CsvMetadataReader.FieldMetadata> metadata = CsvMetadataReader.readMetadata(csvFile);

        for (CsvMetadataReader.FieldMetadata field : metadata) {
            if (field.isMandatory()) {
                log.info("Updating mandatory field {} to null", field.getField());
                currentPayload = ReadDataFromJson.updateJsonWithPath(currentPayload, field.getField(), null);
            }
        }
        BureauEngineOrchestrator.setCurrentPayload(currentPayload);
    }

    @When("KSF update mandatory integer fields with string in payload based on {string}")
    public void updateMandatoryIntegerFieldsWithString(String csvFile) {
        String currentPayload = BureauEngineOrchestrator.getCurrentPayload();
        List<CsvMetadataReader.FieldMetadata> metadata = CsvMetadataReader.readMetadata(csvFile);

        for (CsvMetadataReader.FieldMetadata field : metadata) {
            if (field.isMandatory() && "Integer".equalsIgnoreCase(field.getExpectedType())) {
                log.info("Updating mandatory integer field {} to 'INVALID_STRING' (intentional type mismatch)",
                        field.getField());
                currentPayload = ReadDataFromJson.updateJsonWithPath(currentPayload, field.getField(),
                        "INVALID_STRING");
            }
        }
        BureauEngineOrchestrator.setCurrentPayload(currentPayload);
    }

    @When("KSF remove all non-mandatory fields from payload based on {string}")
    public void removeNonMandatoryFields(String csvFile) {
        String currentPayload = BureauEngineOrchestrator.getCurrentPayload();
        List<CsvMetadataReader.FieldMetadata> metadata = CsvMetadataReader.readMetadata(csvFile);

        for (CsvMetadataReader.FieldMetadata field : metadata) {
            if (!field.isMandatory()) {
                log.info("Removing non-mandatory field {}", field.getField());
                currentPayload = ReadDataFromJson.removeJsonPath(currentPayload, field.getField());
            }
        }
        BureauEngineOrchestrator.setCurrentPayload(currentPayload);
    }

    @Then("Validate BureauEngine response is 200 and check no null values")
    public void validateSuccessAndNoNulls() {
        JsonPath response = BureauEngineOrchestrator.getBureauEngineResponse();
        log.info("Validating response for any null or empty values...");
        ResponseValidator.assertNoNullValues(response);
    }

    @Then("Validate BureauEngine error response status {string} and message {string}")
    public void validateErrorStatusAndMessage(String status, String message) {
        JsonPath response = BureauEngineOrchestrator.getBureauEngineResponse();
        Assert.assertEquals(response.getString("status"), status, "Status mismatch");
        Assert.assertTrue(response.getString("message").toLowerCase().contains(message.toLowerCase()),
                String.format("Message mismatch. Expected part: '%s', Actual: '%s'", message,
                        response.getString("message")));
    }

    @And("KSF hit the BureauEngine Api with refactored payload expecting {int}")
    public void ksfHitApi(int statusCode) throws Exception {
        BureauEngineOrchestrator.sendRequest("GenericBureauPull", statusCode);
    }
}
