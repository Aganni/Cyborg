package helpers;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.List;

public class CsvMetadataReader {

    private static final Logger log = LoggerFactory.getLogger(CsvMetadataReader.class);

    @Data
    public static class FieldMetadata {
        @CsvBindByName(column = "Field")
        private String field;

        @CsvBindByName(column = "Description")
        private String description;

        @CsvBindByName(column = "Mandatory")
        private String mandatory;

        @CsvBindByName(column = "ExpectedType")
        private String expectedType;

        @CsvBindByName(column = "TestScenario")
        private String testScenario;

        public String getField() {
            return field;
        }

        public String getExpectedType() {
            return expectedType;
        }


        public boolean isMandatory() {
            return "Y".equalsIgnoreCase(mandatory) || "true".equalsIgnoreCase(mandatory);
        }
    }

    public static List<FieldMetadata> readMetadata(String fileName) {
        String filePath = Paths.get("src", "main", "resources", "bureauPull", fileName).toString();
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
            log.error("Failed to read metadata CSV: {}", filePath, e);
            throw new RuntimeException("Failed to read metadata CSV: " + filePath, e);
        }
    }
}
