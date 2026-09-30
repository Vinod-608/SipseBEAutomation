package org.jarfinApiBackendAutomation.utils;

import static org.jarfinApiBackendAutomation.dbConfiguration.DBConstants.DB_JARFIN;
import static org.jarfinApiBackendAutomation.dbConfiguration.DBConstants.SIPSE_MOCK_RESPONSES_COLLECTION;
import static org.jarfinApiBackendAutomation.dbConfiguration.DataBaseFactory.jarFinMongo;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

@Slf4j
public class MockDataSeeder {

    private MockDataSeeder() {}

    public static void insertMockBankLookupFp(String phoneNumber) {
        Document response =
                new Document("status", "success")
                        .append("accountHolderName", "Mohan Mock")
                        .append("accountNumber", "82335481193")
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
}
