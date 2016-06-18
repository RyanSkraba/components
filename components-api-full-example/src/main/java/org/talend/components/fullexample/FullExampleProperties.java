package org.talend.components.fullexample;

import static org.talend.daikon.properties.presentation.Widget.*;
import static org.talend.daikon.properties.property.PropertyFactory.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.EnumProperty;
import org.talend.daikon.properties.property.Property;

/**
 * The ComponentProperties subclass provided by a component stores the
 * configuration of a component and is used for:
 * 
 * <ol>
 * <li>Specifying the format and type of information (properties) that is
 * provided at design-time to configure a component for run-time,</li>
 * <li>Validating the properties of the component at design-time,</li>
 * <li>Containing the untyped values of the properties, and</li>
 * <li>All of the UI information for laying out and presenting the
 * properties to the user.</li>
 * </ol>
 * 
 * The FullExampleProperties has two properties:
 * <ol>
 * <li>{code filename}, a simple property which is a String containing the
 * file path that this component will read.</li>
 * <li>{code schema}, an embedded property referring to a Schema.</li>
 * </ol>
 */
public class FullExampleProperties extends ComponentPropertiesImpl {

    /**
     * table property to use with table widget.
     */
    static final String POPUP_FORM_NAME = "popup";

    static class TableProperties extends PropertiesImpl {

        enum ColEnum {
            FOO,
            BAR
        }

        public final Property<String> colString = newString("colString");

        public final EnumProperty<ColEnum> colEnum = newEnum("colEnum", ColEnum.class);

        public final Property<Boolean> colBoolean = newBoolean("colBoolean");

        public TableProperties(String name) {
            super(name);
        }

    }

    /** use the default widget for this String type */
    public final Property<String> stringProp = newString("stringProp", "initialValue");

    /** this shall hide stringProp widget according to it value. */
    public final Property<Boolean> hideStringPropProp = newBoolean("hideStringPropProp", false);

    /** property to check the {@link WidgetType#NAME_SELECTION_AREA} and {@link WidgetType#NAME_SELECTION_REFERENCE} widgets. */
    public final Property<String> multipleSelectionProp = newProperty("multipleSelectionProp");

    // TODO some Component Reference widget use case.

    /** checking {@link WidgetType#BUTTON} */
    public final PresentationItem showNewForm = new PresentationItem("showNewForm", "Show new form");

    /** checking {@link WidgetType#TABLE} */
    public final TableProperties tableProp = new TableProperties("tableProp");

    /** checking {@link WidgetType#FILE} */
    public final Property<String> filepathProp = newString("filepathProp");

    /** checking {@link WidgetType#HIDDEN_TEXT} */
    public final Property<String> hiddenTextProp = newString("hiddenTextProp");

    /**
     * uses 2 widgets, {@link WidgetType#SCHEMA_EDITOR} in the Main form and {@link WidgetType#SCHEMA_REFERENCE} on the REFERENCE
     * form
     */
    public final Property<Schema> schema = newSchema("schema"); //$NON-NLS-1$

    public final PresentationItem validateAllCallbackCalled = new PresentationItem("validateAllCallbackCalled",
            "Validate All Callbacks called");

    private List<String> methodCalled = new ArrayList<>();

    public FullExampleProperties(String name) {
        super(name);
    }

    private void thisMethodWasCalled() {
        methodCalled.add(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        // setup multipleSelectionProp
        ArrayList<NamedThing> multipleSelectableList = new ArrayList<NamedThing>();
        multipleSelectableList.add(new SimpleNamedThing("foo"));
        multipleSelectableList.add(new SimpleNamedThing("bar"));
        multipleSelectableList.add(new SimpleNamedThing("foobar"));
        multipleSelectionProp.setPossibleValues(multipleSelectableList);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(stringProp);
        mainForm.addRow(widget(schema).setWidgetType(Widget.SCHEMA_EDITOR_WIDGET_TYPE));
        mainForm.addRow(widget(schema).setWidgetType(Widget.SCHEMA_REFERENCE_WIDGET_TYPE));
        mainForm.addRow(widget(multipleSelectionProp).setWidgetType(Widget.NAME_SELECTION_AREA_WIDGET_TYPE));
        mainForm.addRow(widget(multipleSelectionProp).setWidgetType(Widget.NAME_SELECTION_REFERENCE_WIDGET_TYPE));
        mainForm.addRow(widget(showNewForm).setWidgetType(Widget.BUTTON_WIDGET_TYPE));
        Form popUpForm = new Form(this, POPUP_FORM_NAME);
        showNewForm.setFormtoShow(popUpForm);
        mainForm.addColumn(widget(tableProp).setWidgetType(Widget.TABLE_WIDGET_TYPE));
        mainForm.addColumn(widget(hiddenTextProp).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));
        mainForm.addColumn(widget(filepathProp).setWidgetType(Widget.FILE_WIDGET_TYPE));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (Form.MAIN.equals(form.getName())) {
            form.getWidget(stringProp.getName()).setHidden(hideStringPropProp.getValue());
        } // else do nothing
    }

    // callback for beforeFormPresent
    public void beforeFormPresentPopup() {
        thisMethodWasCalled();
    }

    public void afterHideStringPropProp() {
        thisMethodWasCalled();
        refreshLayout(getForm(Form.MAIN));
    }

    // implements all dynamic callbacks for the string property and it's default widget.
    // stringProp
    public void validateStringProp() {
        thisMethodWasCalled();
    }

    public void beforeStringProp() {
        thisMethodWasCalled();
    }

    public void beforeStringPropPresent() {
        thisMethodWasCalled();
    }

    public void afterStringProp() {
        thisMethodWasCalled();
    }

    // Schema
    public void validateSchema() {
        thisMethodWasCalled();
    }

    public void beforeSchema() {
        thisMethodWasCalled();
    }

    public void beforeSchemaPresent() {
        thisMethodWasCalled();
    }

    public void afterSchema() {
        thisMethodWasCalled();
    }

    // MultipleSelectionProp
    public void validateMultipleSelectionProp() {
        thisMethodWasCalled();
    }

    public void beforeMultipleSelectionProp() {
        thisMethodWasCalled();
    }

    public void beforeMultipleSelectionPropPresent() {
        thisMethodWasCalled();
    }

    public void afterMultipleSelectionProp() {
        thisMethodWasCalled();
    }

    // FilepathProp
    public void validateFilepathProp() {
        thisMethodWasCalled();
    }

    public void beforeFilepathProp() {
        thisMethodWasCalled();
    }

    public void beforeFilepathPropPresent() {
        thisMethodWasCalled();
    }

    public void afterFilepathProp() {
        thisMethodWasCalled();
    }

    // HiddenTextProp
    public void validateHiddenTextProp() {
        thisMethodWasCalled();
    }

    public void beforeHiddenTextProp() {
        thisMethodWasCalled();
    }

    public void beforeHiddenTextPropPresent() {
        thisMethodWasCalled();
    }

    public void afterHiddenTextProp() {
        thisMethodWasCalled();
    }

    // ShowNewForm
    public void validateShowNewForm() {
        thisMethodWasCalled();
    }

    public void beforeShowNewForm() {
        thisMethodWasCalled();
    }

    public void beforeShowNewFormPresent() {
        thisMethodWasCalled();
    }

    public void afterShowNewForm() {
        thisMethodWasCalled();
    }

    public ValidationResult validateValidateAllCallbackCalled() {
        if (methodCalled.size() == 25) {
            return ValidationResult.OK;
        } else {
            return new ValidationResult().setStatus(Result.ERROR).setMessage("some method where not called :" + methodCalled);
        }
    }

}
