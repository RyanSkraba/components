package org.talend.components.salesforce;

import static org.talend.daikon.properties.PropertyFactory.*;

import java.util.List;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.presentation.Widget.WidgetType;

public class UpsertRelationTable extends ComponentProperties {

    /**
     * 
     */
    private static final TypeLiteral<List<String>> LIST_STRING_TYPE = new TypeLiteral<List<String>>() {// empty
    };

    /**
     * 
     */
    private static final TypeLiteral<List<Boolean>> LIST_BOOLEAN_TYPE = new TypeLiteral<List<Boolean>>() {// empty
    };

    private boolean usePolymorphic;

    /**
     * named constructor to be used is these properties are nested in other properties. Do not subclass this method for
     * initialization, use {@link #init()} instead.
     *
     * @param name
     */
    public UpsertRelationTable(String name) {
        super(name);
    }

    public Property<List<String>> columnName = newProperty(LIST_STRING_TYPE, "columnName");

    public Property<List<String>> lookupFieldName = newProperty(LIST_STRING_TYPE, "lookupFieldName");

    public Property<List<String>> lookupFieldModuleName = newProperty(LIST_STRING_TYPE, "lookupFieldModuleName");

    public Property<List<String>> lookupFieldExternalIdName = newProperty(LIST_STRING_TYPE, "lookupFieldExternalIdName");

    public Property<List<Boolean>> polymorphic = newProperty(LIST_BOOLEAN_TYPE, "polymorphic");

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addColumn(new Widget(columnName).setWidgetType(WidgetType.ENUMERATION));
        mainForm.addColumn(lookupFieldName);
        mainForm.addColumn(lookupFieldModuleName);
        if (usePolymorphic) {
            mainForm.addColumn(polymorphic);
        }
        mainForm.addColumn(lookupFieldExternalIdName);
    }

    /**
     * Getter for usePolymorphic.
     * 
     * @return the usePolymorphic
     */
    public boolean isUsePolymorphic() {
        return this.usePolymorphic;
    }

    /**
     * Sets the usePolymorphic.
     * 
     * @param usePolymorphic the usePolymorphic to set
     */
    public void setUsePolymorphic(boolean usePolymorphic) {
        this.usePolymorphic = usePolymorphic;
    }
}
