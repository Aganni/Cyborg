package constants;

public interface ApiMessages {
    String FIELDS_MISSING_MSG = "fields missing / invalid field values";

    // Error templates using %s for the dynamic value
    String ERR_KYC_TYPE = "invalid KYC type %s. Please check supported types for provided vendor and pullType ";
    String ERR_LOAN_TYPE = "invalid value %s for BureauPullRequest.LoanType. Please check supported loan types for provided vendor and pullType";

    // New constants
    String BUREAU_RECORDS_EXISTS_FOR_CREDIT_API = "bureau records for provided combination exists in db. bureau pull initiation cancelled";
    String BUREAU_RECORDS_EXISTS_FOR_EXTENAL_API = "Bureau file already exists for the given combination";
}
