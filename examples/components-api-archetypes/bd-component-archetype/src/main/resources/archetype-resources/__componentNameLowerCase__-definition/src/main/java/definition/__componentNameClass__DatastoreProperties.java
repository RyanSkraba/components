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

package ${package}.definition;

import static ${packageDaikon}.properties.presentation.Widget.widget;
import static ${packageDaikon}.properties.property.PropertyFactory.*;

import ${packageTalend}.common.datastore.DatastoreProperties;
import ${packageDaikon}.properties.PropertiesImpl;
import ${packageDaikon}.properties.presentation.Form;
import ${packageDaikon}.properties.property.Property;
import ${packageDaikon}.properties.property.PropertyFactory;

public class ${componentNameClass}DatastoreProperties extends PropertiesImpl implements DatastoreProperties {

    /**
     * Example of a new property
     * public Property<Integer> recordCount = PropertyFactory.newInteger("recordCount").setRequired();
     * public Property<Boolean> showCountRecord = PropertyFactory.newBoolean("showCountRecord");
     */

    public ${componentNameClass}DatastoreProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm=new Form(this,Form.MAIN);
        /**
         * Example to add property field in the form :
         * mainForm.addRow(showCountRecord);
         * mainForm.addRow(recordCount);
         */
    }
}
