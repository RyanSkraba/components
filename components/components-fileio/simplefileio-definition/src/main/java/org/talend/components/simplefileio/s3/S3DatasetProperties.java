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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.simplefileio.SimpleFileIODatasetProperties.FieldDelimiterType;
import org.talend.components.simplefileio.SimpleFileIODatasetProperties.RecordDelimiterType;
import org.talend.components.simplefileio.ExcelFormat;
import org.talend.components.simplefileio.SimpleFileIOFormat;
import org.talend.components.simplefileio.local.EncodingType;
import org.talend.components.simplefileio.s3.runtime.IS3DatasetRuntime;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

public class S3DatasetProperties extends PropertiesImpl implements DatasetProperties<S3DatastoreProperties> {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3DatasetProperties.class);
  
    public final transient ReferenceProperties<S3DatastoreProperties> datastoreRef = new ReferenceProperties<>("datastoreRef",
            S3DatastoreDefinition.NAME);

    // S3 Connectivity
    public Property<String> bucket = PropertyFactory.newString("bucket").setRequired();

    public Property<String> object = PropertyFactory.newString("object").setRequired();

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
    
    //CSV and Excel both properties
    public Property<EncodingType> encoding = PropertyFactory.newEnum("encoding", EncodingType.class).setValue(EncodingType.UTF8);
    public Property<String> specificEncoding = PropertyFactory.newString("specificEncoding", "");
    public Property<Boolean> setHeaderLine = PropertyFactory.newBoolean("setHeaderLine", false);
    public Property<Integer> headerLine = PropertyFactory.newInteger("headerLine", 1);
    
    //advice not set them as default they break the split function for hadoop and beam
    public Property<String> textEnclosureCharacter = PropertyFactory.newString("textEnclosureCharacter", "");
    public Property<String> escapeCharacter = PropertyFactory.newString("escapeCharacter", "");

    //Excel propertiess
    public Property<ExcelFormat> excelFormat = PropertyFactory.newEnum("excelFormat", ExcelFormat.class);
    public Property<String> sheet = PropertyFactory.newString("sheet", "");
    public Property<Boolean> setFooterLine = PropertyFactory.newBoolean("setFooterLine", false);
    //not set the default value, TODO check if it works like expected
    public Property<Integer> footerLine = PropertyFactory.newInteger("footerLine");
    
    // TODO: If data-in-motion can be activated in the future, remove this flag.
    public static final boolean ACTIVATE_DATA_IN_MOTION = false;

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
        //TODO should not call here, any reason? now it's necessary, if not, the trigger method don't work.
        afterDatastoreRef();
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        format.setValue(SimpleFileIOFormat.CSV);
        excelFormat.setValue(ExcelFormat.EXCEL2007);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        // S3
        mainForm.addRow(Widget.widget(bucket).setWidgetType(Widget.DATALIST_WIDGET_TYPE));
        mainForm.addRow(object);
        if (ACTIVATE_DATA_IN_MOTION) {
            mainForm.addRow(encryptDataInMotion);
            mainForm.addRow(kmsForDataInMotion);
        }
        mainForm.addRow(encryptDataAtRest);
        mainForm.addRow(kmsForDataAtRest);

        mainForm.addRow(format);
        
        //CSV only properties
        mainForm.addRow(recordDelimiter);
        mainForm.addRow(specificRecordDelimiter);
        mainForm.addRow(fieldDelimiter);
        mainForm.addRow(specificFieldDelimiter);
        mainForm.addRow(textEnclosureCharacter);
        mainForm.addRow(escapeCharacter);
        
        //Excel only properties
        mainForm.addRow(excelFormat);
        mainForm.addRow(sheet);
        
        //CSV and Excel both properties
        mainForm.addRow(encoding);
        mainForm.addColumn(specificEncoding);
        mainForm.addRow(setHeaderLine);
        mainForm.addColumn(headerLine);
        
        //Excel only properties
        mainForm.addColumn(setFooterLine);
        mainForm.addColumn(footerLine);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        // Main properties
        if (form.getName().equals(Form.MAIN)) {
            // S3
            form.getWidget(bucket.getName()).setVisible();
            form.getWidget(object.getName()).setVisible();
            if (ACTIVATE_DATA_IN_MOTION) {
                form.getWidget(encryptDataInMotion.getName()).setVisible();
                boolean isVisibleEncryptDataInMotion = form.getWidget(encryptDataInMotion).isVisible()
                        && encryptDataInMotion.getValue();
                form.getWidget(kmsForDataInMotion.getName()).setVisible(isVisibleEncryptDataInMotion);
            }
            form.getWidget(encryptDataAtRest.getName()).setVisible();
            boolean isVisibleEncryptDataAtRest = form.getWidget(encryptDataAtRest).isVisible() && encryptDataAtRest.getValue();
            form.getWidget(kmsForDataAtRest.getName()).setVisible(isVisibleEncryptDataAtRest);

            form.getWidget(format).setVisible();

            boolean isCSV = format.getValue() == SimpleFileIOFormat.CSV;
            form.getWidget(recordDelimiter).setVisible(isCSV);
            form.getWidget(specificRecordDelimiter)
                    .setVisible(isCSV && recordDelimiter.getValue().equals(RecordDelimiterType.OTHER));

            form.getWidget(fieldDelimiter).setVisible(isCSV);
            form.getWidget(specificFieldDelimiter)
                    .setVisible(isCSV && fieldDelimiter.getValue().equals(FieldDelimiterType.OTHER));
            
            form.getWidget(textEnclosureCharacter).setVisible(isCSV);
            form.getWidget(escapeCharacter).setVisible(isCSV);
            
            boolean isExcel = format.getValue() == SimpleFileIOFormat.EXCEL;
            form.getWidget(excelFormat).setVisible(isExcel);
            //html format no sheet setting
            boolean isHTML = excelFormat.getValue() == ExcelFormat.HTML;
            form.getWidget(sheet).setVisible(isExcel && (!isHTML));
            
            boolean isCSVOrExcel = isCSV || isExcel;
            form.getWidget(encoding).setVisible(isCSV || (isExcel && isHTML));
            form.getWidget(specificEncoding)
                    .setVisible((isCSV || (isExcel && isHTML)) && encoding.getValue().equals(EncodingType.OTHER));
            form.getWidget(setHeaderLine).setVisible(isCSVOrExcel);
            form.getWidget(headerLine).setVisible(isCSVOrExcel && setHeaderLine.getValue());
            
            form.getWidget(setFooterLine).setVisible(isExcel);
            form.getWidget(footerLine).setVisible(isExcel && setFooterLine.getValue());
        }
    }

    public void afterDatastoreRef() {
        //the current method is called at some strange place, need to skip it if bucket list have been set
        if(!this.bucket.getPossibleValues().isEmpty()) {
            return;
        }
        S3DatasetDefinition definition = new S3DatasetDefinition();
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(this);
        try (SandboxedInstance sandboxedInstance = RuntimeUtil.createRuntimeClass(runtimeInfo, getClass().getClassLoader())) {
            IS3DatasetRuntime runtime = (IS3DatasetRuntime) sandboxedInstance.getInstance();
            runtime.initialize(null, this);
            this.bucket.setPossibleValues(new ArrayList<String>(runtime.listBuckets()));
        } catch (Exception e) {
            //TalendRuntimeException.build(ComponentsErrorCode.IO_EXCEPTION, e).throwIt();
            //ignore the exception here, as the trigger method should not block to save the data set properties action. 
            //And as the current after data store trigger method is called at some strange place, the exception should not break something.
            LOGGER.warn(e.getClass() + " : " + e.getMessage());
        }
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
    
    public void afterSetHeaderLine() {
        refreshLayout(getForm(Form.MAIN));
    }
    
    public void afterSetFooterLine() {
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
    
    public void afterEncoding() {
        refreshLayout(getForm(Form.MAIN));
    }
    
    public void afterExcelFormat() {
        refreshLayout(getForm(Form.MAIN));
    }

    public String getEncoding() {
        if (EncodingType.OTHER.equals(encoding.getValue())) {
            return specificEncoding.getValue();
        } else {
            return encoding.getValue().getEncoding();
        }
    }

    public long getHeaderLine() {
        if(setHeaderLine.getValue()) {
            Integer value = headerLine.getValue();
            if(value != null) { 
                return Math.max(0l, value.longValue());
            }
        }
        
        return 0l;
    }
    
    public long getFooterLine() {
        if(setFooterLine.getValue()) {
            Integer value = footerLine.getValue();
            if(value != null) { 
                return Math.max(0l, value.longValue());
            }
        }
        
        return 0l;
    }

    public String getEscapeCharacter() {
        return escapeCharacter.getValue();
    }

    public String getTextEnclosureCharacter() {
        return textEnclosureCharacter.getValue();
    }
    
    public String getSheetName() {
        return this.sheet.getValue();
    }
    
    public ExcelFormat getExcelFormat() {
        return excelFormat.getValue();
    }
}
