package org.jarfinApiBackendAutomation.listeners;

import io.qameta.allure.Allure;
import java.io.File;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.jarfinApiBackendAutomation.utils.AllureReportUtil;
import org.testng.IExecutionListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

@Slf4j
public class Listeners implements ITestListener, IExecutionListener {

    /** Clears stale results before each run so duplicates never accumulate in the report. */
    @Override
    public void onExecutionStart() {
        File resultsDir = new File("allure-results");
        if (resultsDir.exists()) {
            try {
                FileUtils.deleteDirectory(resultsDir);
                log.info("[Allure] Deleted allure-results directory");
            } catch (IOException e) {
                log.warn("[Allure] Could not delete allure-results directory", e);
            }
        }
    }

    /** Test-level start: before any test method in a <test> tag starts */
    @Override
    public void onStart(ITestContext context) {
        log.info("Starting Test Context: {}", context.getName());
    }

    /**
     * Renames GoldSDKTest / SilverTest Allure test case to "@Test description - DataProvider
     * scenario label"
     */
    @Override
    public void onTestStart(ITestResult result) {
        String className = result.getTestClass().getRealClass().getSimpleName();
        if (!className.equals("GoldSDKTest")
                && !className.equals("SilverTest")
                && !className.equals("DigiGoldTest")) {
            return;
        }

        Object[] params = result.getParameters();
        if (params != null && params.length > 0) {
            String scenarioName = String.valueOf(params[0]);
            String description =
                    result.getMethod().getDescription() != null
                            ? result.getMethod().getDescription()
                            : result.getMethod().getMethodName();
            Allure.getLifecycle()
                    .updateTestCase(tc -> tc.setName(description + " - " + scenarioName));
        }
    }

    /** This hook will execute on successful test */
    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("Test Passed: {}", result.getName());
        onTestEnd(result);
    }

    /** This hook will execute on test failure */
    @Override
    public void onTestFailure(ITestResult result) {
        log.error("Test Failed: {}", result.getName());
        if (result.getThrowable() != null) {
            log.error("Error: ", result.getThrowable());
        }
        onTestEnd(result);
    }

    /** Common hook for ending a test method */
    private void onTestEnd(ITestResult result) {
        log.info("Test Status: {}", result.getStatus());
        log.info("Test Ended: {}", result.getName());
        log.info("---------------------------------------");
    }

    @Override
    public void onExecutionFinish() {
        log.info("Execution finished. Generating Allure Report...");
        AllureReportUtil.generateAllureReport();
    }
}
