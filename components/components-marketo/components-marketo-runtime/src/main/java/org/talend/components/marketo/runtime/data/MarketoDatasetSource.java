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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.commons.lang3.StringUtils;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.marketo.data.MarketoDatasetProperties;
import org.talend.components.marketo.data.MarketoInputProperties;
import org.talend.components.marketo.runtime.MarketoInputReader;
import org.talend.components.marketo.runtime.MarketoSource;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadKeyTypeREST;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadSelector;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.CustomObjectAction;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.InputOperation;
import org.talend.daikon.NamedThing;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

public class MarketoDatasetSource extends MarketoSource implements BoundedSource {

    private MarketoInputProperties properties;

    private MarketoDatasetProperties dataset;

    private TMarketoConnectionProperties datastore;

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider().getI18nMessages(MarketoDatasetSource.class);

    @Override
    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        super.initialize(container, properties);
        this.properties = (MarketoInputProperties) properties;
        dataset = this.properties.getDatasetProperties();
        datastore = dataset.getDatastoreProperties();

        return ValidationResult.OK;
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        ValidationResult vr = validateConnection(datastore);
        if (vr.getStatus().equals(Result.ERROR)) {
            return vr;
        }
        switch (dataset.operation.getValue()) {
        case getLeads:
            if (StringUtils.isEmpty(properties.leadKeyValue.getValue())) {
                return new ValidationResult(Result.ERROR, messages.getMessage("error.validation.leadkeyvalue"));
            }
            break;
        case getLeadChanges:
        case getLeadActivities:
            if (StringUtils.isEmpty(properties.sinceDateTime.getValue()) || isInvalidDate(properties.sinceDateTime.getValue())) {
                return new ValidationResult(Result.ERROR, messages.getMessage("error.validation.sincedatetime"));
            }
            break;
        case getCustomObjects:
            if (StringUtils.isEmpty(dataset.customObjectName.getValue())) {
                return new ValidationResult(Result.ERROR, messages.getMessage("error.validation.customobject.customobjectname"));
            }
            if (StringUtils.isEmpty(dataset.filterType.getValue())) {
                return new ValidationResult(Result.ERROR, messages.getMessage("error.validation.customobject.filtertype"));
            }
            if (StringUtils.isEmpty(dataset.filterValue.getValue())) {
                return new ValidationResult(Result.ERROR, messages.getMessage("error.validation.customobject.filtervalues"));
            }
            break;
        }

        return ValidationResult.OK;
    }

    @Override
    public List<? extends BoundedSource> splitIntoBundles(long desiredBundleSizeBytes, RuntimeContainer adaptor)
            throws Exception {
        List<BoundedSource> list = new ArrayList<>();
        list.add(this);
        return list;
    }

    @Override
    public long getEstimatedSizeBytes(RuntimeContainer adaptor) {
        return 1L;
    }

    @Override
    public boolean producesSortedKeys(RuntimeContainer adaptor) {
        return false;
    }

    @Override
    public BoundedReader createReader(RuntimeContainer adaptor) {
        return new MarketoInputReader(adaptor, this, getInputProperties());
    }

    public TMarketoInputProperties getInputProperties() {
        TMarketoInputProperties p = new TMarketoInputProperties("adaptor");
        p.init();
        p.setupProperties();
        p.connection.apiMode.setValue(APIMode.REST);
        switch (dataset.operation.getValue()) {
        case getLeads:
            p.inputOperation.setValue(InputOperation.getMultipleLeads);
            p.leadSelectorREST.setValue(LeadSelector.LeadKeySelector);
            p.leadKeyTypeREST.setValue(LeadKeyTypeREST.valueOf(properties.leadKeyType.getValue()));
            p.leadKeyValues.setValue(properties.leadKeyValue.getValue());
            break;
        case getLeadChanges:
            p.inputOperation.setValue(InputOperation.getLeadChanges);
            p.sinceDateTime.setValue(properties.sinceDateTime.getValue());
            p.fieldList.setValue(properties.fieldList.getValue());
            break;
        case getLeadActivities:
            p.inputOperation.setValue(InputOperation.getLeadActivity);
            p.sinceDateTime.setValue(properties.sinceDateTime.getValue());
            break;
        case getCustomObjects:
            p.inputOperation.setValue(InputOperation.CustomObject);
            p.customObjectAction.setValue(CustomObjectAction.get);
            p.customObjectName.setValue(dataset.customObjectName.getValue());
            p.customObjectFilterType.setValue(dataset.filterType.getValue());
            p.customObjectFilterValues.setValue(dataset.filterValue.getValue());
            break;
        }
        p.afterInputOperation();
        p.schemaInput.schema.setValue(dataset.getSchema());
        p.batchSize.setValue(dataset.batchSize.getValue());
        return p;
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
        return Collections.emptyList();
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer container, String schemaName) throws IOException {
        return dataset.getSchema();
    }
}
