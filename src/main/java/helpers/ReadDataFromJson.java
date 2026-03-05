package helpers;

import base.BaseClass;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.path.json.JsonPath;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

public class ReadDataFromJson {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String startVKYCRequest() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = "";

        try {
            FileReader file = new FileReader("src/test/Resources/startVKYCRequest.json");
            Map<String, Object> jsonMap = objectMapper.readValue(file, new TypeReference<Map<String, Object>>() {
            });

            Object appFormId = jsonMap.get("appFormId");
            Object lpc = jsonMap.get("lpc");
            Object groupId = jsonMap.get("groupId");
            Object applicantId = jsonMap.get("applicantId");

            requestBody = objectMapper.writeValueAsString(jsonMap);

        } catch (FileNotFoundException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        }

        // return
        // Files.readAllBytes(Paths.get("src/test/Resources/startVKYCRequest.json")).toString();
        // src/test/Resources/expectedResult/qa2.json
        return requestBody;
    }

    public static Object readFileReportingDetailsList(String jsonPath) throws Exception {
        JSONParser jsonParser = new JSONParser();
        Object obj = jsonParser.parse(jsonPath);
        JSONObject jsonObject = (JSONObject) obj;
        return jsonObject.get("fileReportingDetailsList");
    }

    public static String getIdFromJson() throws Exception {
        JSONParser jsonParser = new JSONParser();
        FileReader fileReader = new FileReader("src/test/resources/silverSurfer/premiumDetailsRequest.json");
        Object obj = jsonParser.parse(fileReader);
        JsonPath jsonPath = new JsonPath(String.valueOf(obj));
        String appFormID = jsonPath.get(BaseClass.environment + ".appForm").toString();
        return appFormID;
    }

    public static String getksfPolicyId(String productCode, String lpc) throws Exception {
        JSONParser jsonParser = new JSONParser();
        FileReader fileReader = new FileReader("src/test/resources/silverSurfer/ksfPolicyId.json");
        Object obj = jsonParser.parse(fileReader);
        JsonPath jsonPath = new JsonPath(String.valueOf(obj));
        String ksfPolicyId = jsonPath
                .get(BaseClass.environment + ".ksfPolicyId" + productCode.toUpperCase() + lpc.toUpperCase()).toString();
        return ksfPolicyId;
    }

    public static String getApplicantId(String productCode) throws Exception {
        JSONParser jsonParser = new JSONParser();
        FileReader fileReader = new FileReader("src/test/resources/silverSurfer/applicantId.json");
        Object obj = jsonParser.parse(fileReader);
        JsonPath jsonPath = new JsonPath(String.valueOf(obj));
        return jsonPath.get(BaseClass.environment + ".applicantId" + productCode).toString();
    }

    public static String parseJsonToString(String payload) throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        FileReader fileReader = new FileReader(payload);
        Object obj = jsonParser.parse(fileReader);
        JSONObject jsonObject = (JSONObject) obj;
        return jsonObject.toString();
    }

    public static String updateJsonWithPath(String requestBody, String path, Object value) {
        try {
            ObjectNode root;
            if (requestBody == null || requestBody.trim().isEmpty()) {
                root = mapper.createObjectNode();
            } else {
                JsonNode node = mapper.readTree(requestBody);
                if (node instanceof ObjectNode) {
                    root = (ObjectNode) node;
                } else {
                    // If base request is not an object, we cannot safely set a path inside it
                    throw new IllegalArgumentException(
                            "Base request must be a JSON object to set a path: " + requestBody);
                }
            }

            if (path == null || path.isEmpty()) {
                throw new IllegalArgumentException("Path must be a non-empty dot-separated string");
            }

            String[] parts = path.split("\\.");
            ObjectNode current = root;

            for (int i = 0; i < parts.length - 1; i++) {
                String seg = parts[i];

                // Check if segment has array index like "items[0]"
                String nodeName = seg;
                Integer index = null;
                if (seg.contains("[") && seg.endsWith("]")) {
                    int openBracket = seg.indexOf('[');
                    nodeName = seg.substring(0, openBracket);
                    String indexStr = seg.substring(openBracket + 1, seg.length() - 1);
                    try {
                        index = Integer.parseInt(indexStr);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid array index in path segment: " + seg);
                    }
                }

                JsonNode child = current.get(nodeName);

                if (index != null) {
                    // Handle ArrayNode navigation/creation
                    if (child == null || child.isNull() || !child.isArray()) {
                        // If it doesn't exist or isn't an array, we can't really "create" an array
                        // and populate up to index N intuitively without filling gaps.
                        // For simplicity, let's assume if it's missing we create a new ArrayNode
                        // and enough ObjectNodes to reach the index.
                        if (child == null || child.isNull()) {
                            child = mapper.createArrayNode();
                            current.set(nodeName, child);
                        } else {
                            // It exists but is not an array - this is a conflict.
                            // We could overwrite, but let's stick to existing logic:
                            // if type mismatch, current implementation overwrites with object.
                            // Here we overwrite with Array.
                            child = mapper.createArrayNode();
                            current.set(nodeName, child);
                        }
                    }

                    com.fasterxml.jackson.databind.node.ArrayNode arrayNode = (com.fasterxml.jackson.databind.node.ArrayNode) child;

                    // Ensure array has enough elements
                    while (arrayNode.size() <= index) {
                        arrayNode.add(mapper.createObjectNode());
                    }

                    JsonNode indexNode = arrayNode.get(index);
                    if (indexNode == null || indexNode.isNull() || !indexNode.isObject()) {
                        // Overwrite existing non-object at index with new object to continue path
                        ObjectNode newObj = mapper.createObjectNode();
                        arrayNode.set(index, newObj);
                        current = newObj;
                    } else {
                        current = (ObjectNode) indexNode;
                    }

                } else {
                    // Standard ObjectNode navigation
                    if (child == null || child.isNull() || !child.isObject()) {
                        ObjectNode newChild = mapper.createObjectNode();
                        current.set(seg, newChild);
                        current = newChild;
                    } else {
                        current = (ObjectNode) child;
                    }
                }
            }

            String last = parts[parts.length - 1];
            // Put typed value at the last segment using existing helper
            putTypedValue(current, last, value);

            return mapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update JSON by path: " + path, e);
        }
    }

    public static String removeJsonPath(String requestBody, String path) {
        try {
            JsonNode node = mapper.readTree(requestBody);
            if (!(node instanceof ObjectNode)) {
                return requestBody;
            }

            ObjectNode root = (ObjectNode) node;
            String[] parts = path.split("\\.");
            ObjectNode current = root;

            for (int i = 0; i < parts.length - 1; i++) {
                JsonNode child = current.get(parts[i]);
                if (child == null || !child.isObject()) {
                    return requestBody; // Path doesn't exist
                }
                current = (ObjectNode) child;
            }

            current.remove(parts[parts.length - 1]);
            return mapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove JSON path: " + path, e);
        }
    }

    private static void putTypedValue(ObjectNode objectNode, String key, Object value) {
        if (value instanceof Integer) {
            objectNode.put(key, (Integer) value);
        } else if (value instanceof Double) {
            objectNode.put(key, (Double) value);
        } else if (value instanceof Float) {
            objectNode.put(key, (Float) value);
        } else if (value instanceof Long) {
            objectNode.put(key, (Long) value);
        } else if (value instanceof Boolean) {
            objectNode.put(key, (Boolean) value);
        } else if (value instanceof String) {
            objectNode.put(key, (String) value);
        } else {
            objectNode.putPOJO(key, value);
        }
    }
}
