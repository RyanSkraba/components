
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
package org.talend.components.filterrow;

import java.util.HashSet;
import java.util.Set;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.daikon.properties.presentation.Form;

/**
 * The ComponentProperties subclass provided by a component stores the configuration of a component and is used for:
 * 
 * <ol>
 * <li>Specifying the format and type of information (properties) that is provided at design-time to configure a
 * component for run-time,</li>
 * <li>Validating the properties of the component at design-time,</li>
 * <li>Containing the untyped values of the properties, and</li>
 * <li>All of the UI information for laying out and presenting the properties to the user.</li>
 * </ol>
 * 
 * The TFilterRowProperties has two properties:
 * <ol>
 * <li>{code filename}, a simple property which is a String containing the file path that this component will read.</li>
 * <li>{code schema}, an embedded property referring to a Schema.</li>
 * </ol>
 */
public class TFilterRowProperties extends FixedConnectorsComponentProperties {

    public SchemaProperties schemaMain = new SchemaProperties("schemaMain");

    public SchemaProperties schemaFlow = new SchemaProperties("schemaFlow"); //$NON-NLS-1$

    public SchemaProperties schemaReject = new SchemaProperties("schemaReject"); //$NON-NLS-1$

    protected transient PropertyPathConnector mainConnector = new PropertyPathConnector(Connector.MAIN_NAME, "schemaMain");

    protected transient PropertyPathConnector flowConnector = new PropertyPathConnector(Connector.MAIN_NAME, "schemaFlow");

    protected transient PropertyPathConnector rejectConnector = new PropertyPathConnector(Connector.REJECT_NAME, "schemaReject");

    public TFilterRowProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        // Code for property initialization goes here
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form form = Form.create(this, Form.MAIN);
        form.addRow(schemaMain.getForm(Form.REFERENCE));
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputComponent) {
        HashSet<PropertyPathConnector> connectors = new HashSet<>();
        if (isOutputComponent) {
            connectors.add(flowConnector);
            connectors.add(rejectConnector);
        } else {
            connectors.add(mainConnector);
        }
        return connectors;
    }

}
