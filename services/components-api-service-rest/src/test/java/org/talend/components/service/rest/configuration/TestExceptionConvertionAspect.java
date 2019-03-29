package org.talend.components.service.rest.configuration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseBody;
import org.talend.components.service.rest.AbstractSpringIntegrationTests;
import org.talend.daikon.annotation.ServiceImplementation;
import org.talend.daikon.exception.error.CommonErrorCodes;

public class TestExceptionConvertionAspect extends AbstractSpringIntegrationTests {

    @ServiceImplementation
    public static class RestProcessingExceptionThrowingController implements RestProcessingExceptionThrowingControllerI {

        @Override
        public @ResponseBody String find() {
            throw new RuntimeException("global_error_test");
        }
    }

    @Test
    @Ignore("manual test cause the Aspect need to have this class in it's cutpoint")
    public void testException() {
        // checks that the RuntimeException is converted to a TalendRuntimeException by the aspect
        given() //
                .expect() //
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value()).log().ifValidationFails() //
                .when()//
                .get("/tests/exception")//
                .then()//
                .body("code", containsString(CommonErrorCodes.UNEXPECTED_EXCEPTION.toString()))
                .body("message", equalTo("global_error_test"));
    }

}