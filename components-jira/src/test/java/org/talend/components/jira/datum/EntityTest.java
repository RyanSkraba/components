// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.jira.datum;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit-tests for {@link Entity} class
 */
public class EntityTest {

	/**
	 * Check {@link Entity#getJson()} returns JSON representation without
	 * changes
	 */
	@Test
	public void testGetJson() {
		String json = "{\"expand\":\"schema,names\",\"startAt\":0,\"maxResults\":50,\"total\":37,\"issues\":["
				+ "{\"expand\":\"operations,versionedRepresentations,editmeta,changelog,transitions,renderedFields\",\"id\":\"10413\","
				+ "\"self\":\"http://localhost:8080/rest/api/2/issue/10413\",\"key\":\"TP-41\",\"fields\":{\"description\":\"task upd\",\"summary\":\"Updated 37\",}},"
				+ "{\"expand\":\"operations,versionedRepresentations,editmeta,changelog,transitions,renderedFields\",\"id\":\"10412\","
				+ "\"self\":\"http://localhost:8080/rest/api/2/issue/10412\",\"key\":\"TP-40\",\"fields\":{\"description\":\"Task updated\",\"summary\":\"TP 38 updated\",}},"
				+ "{\"expand\":\"operations,versionedRepresentations,editmeta,changelog,transitions,renderedFields\",\"id\":\"10411\","
				+ "\"self\":\"http://localhost:8080/rest/api/2/issue/10411\",\"key\":\"TP-39\",\"fields\":{\"description\":null,\"summary\":\"115\",}}]}";
		Entity entity = new Entity(json);

		String actual = entity.getJson();
		assertEquals(json, actual);
	}
}
