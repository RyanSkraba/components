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
 * The ComponentProperties subclass provided by a component stores the 
 * configuration of a component and is used for:
 * 
 * <ol>
 * <li>Specifying the format and type of information (properties) that is 
 *     provided at design-time to configure a component for run-time,</li>
 * <li>Validating the properties of the component at design-time,</li>
 * <li>Containing the untyped values of the properties, and</li>
 * <li>All of the UI information for laying out and presenting the 
 *     properties to the user.</li>
 * </ol>
 * 
 * The ${componentName}Properties has two properties:
 * <ol>
 * <li>{code filename}, a simple property which is a String containing the 
 *     file path that this component will read.</li>
 * <li>{code schema}, an embedded property referring to a Schema.</li>
 * </ol>
 */
public class ${componentName}Properties extends ComponentProperties {

    public Property filename = PropertyFactory.newString("filename"); //$NON-NLS-1$
    public SchemaProperties schema = new SchemaProperties("schema"); //$NON-NLS-1$

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
