package org.talend.components.salesforce.runtime.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.exception.ComponentException;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.properties.ValidationResult;

import com.sforce.soap.partner.DescribeGlobalResult;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import com.sforce.ws.SessionRenewer;

public final class SalesforceRuntimeCommon {

    private static final Logger LOG = LoggerFactory.getLogger(SalesforceRuntimeCommon.class);

    public static ValidationResult exceptionToValidationResult(Exception ex) {
        String errorMessage = ExceptionUtils.getMessage(ex);
        if (errorMessage.isEmpty()) {
            // If still no error message, we use the class name to report the error
            // this should really never happen, but we keep this to prevent loosing error information
            errorMessage = ex.getClass().getName();
        }
        return new ValidationResult(ValidationResult.Result.ERROR, errorMessage);
    }

    public static void enableTLSv11AndTLSv12ForJava7() {
        String version = System.getProperty("java.version");
        if (version != null && version.startsWith("1.7")) {
            System.setProperty("https.protocols", "TLSv1.1,TLSv1.2");
        }
    }

    /**
     * This is for Buck connection session renew It can't called automatically with current force-wsc api
     */
    public static void renewSession(ConnectorConfig config) throws ConnectionException {
        LOG.debug("renew session bulk connection");
        SessionRenewer renewer = config.getSessionRenewer();
        renewer.renewSession(config);
    }

    public static List<NamedThing> getSchemaNames(PartnerConnection connection) throws IOException {
        List<NamedThing> returnList = new ArrayList<>();
        DescribeGlobalResult result = null;
        try {
            result = connection.describeGlobal();
        } catch (ConnectionException e) {
            throw new ComponentException(e);
        }
        DescribeGlobalSObjectResult[] objects = result.getSobjects();
        for (DescribeGlobalSObjectResult obj : objects) {
            LOG.debug("module label: " + obj.getLabel() + " name: " + obj.getName());
            returnList.add(new SimpleNamedThing(obj.getName(), obj.getLabel()));
        }
        return returnList;
    }

}
