package org.loezto.e.service;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.services.log.Logger;
import org.loezto.e.model.EDatabaseException;
import org.loezto.e.model.IncorrectVersionException;

@Creatable
public class SchemaManager {

	@Inject
	Logger log;

	@Inject
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

	void upgrade(String version) throws EDatabaseException {
		throw new EDatabaseException("Database upgrade not  yet implemented");
	}

}
