#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )


        package ${package};

import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.PropertyFactory;
import org.talend.daikon.properties.presentation.Form;

/**
 * Define properties and layout for the ${componentName} component
 */
public class ${componentName}Properties extends ComponentProperties {

    public Property filename = PropertyFactory.newString("filename"); //$NON-NLS-1$

    public ${componentName}Properties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form form = Form.create(this, Form.MAIN, "File Selection");
        form.addRow(filename);
    }

}
