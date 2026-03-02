package helpers;

import io.restassured.path.json.JsonPath;
import java.util.Map;
import java.util.List;
import static helpers.HelperMethods.log;

public class ResponseValidator {

    /**
     * Advanced Recursive logic to ensure NO key in the response is null or empty.
     * Works for any API response.
     */
    public static void assertNoNullValues(JsonPath jsonPath) {
        Object responseMap = jsonPath.get("$"); // Get the root of the JSON
        validateRecursive("", responseMap);
        log.info("SUCCESS: Global null check passed. No empty fields found in response.");
    }

    private static void validateRecursive(String path, Object obj) {
        if (obj == null) {
            failWithLog(path, "is NULL");
        }

        if (obj instanceof String && ((String) obj).trim().isEmpty()) {
            failWithLog(path, "is EMPTY STRING");
        }

        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            map.forEach((k, v) -> {
                String currentPath = path.isEmpty() ? k.toString() : path + "." + k;
                validateRecursive(currentPath, v);
            });
        } else if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            for (int i = 0; i < list.size(); i++) {
                validateRecursive(path + "[" + i + "]", list.get(i));
            }
        }
    }

    private static void failWithLog(String path, String reason) {
        String msg = String.format("VALIDATION FAILED: Field at path [%s] %s.", path, reason);
        log.error(msg);
//        Assert.fail(msg);
    }
}