package org.talend.components.service.rest.configuration;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/tests")
public interface RestProcessingExceptionThrowingControllerI {

    @RequestMapping(value = "/exception", method = GET)
    public @ResponseBody String find();
}