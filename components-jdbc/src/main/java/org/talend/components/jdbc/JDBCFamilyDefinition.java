package org.talend.components.jdbc;

import org.talend.components.api.AbstractComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.Constants;
import org.talend.components.jdbc.dataprep.JDBCInputDefinition;
import org.talend.components.jdbc.dataprep.di.TDataPrepDBInputDefinition;
import org.talend.components.jdbc.dataset.JDBCDatasetDefinition;
import org.talend.components.jdbc.datastore.JDBCDatastoreDefinition;
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
                new TDataPrepDBInputDefinition(),
                // Component wizards
                new JDBCConnectionWizardDefinition(),
                // Datastore, Dataset and the component
                new JDBCDatastoreDefinition(), new JDBCDatasetDefinition(), new JDBCInputDefinition());
    }

    @Override
    public void install(ComponentFrameworkContext ctx) {
        ctx.registerComponentFamilyDefinition(this);
    }
}
