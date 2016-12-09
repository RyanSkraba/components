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

package ${package};

import ${packageTalend}.common.dataset.DatasetProperties;
import ${packageDaikon}.properties.PropertiesImpl;
import ${packageDaikon}.properties.ReferenceProperties;
import ${packageTalend}.common.SchemaProperties;
import ${packageDaikon}.properties.presentation.Form;
import ${packageDaikon}.properties.property.Property;

public class ${componentNameClass}DatasetProperties extends PropertiesImpl implements DatasetProperties<${componentNameClass}DatastoreProperties> {

    public final transient ReferenceProperties<${componentNameClass}DatastoreProperties> datastoreRef = new ReferenceProperties<>("datastoreRef",
        ${componentNameClass}DatastoreDefinition.NAME);

    public ${componentNameClass}DatasetProperties(String name) {
        super(name);
    }

    @Override
    public ${componentNameClass}DatastoreProperties getDatastoreProperties() {
        return datastoreRef.getReference();
    }

    @Override public void setDatastoreProperties(${componentNameClass}DatastoreProperties datastoreProperties) {
        datastoreRef.setReference(datastoreProperties);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
    }
}
