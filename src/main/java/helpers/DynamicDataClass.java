package helpers;

import base.BaseClass;
import io.restassured.path.json.JsonPath;

import java.util.LinkedHashMap;


public class DynamicDataClass extends BaseClass {

    private static final ThreadLocal<LinkedHashMap<String, Object>> data = ThreadLocal.withInitial(() -> new LinkedHashMap<String, Object>());

    private DynamicDataClass() {
    }
    public static void setValue(String key, Object value) {
        data.get().put(key, value);
    }
    public static Object getValue(String key) {
        return data.get().get(key);
    }

    private static final ThreadLocal<JsonPath> premiumDetails = new ThreadLocal<>();
    private static final ThreadLocal<JsonPath> policyDocument = new ThreadLocal<>();

    public static synchronized void setPremiumDetails(JsonPath request) { premiumDetails.set(request);}
    public static synchronized JsonPath getPremiumDetails() { return premiumDetails.get();}
    public static synchronized void setPolicyDocument(JsonPath request) { policyDocument.set(request);}
    public static synchronized JsonPath getPolicyDocument() { return policyDocument.get();}
    private static final ThreadLocal<JsonPath> policyDetails = new ThreadLocal<>();
    public static synchronized void setPolicyDetails(JsonPath request){policyDetails.set ( request );}
    public static synchronized JsonPath getPolicyDetails(){return policyDetails.get();}
    private static final ThreadLocal<JsonPath> policyDetials = new ThreadLocal<>();
    public static synchronized void setpolicyDetials(JsonPath request) { policyDetials.set(request);}
    public static synchronized JsonPath getpolicyDetials() { return policyDetials.get();}


}