package helpers;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.CsvBindByName;
import lombok.Data;

import java.io.FileReader;
import java.io.Reader;
import java.util.List;
import java.util.stream.Collectors;

public class CsvMetadataReader {

    @Data
    public static class FieldMetadata {
        @CsvBindByName(column = "Field")
        private String field;

        @CsvBindByName(column = "Mandatory")
        private String mandatory;

        @CsvBindByName(column = "ExpectedType")
        private String expectedType;

        @CsvBindByName(column = "TestScenario")
        private String testScenario;

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getMandatory() {
            return mandatory;
        }

        public void setMandatory(String mandatory) {
            this.mandatory = mandatory;
        }

        public String getExpectedType() {
            return expectedType;
        }

        public void setExpectedType(String expectedType) {
            this.expectedType = expectedType;
        }

        public String getTestScenario() {
            return testScenario;
        }

        public void setTestScenario(String testScenario) {
            this.testScenario = testScenario;
        }

        public boolean isMandatory() {
            return "Y".equalsIgnoreCase(mandatory);
        }
    }

    public static List<FieldMetadata> readMetadata(String fileName) {
        String filePath = "src/test/resources/metadata/" + fileName;
        if (!fileName.endsWith(".csv")) {
            filePath += ".csv";
        }

        try (Reader reader = new FileReader(filePath)) {
            CsvToBean<FieldMetadata> csvToBean = new CsvToBeanBuilder<FieldMetadata>(reader)
                    .withType(FieldMetadata.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            return csvToBean.parse();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read metadata CSV: " + filePath, e);
        }
    }

    public static List<String> getMandatoryFields(String fileName) {
        return readMetadata(fileName).stream()
                .filter(FieldMetadata::isMandatory)
                .map(FieldMetadata::getField)
                .collect(Collectors.toList());
    }
}
