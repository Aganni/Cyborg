package constants;

import dynamicData.DynamicDataClass;

/**
 * Centralized repository for all JSON response and request keys.
 * Use these constants in validators to ensure framework stability.
 */
public interface JsonKeys {

    // --- Common Keys ---
    String STATUS = "status";
    String MESSAGE = "message";
    String LPC = "lpc";
    String LOAN_TYPE = "loanType";
    String CUSTOMER_ID = "customerId";

    // --- Bureau Specific Keys ---
    String BUREAU_PULL_ID = "bureauPullId";

    // --- Nested Bureau Keys (Paths) ---
    String B_VENDOR = "creditReports.vendor";
    String B_PULL_TYPE = "creditReports.pullType";
    String B_SCORE = "creditReports.score";
    String B_ENQUIRY_NUMBER = "creditReports.enquiryNumber";

    // --- Request Payload Keys (Nested) ---
    String REQ_BUREAU_VENDOR = "bureau.vendor";
    String REQ_BUREAU_PULL_TYPE = "bureau.pullType";
    String REQ_BUREAU_SCORE = "bureau.score";

    // --- External Bureau Response Keys ---
    String EXTERNAL = "external";
    String PARSE = "parse";
}