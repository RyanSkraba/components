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
package org.talend.components.salesforce.runtime.tmp;

import java.io.IOException;

import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.util.UnshardedInputSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.salesforce.runtime.SalesforceSourceOrSink;
import org.talend.components.salesforce.tsalesforcegetdeleted.TSalesforceGetDeletedProperties;
import org.talend.components.salesforce.tsalesforcegetservertimestamp.TSalesforceGetServerTimestampProperties;
import org.talend.components.salesforce.tsalesforcegetupdated.TSalesforceGetUpdatedProperties;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;

/**
 *
 */
public class SalesforceSource extends UnshardedInputSource<IndexedRecord> {

    /** Default serial version UID. */
    private static final long serialVersionUID = 1L;

    private String filename;

    @Override
    public void initialize(RuntimeContainer adaptor, ComponentProperties properties) {
        try {
            if (properties instanceof TSalesforceInputProperties) {
                TSalesforceInputProperties props = (TSalesforceInputProperties) properties;
                SalesforceSourceOrSink ss = new SalesforceSourceOrSink();
                ss.initialize(null, properties);
                setUnshardedInput(new SalesforceQueryInput(props, ss.getSchema((RuntimeContainer) null,
                        props.module.moduleName.getStringValue())));

                // return new SalesforceInputReader(adaptor, this, (TSalesforceInputProperties) properties);
            } else if (properties instanceof TSalesforceGetServerTimestampProperties) {
                // return new SalesforceServerTimeStampReader(adaptor, this, (TSalesforceGetServerTimestampProperties)
                // properties);
            } else if (properties instanceof TSalesforceGetDeletedProperties) {
                // return new SalesforceGetDeletedReader(adaptor, this, (TSalesforceGetDeletedProperties) properties);
            } else if (properties instanceof TSalesforceGetUpdatedProperties) {
                // return new SalesforceGetUpdatedReader(adaptor, this, (TSalesforceGetUpdatedProperties) properties);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
