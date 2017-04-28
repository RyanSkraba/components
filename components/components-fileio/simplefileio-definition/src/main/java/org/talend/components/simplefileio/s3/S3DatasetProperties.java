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

package org.talend.components.simplefileio.s3;

import java.util.ArrayList;

import org.talend.components.api.exception.error.ComponentsErrorCode;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.simplefileio.SimpleFileIODatasetProperties.FieldDelimiterType;
import org.talend.components.simplefileio.SimpleFileIODatasetProperties.RecordDelimiterType;
import org.talend.components.simplefileio.SimpleFileIOFormat;
import org.talend.components.simplefileio.s3.runtime.IS3DatasetRuntime;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

public class S3DatasetProperties extends PropertiesImpl implements DatasetProperties<S3DatastoreProperties> {

    public final transient ReferenceProperties<S3DatastoreProperties> datastoreRef = new ReferenceProperties<>("datastoreRef",
            S3DatastoreDefinition.NAME);

    // S3 Connectivity
    public Property<S3Region> region = PropertyFactory.newEnum("region", S3Region.class).setValue(S3Region.DEFAULT);

    public Property<String> unknownRegion = PropertyFactory.newString("unknownRegion", "us-east-1");

    public Property<String> bucket = PropertyFactory.newString("bucket");

    public Property<String> object = PropertyFactory.newString("object");

    public Property<Boolean> encryptDataInMotion = PropertyFactory.newBoolean("encryptDataInMotion", false);

    public Property<String> kmsForDataInMotion = PropertyFactory.newString("kmsForDataInMotion");

    public Property<Boolean> encryptDataAtRest = PropertyFactory.newBoolean("encryptDataAtRest", false);

    public Property<String> kmsForDataAtRest = PropertyFactory.newString("kmsForDataAtRest");

    // File properties
    public Property<SimpleFileIOFormat> format = PropertyFactory.newEnum("format", SimpleFileIOFormat.class).setRequired();

    public Property<RecordDelimiterType> recordDelimiter = PropertyFactory.newEnum("recordDelimiter", RecordDelimiterType.class)
            .setValue(RecordDelimiterType.LF);

    public Property<String> specificRecordDelimiter = PropertyFactory.newString("specificRecordDelimiter", "\\n");

    public Property<FieldDelimiterType> fieldDelimiter = PropertyFactory.newEnum("fieldDelimiter", FieldDelimiterType.class)
            .setValue(FieldDelimiterType.SEMICOLON);

    public Property<String> specificFieldDelimiter = PropertyFactory.newString("specificFieldDelimiter", ";");

    public S3DatasetProperties(String name) {
        super(name);
    }

    @Override
    public S3DatastoreProperties getDatastoreProperties() {
        return datastoreRef.getReference();
    }

    @Override
    public void setDatastoreProperties(S3DatastoreProperties datastoreProperties) {
        datastoreRef.setReference(datastoreProperties);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        format.setValue(SimpleFileIOFormat.CSV);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        // S3
        mainForm.addRow(region);
        mainForm.addRow(unknownRegion);
        mainForm.addRow(bucket);
        mainForm.addRow(object);
        mainForm.addRow(encryptDataInMotion);
        mainForm.addRow(kmsForDataInMotion);
        mainForm.addRow(encryptDataAtRest);
        mainForm.addRow(kmsForDataAtRest);

        // File properties
        mainForm.addRow(format);
        mainForm.addRow(recordDelimiter);
        mainForm.addRow(specificRecordDelimiter);
        mainForm.addRow(fieldDelimiter);
        mainForm.addRow(specificFieldDelimiter);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        // Main properties
        if (form.getName().equals(Form.MAIN)) {
            // S3
            form.getWidget(region.getName()).setVisible();
            form.getWidget(unknownRegion.getName()).setVisible(S3Region.OTHER.equals(region.getValue()));

            form.getWidget(bucket.getName()).setVisible();
            form.getWidget(object.getName()).setVisible();
            form.getWidget(encryptDataInMotion.getName()).setVisible();
            boolean isVisibleEncryptDataInMotion = form.getWidget(encryptDataInMotion).isVisible()
                    && encryptDataInMotion.getValue();
            form.getWidget(kmsForDataInMotion.getName()).setVisible(isVisibleEncryptDataInMotion);
            form.getWidget(encryptDataAtRest.getName()).setVisible();
            boolean isVisibleEncryptDataAtRest = form.getWidget(encryptDataAtRest).isVisible() && encryptDataInMotion.getValue();
            form.getWidget(kmsForDataAtRest.getName()).setVisible(isVisibleEncryptDataAtRest);

            form.getWidget(format).setVisible();

            boolean isCSV = format.getValue() == SimpleFileIOFormat.CSV;
            form.getWidget(recordDelimiter).setVisible(isCSV);
            form.getWidget(specificRecordDelimiter)
                    .setVisible(isCSV && recordDelimiter.getValue().equals(RecordDelimiterType.OTHER));

            form.getWidget(fieldDelimiter).setVisible(isCSV);
            form.getWidget(specificFieldDelimiter)
                    .setVisible(isCSV && fieldDelimiter.getValue().equals(FieldDelimiterType.OTHER));
        }
    }

    public void afterRegion() {
        refreshLayout(getForm(Form.MAIN));
        S3DatasetDefinition definition = new S3DatasetDefinition();
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(this);
        try (SandboxedInstance sandboxedInstance = RuntimeUtil.createRuntimeClass(runtimeInfo, getClass().getClassLoader())) {
            IS3DatasetRuntime runtime = (IS3DatasetRuntime) sandboxedInstance.getInstance();
            runtime.initialize(null, this);
            this.bucket.setPossibleValues(new ArrayList<String>(runtime.listBuckets()));
        } catch (Exception e) {
            TalendRuntimeException.build(ComponentsErrorCode.IO_EXCEPTION, e).throwIt();
        }
    }

    public void afterUnknownRegion() {
        afterRegion();
    }

    public void afterEncryptDataInMotion() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterEncryptDataAtRest() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterFieldDelimiter() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterRecordDelimiter() {
        refreshLayout(getForm(Form.MAIN));
    }

    public String getRecordDelimiter() {
        if (RecordDelimiterType.OTHER.equals(recordDelimiter.getValue())) {
            return specificRecordDelimiter.getValue();
        } else {
            return recordDelimiter.getValue().getDelimiter();
        }
    }

    public String getFieldDelimiter() {
        if (FieldDelimiterType.OTHER.equals(fieldDelimiter.getValue())) {
            return specificFieldDelimiter.getValue();
        } else {
            return fieldDelimiter.getValue().getDelimiter();
        }
    }

    public void afterFormat() {
        refreshLayout(getForm(Form.MAIN));
    }
}
