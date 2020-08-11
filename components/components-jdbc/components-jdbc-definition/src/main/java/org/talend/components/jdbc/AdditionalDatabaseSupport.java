package org.talend.components.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.jdbc.dataprep.DBType;
import org.talend.components.jdbc.datastore.JDBCDatastoreProperties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AdditionalDatabaseSupport {

	public static final Logger LOG = LoggerFactory.getLogger(JDBCDatastoreProperties.class);

	public static List<DBType> getAdditionalDatabases() {
		List<DBType> result = new ArrayList<DBType>();

		InputStream is = null;
		try {
			is = AdditionalDatabaseSupport.class.getClassLoader().getResourceAsStream("support_extra_db.json");

			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonNode = null;
			jsonNode = objectMapper.readTree(new InputStreamReader(is));

			for (JsonNode jo : jsonNode) {
				DBType type = new DBType();
				type.id = jo.get("id").asText();
				type.clazz = jo.get("class").asText();
				type.url = jo.get("url").asText();
				JsonNode paths = jo.get("paths");
				for (JsonNode path : paths) {
					JsonNode jo_path = (JsonNode) path;
					type.paths.add(jo_path.get("path").asText());
				}

				result.add(type);
			}

			return result;
		} catch (Exception e) {
			LOG.error("failed to parse file to get additional databases : ", e);
			return result;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					LOG.error("failed to close JDBC config file", e);
				}
			}
		}
	}

}
