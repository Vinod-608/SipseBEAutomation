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

        // Step 4 — finprim BillDesk callback: URL comes from Step 3's form action
        String step4Url = extractFormAction(step3Body);
        if (step4Url == null || step4Url.isBlank()) {
            log.warn("Step 3 — could not extract form action for Step 4, falling back to configured endpoint");
            step4Url = finprimBdCallbackUrl;
        } else {
            log.info("Step 3 — extracted Step 4 URL from form action: {}", step4Url);
        }
        String step4FormBody = "mandate_response=" + java.net.URLEncoder.encode(mandateResponseDecoded, java.nio.charset.StandardCharsets.UTF_8)
                + "&mandate_tokenid=";
        Response step4 = apiRequests.post(RestRequest.builder()
                .headers(Map.of("Content-Type", "application/x-www-form-urlencoded",
                        "origin", "https://uat1.billdesk.com",
                        "referer", "https://uat1.billdesk.com/"))
                .rawFormBody(step4FormBody)
                .url(step4Url)
                .build());
        result.setFinprimBillDeskCallbackStatus(step4.getStatusCode());
        log.info("Step 4 finprimBillDeskCallback — status={}", step4.getStatusCode());

        // Step 5 — finprim ONDC callback: URL and body come from Step 4's response
        String step4Body = step4.getBody().asString();
        String step5Url = extractFormAction(step4Body);
        String step5FormBody = buildFormBodyFromHtml(step4Body);
        if (step5Url == null || step5Url.isBlank()) {
            log.warn("Step 4 — could not extract form action for Step 5, falling back to configured endpoint");
            step5Url = finprimOndcCallbackUrl;
        } else {
            log.info("Step 4 — extracted Step 5 URL from form action: {}", step5Url);
        }
        if (step5FormBody == null || step5FormBody.isBlank()) {
            log.warn("Step 4 — could not extract form body for Step 5, using fallback");
            step5FormBody = "status=success"
                    + "&paymentId=508868"
                    + "&failureReason=Mandate+Successful"
                    + "&failureCode="
                    + "&hash=f202e649a89734cfa807c073dcd848f5e6f49298a6b324923ec228c3e584cabe";
        }
        Response step5 = apiRequests.post(RestRequest.builder()
                .headers(Map.of("Content-Type", "application/x-www-form-urlencoded",
                        "origin", "https://cybrillarta.s.finprim.com",
                        "referer", "https://cybrillarta.s.finprim.com/"))
                .rawFormBody(step5FormBody)
                .url(step5Url)
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

    public static BillDeskMandateMockResponse billdeskNetbankingMockPayment(String redirectUrl) {
        BillDeskMandateMockResponse result = new BillDeskMandateMockResponse();

        // Step 0 — follow the GET-redirect chain to reach the actual BillDesk payment page.
        // Lumpsum flow has multiple GET hops before reaching cybrillarta (which has the data-json):
        //   changejarondc (ONDC/CSRF) → api.sandbox.cybrilla (ONDC payments) → cybrillarta (BillDesk page)
        // We loop until we find a POST form (or data-json without a GET form), then extract the body.
        String callbackSimulatorUrl = SipSeEndPoints.BILLDESK_NETBANKING_CALLBACK_SIMULATOR;
        String step1FormBody = null;

        if (redirectUrl != null && !redirectUrl.isBlank()) {
            String currentUrl = redirectUrl;
            String prevUrl = null;
            for (int hop = 0; hop <= 4; hop++) {
                Response hopResp = apiRequests.get(RestRequest.builder()
                        .headers(prevUrl == null
                                ? Map.of(
                                        "accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                                        "user-agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/154.0.0.0 Mobile Safari/537.36")
                                : Map.of(
                                        "accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                                        "referer", prevUrl,
                                        "user-agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/154.0.0.0 Mobile Safari/537.36"))
                        .url(currentUrl)
                        .build());
                String hopBody = hopResp.getBody().asString();
                log.info("Step 0 hop={} — GET {}, status={}", hop, currentUrl, hopResp.getStatusCode());
                log.info("Step 0 hop={} — body (first 300): {}", hop,
                        hopBody.length() > 300 ? hopBody.substring(0, 300) : hopBody);

                String hopMethod = extractFormMethod(hopBody);
                String hopAction = extractFormAction(hopBody);

                if ("get".equalsIgnoreCase(hopMethod) && hopAction != null && !hopAction.isBlank()) {
                    // Another GET-form redirect — build the next URL.
                    // If the action already has query params (CSRF case), use it as-is.
                    // Otherwise append the hidden-input values as query params (browser GET-form behaviour).
                    String hiddenParams = buildFormBodyFromHtml(hopBody);
                    prevUrl = currentUrl;
                    if (hopAction.contains("?") || hiddenParams.isBlank()) {
                        currentUrl = hopAction;
                    } else {
                        currentUrl = hopAction + "?" + hiddenParams;
                    }
                    log.info("Step 0 hop={} — following GET form to={}", hop, currentUrl);
                } else {
                    // POST form (or data-json page) — this is the actual BillDesk payment page.
                    if (hopAction != null && !hopAction.isBlank()) {
                        callbackSimulatorUrl = hopAction;
                        log.info("Step 0 hop={} — extracted POST form action={}", hop, callbackSimulatorUrl);
                    }
                    step1FormBody = buildFormBodyFromHtml(hopBody);
                    log.info("Step 0 hop={} — reached payment page, form body length={}", hop, step1FormBody.length());
                    break;
                }
            }
        }

        if (step1FormBody == null || step1FormBody.isBlank()) {
            log.error("Could not build Step 1 form body from redirectUrl — netbanking mock cannot proceed");
            result.setCallbackSimulatorStatus(0);
            result.setStatusCode(0);
            return result;
        }

        // Step 1 — callbackSimulator: POST merchant_code + encdata
        Response step1 = apiRequests.post(RestRequest.builder()
                .headers(Map.of(
                        "accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                        "Content-Type", "application/x-www-form-urlencoded",
                        "origin", "https://changejarondc.s.finprim.com",
                        "user-agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/154.0.0.0 Mobile Safari/537.36"))
                .rawFormBody(step1FormBody)
                .url(callbackSimulatorUrl)
                .build());
        result.setCallbackSimulatorStatus(step1.getStatusCode());
        result.setStatusCode(step1.getStatusCode());
        result.setResponseBody(step1.getBody().asString());
        log.info("Step 1 callbackSimulator — status={}", step1.getStatusCode());

        // Step 2 — callbackResponse: same form fields + bankid=SBI + status=Success
        String step1Body = step1.getBody().asString();
        String step2BaseForm = buildFormBodyFromHtml(step1Body);
        String step2FormBody = step2BaseForm
                + (step2BaseForm.isEmpty() ? "" : "&")
                + "bankid=SBI&status=Success";
        Response step2 = apiRequests.post(RestRequest.builder()
                .headers(Map.of(
                        "accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                        "Content-Type", "application/x-www-form-urlencoded",
                        "origin", "https://uat1.billdesk.com",
                        "user-agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/154.0.0.0 Mobile Safari/537.36"))
                .rawFormBody(step2FormBody)
                .url(SipSeEndPoints.BILLDESK_NETBANKING_CALLBACK_RESPONSE)
                .build());
        result.setCallbackResponseStatus(step2.getStatusCode());
        log.info("Step 2 callbackResponse — status={}", step2.getStatusCode());

        // Step 3 — bankresponse: POST with empty body to URL extracted from Step 2 form action or JS redirect
        String step2Body = step2.getBody().asString();
        log.info("Step 2 body (first 500): {}", step2Body.length() > 500 ? step2Body.substring(0, 500) : step2Body);
        String step3Url = extractRedirectUrl(step2Body);
        if (step3Url == null || step3Url.isBlank()) {
            log.error("Could not extract Step 3 bankresponse URL from Step 2 — netbanking mock cannot continue");
            return result;
        }
        log.info("Step 3 bankresponse URL={}", step3Url);
        // Disable redirect following: BillDesk bankresponse redirects to finprim, but following
        // those redirects causes a ClientProtocolException (circular/too-many redirects in Apache HttpClient).
        // We capture the immediate response to extract transaction_response and the finprim callback URL.
        Response step3 = io.restassured.RestAssured.given()
                .relaxedHTTPSValidation()
                .redirects().follow(false)
                .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("content-type", "application/x-www-form-urlencoded")
                .header("origin", "https://uat1.billdesk.com")
                .header("user-agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/154.0.0.0 Mobile Safari/537.36")
                .body("")
                .when()
                .post(step3Url)
                .then()
                .extract()
                .response();
        result.setProcessNpciRespStatus(step3.getStatusCode());
        log.info("Step 3 bankresponse — status={}", step3.getStatusCode());

        // Step 4 — finprim billdesk callback: POST transaction_response extracted from Step 3.
        // BillDesk bankresponse may return a 3xx redirect (Location header → finprim callback URL)
        // or an HTML page with a form that auto-submits. Handle both.
        String step3Body = step3.getBody().asString();
        log.info("Step 3 response body (first 500): {}", step3Body.length() > 500 ? step3Body.substring(0, 500) : step3Body);
        String step4Url = extractFormAction(step3Body);
        String step4FormBody = buildFormBodyFromHtml(step3Body);
        if (step4Url == null || step4Url.isBlank()) {
            // Fallback: redirect case — get URL from Location header
            step4Url = step3.getHeader("Location");
            log.info("Step 3 — no form action found, trying Location header: {}", step4Url);
        }
        if (step4Url == null || step4Url.isBlank()) {
            log.error("Could not extract Step 4 finprim callback URL from Step 3");
            return result;
        }
        log.info("Step 4 finprim callback URL={}", step4Url);
        Response step4 = apiRequests.post(RestRequest.builder()
                .headers(Map.of(
                        "accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                        "Content-Type", "application/x-www-form-urlencoded",
                        "origin", "https://uat1.billdesk.com",
                        "user-agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/154.0.0.0 Mobile Safari/537.36"))
                .rawFormBody(step4FormBody)
                .url(step4Url)
                .build());
        result.setFinprimBillDeskCallbackStatus(step4.getStatusCode());
        log.info("Step 4 finprimBillDeskCallback — status={}", step4.getStatusCode());

        // Step 5 — cybrilla ONDC callback: POSTed with paymentId + hash + status=success.
        // The finprim billdesk-callback page (Step 4) contains a form whose action is
        // api.sandbox.cybrilla.com/ondc/callbacks/payments with hidden fields (paymentId, hash, etc.)
        String step4Body = step4.getBody().asString();
        log.info("Step 4 response body (first 500): {}", step4Body.length() > 500 ? step4Body.substring(0, 500) : step4Body);
        String step5Url = extractRedirectUrl(step4Body);
        String step5FormBody = buildFormBodyFromHtml(step4Body);
        if (step5Url == null || step5Url.isBlank()) {
            log.warn("Step 4 — could not extract cybrilla ONDC callback URL, skipping Steps 5-7");
        } else {
            log.info("Step 5 cybrilla ONDC callback URL={}, hasFormBody={}", step5Url, !step5FormBody.isBlank());
            Response step5;
            if (step5FormBody == null || step5FormBody.isBlank()) {
                step5 = apiRequests.get(RestRequest.builder()
                        .headers(Map.of(
                                "accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                                "origin", "https://cybrillarta.s.finprim.com",
                                "referer", "https://cybrillarta.s.finprim.com/",
                                "user-agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/154.0.0.0 Mobile Safari/537.36"))
                        .url(step5Url)
                        .build());
            } else {
                step5 = apiRequests.post(RestRequest.builder()
                        .headers(Map.of(
                                "accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                                "Content-Type", "application/x-www-form-urlencoded",
                                "origin", "https://cybrillarta.s.finprim.com",
                                "referer", "https://cybrillarta.s.finprim.com/",
                                "user-agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/154.0.0.0 Mobile Safari/537.36"))
                        .rawFormBody(step5FormBody)
                        .url(step5Url)
                        .build());
            }
            result.setFinprimOndcCallbackStatus(step5.getStatusCode());
            log.info("Step 5 cybrilla ONDC callback — status={}", step5.getStatusCode());

            // Step 6 — changejarondc.i finprim ONDC callback: POSTed with transaction_id + payment_id + status=PAID.
            // The cybrilla ONDC callback response (Step 5) contains a form/redirect to
            // changejarondc.i.s.finprim.com/ondc_callback/callback.
            String step5Body = step5.getBody().asString();
            log.info("Step 5 body (first 500): {}", step5Body.length() > 500 ? step5Body.substring(0, 500) : step5Body);
            String step6Url = extractRedirectUrl(step5Body);
            String step6FormBody = buildFormBodyFromHtml(step5Body);
            if (step6Url == null || step6Url.isBlank()) {
                log.warn("Step 5 — could not extract changejarondc.i ONDC callback URL, skipping Steps 6-7");
            } else {
                log.info("Step 6 changejarondc.i ONDC callback URL={}, hasFormBody={}", step6Url, !step6FormBody.isBlank());
                Response step6;
                if (step6FormBody == null || step6FormBody.isBlank()) {
                    step6 = apiRequests.get(RestRequest.builder()
                            .headers(Map.of(
                                    "accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                                    "origin", "https://api.sandbox.cybrilla.com",
                                    "referer", "https://api.sandbox.cybrilla.com/",
                                    "user-agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/154.0.0.0 Mobile Safari/537.36"))
                            .url(step6Url)
                            .build());
                } else {
                    step6 = apiRequests.post(RestRequest.builder()
                            .headers(Map.of(
                                    "accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                                    "Content-Type", "application/x-www-form-urlencoded",
                                    "origin", "https://api.sandbox.cybrilla.com",
                                    "referer", "https://api.sandbox.cybrilla.com/",
                                    "user-agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/154.0.0.0 Mobile Safari/537.36"))
                            .rawFormBody(step6FormBody)
                            .url(step6Url)
                            .build());
                }
                result.setFinprimOndcCallbackStatus(step6.getStatusCode());
                log.info("Step 6 changejarondc.i ONDC callback — status={}", step6.getStatusCode());

                // Step 7 — final changejarondc.s ONDC callback: GET with payment_id + status=PAID.
                // The changejarondc.i response (Step 6) redirects to changejarondc.s for the
                // final client-facing ONDC payment completion callback.
                String step6Body = step6.getBody().asString();
                log.info("Step 6 body (first 500): {}", step6Body.length() > 500 ? step6Body.substring(0, 500) : step6Body);
                String step7Url = extractRedirectUrl(step6Body);
                if (step7Url == null || step7Url.isBlank()) {
                    log.warn("Step 6 — could not extract final changejarondc.s ONDC callback URL, skipping Step 7");
                } else {
                    log.info("Step 7 final ONDC callback URL={}", step7Url);
                    Response step7 = apiRequests.get(RestRequest.builder()
                            .headers(Map.of(
                                    "accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                                    "origin", "https://changejarondc.i.s.finprim.com",
                                    "referer", "https://changejarondc.i.s.finprim.com/",
                                    "user-agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/154.0.0.0 Mobile Safari/537.36"))
                            .url(step7Url)
                            .build());
                    result.setFinprimOndcCallbackStatus(step7.getStatusCode());
                    log.info("Step 7 final ONDC callback — status={}", step7.getStatusCode());
                }
            }
        }

        log.info("BillDesk Netbanking Mock Payment complete — simulator={}, callbackResponse={}, bankresponse={}, finprimBd={}, ondc={}",
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

    private static String extractRedirectUrl(String html) {
        if (html == null || html.isBlank()) return null;
        // 1. HTML <form action>
        String formAction = extractFormAction(html);
        if (formAction != null && !formAction.isBlank()) return formAction;
        int flags = java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL;
        // 2. <meta http-equiv="refresh" content="N; url=...">
        java.util.regex.Matcher meta = java.util.regex.Pattern.compile(
                "<meta[^>]+http-equiv=[\"']refresh[\"'][^>]+content=[\"'][^;]*;\\s*url=([^\"'\\s>]+)", flags).matcher(html);
        if (meta.find()) return unescapeHtml(meta.group(1).trim());
        meta = java.util.regex.Pattern.compile(
                "<meta[^>]+content=[\"'][^;]*;\\s*url=([^\"'\\s>]+)[^>]+http-equiv=[\"']refresh[\"']", flags).matcher(html);
        if (meta.find()) return unescapeHtml(meta.group(1).trim());
        // 3. JS window.location.href = '...' or window.location = '...'
        java.util.regex.Matcher js = java.util.regex.Pattern.compile(
                "window\\.location(?:\\.href)?\\s*=\\s*[\"']([^\"']+)[\"']", flags).matcher(html);
        if (js.find()) return unescapeHtml(js.group(1).trim());
        // 4. JS form.action = '...' (dynamically created form)
        java.util.regex.Matcher jsFormAction = java.util.regex.Pattern.compile(
                "form\\.action\\s*=\\s*[\"']([^\"']+)[\"']", flags).matcher(html);
        if (jsFormAction.find()) return unescapeHtml(jsFormAction.group(1).trim());
        return null;
    }

    private static String extractFormAction(String html) {
        if (html == null || html.isBlank()) return null;
        int flags = java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(
                "<form[^>]+action=[\"']([^\"']+)[\"']", flags).matcher(html);
        return m.find() ? unescapeHtml(m.group(1)) : null;
    }

    private static String extractFormMethod(String html) {
        if (html == null || html.isBlank()) return "post";
        int flags = java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(
                "<form[^>]+method=[\"']([^\"']+)[\"']", flags).matcher(html);
        return m.find() ? m.group(1).toLowerCase().trim() : "post";
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

        // Also check for <div data-json="..."> — used by finprim pages for JS auto-submit.
        // When hidden inputs are found but have empty values (e.g. lumpsum's cybrilla page),
        // data-json carries the real values; we let it fill in any empty/missing fields.
        java.util.regex.Matcher jsonDivMatcher = java.util.regex.Pattern.compile(
                "<div[^>]+data-json=[\"']([^\"']*)[\"']", flags).matcher(html);
        if (jsonDivMatcher.find()) {
            String jsonAttr = unescapeHtml(jsonDivMatcher.group(1));
            log.info("buildFormBody — found data-json div, raw JSON length={}", jsonAttr.length());
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                // Allow raw control chars (e.g. CR \r from &#13; entities in XML embedded in JSON)
                mapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
                java.util.Map<String, String> jsonMap = mapper.readValue(jsonAttr,
                        new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, String>>() {});
                // data-json values fill in any hidden-input fields that are empty/missing
                for (java.util.Map.Entry<String, String> e : jsonMap.entrySet()) {
                    fields.merge(e.getKey(), e.getValue(), (existing, fromJson) ->
                            (existing == null || existing.isBlank()) ? fromJson : existing);
                }
                log.info("buildFormBody — merged {} data-json fields, total fields={}", jsonMap.size(), fields.size());
            } catch (Exception e) {
                log.warn("buildFormBody — failed to parse data-json attribute: {}", e.getMessage());
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
