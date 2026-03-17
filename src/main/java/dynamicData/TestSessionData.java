package dynamicData;

import io.restassured.path.json.JsonPath;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Data
public class TestSessionData {

    private LinkedHashMap<String, Object> data = new LinkedHashMap<>();
    private String bureauDocId;
    private String bureauDocContent;
    private String bureauExternalUploadPayload;
    private String bureauDocPresignedUrl;

    // Orchestrator State
    private String currentPayload;
    private String bureauPullPayload;
    private JsonPath currentResponse;
    private JsonPath bureauEngineResponse;
    private Integer lastStatusCode;

    // External Bureau API session fields
    private JsonPath externalBureauResponse;
    private Integer externalScoreSent;
    private boolean externalParse;

    private List<String> removedFields = new ArrayList<>();

}