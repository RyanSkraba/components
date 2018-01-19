package org.talend.components.jdbc.wizard;

/**
 * 
 * JDBC wizard for edit a existed JDBC meta data
 * also need to another wizard definition for retrieve the tables by right click.
 * Now this part can't work as need some work on TUP part too.
 */
public class JDBCConnectionEditWizardDefinition extends JDBCConnectionWizardDefinition {

    public static final String COMPONENT_WIZARD_NAME = "JDBC.edit";

    @Override
    public String getName() {
        return COMPONENT_WIZARD_NAME;
    }

    @Override
    public boolean isTopLevel() {
        return false;
    }

}
