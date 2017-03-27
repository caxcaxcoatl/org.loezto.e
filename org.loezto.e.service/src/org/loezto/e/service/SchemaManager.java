package org.loezto.e.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.log.Logger;
import org.loezto.e.model.EDatabaseException;
import org.loezto.e.model.IncorrectVersionException;
import org.osgi.framework.FrameworkUtil;

@Creatable
public class SchemaManager {

	@Inject
	Logger log;

	@Inject
	@Optional
	EntityManager em;

	void connect(String version) throws EDatabaseException {

		try {
			String dbname = (String) em.createNativeQuery("SELECT value from e.DBProps where name = 'DBName'")
					.getSingleResult();
			String dbversion = (String) em.createNativeQuery("SELECT value from e.DBProps where name = 'DBVersion'")
					.getSingleResult();

			log.info("DB Name: " + dbname);
			log.info("DB version: " + dbversion);

			if (!(dbname.equals("é") && dbversion.equals(version))) {
				IncorrectVersionException ivException = new IncorrectVersionException();
				ivException.setReason("Wrong DB/version");
				ivException.setDbVersion(dbversion);
				ivException.setRequestedVersion(version);
				throw (ivException);
			}
		} catch (PersistenceException e) {
			EDatabaseException edb = new EDatabaseException(e);
			edb.setReason("Incorrect database format");
			throw edb;
		}
	}

	void upgrade(String from, String to) throws EDatabaseException {

		em.getTransaction().begin();

		Connection con = em.unwrap(java.sql.Connection.class);

		log.debug("Retrieved connection for upgrade from EM: " + con);

		try {
			patchDB(con, from, to, true);
		} catch (Throwable e) {
			log.error(e, "Unable to upgrade the database: " + e.getMessage());
			em.getTransaction().rollback();
			throw new EDatabaseException(e, "Unable to upgrade the database");
		}
		if (em.getTransaction().isActive())
			em.getTransaction().commit();

	}

	/**
	 * 
	 * Given an URL for a DB version sql file, extract its version (the file
	 * name, without '.sql')
	 * 
	 * @param url
	 *            An URL for a .sql file inside db/schema
	 * @return
	 */
	private String versionFor(URL url) {
		return new File(url.getPath()).getName().replaceFirst("\\.[^.]*$", "");
	}

	/**
	 * 
	 * Returns a list of schema files contained on db/schema/*.*.*.sql
	 * 
	 * The files follow the format x.y.z.sql, where x, y and z are integers
	 * 
	 * @return
	 */
	private List<URL> getSchemaFiles() {
		Enumeration<URL> findEntries = FrameworkUtil.getBundle(this.getClass()).findEntries("db/schema", "*.sql",
				false);
		List<URL> ret = Collections.list(findEntries);
		ret.sort((e1, e2) -> {

			log.debug("Comparing versions " + e1 + " and " + e2);

			String n1 = versionFor(e1);
			String n2 = versionFor(e2);

			log.debug("(Reduced to " + n1 + " and " + n2);

			int ver1[] = Arrays.stream(n1.split("\\.")).mapToInt(i -> Integer.parseInt(i)).toArray();
			int ver2[] = Arrays.stream(n2.split("\\.")).mapToInt(i -> Integer.parseInt(i)).toArray();

			for (int n = 0; n < 3; n++) {
				int res = ver1[n] - ver2[n];
				if (res != 0)
					return res;
			}

			return 0;
		});
		return ret;
	}

	/**
	 * 
	 * Returns a filtered list of DB patches. The list is composed of URLs to
	 * the .sql files
	 * 
	 * 
	 * @param first
	 *            First version (i.e "0.0.1") to be included on the list. Can be
	 *            null, meaning to patch from the first version
	 * @param last
	 *            Last version to be included. Can be null, meaning to include
	 *            all up to the last one
	 * @return
	 */
	private List<URL> getPatchFiles(String first, String last) {

		boolean started = false;

		if (first == null)
			started = true;

		List<URL> allFiles = getSchemaFiles();

		List<URL> ret = new LinkedList<>();

		for (URL url : allFiles) {
			if (versionFor(url).equals(first))
				started = true;

			if (started)
				ret.add(url);

			if (versionFor(url).equals(last))
				break;
		}

		return ret;
	}

	/**
	 * 
	 * Runs the schema patch files, in order, on a database connection
	 * 
	 * The from/to parameters are version strings, in the format x.y.z (e.g.
	 * 0.0.1)
	 * 
	 * @param con
	 *            The connection to apply the patches
	 * @param from
	 *            The first DB patch to be applied. Can be null, meaning to
	 *            start from the first version
	 * @param to
	 *            The last DB patch to be applied. Can be null, meaning to all
	 *            the way to the last one
	 * @param skipFirst
	 *            If true, 'from' is interpreted as the current version, and the
	 *            patch will start on the following one
	 * @throws SQLException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	void patchDB(Connection con, String from, String to, boolean skipFirst)
			throws SQLException, MalformedURLException, IOException {

		log.debug("Reading schema...");

		List<URL> patches = getPatchFiles(from, to);

		log.debug("Original list of patches: " + patches);

		if (skipFirst && patches.size() > 0)
			patches.remove(0);

		log.info("Creating DB structure: " + patches.size() + " patches to run");
		for (URL url : patches) {
			log.info("Processing file " + url);
			// URL url = FileLocator.find(new
			// URL("platform:/plugin/org.loezto.e.service/Schema.sql"));

			Statement stmnt = readSchema(con, url);

			stmnt.executeBatch();
			stmnt.close();
		}
		con.commit();
		log.info("Creation committed");
	}

	/**
	 * Reads a .sql file and returns a Statement with the commands defined in
	 * it. Each SQL command is separated by empty lines, and comments are
	 * ignored (--).
	 * 
	 * TODO: for now, the file needs to end in a blank line
	 * 
	 * @param con
	 *            The connection for which the statement will be created
	 * @param url
	 *            The location to the SQL file to be read
	 * @return A statement with a series of batch SQL commands
	 * @throws SQLException
	 * @throws IOException
	 */
	private Statement readSchema(Connection con, URL url) throws SQLException, IOException {
		Statement stmnt = con.createStatement();
		try (InputStream is = url.openConnection().getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr)) {

			String inputline;
			StringBuffer command = new StringBuffer("");

			while ((inputline = br.readLine()) != null) {
				if (inputline.trim().equals("")) {
					if (!command.toString().equals("")) {
						log.info("Adding command " + command.toString());
						stmnt.addBatch(command.toString());
						command.setLength(0);
					}
				} else if (!inputline.matches(" *--.*")) {
					command.append(inputline);
					command.append(" ");
				}
			}
		} catch (Throwable e) {
			log.error(e, "readSchema failed with exception " + e.getMessage());
			throw e;
		}
		return stmnt;
	}

}
