package org.talend.components.jdbc.wizard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.api.wizard.TestComponentWizardDefinition;

/**
 * The class <code>JDBCConnectionWizardTest</code> contains tests for the class <code>{@link JDBCConnectionWizard}</code>.
 *
 * @generatedBy CodePro at 17-6-23 PM3:31
 * @author wangwei
 * @version $Revision: 1.0 $
 */
public class JDBCConnectionWizardTest {

    /**
     * Run the JDBCConnectionWizard(ComponentWizardDefinition,String) constructor test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 17-6-23 PM3:31
     */
    @Test
    public void testJDBCConnectionWizard() throws Exception {
        ComponentWizardDefinition def = new TestComponentWizardDefinition();
        String repositoryLocation = "rep";

        JDBCConnectionWizard result = new JDBCConnectionWizard(def, repositoryLocation);

        assertNotNull(result);
        assertEquals("rep", result.getRepositoryLocation());
    }

}