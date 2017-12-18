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

import ${packageTalend}.common.dataset.DatasetProperties;
import ${packageDaikon}.properties.PropertiesImpl;
import ${packageDaikon}.properties.ReferenceProperties;
import ${packageTalend}.common.SchemaProperties;
import ${packageDaikon}.properties.presentation.Form;
import ${packageDaikon}.properties.property.Property;

public class ${componentNameClass}DatasetProperties extends PropertiesImpl implements DatasetProperties<${componentNameClass}DatastoreProperties> {

    public final ReferenceProperties<${componentNameClass}DatastoreProperties> datastoreRef = new ReferenceProperties<>("datastoreRef",
            ${componentNameClass}DatastoreDefinition.NAME);

    /**
     * Example of a new property
     * public Property<Integer> recordCount = PropertyFactory.newInteger("recordCount").setRequired();
     * public Property<Boolean> showCountRecord = PropertyFactory.newBoolean("showCountRecord");
     */

    public ${componentNameClass}DatasetProperties(String name) {
        super(name);
    }

    @Override
    public ${componentNameClass}DatastoreProperties getDatastoreProperties() {
        return datastoreRef.getReference();
    }

    @Override
    public void setDatastoreProperties(${componentNameClass}DatastoreProperties datastoreProperties) {
        datastoreRef.setReference(datastoreProperties);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        /**
         * Example to add property field in the form :
         * mainForm.addRow(showCountRecord);
         * mainForm.addRow(recordCount);
         */
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        /**
         * Example to hide or show a property in a form
         * if (form.getName().equals(Form.MAIN)) {
         form.getWidget(recordCount.getName()).setHidden(showCountRecord);
         }
         */
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        /**
         * Example to set default value to a property in a form :
         * recordCount.setValue(20);
         * showCountRecord.setValue(false);
         */
    }

    /*
        Trigger example to refresh the ui
    */
    /**
     * public  void afterShowCountRecord() {refreshLayout(getForm(Form.MAIN));}
     */


}
