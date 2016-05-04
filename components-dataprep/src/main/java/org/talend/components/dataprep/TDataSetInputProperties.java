// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.dataprep;

import org.apache.avro.Schema;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.PropertyFactory;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * The ComponentProperties subclass provided by a component stores the 
 * configuration of a component and is used for:
 * 
 * <ol>
 * <li>Specifying the format and type of information (properties) that is 
 *     provided at design-time to configure a component for run-time,</li>
 * <li>Validating the properties of the component at design-time,</li>
 * <li>Containing the untyped values of the properties, and</li>
 * <li>All of the UI information for laying out and presenting the 
 *     properties to the user.</li>
 * </ol>
 * 
 * The TDataSetInputProperties has two properties:
 * <ol>
 * <li>{code filename}, a simple property which is a String containing the 
 *     file path that this component will read.</li>
 * <li>{code schema}, an embedded property referring to a Schema.</li>
 * </ol>
 */
public class TDataSetInputProperties extends FixedConnectorsComponentProperties {

    public SchemaProperties schema = new SchemaProperties("schema");
    protected transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "schema");
    public Property dataSetName = PropertyFactory.newString("dataSetName");
    public Property login = PropertyFactory.newString("login");
    public Property pass = PropertyFactory.newString("pass");
    public Property url = PropertyFactory.newString("url");
    public PresentationItem fetchSchema = new PresentationItem("fetchSchema", "FetchSchema");

    public TDataSetInputProperties(String name) {
        super(name);
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean b) {

        return Collections.singleton(MAIN_CONNECTOR);
    }

    @Override
    public void setupLayout() {
        Form form = new Form(this, Form.MAIN);
        form.addRow(schema.getForm(Form.REFERENCE));
        form.addRow(url);
        form.addRow(login);
        form.addRow(Widget.widget(pass).setWidgetType(Widget.WidgetType.HIDDEN_TEXT));
        form.addRow(dataSetName);
        form.addRow(Widget.widget(fetchSchema).setWidgetType(Widget.WidgetType.BUTTON));
    }

    private String removeQuotes(String str) {
        String some = str.substring(1,str.length()-1);
        return some;
    }

    public void afterFetchSchema() throws IOException {
        if (login.getStringValue().isEmpty() && dataSetName.getStringValue().isEmpty()
                && url.getStringValue().isEmpty() && pass.getStringValue().isEmpty()) {
            //TODO Some error here
        } else {
            DataPrepConnectionHandler connectionHandler = new DataPrepConnectionHandler(
                    removeQuotes(url.getStringValue()), removeQuotes(login.getStringValue()),
                    removeQuotes(pass.getStringValue()), removeQuotes(dataSetName.getStringValue()));
            List<Column> columnList = null;
            connectionHandler.connect();
            columnList = connectionHandler.readSourceSchema();
            DataPrepField[] scemaRow = new DataPrepField[columnList.size()];
            int i = 0;
            for (Column column: columnList) {
                scemaRow[i] = new DataPrepField(column.getName(),column.getType(), null);
                i++;
            }
            AvroRegistry avroRegistry = DataPrepAvroRegistry.getDataPrepInstance();
            this.schema.schema.setValue(avroRegistry.inferSchema(scemaRow));
        }
    }

    public Schema getSchema() {
        return (Schema) schema.schema.getValue();
    }
}
