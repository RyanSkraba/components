// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.marketo.runtime.data;

import java.io.IOException;
import java.util.Collections;

import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.datastore.runtime.DatastoreRuntime;
import org.talend.components.marketo.data.MarketoDatastoreProperties;
import org.talend.components.marketo.runtime.MarketoSourceOrSink;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

public class MarketoDatastoreRuntime implements DatastoreRuntime<MarketoDatastoreProperties> {

    private MarketoDatastoreProperties properties;

    @Override
    public ValidationResult initialize(RuntimeContainer container, MarketoDatastoreProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

    @Override
    public Iterable<ValidationResult> doHealthChecks(RuntimeContainer container) {
        ValidationResult vr;
        try {
            MarketoSourceOrSink sos = getNewSourceOrSink();
            sos.initialize(container, properties);
            sos.getClientService(container);
            vr = new ValidationResult(Result.OK);
        } catch (IOException e) {
            vr = new ValidationResult(Result.ERROR, e.getMessage());
        }

        return Collections.singletonList(vr);
    }

    public MarketoSourceOrSink getNewSourceOrSink() {
        return new MarketoSourceOrSink();
    }

}
