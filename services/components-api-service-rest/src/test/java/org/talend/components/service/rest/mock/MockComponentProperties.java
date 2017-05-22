// ==============================================================================
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
// ==============================================================================
package org.talend.components.service.rest.mock;

import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

/**
 * Mock component properties for tests.
 */
public class MockComponentProperties extends ComponentPropertiesImpl {

    public Property<String> tag = PropertyFactory.newString("tag");

    /**
     * Default constructor.
     * 
     * @param name the properties name.
     */
    public MockComponentProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addColumn(tag);
    }

    public ValidationResult validateTag() {
        return new ValidationResult(ValidationResult.Result.OK);
    }

    public ValidationResult validateTagId() {
        return new ValidationResult(ValidationResult.Result.OK, "Everything is OK");
    }

}
