package sipSe.lumSumPayment;

import org.jarfinApiBackendAutomation.data.responseModel.sipSe.lumSum.LumpSumIntiateResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.lumSum.LumpSumStatusResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.lumSum.LumpSumSubmitResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.lumSum.LumpSumVerifyResponse;
import org.jarfinApiBackendAutomation.data.responseModel.sipSe.mandate.BillDeskMandateMockResponse;
import org.jarfinApiBackendAutomation.utils.ApiAssertions;
import org.testng.asserts.SoftAssert;

import static org.apache.http.HttpStatus.SC_OK;

public class LumpSumValidation extends ApiAssertions {


    public LumpSumValidation(SoftAssert softAssert) {
        super(softAssert);
    }

    public void assertLumpSumInitiateSuccess(LumpSumIntiateResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "LumpSum Initiate")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "LumpSum Initiate — expected success=true");
    }

    public void assertLumpSumVerifySuccess(LumpSumVerifyResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "LumpSum Verify")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "LumpSum Verify — expected success=true");
    }

    public void assertLumpSumSubmitSuccess(LumpSumSubmitResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "LumpSum Submit")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "LumpSum Submit — expected success=true");
    }

    public void assertLumpSumStatusSuccess(LumpSumStatusResponse response) {
        if (!assertStatusCode(response.getStatusCode(), SC_OK, "LumpSum Status")) {
            softAssert.assertAll();
            return;
        }
        softAssert.assertTrue(response.isSuccess(), "LumpSum Status — expected success=true");
    }







}
