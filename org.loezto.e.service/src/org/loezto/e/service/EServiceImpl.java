package org.loezto.e.service;

import java.util.Properties;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.loezto.e.model.EService;
import org.loezto.e.model.Topic;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;

public class EServiceImpl implements EService {

	@Inject
	Logger log;

	BundleContext bundleContext;

	EntityManagerFactory emf;

	EntityManager em;

	@Inject
	IEclipseContext eContext;

	public EServiceImpl() {
	}

	public void activate() {

		log.debug("Starting EServiceImpl");

		Properties props = (Properties) eContext
				.get(EService.ESERVICE_PROPERTIES);

		// Get reference for contexts
		bundleContext = FrameworkUtil.getBundle(this.getClass())
				.getBundleContext();

		log.info("Getting EntityManager");
		// Get reference for EntityManagerFactoryBuilder
		String unitName = "org.loezto.e.model";
		ServiceReference<?>[] refs = null;
		try {
			refs = bundleContext.getServiceReferences(
					EntityManagerFactoryBuilder.class.getName(),
					"(osgi.unit.name=" + unitName + ")");
		} catch (InvalidSyntaxException isEx) {
			throw new RuntimeException("Filter error", isEx);
		}

		EntityManagerFactoryBuilder emfb = (EntityManagerFactoryBuilder) bundleContext
				.getService(refs[0]);

		emf = emfb.createEntityManagerFactory(props);
		em = emf.createEntityManager();

	}

	@Override
	public Topic getTopic(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void begin() {
		// TODO Auto-generated method stub

	}

	@Override
	public void commit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void abort() {
		// TODO Auto-generated method stub

	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub

	}

}
