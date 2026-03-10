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

    // Explicit Getters and Setters for compatibility
    public String getCurrentPayload() {
        return currentPayload;
    }

    public void setCurrentPayload(String currentPayload) {
        this.currentPayload = currentPayload;
    }

    public JsonPath getCurrentResponse() {
        return currentResponse;
    }

    public void setCurrentResponse(JsonPath currentResponse) {
        this.currentResponse = currentResponse;
    }

    public List<String> getRemovedFields() {
        return removedFields;
    }

    public void setRemovedFields(List<String> removedFields) {
        this.removedFields = removedFields;
    }

    public String getBureauExternalUploadPayload() {
        return bureauExternalUploadPayload;
    }

    public void setBureauExternalUploadPayload(String bureauExternalUploadPayload) {
        this.bureauExternalUploadPayload = bureauExternalUploadPayload;
    }

    public JsonPath getBureauEngineResponse() {
        return bureauEngineResponse;
    }

    public void setBureauEngineResponse(JsonPath bureauEngineResponse) {
        this.bureauEngineResponse = bureauEngineResponse;
    }

    public JsonPath getExternalBureauResponse() {
        return externalBureauResponse;
    }

    public void setExternalBureauResponse(JsonPath externalBureauResponse) {
        this.externalBureauResponse = externalBureauResponse;
    }

    public Integer getLastStatusCode() {
        return lastStatusCode;
    }

    public void setLastStatusCode(Integer lastStatusCode) {
        this.lastStatusCode = lastStatusCode;
    }

    public Integer getExternalScoreSent() {
        return externalScoreSent;
    }

    public void setExternalScoreSent(Integer externalScoreSent) {
        this.externalScoreSent = externalScoreSent;
    }

    public boolean isExternalParse() {
        return externalParse;
    }

    public void setExternalParse(boolean externalParse) {
        this.externalParse = externalParse;
    }

    public String getBureauDocId() {
        return bureauDocId;
    }

    public void setBureauDocId(String bureauDocId) {
        this.bureauDocId = bureauDocId;
    }

    public String getBureauDocContent() {
        return bureauDocContent;
    }

    public void setBureauDocContent(String bureauDocContent) {
        this.bureauDocContent = bureauDocContent;
    }

    public String getBureauDocPresignedUrl() {
        return bureauDocPresignedUrl;
    }

    public void setBureauDocPresignedUrl(String bureauDocPresignedUrl) {
        this.bureauDocPresignedUrl = bureauDocPresignedUrl;
    }

    public String getBureauPullPayload() {
        return bureauPullPayload;
    }

    public void setBureauPullPayload(String bureauPullPayload) {
        this.bureauPullPayload = bureauPullPayload;
    }

    public LinkedHashMap<String, Object> getData() {
        return data;
    }

    public void setData(LinkedHashMap<String, Object> data) {
        this.data = data;
    }
}