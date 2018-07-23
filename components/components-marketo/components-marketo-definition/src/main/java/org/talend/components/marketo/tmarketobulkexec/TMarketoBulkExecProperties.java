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
package org.talend.components.marketo.tmarketobulkexec;

import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newBoolean;
import static org.talend.daikon.properties.property.PropertyFactory.newEnum;
import static org.talend.daikon.properties.property.PropertyFactory.newInteger;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

import java.util.HashSet;
import java.util.Set;

import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.marketo.MarketoComponentProperties;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties.RESTLookupFields;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.ValidationResultMutable;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

public class TMarketoBulkExecProperties extends MarketoComponentProperties {

    public Property<Boolean> dieOnError = newBoolean("dieOnError");

    public enum BulkImportTo {
        Leads,
        CustomObjects
    }

    public enum BulkFileFormat {
        csv,
        tsv,
        ssv
    }

    public Property<BulkImportTo> bulkImportTo = newEnum("bulkImportTo", BulkImportTo.class).setRequired();

    public Property<RESTLookupFields> lookupField = newEnum("lookupField", RESTLookupFields.class).setRequired();

    public Property<Integer> listId = newInteger("listId");

    public Property<String> partitionName = newString("partitionName");

    public Property<String> customObjectName = newString("customObjectName");

    public Property<String> bulkFilePath = newString("bulkFilePath").setRequired();

    public Property<BulkFileFormat> bulkFileFormat = newEnum("bulkFileFormat", BulkFileFormat.class).setRequired();

    public Property<Integer> pollWaitTime = newInteger("pollWaitTime").setRequired();

    public Property<String> logDownloadPath = newString("logDownloadPath").setRequired();

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(TMarketoBulkExecProperties.class);

    public TMarketoBulkExecProperties(String name) {
        super(name);
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        Set<PropertyPathConnector> connectors = new HashSet<>();
        if (isOutputConnection) {
            connectors.add(MAIN_CONNECTOR);
        }
        return connectors;
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        dieOnError.setValue(true);

        connection.apiMode.setPossibleValues(APIMode.REST);
        connection.apiMode.setValue(APIMode.REST);

        schemaInput.schema.setValue(MarketoConstants.getBulkImportLeadSchema());

        bulkImportTo.setPossibleValues(BulkImportTo.values());
        bulkImportTo.setValue(BulkImportTo.Leads);
        lookupField.setPossibleValues(RESTLookupFields.values());
        lookupField.setValue(RESTLookupFields.email);
        bulkFileFormat.setPossibleValues(BulkFileFormat.values());
        bulkFileFormat.setValue(BulkFileFormat.csv);

        pollWaitTime.setValue(15);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(bulkImportTo);
        mainForm.addColumn(bulkFileFormat);
        mainForm.addRow(lookupField);
        mainForm.addColumn(listId);
        mainForm.addColumn(partitionName);
        mainForm.addRow(customObjectName);
        mainForm.addRow(widget(bulkFilePath).setWidgetType(Widget.FILE_WIDGET_TYPE));
        mainForm.addRow(pollWaitTime);
        mainForm.addRow(widget(logDownloadPath).setWidgetType(Widget.DIRECTORY_WIDGET_TYPE));
        mainForm.addRow(dieOnError);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        Boolean leadParamsVisibles = Boolean.FALSE;
        if (form.getName().equals(Form.MAIN)) {
            if (bulkImportTo.getValue().equals(BulkImportTo.Leads)) {
                leadParamsVisibles = true;
            }
            form.getWidget(lookupField.getName()).setVisible(leadParamsVisibles);
            form.getWidget(listId.getName()).setVisible(leadParamsVisibles);
            form.getWidget(partitionName.getName()).setVisible(leadParamsVisibles);
            form.getWidget(customObjectName.getName()).setVisible(!leadParamsVisibles);
        }
    }

    public void afterBulkImportTo() {
        // update outgoing schema
        if (bulkImportTo.getValue().equals(BulkImportTo.Leads)) {
            schemaInput.schema.setValue(MarketoConstants.getBulkImportLeadSchema());
        } else {
            schemaInput.schema.setValue(MarketoConstants.getBulkImportCustomObjectSchema());
        }
        //
        refreshLayout(getForm(Form.MAIN));
    }

    public ValidationResult validateBulkImportTo() {
        ValidationResultMutable vr = new ValidationResultMutable().setStatus(Result.OK);
        if (isApiSOAP()) {
            vr.setStatus(Result.ERROR);
            vr.setMessage(messages.getMessage("error.validation.soap.bulkexec"));
            return vr;
        }
        return vr;
    }
}
