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
package org.talend.components.google.drive.get;

import static org.talend.components.google.drive.GoogleDriveMimeTypes.MimeType.CSV;
import static org.talend.components.google.drive.GoogleDriveMimeTypes.MimeType.CSV_TAB;
import static org.talend.components.google.drive.GoogleDriveMimeTypes.MimeType.EPUB;
import static org.talend.components.google.drive.GoogleDriveMimeTypes.MimeType.EXCEL;
import static org.talend.components.google.drive.GoogleDriveMimeTypes.MimeType.HTML;
import static org.talend.components.google.drive.GoogleDriveMimeTypes.MimeType.HTML_ZIPPED;
import static org.talend.components.google.drive.GoogleDriveMimeTypes.MimeType.JPG;
import static org.talend.components.google.drive.GoogleDriveMimeTypes.MimeType.OO_DOCUMENT;
import static org.talend.components.google.drive.GoogleDriveMimeTypes.MimeType.OO_PRESENTATION;
import static org.talend.components.google.drive.GoogleDriveMimeTypes.MimeType.OO_SPREADSHEET;
import static org.talend.components.google.drive.GoogleDriveMimeTypes.MimeType.PDF;
import static org.talend.components.google.drive.GoogleDriveMimeTypes.MimeType.PNG;
import static org.talend.components.google.drive.GoogleDriveMimeTypes.MimeType.POWERPOINT;
import static org.talend.components.google.drive.GoogleDriveMimeTypes.MimeType.RTF;
import static org.talend.components.google.drive.GoogleDriveMimeTypes.MimeType.SVG;
import static org.talend.components.google.drive.GoogleDriveMimeTypes.MimeType.TEXT;
import static org.talend.components.google.drive.GoogleDriveMimeTypes.MimeType.WORD;
import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newBoolean;
import static org.talend.daikon.properties.property.PropertyFactory.newEnum;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.talend.components.google.drive.GoogleDriveComponentProperties;
import org.talend.components.google.drive.GoogleDriveMimeTypes.MimeType;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

public class GoogleDriveGetProperties extends GoogleDriveComponentProperties {

    public Property<AccessMethod> fileAccessMethod = newEnum("fileAccessMethod", AccessMethod.class).setRequired();

    public Property<String> file = newString("file").setRequired();

    public Property<Boolean> storeToLocal = newBoolean("storeToLocal");

    public Property<String> outputFileName = newString("outputFileName").setRequired();

    // Advanced properties

    public Property<MimeType> exportDocument = newEnum("exportDocument", MimeType.class);

    public Property<MimeType> exportDrawing = newEnum("exportDrawing", MimeType.class);

    public Property<MimeType> exportPresentation = newEnum("exportPresentation", MimeType.class);

    public Property<MimeType> exportSpreadsheet = newEnum("exportSpreadsheet", MimeType.class);

    public Property<Boolean> setOutputExt = newBoolean("setOutputExt");

    public GoogleDriveGetProperties(String name) {
        super(name);
    }

    public void setupProperties() {
        super.setupProperties();

        Schema schema = SchemaBuilder.builder().record(GoogleDriveGetDefinition.COMPONENT_NAME).fields() //
                .name(GoogleDriveGetDefinition.RETURN_CONTENT).type(AvroUtils._bytes()).noDefault() //
                .endRecord();
        schema.addProp(SchemaConstants.TALEND_IS_LOCKED, "true");
        schemaMain.schema.setValue(schema);

        fileAccessMethod.setPossibleValues(AccessMethod.values());
        fileAccessMethod.setValue(AccessMethod.Name);
        file.setValue("");
        storeToLocal.setValue(false);
        outputFileName.setValue("");
        //
        exportDocument.setPossibleValues(HTML, HTML_ZIPPED, TEXT, RTF, OO_DOCUMENT, PDF, WORD, EPUB);
        exportDocument.setValue(WORD);
        exportDrawing.setPossibleValues(JPG, PNG, SVG, PDF);
        exportDrawing.setValue(PNG);
        exportPresentation.setPossibleValues(POWERPOINT, OO_PRESENTATION, PDF, TEXT);
        exportPresentation.setValue(PDF);
        exportSpreadsheet.setPossibleValues(EXCEL, OO_SPREADSHEET, PDF, CSV, CSV_TAB, HTML_ZIPPED);
        exportSpreadsheet.setValue(EXCEL);
        setOutputExt.setValue(false);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(file);
        mainForm.addColumn(fileAccessMethod);
        mainForm.addRow(storeToLocal);
        mainForm.addRow(widget(outputFileName).setWidgetType(Widget.FILE_WIDGET_TYPE));
        mainForm.addRow(schemaMain.getForm(Form.REFERENCE));

        Form advancedForm = getForm(Form.ADVANCED);
        advancedForm.addRow(exportDocument);
        advancedForm.addRow(exportDrawing);
        advancedForm.addRow(exportPresentation);
        advancedForm.addRow(exportSpreadsheet);
        advancedForm.addRow(setOutputExt);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (Form.MAIN.equals(form.getName())) {
            form.getWidget(outputFileName.getName()).setVisible(storeToLocal.getValue());
        }
        if (Form.ADVANCED.equals(form.getName())) {
            form.getWidget(setOutputExt.getName()).setVisible(storeToLocal.getValue());
        }
    }

    public void afterStoreToLocal() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.ADVANCED));
    }
}
