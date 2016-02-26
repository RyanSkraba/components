#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )


package ${package};

import org.talend.components.api.component.StudioConstants;
import org.talend.components.api.component.Connector.ConnectorType;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.PropertyFactory;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.schema.SchemaElement.Type;
import org.talend.daikon.schema.SchemaFactory;

/**
 * Define properties and layout for the ${componentName} component
 */
public class ${componentName}Properties extends ComponentProperties {

    public Property filename = PropertyFactory.newString("filename"); //$NON-NLS-1$
    public SchemaProperties schema = new SchemaProperties("schema");

    public ${componentName}Properties(String name) {
        super(name);
    }
    
    @Override
    public void setupProperties() {
        super.setupProperties();
        // create an default shema element that is
        schema.schema.addChild(SchemaFactory.newSchemaElement(Type.STRING, "line"));
        schema.schema.setTaggedValue(StudioConstants.CONNECTOR_TYPE_SCHEMA_KEY, ConnectorType.FLOW);
    }
    

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form form = Form.create(this, Form.MAIN, "File Selection");
        form.addRow(schema.getForm(Form.REFERENCE));
        form.addRow(filename);
    }

}
