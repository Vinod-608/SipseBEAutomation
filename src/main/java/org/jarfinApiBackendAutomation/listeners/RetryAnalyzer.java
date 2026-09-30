package org.jarfinApiBackendAutomation.listeners;

import io.qameta.allure.Flaky;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

@Slf4j
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final int MAX_RETRY_COUNT = 2;
    private static final ConcurrentHashMap<String, Integer> retryCountMap =
            new ConcurrentHashMap<>();

    @Override
    public boolean retry(ITestResult result) {
        if (result.isSuccess()) return false;

        int attempt = retryCountMap.merge(buildKey(result), 1, Integer::sum);
        if (attempt <= MAX_RETRY_COUNT) {
            log.info("Retrying test: {} | Attempt: {}", result.getName(), attempt);
            result.setStatus(ITestResult.SKIP);
            return true;
        }

        boolean isFlaky =
                result.getMethod()
                        .getConstructorOrMethod()
                        .getMethod()
                        .isAnnotationPresent(Flaky.class);
        result.setStatus(isFlaky ? ITestResult.SKIP : ITestResult.FAILURE);
        return false;
    }

    private String buildKey(ITestResult result) {
        Object[] params = result.getParameters();
        int identityKey = 0;
        for (Object p : params) {
            identityKey = identityKey * 31 + System.identityHashCode(p);
        }
        return result.getMethod().getRealClass().getName()
                + "#"
                + result.getMethod().getMethodName()
                + "#"
                + identityKey;
    }
}
