package dynamicData;

import io.restassured.path.json.JsonPath;
import lombok.Data;

import java.util.LinkedHashMap;

@Data
public class TestSessionData {

    private LinkedHashMap<String, Object> data = new LinkedHashMap<>();
    private String bureauDocId;
    private String bureauDocContent;
    private String bureauExternalUploadPayload;
    private String bureauDocPresignedUrl;

    // External Bureau API session fields
    private JsonPath externalBureauResponse;
    private Integer externalScoreSent;
    private boolean externalParse;

    private java.util.List<String> removedFields = new java.util.ArrayList<>();

    // Explicit getters/setters (Lombok @Data may not generate all in this project)
    public java.util.List<String> getRemovedFields() {
        return removedFields;
    }

    public void setRemovedFields(java.util.List<String> v) {
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