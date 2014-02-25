package org.loezto.e.service;

import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;
import org.loezto.e.events.EEvents;
import org.loezto.e.model.EService;
import org.loezto.e.model.Topic;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;

public class EServiceImpl implements EService {

	public static final long SUPER_ROOT_ID = 0;
	public static final long ROOT_TOPIC_ID = 1;

	@Inject
	Logger log;

	BundleContext bundleContext;

	EntityManagerFactory emf;

	EntityManager em;

	@Inject
	IEclipseContext eContext;

	@Inject
	IEventBroker broker;

	boolean inTransaction = false;
	EntityTransaction transaction;

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

		// Create Entity Manager
		EntityManagerFactoryBuilder emfb = (EntityManagerFactoryBuilder) bundleContext
				.getService(refs[0]);

		emf = emfb.createEntityManagerFactory(props);
		em = emf.createEntityManager();

	}

	@Override
	public Topic getTopic(long id) {
		return em.find(Topic.class, id);
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

	@Override
	public void save(Topic topic) {
		// TODO: Exception?
		if (topic == null) {
			log.debug("Null topic save.");
			return;
		}

		@SuppressWarnings("unused")
		boolean newItem = false;

		// Considering new topic, since 0 is special
		if (topic.getId() == 0)
			newItem = true;

		if (topic.getParent() == null) {
			// assert newItem = true :
			// "Trying to create existing null parent topic";
			topic.setParent(getRootTopic());
		}

		Topic parent = em.find(Topic.class, topic.getParent().getId());

		if (!inTransaction)
			em.getTransaction().begin();

		Topic added = em.merge(topic);
		parent.addChild(added);
		parent = em.merge(parent);

		if (!inTransaction) {
			em.getTransaction().commit();
			em.clear();
			broker.post(EEvents.TOPIC_ADD, em.find(Topic.class, added.getId()));
		}

	}

	@Override
	public Topic getRootTopic() {
		return getTopic(ROOT_TOPIC_ID);
	}

	@Override
	public List<Topic> getRootTopics() {
		List<Topic> list = em
				.createQuery(
						"Select t from Topic t where t.parent = :rootTopic",
						Topic.class)
				.setHint(QueryHints.REFRESH, HintValues.TRUE)
				.setParameter("rootTopic",
						em.getReference(Topic.class, ROOT_TOPIC_ID))
				.getResultList();
		return list;
	}
}
