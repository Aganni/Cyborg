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

    // Explicit getters/setters (Supporting environment where Lombok might be
    // limited)
    public String getCurrentPayload() {
        return currentPayload;
    }

    public void setCurrentPayload(String v) {
        this.currentPayload = v;
    }

    public String getBureauPullPayload() {
        return bureauPullPayload;
    }

    public void setBureauPullPayload(String v) {
        this.bureauPullPayload = v;
    }

    public JsonPath getCurrentResponse() {
        return currentResponse;
    }

    public void setCurrentResponse(JsonPath v) {
        this.currentResponse = v;
    }

    public JsonPath getBureauEngineResponse() {
        return bureauEngineResponse;
    }

    public void setBureauEngineResponse(JsonPath v) {
        this.bureauEngineResponse = v;
    }

    public Integer getLastStatusCode() {
        return lastStatusCode;
    }

    public void setLastStatusCode(Integer v) {
        this.lastStatusCode = v;
    }

    public List<String> getRemovedFields() {
        return removedFields;
    }

    public void setRemovedFields(List<String> v) {
        this.removedFields = v;
    }

    public LinkedHashMap<String, Object> getData() {
        return data;
    }

    public void setData(LinkedHashMap<String, Object> v) {
        this.data = v;
    }

    public String getBureauDocId() {
        return bureauDocId;
    }

    public void setBureauDocId(String v) {
        this.bureauDocId = v;
    }

    public String getBureauDocContent() {
        return bureauDocContent;
    }

    public void setBureauDocContent(String v) {
        this.bureauDocContent = v;
    }

    public String getBureauExternalUploadPayload() {
        return bureauExternalUploadPayload;
    }

    public void setBureauExternalUploadPayload(String v) {
        this.bureauExternalUploadPayload = v;
    }

    public String getBureauDocPresignedUrl() {
        return bureauDocPresignedUrl;
    }

    public void setBureauDocPresignedUrl(String v) {
        this.bureauDocPresignedUrl = v;
    }

    public JsonPath getExternalBureauResponse() {
        return externalBureauResponse;
    }

    public void setExternalBureauResponse(JsonPath v) {
        this.externalBureauResponse = v;
    }

    public Integer getExternalScoreSent() {
        return externalScoreSent;
    }

    public void setExternalScoreSent(Integer v) {
        this.externalScoreSent = v;
    }

    public boolean isExternalParse() {
        return externalParse;
    }

    public void setExternalParse(boolean v) {
        this.externalParse = v;
    }
}