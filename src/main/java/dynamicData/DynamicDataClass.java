package dynamicData;

import constants.Constants;

import java.util.Map;

public class DynamicDataClass {

    static final ThreadLocal<TestSessionData> session = ThreadLocal.withInitial(TestSessionData::new);

    private DynamicDataClass() {}
    public static TestSessionData get() {
        return session.get();
    }
    public static void resetConstants() {
        session.remove();
    }
    public static String setValue(String key, Object value) {
        get().getData().put(key, value);
        return key;
    }
    public static Object getValue(String key) {
        return get().getData().get(key);
    }
}