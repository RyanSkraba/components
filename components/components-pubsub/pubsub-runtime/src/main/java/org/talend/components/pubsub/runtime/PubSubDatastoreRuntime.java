// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.pubsub.runtime;

import java.io.IOException;
import java.util.Arrays;

import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.datastore.runtime.DatastoreRuntime;
import org.talend.components.pubsub.PubSubDatastoreProperties;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.properties.ValidationResult;

public class PubSubDatastoreRuntime implements DatastoreRuntime<PubSubDatastoreProperties> {

    /**
     * The datastore instance that this runtime is configured for.
     */
    private PubSubDatastoreProperties properties = null;

    @Override
    public ValidationResult initialize(RuntimeContainer container, PubSubDatastoreProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

    @Override
    public Iterable<ValidationResult> doHealthChecks(RuntimeContainer container) {
        PubSubClient client = PubSubConnection.createClient(properties);
        try {
            client.listTopics();
            return Arrays.asList(ValidationResult.OK);
        } catch (IOException e) {
            return Arrays.asList(new ValidationResult(TalendRuntimeException.createUnexpectedException(e.getMessage())));
        }
    }
}
