// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.salesforce.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.adaptor.Adaptor;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

public class SalesforceSink extends SalesforceSourceOrSink implements Sink {

    private static final Logger LOG = LoggerFactory.getLogger(SalesforceSink.class);

    @Override
    public ValidationResult validate(Adaptor adaptor) {
        ValidationResult validate = super.validate(adaptor);
        // also check that the properties is the right type
        if (validate.getStatus() != Result.ERROR) {
            if (!(properties instanceof TSalesforceOutputProperties)) {
                return new ValidationResult().setStatus(Result.ERROR)
                        .setMessage("properties should be of type :" + TSalesforceOutputProperties.class.getCanonicalName());
            } // else this is the right type
        } // else already an ERROR olready
        return validate;
    }

    @Override
    public WriteOperation<?> createWriteOperation() {
        return new SalesforceWriteOperation(this);
    }

    /**
     * this should never becalled before {@link #validate(Adaptor)} is called but this should not be the case anyway
     * cause validate is called before the pipeline is created.
     * 
     * @return the properties
     */
    public TSalesforceOutputProperties getSalesforceOutputProperties() {
        return (TSalesforceOutputProperties) properties;
    }

}
