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
package org.talend.components.common;

import static org.talend.daikon.properties.presentation.Widget.widget;

import java.util.Arrays;
import java.util.List;

import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class EncodingTypeProperties extends PropertiesImpl {

    public static final String ENCODING_TYPE_UTF_8 = "UTF-8";

    public static final String ENCODING_TYPE_ISO_8859_15 = "ISO-8859-15";

    public static final String ENCODING_TYPE_CUSTOM = "CUSTOM";

    public Property<String> encodingType = PropertyFactory.newString("encodingType");

    public Property<String> customEncoding = PropertyFactory.newString("customEncoding").setRequired();

    public EncodingTypeProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        List<String> defaultEncodings = getDefaultEncodings();
        if (defaultEncodings.size() > 0) {
            encodingType.setPossibleValues(getProperties());
            encodingType.setValue(defaultEncodings.get(0));
        }
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form form = Form.create(this, Form.MAIN);
        form.addRow(widget(encodingType).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
        form.addColumn(customEncoding);
    }

    public void afterEncodingType() {
        refreshLayout(getForm(Form.MAIN));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form.getName().equals(Form.MAIN)) {
            boolean isCustom = ENCODING_TYPE_CUSTOM.equals(encodingType.getValue());
            form.getWidget(customEncoding.getName()).setHidden(!isCustom);
        }
    }

    public List<String> getDefaultEncodings() {
        return Arrays.asList(ENCODING_TYPE_ISO_8859_15, ENCODING_TYPE_UTF_8, ENCODING_TYPE_CUSTOM);
    }

    /**
     * Get the value of encoding based on encoding type
     */
    public String getEncoding() {
        if (!ENCODING_TYPE_CUSTOM.equals(encodingType.getValue())) {
            return encodingType.getValue();
        } else {
            return customEncoding.getValue();
        }
    }
}
