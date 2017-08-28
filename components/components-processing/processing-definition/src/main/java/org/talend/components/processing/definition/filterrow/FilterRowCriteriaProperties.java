package org.talend.components.processing.definition.filterrow;

import org.talend.components.api.component.ISchemaListener;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class FilterRowCriteriaProperties extends PropertiesImpl {

    public FilterRowCriteriaProperties(String name) {
        super(name);
    }

    /**
     * This enum will be filled with the name of the input columns.
     */
    public Property<String> columnName = PropertyFactory.newString("columnName", "");

    /**
     * This enum represent the function applicable to the input value before making the comparison. The functions
     * displayed by the UI are dependent of the type of the columnName.
     * 
     * If columnName's type is numerical (Integer, Long, Float or Double), Function will contain "ABS_VALUE" and "EMPTY"
     * 
     * If columnName's type is String, Function will contain "LC", "UC", "LCFIRST", "UCFIRST", "LENGTH", "MATCH" and
     * "EMPTY"
     * 
     * For any other case, Function will contain "EMPTY".
     */
    public Property<String> function = PropertyFactory.newString("function", "EMPTY", "EMPTY")
            .setPossibleValues(ConditionsRowConstant.ALL_FUNCTIONS);

    /**
     * This enum represent the comparison function. The operator displayed by the UI are dependent of the function
     * selected by the user.
     * 
     * If the function is "MATCH", Operator will contain only "==" and "!=".
     * 
     * If the function is not "MATCH" but the columnName's type is String, Operator will contain only "==", "!=",
     * "<", "<=", ">", ">=" and "CONTAINS".
     * 
     * For any other case, Operator will contain "==", "!=", "<", "<=", ">" and ">=".
     */
    public Property<String> operator = PropertyFactory.newString("operator", "==")
            .setPossibleValues(ConditionsRowConstant.DEFAULT_OPERATORS);

    /**
     * This field is the reference value of the comparison. It will be filled directly by the user.
     */
    public Property<String> value = PropertyFactory.newString("value", "");

    public transient ISchemaListener schemaListener;

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(Widget.widget(columnName).setWidgetType(Widget.DATALIST_WIDGET_TYPE));
        mainForm.addColumn(function);
        mainForm.addColumn(operator);
        mainForm.addColumn(value);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        schemaListener = new ISchemaListener() {

            @Override
            public void afterSchema() {
                updateFuntionColumn();
                updateOperatorColumn();
            }

        };
    }

    public void updateFuntionColumn() {
        function.setPossibleValues(ConditionsRowConstant.ALL_FUNCTIONS);
    }

    public void updateOperatorColumn() {
        operator.setPossibleValues(ConditionsRowConstant.DEFAULT_OPERATORS);
    }
}
