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

package org.talend.components.simplefileio.local;

import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.simplefileio.SimpleFileIOFormat;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class LocalFileIODatasetProperties extends PropertiesImpl implements DatasetProperties<LocalFileIODatastoreProperties> {

    public final transient ReferenceProperties<LocalFileIODatastoreProperties> datastoreRef = new ReferenceProperties<>(
        "datastoreRef", LocalFileIODatastoreDefinition.NAME);
  
    //TODO path is not a format property, maybe move it to other class
    public Property<String> path = PropertyFactory.newString("path", "").setRequired();
  
    public Property<SimpleFileIOFormat> format = PropertyFactory.newEnum("format", SimpleFileIOFormat.class).setRequired();
    
    //CSV and Excel both need:
    public Property<EncodingType> encoding = PropertyFactory.newEnum("encoding", EncodingType.class);

    public Property<Boolean> setHeaderLine = PropertyFactory.newBoolean("setHeaderLine", true);
    public Property<Integer> headerLine = PropertyFactory.newInteger("headerLine", 1);
    
    public Property<Boolean> setFooterLine = PropertyFactory.newBoolean("setFooterLine", false);
    //not set the default value, TODO check if it works like expected
    public Property<Integer> footerLine = PropertyFactory.newInteger("footerLine");
    
    ///////////////////////////////CSV format only parameters///////////////////////////////
    
    //TODO not appear in the require list, maybe remove it later
    //https://talend.aha.io/features/COMP-79#requirement_6513817671682778644
    //https://talend.aha.io/features/COMP-79#requirement_6513828731975002373
    public Property<RecordDelimiterType> recordDelimiter = PropertyFactory.newEnum("recordDelimiter", RecordDelimiterType.class)
            .setValue(RecordDelimiterType.LF);

    public Property<String> specificRecordDelimiter = PropertyFactory.newString("specificRecordDelimiter", "\\n");

    //TODO the ui is different with https://talend.aha.io/features/COMP-79#requirement_6513828731975002373, maybe adjust it
    //and the default value is different now, adjust it later 
    public Property<FieldDelimiterType> fieldDelimiter = PropertyFactory.newEnum("fieldDelimiter", FieldDelimiterType.class)
            .setValue(FieldDelimiterType.SEMICOLON);

    public Property<String> specificFieldDelimiter = PropertyFactory.newString("specificFieldDelimiter", ";");

    public Property<String> textEnclosureCharacter = PropertyFactory.newString("textEnclosureCharacter", "\"");
    public Property<String> escapeCharacter = PropertyFactory.newString("escapeCharacter", "\\");
    
    ///////////////////////////////Excel format only parameters///////////////////////////////
    //public Property<String> sheet = PropertyFactory.newString("sheet");

    public LocalFileIODatasetProperties(String name) {
        super(name);
    }

    @Override
    public LocalFileIODatastoreProperties getDatastoreProperties() {
        return datastoreRef.getReference();
    }

    @Override
    public void setDatastoreProperties(LocalFileIODatastoreProperties datastoreProperties) {
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
        mainForm.addRow(path);
        
        mainForm.addRow(format);
        
        //CSV only
        mainForm.addRow(recordDelimiter);
        mainForm.addRow(specificRecordDelimiter);
        mainForm.addRow(fieldDelimiter);
        mainForm.addRow(specificFieldDelimiter);
        mainForm.addRow(textEnclosureCharacter);
        mainForm.addRow(escapeCharacter);
        
        //Excel only
        //mainForm.addRow(sheet);
        
        //CSV and Excel Both
        mainForm.addRow(encoding);
        mainForm.addRow(setHeaderLine);
        mainForm.addColumn(headerLine);
        mainForm.addColumn(setFooterLine);
        mainForm.addColumn(footerLine);
        
        //seem Avro and Parquet is self defined, no format config parameters, TODO make sure it
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        // Main properties
        if (!form.getName().equals(Form.MAIN)) {//now only Main page support
            return;
        }
        
        //CSV
        boolean csvMode = format.getValue() == SimpleFileIOFormat.CSV;
        
        form.getWidget(recordDelimiter).setVisible(csvMode);
        form.getWidget(specificRecordDelimiter).setVisible(
            csvMode && recordDelimiter.getValue().equals(RecordDelimiterType.OTHER));

        form.getWidget(fieldDelimiter).setVisible(csvMode);
        form.getWidget(specificFieldDelimiter).setVisible(
            csvMode && fieldDelimiter.getValue().equals(FieldDelimiterType.OTHER));
        
        form.getWidget(textEnclosureCharacter).setVisible(csvMode);
        form.getWidget(escapeCharacter).setVisible(csvMode);
        
        //Excel
        //boolean excelMode = format.getValue() == SimpleFileIOFormat.EXCEL;
        //form.getWidget(sheet).setVisible(excelMode);
        
        //CSV and Excel both
        boolean csvOrExcel = csvMode/* || excelMode */;
        form.getWidget(encoding).setVisible(csvOrExcel);
        form.getWidget(setHeaderLine).setVisible(csvOrExcel);
        form.getWidget(headerLine).setVisible(csvOrExcel && setHeaderLine.getValue());
        form.getWidget(setFooterLine).setVisible(csvOrExcel);
        form.getWidget(footerLine).setVisible(csvOrExcel && setFooterLine.getValue());
    }
    
    public void afterSetHeaderLine() {
        refreshLayout(getForm(Form.MAIN));
    }
    
    public void afterSetFooterLine() {
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

    public enum RecordDelimiterType {
        LF("\n"),
        CR("\r"),
        CRLF("\r\n"),
        OTHER("Other");

        private final String value;

        private RecordDelimiterType(final String value) {
            this.value = value;
        }

        public String getDelimiter() {
            return value;
        }
    }

    public enum FieldDelimiterType {
        SEMICOLON(";"),
        COMMA(","),
        TABULATION("\t"),
        SPACE(" "),
        OTHER("Other");

        private final String value;

        private FieldDelimiterType(final String value) {
            this.value = value;
        }

        public String getDelimiter() {
            return value;
        }
    }
}
