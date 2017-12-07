package org.talend.components.couchbase;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.service.common.DefinitionRegistry;

public class CouchbaseFamilyDefinitionTest {

    private CouchbaseFamilyDefinition definition;

    @Before
    public void setup() {
        definition = new CouchbaseFamilyDefinition();
    }

    @Test
    public void testInstall() {
        DefinitionRegistry ctx = new DefinitionRegistry();
        definition.install(ctx);

        Assert.assertEquals(2, ctx.getDefinitions().size());
    }
}
