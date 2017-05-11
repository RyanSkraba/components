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
package org.talend.components.azurestorage.blob.tazurestoragelist;

import static org.talend.daikon.properties.presentation.Widget.widget;

import java.util.Collections;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.Schema.Field;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.azurestorage.blob.AzureStorageBlobProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

public class TAzureStorageListProperties extends AzureStorageBlobProperties {

    private static final long serialVersionUID = 8673604530837504643L;

    protected transient PropertyPathConnector MAIN_NAME = new PropertyPathConnector(Connector.MAIN_NAME, "schema");

	public static final Schema outOfBandSchema;
    
	/**
     * Sets Out of band schema. This schema is not supposed to be changed by user
     */
    static {       
        Field currentBlobField = new Field("CURRENT_BLOB", Schema.create(Schema.Type.STRING), null, (Object) null);
        outOfBandSchema = Schema.createRecord("OutOfBand", null, null, false);
        outOfBandSchema.setFields(Collections.singletonList(currentBlobField));
    }

    public TAzureStorageListProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(widget(remoteBlobs).setWidgetType(Widget.TABLE_WIDGET_TYPE));
        mainForm.addRow(schema.getForm(Form.REFERENCE));
        mainForm.addRow(dieOnError);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        Schema s = SchemaBuilder.record("Main").fields().name("BlobName").prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "300")// $NON-NLS-3$
                .prop(SchemaConstants.TALEND_IS_LOCKED, "true")//$NON-NLS-1$
                .type(AvroUtils._string()).noDefault().endRecord();
        schema.schema.setValue(s);
    }
    
    @Override
    public Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        return Collections.singleton(new PropertyPathConnector(Connector.MAIN_NAME, "schema")); //$NON-NLS-1$
    }

}
