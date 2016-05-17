package org.talend.components.salesforce;

import static org.talend.daikon.properties.PropertyFactory.*;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.presentation.Form;

public class UpsertRelationTable extends ComponentProperties {

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

    public Property columnName = newEnum("columnName");

    public Property lookupFieldName = newString("lookupFieldName");

    public Property lookupFieldModuleName = newString("lookupFieldModuleName");

    public Property lookupFieldExternalIdName = newString("lookupFieldExternalIdName");

    public Property polymorphic = newProperty(Property.Type.BOOLEAN, "polymorphic");

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = Form.create(this, Form.MAIN);
        mainForm.addColumn(columnName);
        mainForm.addColumn(lookupFieldName);
        mainForm.addColumn(lookupFieldModuleName);
        if (usePolymorphic) {
            mainForm.addColumn(polymorphic);
        }
        mainForm.addColumn(lookupFieldExternalIdName);
    }

    
    /**
     * Getter for usePolymorphic.
     * @return the usePolymorphic
     */
    public boolean isUsePolymorphic() {
        return this.usePolymorphic;
    }

    
    /**
     * Sets the usePolymorphic.
     * @param usePolymorphic the usePolymorphic to set
     */
    public void setUsePolymorphic(boolean usePolymorphic) {
        this.usePolymorphic = usePolymorphic;
    }
}
