package com.guacamole.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.DriverManager;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;

class FlywayMigrationTests {
	@Test
	void refusesNonemptySchemaWithoutHistoryByDefault() throws Exception {
		String url = "jdbc:h2:mem:flyway_existing;MODE=MySQL;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1";
		try (var connection = DriverManager.getConnection(url, "sa", ""); var statement = connection.createStatement()) {
			statement.execute("CREATE TABLE existing_data (id INT)");
		}
		Flyway flyway = Flyway.configure().dataSource(url, "sa", "").baselineOnMigrate(false).load();
		assertThrows(org.flywaydb.core.api.FlywayException.class, flyway::migrate);
	}
	@Test
	void migratesFromEmptyV2ToV3WithoutRepair() throws Exception {
		String url = "jdbc:h2:mem:flyway_upgrade;MODE=MySQL;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1";
		Flyway throughV2 = Flyway.configure().dataSource(url, "sa", "")
				.target(MigrationVersion.fromVersion("2")).load();
		throughV2.migrate();
		try (var connection = DriverManager.getConnection(url, "sa", "");
				var statement = connection.createStatement();
				var result = statement.executeQuery("SELECT \"checksum\", \"description\" FROM \"flyway_schema_history\" WHERE \"version\" = '2'")) {
			result.next();
			assertEquals(0, result.getInt(1));
			assertEquals("add category to course", result.getString(2));
		}
		Flyway current = Flyway.configure().dataSource(url, "sa", "").load();
		current.migrate();
		current.validate();
		try (var connection = DriverManager.getConnection(url, "sa", "");
				var statement = connection.createStatement();
				var result = statement.executeQuery("SELECT COUNT(*) FROM revoked_token")) {
			result.next();
			assertEquals(0, result.getInt(1));
		}
	}
}
