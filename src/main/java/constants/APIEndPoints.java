package constants;

public class APIEndPoints {
    public static String Start_VKYC = "https://lawliet.uat.creditsaison.xyz/api/v1/vkyc/start";
    public static String VideoCXURL = "https://videobanking.uat.creditsaison.xyz/";
    public static String Customer_Info = "https://shield.int.creditsaison.in/api/v1/appForm/";
    public static final String CKYC_INITIATE_API = "/api/v1/initiateCkycReporting";
    public static final String CKYC_FILE_REPORTING_API = "/api/v1/file/triggerReporting";
    public static String CKYC_FILE_STATUS_API = "/api/v1/file/";
    public static final String CKYC_BATCH_STATUS_API = "/api/v1/batchStatus";
    public static final String CKYC_STATUS_API = "/api/v1/status";
    public static final String PREMIUM_DETAILS = "/api/v1/premiumDetails";
    public static final String POLICY_DOCUMENT = "/api/v1/policyDocuments";
    public static final String POLICY_DETAILS = "/api/v1/policyDetails";
    public static final String APPLICANT_POLICIES_HISTORY = "api/v1/applicantPoliciesHistory";
    public static final String BUREAU_ENGINE_BUREAUPULL = "api/v1/appForm/{appformId}/applicant/{applicantId}/creditReport";
    public static final String BUREAU_PRESIGNED_S3_LINK = "api/v1/file/{docId}";
    public static final String BUREAU_ENGINE_EXTERNAL_REPORT_UPLOAD = "api/v1/appForm/{appformId}/applicant/{applicantId}/creditReport/external";
    public static final String BUREAU_REPLICATION_API = "/api/v1/bureau/replicate";
}
