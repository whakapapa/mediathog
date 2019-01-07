package db


type tMovDB struct {

}

func (mDB *tMovDB) DBinit() {
	//TODO to replace initializeDatabase
}


func (mDB *tMovDB) DBclose() {
	//TODO to replace closeDatabase
}


func (mDB *tMovDB) DBindex() {
	//TODO to replace createIndices
}



//TODO old java below

public static class Database {
	private Database() {
	}

	public static void closeDatabase() {
		try (Connection connection = PooledDatabaseConnection.getInstance().getConnection();
		Statement statement = connection.createStatement()) {
			//statement.executeUpdate("DROP SCHEMA mediathekview");
			statement.executeUpdate("SHUTDOWN COMPACT");
		} catch (SQLException e) {
			logger.error(e);
		}
		PooledDatabaseConnection.getInstance().close();
	}

	public static void createIndices() {
		logger.info("Creating SQL indices");
		try (Connection connection = PooledDatabaseConnection.getInstance().getConnection();
		Statement statement = connection.createStatement()) {
			statement.executeUpdate("CREATE INDEX IF NOT EXISTS IDX_FILM_ID ON mediathekview.film (id)");
			statement.executeUpdate("CREATE INDEX IF NOT EXISTS IDX_DESC_ID ON mediathekview.description (id)");
			statement.executeUpdate("CREATE INDEX IF NOT EXISTS IDX_WEBSITE_LINKS_ID ON mediathekview.website_links (id)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		logger.info("Finished creating SQL indices");
	}

	private static void initializeDatabase() {
		logger.info("initializeDatabase()");
		try (Connection connection = PooledDatabaseConnection.getInstance().getConnection();
		Statement statement = connection.createStatement()) {
			if (!MemoryUtils.isLowMemoryEnvironment()) {
				statement.executeUpdate("SET WRITE_DELAY 2000");
				statement.executeUpdate("SET MAX_OPERATION_MEMORY 0");
			}

			statement.executeUpdate("SET LOG 0");

			statement.executeUpdate("CREATE SCHEMA IF NOT EXISTS mediathekview");
			statement.executeUpdate("SET SCHEMA mediathekview");

			statement.executeUpdate("DROP INDEX IF EXISTS IDX_FILM_ID");
			statement.executeUpdate("DROP INDEX IF EXISTS IDX_DESC_ID");
			statement.executeUpdate("DROP INDEX IF EXISTS IDX_WEBSITE_LINKS_ID");

			statement.executeUpdate("DROP TABLE IF EXISTS mediathekview.description");
			statement.executeUpdate("DROP TABLE IF EXISTS mediathekview.website_links");
			statement.executeUpdate("DROP TABLE IF EXISTS mediathekview.film");

			statement.executeUpdate("CREATE TABLE IF NOT EXISTS film (id INTEGER NOT NULL PRIMARY KEY)");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS description (id INTEGER NOT NULL PRIMARY KEY REFERENCES mediathekview.film ON DELETE CASCADE, desc VARCHAR(1024))");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS website_links (id INTEGER NOT NULL PRIMARY KEY REFERENCES mediathekview.film ON DELETE CASCADE, link VARCHAR(1024))");

		} catch (SQLException e) {
			e.printStackTrace();
		}
		logger.info("initializeDatabase() done.");

	}
}

}

//TODO old references to keep track and remove later


package mSearch.daten;

import mSearch.tool.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.h2.jdbc.JdbcSQLException;
import org.jetbrains.annotations.NotNull;
import sun.misc.Cleaner;

import java.lang.ref.WeakReference;
import java.sql.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
