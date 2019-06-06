// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import static org.talend.components.marketo.MarketoConstants.DATETIME_PATTERN_PARAM;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.lang3.StringUtils;
import org.talend.components.api.component.runtime.ReaderDataProvider;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.dataset.runtime.DatasetRuntime;
import org.talend.components.marketo.data.MarketoDatasetProperties;
import org.talend.components.marketo.data.MarketoDatasetProperties.Operation;
import org.talend.components.marketo.data.MarketoInputProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.java8.Consumer;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

public class MarketoDatasetRuntime implements DatasetRuntime<MarketoDatasetProperties> {

    private MarketoDatasetProperties dataset;

    private RuntimeContainer container;

    private MarketoDatasetSource source;

    private MarketoInputProperties properties;

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider().getI18nMessages(MarketoDatasetRuntime.class);

    @Override
    public ValidationResult initialize(RuntimeContainer container, MarketoDatasetProperties properties) {
        this.container = container;
        dataset = properties;

        if (Operation.getCustomObjects.equals(dataset.operation.getValue())) {
            if (StringUtils.isEmpty(dataset.customObjectName.getValue())) {
                return new ValidationResult(Result.ERROR, messages.getMessage("error.validation.customobject.customobjectname"));
            }
            if (StringUtils.isEmpty(dataset.filterType.getValue())) {
                return new ValidationResult(Result.ERROR, messages.getMessage("error.validation.customobject.filtertype"));
            }
            if (StringUtils.isEmpty(dataset.filterValue.getValue())) {
                return new ValidationResult(Result.ERROR, messages.getMessage("error.validation.customobject.filtervalues"));
            }
        }

        return ValidationResult.OK;
    }

    @Override
    public Schema getSchema() {
        return dataset.getSchema();
    }

    public void getLeadsSample(int limit, Consumer<IndexedRecord> consumer) {
        final List<String> idList = new ArrayList<>();
        Consumer<IndexedRecord> changes = new Consumer<IndexedRecord>() {

            @Override
            public void accept(IndexedRecord r) {
                idList.add(String.valueOf(r.get(r.getSchema().getField("leadId").pos())));
            }
        };
        dataset.operation.setValue(Operation.getLeadChanges);
        dataset.afterOperation();
        getLeadChangesSample();
        ReaderDataProvider<IndexedRecord> readerDataProvider = new ReaderDataProvider<>(source.createReader(container), limit,
                changes);
        readerDataProvider.retrieveData();
        dataset.operation.setValue(Operation.getLeads);
        dataset.afterOperation();
        properties.leadKeyType.setValue("id");
        properties.leadKeyValue.setValue(idList.stream().collect(Collectors.joining(",")));
        source.initialize(container, properties);
    }

    public void getLeadChangesSample() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -5);
        properties.sinceDateTime.setValue(new SimpleDateFormat(DATETIME_PATTERN_PARAM).format(cal.getTime()));
        source.initialize(container, properties);
    }

    public void getCustomObjectsSample() {
        source.initialize(container, properties);
    }

    @Override
    public void getSample(int limit, Consumer<IndexedRecord> consumer) {
        dataset.batchSize.setValue(limit);
        dataset.afterOperation();
        source = getSource();
        properties = new MarketoInputProperties("sample");
        properties.setupProperties();
        properties.setDatasetProperties(dataset);
        switch (dataset.operation.getValue()) {
        case getLeads:
            getLeadsSample(limit, consumer);
            break;
        case getLeadChanges:
            getLeadChangesSample();
            break;
        case getLeadActivities:
            getLeadChangesSample();
            break;
        case getCustomObjects:
            dataset.afterCustomObjectName();
            getCustomObjectsSample();
            break;
        }
        ReaderDataProvider<IndexedRecord> readerDataProvider = new ReaderDataProvider<>(source.createReader(container), limit,
                consumer);
        readerDataProvider.retrieveData();
    }

    public MarketoDatasetSource getSource() {
        return new MarketoDatasetSource();
    }
}
