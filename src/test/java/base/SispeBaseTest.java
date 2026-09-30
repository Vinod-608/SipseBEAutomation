package base;

import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.jarfinApiBackendAutomation.dbConfiguration.DataBaseFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

public class SispeBaseTest {

    @Slf4j
    public static class BaseTest {
        @BeforeSuite(alwaysRun = true)
        @Parameters("dbServer")
        public void setup(@Optional("jarfin") String dbServer) {
            log.info("======= Test Execution Started =======");
            DataBaseFactory.initializeDBConnections(dbServer);
            log.info("[MongoDB]  Connection initialized successfully");
            configureRestAssured();
        }

        private void configureRestAssured() {
            RestAssured.config =
                    RestAssuredConfig.config()
                            .httpClient(
                                    HttpClientConfig.httpClientConfig()
                                            .httpClientFactory(
                                                    () -> {
                                                        org.apache.http.impl.client
                                                                        .DefaultHttpClient
                                                                client =
                                                                        new org.apache.http.impl
                                                                                .client
                                                                                .DefaultHttpClient();
                                                        // Retry up to 3 times, including on
                                                        // NoHttpResponseException
                                                        client.setHttpRequestRetryHandler(
                                                                new DefaultHttpRequestRetryHandler(
                                                                        3, true));
                                                        return client;
                                                    }));
            log.info(
                    "[RestAssured] Configured with retry handler (3 attempts on stale connection)");
        }

        @AfterSuite(alwaysRun = true)
        public void closeConnection() {
            log.info("======= Test Execution Finished =======");
            DataBaseFactory.closeAllDBConnections();
        }
    }
}
