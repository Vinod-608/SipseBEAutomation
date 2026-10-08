package org.jarfinApiBackendAutomation.utils;

import static org.jarfinApiBackendAutomation.dbConfiguration.DBConstants.DB_JARFIN;
import static org.jarfinApiBackendAutomation.dbConfiguration.DBConstants.SIPSE_MOCK_RESPONSES_COLLECTION;
import static org.jarfinApiBackendAutomation.dbConfiguration.DataBaseFactory.jarFinMongo;

import java.util.Map;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.jarfinApiBackendAutomation.data.common.RestRequest;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.mandate.BillDeskMandateMockResponse;
import org.jarfinApiBackendAutomation.endPoints.SipSeEndPoints;

@Slf4j
public class MockDataSeeder extends RestRequest {

    private static final ApiRequests apiRequests = new ApiRequests();
    private MockDataSeeder() {}

    public static void insertMockBankLookupFp(String phoneNumber) {
        Document response =
                new Document("status", "success")
                        .append("accountHolderName", "Mohan Mock")
                        .append("accountNumber", "82445481193")
                        .append("ifscCode", "SBIN0011311")
                        .append("type", "savings");
        insertIfAbsent(phoneNumber, "BANK_LOOKUP_FP", response);
    }

    public static void insertMockProfileLookupFp(String phoneNumber) {
        Document response =
                new Document("status", "success")
                        .append("name", "Mohan Mock")
                        .append("dateOfBirth", "1999-09-09")
                        .append("gender", "male")
                        .append("pan", "LERPC3751H")
                        .append("incomeSlab", "above_5lakh_upto_10lakh")
                        .append("occupation", "Bussiness")
                        .append("pincode", "560010");
        insertIfAbsent(phoneNumber, "PROFILE_LOOKUP_FP", response);
    }

    public static void insertMockPanLookupOnGrid(String phoneNumber) {
        Document response =
                new Document("status", "success")
                        .append("pan", "LERPC3751H")
                        .append("dob", "1999-01-02")
                        .append("name", "Mohan Mock")
                        .append("gender", "MALE");
        insertIfAbsent(phoneNumber, "PAN_LOOKUP_ONGRID", response);
    }

    private static void insertIfAbsent(String phoneNumber, String mockType, Document responseDoc) {
        Document existing =
                jarFinMongo()
                        .fetchDataMultiFilter(
                                DB_JARFIN,
                                SIPSE_MOCK_RESPONSES_COLLECTION,
                                Map.of("userId", phoneNumber, "mockType", mockType),
                                null);

        if (existing != null) {
            log.info(
                    "Mock [{}] already exists for phoneNumber={} — skipping insert",
                    mockType,
                    phoneNumber);
            return;
        }

        Document document =
                new Document("userId", phoneNumber)
                        .append("mockType", mockType)
                        .append("isEnabled", true)
                        .append("response", responseDoc);

        jarFinMongo().insertDocument(DB_JARFIN, SIPSE_MOCK_RESPONSES_COLLECTION, document);
        log.info("Inserted mock [{}] for phoneNumber={}", mockType, phoneNumber);
    }


    public static BillDeskMandateMockResponse billdeskMandateMockPayment(String redirectUrl) {
        BillDeskMandateMockResponse result = new BillDeskMandateMockResponse();

        // Extract txnId from redirectUrl to build dynamic finprim callback URLs
        String txnId = extractQueryParam(redirectUrl, "txnId");
        if (txnId != null && !txnId.isBlank()) {
            log.info("Extracted txnId={} from redirectUrl", txnId);
        } else {
            log.warn("Could not extract txnId from redirectUrl={} — finprim callbacks will omit id param", redirectUrl);
        }
        String mandateIdParam = (txnId != null && !txnId.isBlank()) ? "?type=mandate&id=" + txnId : "";
        String finprimBdCallbackUrl = SipSeEndPoints.FINPRIM_BILLDESK_CALLBACK + mandateIdParam;
        String finprimOndcCallbackUrl = SipSeEndPoints.FINPRIM_ONDC_CALLBACK + mandateIdParam;
        log.info("finprimBdCallbackUrl={}", finprimBdCallbackUrl);
        log.info("finprimOndcCallbackUrl={}", finprimOndcCallbackUrl);

        // Step 0 — follow mandateRedirectUrl to get the current mandate's signed form
        String step1FormBody = null;
        String callbackSimulatorUrl = SipSeEndPoints.BILLDESK_ENACH_CALLBACK_SIMULATOR;

        if (redirectUrl != null && !redirectUrl.isBlank()) {
            Response step0 = apiRequests.get(RestRequest.builder()
                    .headers(Map.of(
                            "accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                            "user-agent", "Mozilla/5.0 (Linux; Android 16; CPH2619 Build/BP2A.250605.015; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/150.0.7871.124 Mobile Safari/537.36"))
                    .url(redirectUrl)
                    .build());
            String step0Body = step0.getBody().asString();
            log.info("Step 0 — followed redirectUrl={}, status={}", redirectUrl, step0.getStatusCode());

            String extractedAction = extractFormAction(step0Body);
            if (extractedAction != null && !extractedAction.isBlank()) {
                callbackSimulatorUrl = extractedAction;
                log.info("Step 0 — extracted form action={}", callbackSimulatorUrl);
            }
            step1FormBody = buildFormBodyFromHtml(step0Body);
            log.info("Step 0 — built form body length={}", step1FormBody.length());
        }

        if (step1FormBody == null || step1FormBody.isBlank()) {
            log.error("Could not build Step 1 form body from redirectUrl — mandate mock cannot proceed");
            result.setCallbackSimulatorStatus(0);
            result.setStatusCode(0);
            return result;
        }

        // Step 1 — callbackSimulator: submit the current mandate's XML to get the uniqueID form
        Response step1 = apiRequests.post(RestRequest.builder()
                .headers(Map.of(
                        "accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                        "origin", "https://cybrillarta.s.finprim.com",
                        "user-agent", "Mozilla/5.0 (Linux; Android 16; CPH2619 Build/BP2A.250605.015; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/150.0.7871.124 Mobile Safari/537.36",
                        "x-requested-with", "com.sipse.app.staging"))
                .rawFormBody(step1FormBody)
                .url(callbackSimulatorUrl)
                .build());
        result.setCallbackSimulatorStatus(step1.getStatusCode());
        result.setStatusCode(step1.getStatusCode());
        result.setResponseBody(step1.getBody().asString());

        // Extract uniqueID from hidden input in the returned HTML form
        String step1Body = step1.getBody().asString();
        String uniqueID = extractHiddenInputValue(step1Body, "uniqueID");
        if (uniqueID == null || uniqueID.isBlank()) {
            log.error("uniqueID not found in Step 1 HTML — cannot continue mandate mock");
            return result;
        }
        log.info("Step 1 — extracted uniqueID={}", uniqueID);
        result.setUniqueID(uniqueID);

        // Step 2 — callbackResponse: select "Success" for the extracted uniqueID
        String step2FormBody = "uniqueID=" + uniqueID + "&status=Success";
        Response step2 = apiRequests.post(RestRequest.builder()
                .headers(Map.of("Content-Type", "application/x-www-form-urlencoded"))
                .rawFormBody(step2FormBody)
                .url(SipSeEndPoints.BILLDESK_ENACH_CALLBACK_RESPONSE)
                .build());
        result.setCallbackResponseStatus(step2.getStatusCode());
        log.info("Step 2 callbackResponse — status={}", step2.getStatusCode());

        // Extract MandateRespDoc from Step 2 response (hidden input or form field)
        String step2Body = step2.getBody().asString();
        log.info("Step 2 response body (first 500 chars): {}", step2Body.length() > 500 ? step2Body.substring(0, 500) : step2Body);
        String mandateRespDoc = extractHiddenInputValue(step2Body, "MandateRespDoc");
        if (mandateRespDoc == null || mandateRespDoc.isBlank()) {
            log.warn("MandateRespDoc not found in Step 2 HTML — full body length={}", step2Body.length());
            mandateRespDoc = "";
        } else {
            log.info("Step 2 — extracted MandateRespDoc (length={})", mandateRespDoc.length());
        }
        result.setMandateRespDoc(mandateRespDoc);

        // HTML-decode the value extracted from the HTML attribute before URL-encoding
        String mandateRespDocDecoded = unescapeHtml(mandateRespDoc);
        log.info("Step 2 — MandateRespDoc decoded length={}", mandateRespDocDecoded.length());

        // Step 3 — processnpciresp: forward the mandate response XML to BillDesk NPCI processor
        String step3FormBody = "MandateRespDoc=" + java.net.URLEncoder.encode(mandateRespDocDecoded, java.nio.charset.StandardCharsets.UTF_8)
                + "&respType=RespXML";
        Response step3 = apiRequests.post(RestRequest.builder()
                .headers(Map.of("Content-Type", "application/x-www-form-urlencoded"))
                .rawFormBody(step3FormBody)
                .url(SipSeEndPoints.BILLDESK_MANDATE_PROCESS_NPCI_RESP)
                .build());
        result.setProcessNpciRespStatus(step3.getStatusCode());
        log.info("Step 3 processNpciResp — status={}", step3.getStatusCode());

        // Extract mandate_response token from Step 3 for the finprim callback
        String step3Body = step3.getBody().asString();
        log.info("Step 3 response body (full, length={}): {}", step3Body.length(), step3Body.length() > 1000 ? step3Body.substring(0, 1000) : step3Body);
        String mandateResponse = extractHiddenInputValue(step3Body, "mandate_response");
        if (mandateResponse == null || mandateResponse.isBlank()) {
            log.warn("mandate_response not found in Step 3 HTML — body: {}", step3Body);
            mandateResponse = "";
        } else {
            log.info("Step 3 — extracted mandate_response (length={})", mandateResponse.length());
        }
        result.setMandateResponse(mandateResponse);

        // HTML-decode before URL-encoding
        String mandateResponseDecoded = unescapeHtml(mandateResponse);
        log.info("Step 3 — mandate_response decoded length={}", mandateResponseDecoded.length());

        // Step 4 — finprim BillDesk callback: deliver the mandate token via sipse gateway with txnId
        String step4FormBody = "mandate_response=" + java.net.URLEncoder.encode(mandateResponseDecoded, java.nio.charset.StandardCharsets.UTF_8)
                + "&mandate_tokenid=";
        Response step4 = apiRequests.post(RestRequest.builder()
                .headers(Map.of("Content-Type", "application/x-www-form-urlencoded"))
                .rawFormBody(step4FormBody)
                .url(finprimBdCallbackUrl)
                .build());
        result.setFinprimBillDeskCallbackStatus(step4.getStatusCode());
        log.info("Step 4 finprimBillDeskCallback — status={}", step4.getStatusCode());

        // Step 5 — finprim ONDC callback: notify ONDC of the successful mandate via sipse gateway with txnId
        String step5FormBody = "status=success"
                + "&paymentId=508868"
                + "&failureReason=Mandate+Successful"
                + "&failureCode="
                + "&hash=f202e649a89734cfa807c073dcd848f5e6f49298a6b324923ec228c3e584cabe";
        Response step5 = apiRequests.post(RestRequest.builder()
                .headers(Map.of("Content-Type", "application/x-www-form-urlencoded"))
                .rawFormBody(step5FormBody)
                .url(finprimOndcCallbackUrl)
                .build());
        result.setFinprimOndcCallbackStatus(step5.getStatusCode());
        log.info("Step 5 finprimOndcCallback — status={}", step5.getStatusCode());

        log.info("BillDesk Mandate Mock Payment complete — steps: simulator={}, callbackResponse={}, npci={}, finprimBd={}, finprimOndc={}",
                result.getCallbackSimulatorStatus(),
                result.getCallbackResponseStatus(),
                result.getProcessNpciRespStatus(),
                result.getFinprimBillDeskCallbackStatus(),
                result.getFinprimOndcCallbackStatus());
        return result;
    }

    private static String extractQueryParam(String url, String paramName) {
        if (url == null || url.isBlank()) return null;
        try {
            java.net.URI uri = new java.net.URI(url);
            String query = uri.getQuery();
            if (query == null) return null;
            for (String param : query.split("&")) {
                String[] pair = param.split("=", 2);
                if (pair.length == 2 && pair[0].equals(paramName)) {
                    return java.net.URLDecoder.decode(pair[1], java.nio.charset.StandardCharsets.UTF_8);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract query param '{}' from url={}: {}", paramName, url, e.getMessage());
        }
        return null;
    }

    private static String extractFormAction(String html) {
        if (html == null || html.isBlank()) return null;
        int flags = java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(
                "<form[^>]+action=[\"']([^\"']+)[\"']", flags).matcher(html);
        return m.find() ? unescapeHtml(m.group(1)) : null;
    }

    private static String buildFormBodyFromHtml(String html) {
        if (html == null || html.isBlank()) return "";
        int flags = java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL;
        java.util.regex.Pattern inputPattern = java.util.regex.Pattern.compile("<input([^>]*)>", flags);
        java.util.regex.Matcher inputMatcher = inputPattern.matcher(html);
        java.util.LinkedHashMap<String, String> fields = new java.util.LinkedHashMap<>();
        while (inputMatcher.find()) {
            String tag = inputMatcher.group(1);
            String name = extractAttrFromTag(tag, "name");
            String value = extractAttrFromTag(tag, "value");
            if (name != null && !name.isBlank()) {
                fields.put(name, value != null ? unescapeHtml(value) : "");
            }
        }

        // Fallback: finprim pages use <div data-json="..."> with JS auto-submit — no static <input> tags
        if (fields.isEmpty()) {
            java.util.regex.Matcher jsonDivMatcher = java.util.regex.Pattern.compile(
                    "<div[^>]+data-json=[\"']([^\"']*)[\"']", flags).matcher(html);
            if (jsonDivMatcher.find()) {
                String jsonAttr = unescapeHtml(jsonDivMatcher.group(1));
                log.info("Step 0 — found data-json div, raw JSON length={}", jsonAttr.length());
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    // Allow raw control chars (e.g. CR \r from &#13; entities in XML embedded in JSON)
                    mapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
                    java.util.Map<String, String> jsonMap = mapper.readValue(jsonAttr,
                            new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, String>>() {});
                    fields.putAll(jsonMap);
                    log.info("Step 0 — parsed {} fields from data-json", fields.size());
                } catch (Exception e) {
                    log.warn("Step 0 — failed to parse data-json attribute: {}", e.getMessage());
                }
            }
        }

        return fields.entrySet().stream()
                .map(e -> java.net.URLEncoder.encode(e.getKey(), java.nio.charset.StandardCharsets.UTF_8)
                        + "=" + java.net.URLEncoder.encode(e.getValue(), java.nio.charset.StandardCharsets.UTF_8))
                .collect(java.util.stream.Collectors.joining("&"));
    }

    private static String extractAttrFromTag(String tag, String attrName) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(
                "\\b" + java.util.regex.Pattern.quote(attrName) + "\\s*=\\s*[\"']([^\"']*)[\"']",
                java.util.regex.Pattern.CASE_INSENSITIVE).matcher(tag);
        return m.find() ? m.group(1) : null;
    }

    private static String unescapeHtml(String s) {
        if (s == null || s.isBlank()) return s == null ? "" : s;
        // Replace named entities first, then strip numeric character references
        String result = s.replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&apos;", "'");
        // Replace decimal numeric references like &#13; &#10;
        java.util.regex.Matcher dm = java.util.regex.Pattern.compile("&#(\\d+);").matcher(result);
        StringBuffer sbDec = new StringBuffer();
        while (dm.find()) {
            dm.appendReplacement(sbDec, String.valueOf((char) Integer.parseInt(dm.group(1))));
        }
        dm.appendTail(sbDec);
        result = sbDec.toString();
        // Replace hex numeric references like &#xD; &#x0A;
        java.util.regex.Matcher hm = java.util.regex.Pattern.compile("&#x([0-9a-fA-F]+);").matcher(result);
        StringBuffer sbHex = new StringBuffer();
        while (hm.find()) {
            hm.appendReplacement(sbHex, String.valueOf((char) Integer.parseInt(hm.group(1), 16)));
        }
        hm.appendTail(sbHex);
        return sbHex.toString();
    }

    private static String extractHiddenInputValue(String html, String fieldName) {
        if (html == null || html.isBlank()) return null;
        int flags = java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL;
        String quotedName = java.util.regex.Pattern.quote(fieldName);
        // name before value
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(
                "<input[^>]+name=[\"']" + quotedName + "[\"'][^>]+value=[\"']([^\"']*)[\"']", flags);
        java.util.regex.Matcher m = p.matcher(html);
        if (m.find()) return m.group(1);
        // value before name
        p = java.util.regex.Pattern.compile(
                "<input[^>]+value=[\"']([^\"']*)[\"'][^>]+name=[\"']" + quotedName + "[\"']", flags);
        m = p.matcher(html);
        if (m.find()) return m.group(1);
        // textarea fallback: <textarea name="fieldName">value</textarea>
        p = java.util.regex.Pattern.compile(
                "<textarea[^>]+name=[\"']" + quotedName + "[\"'][^>]*>(.*?)</textarea>", flags);
        m = p.matcher(html);
        if (m.find()) return m.group(1).trim();
        return null;
    }



}
