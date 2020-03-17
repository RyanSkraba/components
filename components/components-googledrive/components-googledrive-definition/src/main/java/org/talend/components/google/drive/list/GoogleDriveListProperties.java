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
package org.talend.components.google.drive.list;

import static org.talend.daikon.properties.presentation.Form.MAIN;
import static org.talend.daikon.properties.property.PropertyFactory.newBoolean;
import static org.talend.daikon.properties.property.PropertyFactory.newEnum;
import static org.talend.daikon.properties.property.PropertyFactory.newInteger;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

import java.util.Date;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.talend.components.api.component.Connector;
import org.talend.components.google.drive.GoogleDriveComponentProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

public class GoogleDriveListProperties extends GoogleDriveComponentProperties {

    public Property<AccessMethod> folderAccessMethod = newEnum("folderAccessMethod", AccessMethod.class).setRequired();

    public Property<String> folder = newString("folder").setRequired();

    public Property<Boolean> includeSubDirectories = newBoolean("includeSubDirectories");

    public enum ListMode {
        Files,
        Directories,
        Both
    }

    public Property<ListMode> listMode = newEnum("listMode", ListMode.class);

    public Property<Boolean> includeTrashedFiles = newBoolean("includeTrashedFiles");

    public Property<Integer> pageSize = newInteger("pageSize");

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(GoogleDriveListProperties.class);

    /*
     * TODO new feature to add: orderBy [in main]
     * 
     * orderBy string A comma-separated list of sort keys. Valid keys are 'createdTime', 'folder', 'modifiedByMeTime',
     * 'modifiedTime', 'name', 'quotaBytesUsed', 'recency', 'sharedWithMeTime', 'starred', and 'viewedByMeTime'. Each key
     * sorts ascending by default, but may be reversed with the 'desc' modifier. Example usage: ?orderBy=folder,modifiedTime
     * desc,name. Please note that there is a current limitation for users with approximately one million files in which the
     * requested sort order is ignored.
     */

    /*
     * TODO new feature to add: spaces [in advanced]
     * 
     * spaces string A comma-separated list of spaces to query within the corpus. Supported values are 'drive',
     * 'appDataFolder' and 'photos'.
     */

    public GoogleDriveListProperties(String name) {
        super(name);
    }

    public Schema getSchema() {
        return schemaMain.schema.getValue();
    }

    @Override
    public Schema getSchema(Connector connector, boolean isOutputConnection) {
        return getSchema();
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        Schema schema = SchemaBuilder.builder().record(GoogleDriveListDefinition.COMPONENT_NAME).fields() //
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
        schema.addProp(SchemaConstants.TALEND_IS_LOCKED, "true");
        schemaMain.schema.setValue(schema);

        folderAccessMethod.setPossibleValues(AccessMethod.values());
        folderAccessMethod.setValue(AccessMethod.Name);
        folder.setValue("root");
        listMode.setPossibleValues(ListMode.values());
        listMode.setValue(ListMode.Files);
        pageSize.setValue(1000);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = getForm(MAIN);
        mainForm.addRow(folder);
        mainForm.addColumn(folderAccessMethod);
        mainForm.addRow(Widget.widget(listMode).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
        mainForm.addRow(includeSubDirectories);
        mainForm.addRow(schemaMain.getForm(Form.REFERENCE));

        Form advancedForm = getForm(Form.ADVANCED);
        advancedForm.addRow(includeTrashedFiles);
        advancedForm.addRow(pageSize);
    }

}
