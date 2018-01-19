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
package org.talend.components.google.drive.delete;

import static org.talend.daikon.properties.property.PropertyFactory.newBoolean;
import static org.talend.daikon.properties.property.PropertyFactory.newEnum;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.talend.components.google.drive.GoogleDriveComponentProperties;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;

public class GoogleDriveDeleteProperties extends GoogleDriveComponentProperties {

    public Property<AccessMethod> deleteMode = newEnum("deleteMode", AccessMethod.class);

    public Property<String> file = newString("file").setRequired();

    public Property<Boolean> useTrash = newBoolean("useTrash");

    public GoogleDriveDeleteProperties(String name) {
        super(name);
    }

    public void setupProperties() {
        super.setupProperties();

        Schema schema = SchemaBuilder.builder().record(GoogleDriveDeleteDefinition.COMPONENT_NAME).fields() //
                .name(GoogleDriveDeleteDefinition.RETURN_FILE_ID)//
                .prop(SchemaConstants.TALEND_IS_LOCKED, "true")//
                .type().nullable().stringType().noDefault() //
                .endRecord();
        schema.addProp(SchemaConstants.TALEND_IS_LOCKED, "true");
        schemaMain.schema.setValue(schema);

        deleteMode.setPossibleValues(AccessMethod.values());
        deleteMode.setValue(AccessMethod.Name);
        file.setValue("");
        useTrash.setValue(true);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(file);
        mainForm.addColumn(deleteMode);
        mainForm.addRow(useTrash);
        mainForm.addRow(schemaMain.getForm(Form.REFERENCE));
    }

}
