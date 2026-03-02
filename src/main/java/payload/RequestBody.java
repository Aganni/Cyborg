package payload;

public class RequestBody {

    public static String premiumDetails(String insurancePlatform, String planCode, String productCode, String appFormId, int sumInsured, int loanTenure) {

        return "{\n" +
                "  \"plans\": [\n" +
                "    {\n" +
                "      \"insurancePlatform\": \""+ insurancePlatform +"\",\n" +
                "      \"products\": [\n" +
                "        {\n" +
                "          \"planCode\": \""+ planCode +"\",\n" +
                "          \"productCode\": \""+ productCode +"\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"referenceIdType\": \"APPFORMID\",\n" +
                "  \"referenceId\": \""+ appFormId +"\",\n" +
                "  \"sumInsured\": "+ sumInsured +",\n" +
                "  \"loanTenure\": "+ loanTenure +"\n" +
                "}";
    }
}
