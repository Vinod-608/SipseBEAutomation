package sipSe.withdrawl;

import org.jarfinApiBackendAutomation.data.responseModel.sipSe.Withdrawl.LockerFeatureSectionResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.Withdrawl.PreRedemptionSummaryResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.Withdrawl.WithdrawlInitiateResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.Withdrawl.WithdrawlStatusResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.Withdrawl.WithdrawlSubmitResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.Withdrawl.SchemeFolioBalanceResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.Withdrawl.SchemeWiseBalanceResponse;
import org.jarfinApiBackendAutomation.utils.ApiAssertions;
import org.testng.asserts.SoftAssert;

import static org.apache.http.HttpStatus.SC_OK;

public class WithdrawlValidation extends ApiAssertions {

    public WithdrawlValidation(SoftAssert softAssert) {
        super(softAssert);
    }

    public void assertLockerDetailsSuccess(LockerFeatureSectionResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "Locker Feature Section")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "Locker Feature Section — expected success=true");
        softAssert.assertNotNull(response.getData(), "Locker Feature Section — data must not be null");
        if (response.getData() != null) {
            softAssert.assertEquals(
                    response.getData().getFeatureSection(),
                    "LOCKER",
                    "Locker Feature Section — featureSection must be LOCKER");
        }
    }

    public void assertSchemeWiseBalanceSuccess(SchemeWiseBalanceResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "Scheme Wise Balance")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "Scheme Wise Balance — expected success=true");
        softAssert.assertNotNull(response.getData(), "Scheme Wise Balance — data must not be null");
        if (response.getData() != null) {
            softAssert.assertNotNull(
                    response.getData().getSchemeWiseBalances(),
                    "Scheme Wise Balance — schemeWiseBalances must not be null");
        }
    }

    public void assertSchemeFolioBalanceSuccess(SchemeFolioBalanceResponse response, String fundSchemeId) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "Scheme Folio Balance")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "Scheme Folio Balance — expected success=true");
        softAssert.assertNotNull(response.getData(), "Scheme Folio Balance — data must not be null");
        if (response.getData() != null) {
            softAssert.assertNotNull(
                    response.getData().getFolioBalances(),
                    "Scheme Folio Balance — folioBalances must not be null for fundSchemeId=" + fundSchemeId);
        }
    }

    public void assertPreRedemptionSummarySuccess(PreRedemptionSummaryResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "Pre Redemption Summary")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "Pre Redemption Summary — expected success=true");
        softAssert.assertNotNull(response.getData(), "Pre Redemption Summary — data must not be null");
        if (response.getData() != null) {
            softAssert.assertNotNull(
                    response.getData().getFundSchemeId(),
                    "Pre Redemption Summary — fundSchemeId must not be null");
            softAssert.assertNotNull(
                    response.getData().getUserFolioId(),
                    "Pre Redemption Summary — userFolioId must not be null");
            softAssert.assertNotNull(
                    response.getData().getRedeemableAmount(),
                    "Pre Redemption Summary — redeemableAmount must not be null");
        }
    }

    public void assertWithdrawlInitiateSuccess(WithdrawlInitiateResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "Withdrawal Initiate")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "Withdrawal Initiate — expected success=true");
        softAssert.assertNotNull(response.getData(), "Withdrawal Initiate — data must not be null");
        if (response.getData() != null) {
            softAssert.assertNotNull(
                    response.getData().getOrderId(),
                    "Withdrawal Initiate — orderId must not be null");
        }
    }

    public void assertWithdrawlSubmitSuccess(WithdrawlSubmitResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "Withdrawal Submit")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "Withdrawal Submit — expected success=true");
        softAssert.assertNotNull(response.getData(), "Withdrawal Submit — data must not be null");
        if (response.getData() != null) {
            softAssert.assertNotNull(
                    response.getData().getOrderId(),
                    "Withdrawal Submit — orderId must not be null");
        }
    }

    public void assertWithdrawlStatusSuccess(WithdrawlStatusResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "Withdrawal Status")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "Withdrawal Status — expected success=true");
        softAssert.assertNotNull(response.getData(), "Withdrawal Status — data must not be null");
        if (response.getData() != null) {
            softAssert.assertNotNull(
                    response.getData().getStatus(),
                    "Withdrawal Status — status must not be null");
            softAssert.assertEquals(
                    response.getData().getStatus(),
                    "SUCCESS",
                    "Withdrawal Status — expected status=SUCCESS");
        }
    }
}
