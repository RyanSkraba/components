package org.talend.components.jdbc;

import org.talend.components.api.AbstractComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.Constants;
import org.talend.components.jdbc.tjdbcclose.TJDBCCloseDefinition;
import org.talend.components.jdbc.tjdbccommit.TJDBCCommitDefinition;
import org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionDefinition;
import org.talend.components.jdbc.tjdbcinput.TJDBCInputDefinition;
import org.talend.components.jdbc.tjdbcoutput.TJDBCOutputDefinition;
import org.talend.components.jdbc.tjdbcrollback.TJDBCRollbackDefinition;
import org.talend.components.jdbc.tjdbcrow.TJDBCRowDefinition;
import org.talend.components.jdbc.wizard.JDBCConnectionWizardDefinition;

import aQute.bnd.annotation.component.Component;

/**
 * Install all of the definitions provided for the JDBC family of components.
 */
@Component(name = Constants.COMPONENT_INSTALLER_PREFIX + JDBCFamilyDefinition.NAME, provide = ComponentInstaller.class)
public class JDBCFamilyDefinition extends AbstractComponentFamilyDefinition implements ComponentInstaller {

    public static final String NAME = "Jdbc";

    public JDBCFamilyDefinition() {
        super(NAME,
                // Components
                new TJDBCCloseDefinition(), new TJDBCCommitDefinition(), new TJDBCConnectionDefinition(),
                new TJDBCInputDefinition(), new TJDBCOutputDefinition(), new TJDBCRollbackDefinition(), new TJDBCRowDefinition(),
                // Component wizards
                new JDBCConnectionWizardDefinition());
    }

    @Override
    public void install(ComponentFrameworkContext ctx) {
        ctx.registerComponentFamilyDefinition(this);
    }
}
