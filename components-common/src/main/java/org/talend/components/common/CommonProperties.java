package org.talend.components.common;

import static org.talend.components.api.properties.presentation.Layout.layout;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.Property;
import org.talend.components.api.properties.presentation.Form;

/**
 * Properties common to all components.
 */

public class CommonProperties extends ComponentProperties {

    public Property<String>    componentName = new Property<String>("componentName", "Component Name");

    public static final String MAIN          = "Main";

    public CommonProperties() {
        super();
        setupLayout();
    }

    @Override
    protected void setupLayout() {
        super.setupLayout();

        Form main = Form.create(this, MAIN, "Common");
        main.addChild(componentName, layout().setRow(1));
        refreshLayout(main);
    }
}
