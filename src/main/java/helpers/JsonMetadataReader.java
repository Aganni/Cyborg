package helpers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class JsonMetadataReader {

    private static final Logger log = LoggerFactory.getLogger(JsonMetadataReader.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Data
    public static class FieldMetadata {
        private String field;
        private String description;
        private boolean mandatory;
        private String expectedType;
        private String testScenario;

        // Explicit getters for environments where Lombok might struggle
        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public boolean isMandatory() {
            return mandatory;
        }

        public void setMandatory(boolean mandatory) {
            this.mandatory = mandatory;
        }

        public String getTestScenario() {
            return testScenario;
        }

        public void setTestScenario(String testScenario) {
            this.testScenario = testScenario;
        }
    }

    public static List<FieldMetadata> readMetadata(String fileName) {
        String filePath = "src/test/resources/metadata/" + fileName;
        if (!fileName.endsWith(".json")) {
            filePath += ".json";
        }

        try {
            return mapper.readValue(new File(filePath), new TypeReference<List<FieldMetadata>>() {
            });
        } catch (Exception e) {
            log.error("Failed to read metadata JSON: {}", filePath, e);
            throw new RuntimeException("Failed to read metadata JSON: " + filePath, e);
        }
    }

    public static List<String> getMandatoryFields(String fileName) {
        return readMetadata(fileName).stream()
                .filter(FieldMetadata::isMandatory)
                .map(FieldMetadata::getField)
                .collect(Collectors.toList());
    }
}
