package org.talend.components.jdbc;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.talend.components.jdbc.dataprep.DBType;

public class AdditionalDatabaseSupportTest {
	
	@Test
    public void test() {
		List<DBType> result = AdditionalDatabaseSupport.getAdditionalDatabases();
		Assert.assertTrue(result!=null);
		Assert.assertTrue(!result.isEmpty());
    }
	
}

