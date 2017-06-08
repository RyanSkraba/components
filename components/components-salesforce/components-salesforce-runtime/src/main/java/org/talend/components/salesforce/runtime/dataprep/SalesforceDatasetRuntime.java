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
package org.talend.components.salesforce.runtime.dataprep;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.ReaderDataProvider;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.common.dataset.runtime.DatasetRuntime;
import org.talend.components.salesforce.dataprep.SalesforceInputProperties;
import org.talend.components.salesforce.dataset.SalesforceDatasetProperties;
import org.talend.components.salesforce.dataset.SalesforceDatasetProperties.SourceType;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.daikon.java8.Consumer;
import org.talend.daikon.properties.ValidationResult;

/**
 * the data set runtime for salesforce
 *
 */
public class SalesforceDatasetRuntime implements DatasetRuntime<SalesforceDatasetProperties> {

    /**
     * 
     */
    private static final long serialVersionUID = 5829335010543623248L;

    private SalesforceDatasetProperties dataset;

    private RuntimeContainer container;

    @Override
    public ValidationResult initialize(RuntimeContainer container, SalesforceDatasetProperties properties) {
        this.dataset = properties;
        this.container = container;
        return ValidationResult.OK;
    }

    @Override
    public Schema getSchema() {
        SalesforceDataprepSource sds = new SalesforceDataprepSource();

        SalesforceInputProperties properties = new SalesforceInputProperties("model");
        properties.setDatasetProperties(dataset);

        sds.initialize(container, properties);
        sds.validate(container);

        try {
            // TODO the UI will be a radio, need to adjust here
            return SalesforceSchemaUtils.getSchema(dataset, sds, container);

        } catch (IOException e) {
            throw new ComponentException(e);
        }
    }



    @Override
    public void getSample(int limit, Consumer<IndexedRecord> consumer) {
        SalesforceDataprepSource sds = new SalesforceDataprepSource();

        SalesforceInputProperties properties = new SalesforceInputProperties("model");
        properties.setDatasetProperties(dataset);

        throwExceptionIfValidationResultIsError(sds.initialize(container, properties));
        throwExceptionIfValidationResultIsError(sds.validate(container));

        SalesforceBulkQueryReader reader = (SalesforceBulkQueryReader) sds.createReader(container);
        ReaderDataProvider<IndexedRecord> readerDataProvider = new ReaderDataProvider<>(reader, limit, consumer);
        readerDataProvider.retrieveData();
    }

    private void throwExceptionIfValidationResultIsError(ValidationResult validationResult) {
        if (validationResult == null) {
            return;
        }

        if (validationResult.getStatus() == ValidationResult.Result.ERROR) {
            throw TalendRuntimeException.createUnexpectedException(validationResult.getMessage());
        }
    }

}
