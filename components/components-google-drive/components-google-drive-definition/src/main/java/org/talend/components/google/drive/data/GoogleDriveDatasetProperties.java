package org.talend.components.google.drive.data;

import static org.talend.components.google.drive.data.GoogleDriveDatasetProperties.ListMode.Both;
import static org.talend.daikon.properties.property.PropertyFactory.newBoolean;
import static org.talend.daikon.properties.property.PropertyFactory.newEnum;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

import java.util.Date;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.talend.components.common.SchemaProperties;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.google.drive.list.GoogleDriveListDefinition;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

public class GoogleDriveDatasetProperties extends PropertiesImpl implements DatasetProperties<GoogleDriveDatastoreProperties> {

    public ReferenceProperties<GoogleDriveDatastoreProperties> datastore = new ReferenceProperties<>("datastore",
            GoogleDriveDatastoreDefinition.NAME);

    public Property<String> folder = newString("folder").setRequired();

    public Property<Boolean> includeSubDirectories = newBoolean("includeSubDirectories");

    public enum ListMode {
        Files,
        Directories,
        Both
    }

    public Property<ListMode> listMode = newEnum("listMode", ListMode.class);

    public Property<Boolean> includeTrashedFiles = newBoolean("includeTrashedFiles");

    public SchemaProperties main = new SchemaProperties("main");

    public GoogleDriveDatasetProperties(String name) {
        super(name);
    }

    @Override
    public GoogleDriveDatastoreProperties getDatastoreProperties() {
        return datastore.getReference();
    }

    @Override
    public void setDatastoreProperties(GoogleDriveDatastoreProperties datastoreProperties) {
        datastore.setReference(datastoreProperties);
        afterDatastore();
    }

    @Override
    public void setupProperties() {
        Schema s = SchemaBuilder.builder().record(GoogleDriveListDefinition.COMPONENT_NAME).fields() //
                .name(GoogleDriveListDefinition.RETURN_ID).prop(SchemaConstants.TALEND_IS_LOCKED, "true").type().nullable()
                .stringType().noDefault()//
                .name(GoogleDriveListDefinition.RETURN_NAME).prop(SchemaConstants.TALEND_IS_LOCKED, "true").type().nullable()
                .stringType().noDefault()//
                .name(GoogleDriveListDefinition.RETURN_MIME_TYPE).prop(SchemaConstants.TALEND_IS_LOCKED, "true").type().nullable()
                .stringType().noDefault()//
                .name(GoogleDriveListDefinition.RETURN_MODIFIED_TIME).prop(SchemaConstants.TALEND_IS_LOCKED, "true")
                .prop(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy'-'MM'-'dd'T'HH':'mm':'ss'Z'")//
                .prop(SchemaConstants.JAVA_CLASS_FLAG, Date.class.getCanonicalName()) //
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255")//
                .type(AvroUtils._logicalTimestamp()).noDefault() //
                .name(GoogleDriveListDefinition.RETURN_SIZE).prop(SchemaConstants.TALEND_IS_LOCKED, "true").type().nullable()
                .longType().noDefault() //
                .name(GoogleDriveListDefinition.RETURN_KIND).prop(SchemaConstants.TALEND_IS_LOCKED, "true").type().nullable()
                .stringType().noDefault() //
                .name(GoogleDriveListDefinition.RETURN_TRASHED).prop(SchemaConstants.TALEND_IS_LOCKED, "true").type().nullable()
                .booleanType().noDefault() //
                // TODO This should be a List<String>
                .name(GoogleDriveListDefinition.RETURN_PARENTS).prop(SchemaConstants.TALEND_IS_LOCKED, "true").type().nullable()
                .stringType().noDefault() //
                .name(GoogleDriveListDefinition.RETURN_WEB_VIEW_LINK).prop(SchemaConstants.TALEND_IS_LOCKED, "true").type()
                .nullable().stringType().noDefault() //
                .endRecord();
        s.addProp(SchemaConstants.TALEND_IS_LOCKED, "true");
        main.schema.setValue(s);
        folder.setValue("root");
        listMode.setPossibleValues(ListMode.values());
        listMode.setValue(Both);
        includeSubDirectories.setValue(true);
        includeTrashedFiles.setValue(false);
    }

    @Override
    public void setupLayout() {
        Form mainForm = Form.create(this, Form.MAIN);
        mainForm.addRow(folder);
        mainForm.addRow(Widget.widget(listMode).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
        mainForm.addRow(includeSubDirectories);
        mainForm.addColumn(includeTrashedFiles);
    }

    public Schema getSchema() {
        return main.schema.getValue();
    }

    public void afterDatastore() {
        refreshLayout(getForm(Form.MAIN));
    }

}
