package stepDefinitions;

import base.BaseClass;
import constants.APIEndPoints;
import helpers.HelperMethods;
import helpers.ReadDataFromJson;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import payload.RequestBody;

import static helpers.DynamicDataClass.*;

public class SilverSurferStepDefinition extends BaseClass {

    @When( "KSF hits premium details api for {string} {string} {string} {int} {int}" )
    public void requestPremiumDetails (String insurancePlatform, String planCode, String productCode, int sumInsured, int loanTenure) throws Exception {
        log.info("Get premium details with insurance platform as "+insurancePlatform+", plan code as "+planCode+" and product as "+productCode+" with sum insured "+sumInsured+" and tenure "+loanTenure);
        fetchPremiumDetails(insurancePlatform, planCode, productCode, sumInsured, loanTenure);
    }
    public void fetchPremiumDetails(String insurancePlatform, String planCode, String productCode, int sumInsured, int loanTenure) throws Exception {
        String appFormId = ReadDataFromJson.getIdFromJson();
        String request = RequestBody.premiumDetails(insurancePlatform, planCode, productCode,appFormId, sumInsured, loanTenure);
        setPremiumDetails(HelperMethods.doPostStatus("silverSurferUri", request, APIEndPoints.PREMIUM_DETAILS));
    }

    @Then("verify {string} in premium details response")
    public void verifyPremiumDetails(String message){
        JsonPath preiumDetails = getPremiumDetails();
        if(preiumDetails.getString("[0].planName") == null){
            log.info("Error: "+preiumDetails.get("errorMessage").toString());
            Assert.assertEquals(preiumDetails.getString("errorMessage"),message);
        }else {
            log.info("Plan Name: " + preiumDetails.getString("[0].planName"));
            Assert.assertEquals(preiumDetails.getString("[0].planName"),message);
        }
    }

    @When("KSF hits policy document api")
    public void ksfHitsPolicyDocumentApi() throws Exception {
        JsonPath response = HelperMethods.doGetWithQueryParam("silverSurferUri","policyNumber","policy_8wayj2SxoTkYlha",APIEndPoints.POLICY_DOCUMENT);
        setPolicyDocument(response);
    }

//    @Then("verify the policy document")
//    public void verifyThePolicyDocument() throws Exception {
//        String url = getPolicyDocument().get("[0].url").toString();
//        log.info("Insurance policy document url: "+ url);
//        String filePath = "src/test/resources/silverSurfer/PolicyDocuments.pdf";
//        HelperMethods.downloadFile(url,filePath);
//        String fileContent = HelperMethods.parseDocument(filePath);
//        if(fileContent.contains("Income Protect")){
//            validatePolicyFieldsIP(fileContent);
//        } else if (fileContent.contains("Pocket loan Insurance")) {
//            validatePolicyFieldsBNPL(fileContent);
//        } else {
//            log.info("Invalid insurance policy");
//        }
//
//    }

    public static void validatePolicyFieldsIP(String fileContent){
        String loanId = fileContent.substring(fileContent.lastIndexOf("(as the case may be)")+22,fileContent.lastIndexOf("Type of Loan")-1);
        log.info("LoanId: " + loanId);
        String loanSanctionedAmount = fileContent.substring(fileContent.indexOf("Rs. ")+4,fileContent.indexOf("Loan Disbursal Date")-1);
        log.info("Loan Sanctioned Amount: " + loanSanctionedAmount);
        String masterPolicyNumber1 = fileContent.substring(fileContent.indexOf("Master Policy Number:")+22,fileContent.indexOf("Issued At:")-1);
        String masterPolicyNumber2 = fileContent.substring(fileContent.indexOf("Master Policy Number –")+23,fileContent.indexOf("Master Policy Number –")+47);
        String masterPolicyNumber3 = fileContent.substring(fileContent.indexOf("Master Policy No.")+18,fileContent.indexOf("Master Policy No.")+42);
        log.info("masterPolicyNumber1: "+ masterPolicyNumber1 + ", masterPolicyNumber2: " + masterPolicyNumber2 +", masterPolicyNumber3: "+masterPolicyNumber3);
        String emiAmount = fileContent.substring(fileContent.indexOf("the case may be) ")+18,fileContent.indexOf("Loan Sanctioned Amount")-1);
        log.info("emiAmount: " + emiAmount);
        String loanSanctionedFrom = fileContent.substring(fileContent.lastIndexOf("date*:")+12,fileContent.lastIndexOf("date*:")+22);
        log.info("loanSanctionedFrom: " + loanSanctionedFrom);
        String loanSanctionedTo = fileContent.substring(fileContent.lastIndexOf("date*:")+26,fileContent.lastIndexOf("date*:")+36);
        log.info("loanSanctionedTo: " + loanSanctionedTo);
        String totalPremium1 = fileContent.substring(fileContent.indexOf("Total Premium^ (in Rs.) ")+25,fileContent.indexOf("Mode")-1);
        log.info("totalPremium1: " + totalPremium1);
        String totalPremium2 = fileContent.substring(fileContent.indexOf("Total Premium ")+15,fileContent.indexOf("Total Premium ")+22);
        log.info("totalPremium2: " + totalPremium2);
        String totalPremium3 = fileContent.substring(fileContent.indexOf("received Rs ")+12,fileContent.indexOf("towards")-1);
        log.info("totalPremium3: " + totalPremium3);
        String netPremium1 = fileContent.substring(fileContent.indexOf("Net Premium^ (in Rs.) ")+23,fileContent.indexOf("SGST ")-1);
        log.info("netPremium1: " + netPremium1);
        String netPremium2 = fileContent.substring(fileContent.indexOf("Net Premium ")+13,fileContent.lastIndexOf("CGST")-1);
        log.info("netPremium2: " + netPremium2);
        String sgst1 = fileContent.substring(fileContent.indexOf("SGST (in Rs.) ")+15,fileContent.indexOf("CGST (in Rs.) ")-1);
        log.info("sgst1: " + sgst1);
        String cgst1 = fileContent.substring(fileContent.indexOf("CGST (in Rs.) ")+15,fileContent.indexOf("IGST (")-1);
        log.info("cgst1: " + cgst1);
        String cgst2 = fileContent.substring(fileContent.lastIndexOf("CGST ")+6,fileContent.lastIndexOf("SGST ")-1);
        log.info("cgst2: " + cgst2);
        String sgst2 = fileContent.substring(fileContent.lastIndexOf("SGST ")+6,fileContent.lastIndexOf("IGST ")-1);
        log.info("sgst2: " + sgst2);
    }

    public static void validatePolicyFieldsBNPL(String fileContent){
        String loanId = fileContent.substring(fileContent.indexOf("Loan account Number")+21,fileContent.indexOf("SUM INSURED (INR)")-5);
        log.info("LoanId: " + loanId);
        String loanSanctionAmount = fileContent.substring(fileContent.indexOf("INR ")+4,fileContent.indexOf("INR ")+11);
        log.info("loanSanctionAmount: " + loanSanctionAmount);
        String masterPolicyNumber = fileContent.substring(fileContent.indexOf("Master Policy Number:")+22,fileContent.indexOf("POLICY DETAILS")-1);
        log.info("masterPolicyNumber: " + masterPolicyNumber);
        String premiumAmount = fileContent.substring(fileContent.indexOf("Total premium")+15,fileContent.indexOf("Total premium")+22);
        log.info("premiumAmount: " + premiumAmount);
        String GST = fileContent.substring(fileContent.indexOf("Total premium")+24,fileContent.indexOf("Total premium")+30);
        log.info("GST: " + GST);
        String totalPremium = fileContent.substring(fileContent.indexOf("Total premium")+32,fileContent.indexOf("Total premium")+38);
        log.info("totalPremium: " + totalPremium);
    }

    @When ( "KSF hits policy details api for {string} {string}{string}" )
    public void ksfHitsPolicyDetailsApiFor ( String insurancePlatform , String productName, String lpc ) throws Exception {
        String ksfPolicyId = ReadDataFromJson.getksfPolicyId (productName,lpc);
        log.info("Get policy details with insurance platform as "+insurancePlatform+" "+productName+" "+lpc+" "+ksfPolicyId);
        setPolicyDetails( HelperMethods.doGetWithPolicyId("silverSurferUri",ksfPolicyId,APIEndPoints.POLICY_DETAILS));
    }

    @Then ( "verify {string}{string} in policy details response" )
    public void verifyInPolicyDetailsResponse ( String policyPurchaseStatus,String productCode ) {
        JsonPath policyDetails =getPolicyDetails();
        log.info ( "Plan Name: " + policyDetails.getString ( "productName" )+" and Policy Number: "+policyDetails.getString ( "policyNumber" ) );
        Assert.assertEquals ( policyDetails.getString ( "policyPurchaseStatus" ) , policyPurchaseStatus );
        Assert.assertEquals ( policyDetails.getString ( "policyStatus" ),"ACTIVE" );
        Assert.assertEquals ( policyDetails.getString ( "productCode" ),productCode );
    }

    @When("KSF hits policy history api for {string}")
    public void requestApplicantPoliciesHistory(String productCode) throws Exception {
        String applicantId = ReadDataFromJson.getApplicantId(productCode);
        log.info("Get policy detail for the applicant"+ applicantId);
        setpolicyDetials(HelperMethods.getApplicantPoliciesHistory("silverSurferUri", applicantId, APIEndPoints.APPLICANT_POLICIES_HISTORY));

    }
    @Then("verify {string} {string} in policy details response")
    public void verifyPolicydDetails(String planCode, String productCode)  throws Exception {
        JsonPath policyDetials = getpolicyDetials();
        if (policyDetials.getString("loans[0].policies[0].planCode") == null || policyDetials.getString("loans[0].policies[0].productCode") == null) {
            log.info("Error: " + policyDetials.get("errorMessage").toString());
            Assert.assertEquals(policyDetials.getString("errorMessage"), "Plan code or product code is missing");
        } else {
            log.info("Plan Code: " + policyDetials.getString("loans[0].policies[0].planCode"));
            Assert.assertEquals(policyDetials.getString("loans[0].policies[0].planCode"), planCode);

            log.info("Product Code: " + policyDetials.getString("loans[0].policies[0].productCode"));
            Assert.assertEquals(policyDetials.getString("loans[0].policies[0].productCode"), productCode);
        }

    }

}
