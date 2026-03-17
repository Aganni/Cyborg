package helpers;

import base.BaseClass;
import constants.APIEndPoints;
import constants.Constants;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.SSLConfig;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static constants.Headers.*;
import static io.restassured.RestAssured.given;

public class HelperMethods extends BaseClass {

    public static RequestSpecification request;
    public static RequestSpecification multipartrequest;
    public static String s3Url = null;

    public static RequestSpecification multipartRequestSpecifications() throws Exception {

        if (multipartrequest == null) {
            PrintStream log = new PrintStream(new FileOutputStream("logs/fileUploadLogs.txt"));
            multipartrequest = new RequestSpecBuilder().setContentType(ContentType.MULTIPART)
                    .addFilter(RequestLoggingFilter.logRequestTo(log))
                    .addFilter(ResponseLoggingFilter.logResponseTo(log)).addHeader(HEADER_NAME, HEADER_VALUE).build();

            return multipartrequest;
        }
        return multipartrequest;
    }

    public static RequestSpecification requestSpecifications() throws Exception {

        if (request == null) {
            PrintStream log = new PrintStream(new FileOutputStream("logs/workflowLogs.txt"));
            request = new RequestSpecBuilder().setContentType(ContentType.JSON)
                    .addFilter(RequestLoggingFilter.logRequestTo(log))
                    .addFilter(ResponseLoggingFilter.logResponseTo(log)).addHeader(HEADER_NAME, HEADER_VALUE).build();

            return request;
        }
        return request;
    }

    public static String currentSysDatePattern(String pattern) {
        String dateInString = new SimpleDateFormat(pattern).format(new Date());
        return dateInString;
    }

    public static String doPostCkycFile(String baseUri, String apiEndPoints) {
        File file = null;
        Response apiResponse = null;
        try {
            file = new File(
                    "src/test/resources/ckycInitiate/ckycInitiateAppforms" + environment.toLowerCase() + ".csv");
            log.info("ckyc csv file has been read");

            RestAssured.config = RestAssured.config().sslConfig(SSLConfig.sslConfig().with().relaxedHTTPSValidation());
            RequestSpecification apiRequest = given().spec(multipartRequestSpecifications())
                    .baseUri(initializeEnvironment(baseUri)).multiPart("appFormFile", file, "text/csv")
                    .queryParam("date", currentSysDatePattern("yyyy-MM-dd")).param("reportRepeatCustomer", "true");
            apiResponse = apiRequest.when().post(apiEndPoints).then().statusCode(200).extract().response();
            String uuidNumber = apiResponse.jsonPath().getString("UUID");
            DynamicDataClass.setValue(Constants.UUID_NUMBER, uuidNumber);
            return uuidNumber;
        } catch (Exception E) {
            return null;
        }
    }

    public static String doPostCkycFileProtium(String baseUri, String apiEndPoints) {
        File file = null;
        Response apiResponse = null;
        try {
            file = new File(
                    "src/test/resources/ckycInitiate/ckycInitiateAppforms" + environment.toLowerCase() + ".csv");
            log.info("ckyc csv file has been read");

            RestAssured.config = RestAssured.config().sslConfig(SSLConfig.sslConfig().with().relaxedHTTPSValidation());
            RequestSpecification apiRequest = given().spec(multipartRequestSpecifications())
                    .baseUri(initializeEnvironment(baseUri)).multiPart("appFormFile", file, "text/csv")
                    .queryParam("reportRepeatCustomer", "true");
            apiResponse = apiRequest.when().post(apiEndPoints).then().log().body().statusCode(200).extract().response();
            String requestId = apiResponse.jsonPath().getString("requestId");
            DynamicDataClass.setValue(Constants.REQUEST_ID, requestId);
            return requestId;
        } catch (Exception E) {
            return null;
        }
    }

    public static JsonPath lamdaPost(String apiEndPoint, String requestBody) throws Exception {

        hmacSignedPath = "/" + environment + apiEndPoint;
        JsonPath lambdaResponse = HelperMethods.doPostLamda(requestBody, apiEndPoint, hmacSignedPath, HttpStatus.SC_OK);
        return lambdaResponse;
    }

    public static JsonPath doPostLamda(String requestBody, String apiEndPoint, String hmacSignedPath, int statusCode)
            throws Exception {
        RequestSpecification postHmacRequest = given()
                .spec(requestSpecifications())
                .baseUri(initializeEnvironment("hmacUri"))
                .header(CONTENT_TYPE, JSON_TYPE)
                .header(API_KEY, HMAC_API_KEY)
                .queryParam(HMAC_SIGNED_PATH, hmacSignedPath)
                .queryParam(API_METHOD_CALL, POST_CALL)
                .body(requestBody);

        String hmacResponse = postHmacRequest.when().post().then().statusCode(200).extract().response().asString();
        JsonPath jsonPath = new JsonPath(hmacResponse);
        String signedDate = jsonPath.getString("signedDate");
        String signGenerated = jsonPath.get("signGenerated");

        RequestSpecification postLambdaRequest = given()
                .spec(requestSpecifications())
                .baseUri(initializeEnvironment("lambdaUri"))
                .header(CONTENT_TYPE, JSON_TYPE)
                .header(USER_NAME, getUserName())
                .header(API_KEY, getXapiKey())
                .header(SIGNATURE, signGenerated)
                .queryParam(SIGNED_DATE, signedDate)
                .body(requestBody);

        Response lambdaResponse = postLambdaRequest.when().post(apiEndPoint).then().statusCode(statusCode).extract()
                .response();

        // Check for the expected status code which is 200 in this case
        if (lambdaResponse.statusCode() != statusCode) {
            String errorMessage = lambdaResponse.body().asString();
            log.error("Lambda API returned unexpected status code: " + lambdaResponse.statusCode() + ". Error Message: "
                    + errorMessage);
            throw new Exception("Error: " + errorMessage);
        }

        JsonPath postlambdaResponse = lambdaResponse.jsonPath();
        return postlambdaResponse;
    }

    public static String doGetWithUuid(String baseUri, String uuid, String apiEndPoints) throws Exception {
        RestAssured.config = RestAssured.config().sslConfig(SSLConfig.sslConfig().with().relaxedHTTPSValidation());
        RequestSpecification apiRequest = given().log().uri().spec(requestSpecifications())
                .baseUri(initializeEnvironment(baseUri)).queryParam("uuid", uuid);
        Response apiResponse = apiRequest.when().get(apiEndPoints).then().log().status().log().body().statusCode(200)
                .extract().response();

        String response = apiResponse.getBody().asString();
        return response;
    }

    public static String doGetWithRequestId(String baseUri, String requestId, String apiEndPoints) throws Exception {
        RestAssured.config = RestAssured.config().sslConfig(SSLConfig.sslConfig().with().relaxedHTTPSValidation());
        RequestSpecification apiRequest = given().log().uri().spec(requestSpecifications())
                .baseUri(initializeEnvironment(baseUri)).queryParam("detailed", true);
        Response apiResponse = apiRequest.when().get(apiEndPoints + requestId + "/status").then().log().status().log()
                .body().statusCode(200).extract().response();

        String response = apiResponse.getBody().asString();
        return response;
    }

    public static String doGetLatestS3UrlWithDate(String baseUri, String apiEndPoints) throws Exception {
        RequestSpecification apiRequest = given().log().uri().spec(requestSpecifications())
                .baseUri(initializeEnvironment(baseUri)).queryParam("date", currentSysDatePattern("yyyy-MM-dd"));
        String response = apiRequest.when().get(apiEndPoints).then().log().status().log().body().statusCode(200)
                .extract().response().asString();

        JsonPath jsonPath = new JsonPath(response);
        int s3UrllastIndex = jsonPath.getList("s3url").size() - 1;
        s3Url = jsonPath.getString("s3url[" + s3UrllastIndex + "]");
        log.info("Latest s3 url fetched: " + s3Url);
        DynamicDataClass.setValue(Constants.s3Url, s3Url);
        log.info("dynamic getvalue of s3: " + DynamicDataClass.getValue(Constants.s3Url));
        return s3Url;
    }

    public static void downloadZipFile(String fileURL) {
        String fileUrl = fileURL;
        String zipFilePath = "src/test/resources/destination/file.zip";

        try {
            downloadFile(fileUrl, zipFilePath);
            unzip();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void downloadFile(String fileUrl, String destinationPath) throws IOException {
        URL url = new URL(fileUrl);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        int responseCode = httpURLConnection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = httpURLConnection.getInputStream();
                    FileOutputStream outputStream = new FileOutputStream(destinationPath)) {

                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            log.info("File downloaded successfully.");
        } else {
            log.info("Failed to download the file. HTTP status code: " + responseCode);
        }
        httpURLConnection.disconnect();
    }

    public static void unzip() throws IOException {
        String zipFilePath = "src/test/resources/destination/file.zip";
        String destDirectory = "src/test/resources/destination";
        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryName = entry.getName();
                String filePath = destDirectory + File.separator + entryName;

                if (entry.isDirectory()) {
                    Files.createDirectories(Paths.get(filePath));
                } else {
                    Files.copy(zipFile.getInputStream(entry), Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
                }
            }
            log.info("Zip file extracted successfully.");
        }
    }

    public static void compareResults(String response, String field, String partner) throws Exception {
        List<String> lines;
        if (partner.equals("Trackwizz")) {
            lines = Files.readAllLines(
                    Paths.get("src/test/Resources/expectedResult/" + environment.toLowerCase() + "Trackwizz.csv"));
        } else {
            lines = Files.readAllLines(
                    Paths.get("src/test/Resources/expectedResult/" + environment.toLowerCase() + "Protium.csv"));
        }

        JSONArray fileReportingDetailsList = new JSONArray(response);
        for (int i = 0; i < fileReportingDetailsList.length(); i++) {
            int flag = 0;
            JSONObject details = fileReportingDetailsList.getJSONObject(i);
            for (String line : lines) {
                String[] columns = line.split(",");
                if (details.get(field).equals(columns[0])) {
                    Assert.assertEquals(details.get("status"), columns[1], "For " + details.get(field));
                    String appFormID = partner.equals("Trackwizz") ? "app_form_id" : "appFormId";
                    if (details.get("status").equals("failed")) {
                        log.info("For " + details.get(appFormID) + " CKYC reporting failed with failure reason "
                                + details.get("failure_info"));
                    } else {
                        log.info("For " + details.get(appFormID) + " CKYC reporting is Success");
                    }
                    flag = 1;
                }
            }
            if (flag == 0) {
                Assert.fail(details.get(field) + " not present in expected result sheet");
            }
        }
    }

    public static JsonPath doGet(String baseUri, String partnerLoanId, String apiEndPoints) throws Exception {

        RequestSpecification apiRequest = given().log().uri().spec(requestSpecifications())
                .baseUri(initializeEnvironment(baseUri)).pathParam("partnerLoanId", partnerLoanId);
        String apiResponse = apiRequest.when().get(apiEndPoints).then().log().status().log().body().statusCode(200)
                .extract().response().asString();

        JsonPath jsonPath = new JsonPath(apiResponse);
        return jsonPath;
    }

    public static JsonPath doPost(String baseUri, String apiEndPoints, String requestBody, int statusCode)throws Exception {
        RestAssured.config = RestAssured.config().sslConfig(SSLConfig.sslConfig().with().relaxedHTTPSValidation());
        RequestSpecification apiRequest = given().spec(requestSpecifications()).baseUri(initializeEnvironment(baseUri))
                .body(requestBody);
        Response response = apiRequest.when().post(apiEndPoints).then().statusCode(statusCode).extract().response();

//        JsonPath jsonPath = new JsonPath(apiResponse);
        return response.jsonPath();
    }

    public static JsonPath doPostStatus(String baseUri, String requestBody, String apiEndPoints) throws Exception {
        RestAssured.config = RestAssured.config().sslConfig(SSLConfig.sslConfig().with().relaxedHTTPSValidation());
        RequestSpecification apiRequest = given().spec(requestSpecifications()).baseUri(initializeEnvironment(baseUri))
                .body(requestBody);
        String apiResponse = apiRequest.when().post(apiEndPoints).then().extract().response().asString();

        JsonPath jsonPath = new JsonPath(apiResponse);
        return jsonPath;
    }

    public static JsonPath doGetWithQueryParam(String baseUri, String queryParam, String queryParamValue,
            String apiEndPoints) throws Exception {
        RestAssured.config = RestAssured.config().sslConfig(SSLConfig.sslConfig().with().relaxedHTTPSValidation());
        RequestSpecification apiRequest = given().log().uri().spec(requestSpecifications())
                .baseUri(initializeEnvironment(baseUri)).queryParam(queryParam, queryParamValue);
        String apiResponse = apiRequest.when().get(apiEndPoints).then().log().status().log().body().statusCode(200)
                .extract().response().asString();

        JsonPath jsonPath = new JsonPath(apiResponse);
        return jsonPath;
    }

    // public static String parseDocument(String pdfFilePath) {
    // // Use try-with-resources to ensure the PDDocument is closed reliably
    // if (pdfFilePath == null || pdfFilePath.isEmpty()) {
    // return null;
    // }
    // try (PDDocument document = PDDocument.load(new File(pdfFilePath))) {
    // PDFTextStripper pdfStripper = new PDFTextStripper();
    // return pdfStripper.getText(document);
    // } catch (IOException e) {
    // // log the error and return null so callers can handle failure
    // log.error("Failed to parse PDF document: {}", pdfFilePath, e);
    // return null;
    // }
    // }

    public static String parseHtmlDocument(String htmlFilePath) throws IOException {
        java.io.File file = new java.io.File(htmlFilePath);
        if (!file.exists()) {
            throw new IOException("HTML file not found: " + htmlFilePath);
        }
        byte[] encoded = java.nio.file.Files.readAllBytes(file.toPath());
        return new String(encoded, "UTF-8");
    }

    public static JsonPath doGetWithPolicyId(String baseUri, String ksfPolicyId, String apiEndPoints) throws Exception {
        RestAssured.config = RestAssured.config().sslConfig(SSLConfig.sslConfig().with().relaxedHTTPSValidation());
        RequestSpecification apiRequest = given().log().uri().spec(requestSpecifications())
                .baseUri(initializeEnvironment(baseUri)).queryParam("ksfPolicyId", ksfPolicyId);
        String apiResponse = apiRequest.when().get(apiEndPoints).then().log().status().log().body().statusCode(200)
                .extract().response().asString();

        JsonPath jsonPath = new JsonPath(apiResponse);
        return jsonPath;
    }

    public static JsonPath getApplicantPoliciesHistory(String baseUri, String applicantId, String apiEndPoints)
            throws Exception {
        RestAssured.config = RestAssured.config().sslConfig(SSLConfig.sslConfig().with().relaxedHTTPSValidation());
        RequestSpecification apiRequest = given().log().uri().spec(requestSpecifications())
                .baseUri(initializeEnvironment(baseUri)).queryParam("applicantId", applicantId);
        String apiResponse = apiRequest.when().get(apiEndPoints).then().log().status().log().body().statusCode(200)
                .extract().response().asString();

        JsonPath jsonPath = new JsonPath(apiResponse);
        return jsonPath;

    }

    public static JsonPath doPostBureauPull(String baseUri, String apiEndPoints, String requestBody, String appformId,
            int applicantId, int statusCode) throws Exception {
        RequestSpecification apiRequest;
        RestAssured.config = RestAssured.config().sslConfig(SSLConfig.sslConfig().with().relaxedHTTPSValidation());
        apiRequest = given().spec(requestSpecifications()).baseUri(initializeEnvironment(baseUri))
                .pathParams("appformId", appformId).pathParam("applicantId", applicantId).body(requestBody);
        String apiResponse = apiRequest.when().post(apiEndPoints).then().statusCode(statusCode).extract().response()
                .asString();
        return new JsonPath(apiResponse);
    }

    // public static JsonPath getReportResponse(String bureauType, String appformId,
    // int applicantId) {
    // JsonPath response = null;
    // try {
    //
    // response = HelperMethods.doGetwithQuery("bureauengine",
    // APIEndPoints.BUREAU_ENGINE_BUREAUPULL, appformId,
    // applicantId, bureauType);
    // // Validate response to ensure it's not null
    // } catch (Exception e) {
    // log.error("Error occurred while hitting the Get report API for applicantId:
    // {}", applicantId, e);
    // }
    // return response;
    // }

    /**
     * Fetches the credit reports list for an appForm/applicant via GET on the
     * creditReport endpoint.
     */
    public static JsonPath getCreditReports(String appformId, int applicantId) throws Exception {
        RestAssured.config = RestAssured.config().sslConfig(SSLConfig.sslConfig().with().relaxedHTTPSValidation());
        RequestSpecification apiRequest = given().spec(requestSpecifications())
                .baseUri(initializeEnvironment("bureauengine"))
                .pathParams("appformId", appformId)
                .pathParam("applicantId", applicantId);
        String apiResponse = apiRequest.when().get(APIEndPoints.BUREAU_ENGINE_BUREAUPULL)
                .then().statusCode(200).extract().response().asString();
        log.info("Fetched credit reports for appFormId: {}, applicantId: {}", appformId, applicantId);
        return new JsonPath(apiResponse);
    }

    /**
     * Gets a pre-signed S3 URL for a bureau document by its ID.
     */
    public static String getFilePresignedUrl(String docId) throws Exception {
        RestAssured.config = RestAssured.config().sslConfig(SSLConfig.sslConfig().with().relaxedHTTPSValidation());
        RequestSpecification apiRequest = given().spec(requestSpecifications())
                .baseUri(initializeEnvironment("bureauengine"))
                .pathParam("docId", docId);
        String apiResponse = apiRequest.when().get(APIEndPoints.BUREAU_PRESIGNED_S3_LINK)
                .then().statusCode(200).extract().response().asString();
        JsonPath jsonPath = new JsonPath(apiResponse);
        String fileUrl = jsonPath.getString("fileUrl");
        log.info("Got pre-signed URL for docId: {}", docId);
        return fileUrl;
    }

    /**
     * Downloads content from a URL and returns it as a String.
     */
    public static String downloadFileToString(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        int responseCode = httpURLConnection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = httpURLConnection.getInputStream();
                    ByteArrayOutputStream result = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    result.write(buffer, 0, bytesRead);
                }
                return result.toString("UTF-8");
            }
        } else {
            throw new IOException("Failed to download file. HTTP status code: " + responseCode);
        }
    }

    /**
     * Decodes raw XML content fetched from S3/pre-signed URL.
     *
     * For SOAP envelope responses (ConsumerExperian, ConsumerCibil, etc.),
     * the actual search tags are HTML-entity-encoded somewhere inside — either
     * as entity-encoded text in a wrapper element (Experian SOAP) or as
     * doubly-encoded values inside CDATA Field elements (Cibil SOAP).
     *
     * Strategy: HTML-unescape the entire SOAP string so that all encoded
     * {@code &lt;Tag&gt;value&lt;/Tag&gt;} sequences become real
     * {@code <Tag>value</Tag>}, then let the regex in validateXmlTagWithValue
     * find the tag directly — regardless of how deeply it is nested or wrapped.
     */
    public static String decodeRawXml(String rawContent) {
        if (rawContent == null) {
            return rawContent;
        }

        String trimmed = rawContent.trim();

        // Case 1: SOAP envelope (either namespace prefix works)
        if (trimmed.contains("SOAP-ENV:") || trimmed.contains("soapenv:")
                || trimmed.contains("soap:") || trimmed.contains("<soap")) {
            log.info("Content looks like a SOAP envelope. Performing full HTML-entity unescape...");
            String unescaped = htmlUnescapeAll(trimmed);
            log.info("Unescaped SOAP content. First 300 chars: {}",
                    unescaped.substring(0, Math.min(300, unescaped.length())));
            return unescaped;
        }

        // Case 2: Already looks like XML
        if (trimmed.startsWith("<") || trimmed.startsWith("<?xml")) {
            return trimmed;
        }

        // Case 3: Try Base64 decode
        try {
            byte[] decodedBytes = java.util.Base64.getDecoder().decode(trimmed);
            String decoded = new String(decodedBytes, "UTF-8");
            log.info("Content was Base64 encoded, decoded successfully. First 200 chars: {}",
                    decoded.substring(0, Math.min(200, decoded.length())));
            return decoded;
        } catch (Exception e) {
            log.info("Content is not Base64 or decoding failed, using raw content. First 200 chars: {}",
                    trimmed.substring(0, Math.min(200, trimmed.length())));
            return trimmed;
        }
    }

    /**
     * Unescapes all HTML/XML character entities in the input string:
     * named entities (&amp;lt; &amp;gt; &amp;amp; &amp;quot; &amp;apos;)
     * and numeric character references (&#34; &#xA; etc.).
     */
    private static String htmlUnescapeAll(String input) {
        if (input == null)
            return null;
        // Named entities first (order matters: &amp; must be last to avoid
        // double-replacing)
        String result = input
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .replace("&amp;", "&");

        // Decimal numeric references, e.g. &#34; &#10;
        result = replaceNumericEntities(result, java.util.regex.Pattern.compile("&#([0-9]{1,5});"), 10);
        // Hex numeric references, e.g. &#xA; &#x22;
        result = replaceNumericEntities(result, java.util.regex.Pattern.compile("&#x([0-9A-Fa-f]{1,4});"), 16);
        return result;
    }

    private static String replaceNumericEntities(String input, java.util.regex.Pattern p, int radix) {
        java.util.regex.Matcher m = p.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            try {
                int cp = Integer.parseInt(m.group(1), radix);
                m.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(String.valueOf((char) cp)));
            } catch (NumberFormatException ex) {
                m.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(m.group(0)));
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * Validates that a specific XML tag contains the expected value.
     * The regex is namespace-aware: matches both {@code <Tag>} and
     * {@code <ns:Tag>}.
     */
    public static void validateXmlTagWithValue(String vendor, String xmlTag, String expectedValue, String docType) {
        String contentToValidate = session().getBureauDocContent();
        log.info("Validating {} XML:\n{}", docType, contentToValidate);

        // Namespace-aware regex: matches <Tag> or <ns:Tag> or <ns1:Tag> etc.
        String regex = "<(?:[\\w]+:)?" + xmlTag + "[^>]*>(.*?)</(?:[\\w]+:)?" + xmlTag + ">";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex, java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher matcher = pattern.matcher(contentToValidate);

        if (matcher.find()) {
            String actualValue = matcher.group(1).trim();
            Assert.assertEquals(actualValue, expectedValue,
                    String.format("XML tag <%s> expected '%s' but found '%s'", xmlTag, expectedValue, actualValue));
            log.info("XML tag <{}> has expected value: {} for Vendor : {}", xmlTag, actualValue, vendor);
        } else {
            // Log more of the content to aid debugging
            log.error("XML tag <{}> not found in {} XML. Full content length: {}. Content snippet: {}",
                    xmlTag, docType,
                    contentToValidate != null ? contentToValidate.length() : 0,
                    contentToValidate != null
                            ? contentToValidate.substring(0, Math.min(1000, contentToValidate.length()))
                            : "null");
            Assert.fail(String.format("XML tag <%s> not found in the %s XML for vendor %s", xmlTag, docType, vendor));
        }
    }

}
