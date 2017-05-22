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
package org.talend.components.fullexample.datastore;

import org.talend.components.common.datastore.DatastoreProperties;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

/**
 * Properties that can be used to configure a FullExampleDatastore.
 */
public class FullExampleDatastoreProperties extends PropertiesImpl implements DatastoreProperties {

    public Property<String> tag = PropertyFactory.newString("tag");

    public Property<Integer> tagId = PropertyFactory.newInteger("tagId");

    public FullExampleDatastoreProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addColumn(tag);
        mainForm.addColumn(tagId);
    }

    public ValidationResult validateTag() {
        return new ValidationResult();
    }

    public ValidationResult validateTagId() {
        return new ValidationResult(ValidationResult.Result.OK, "tagId is OK");
    }

}
