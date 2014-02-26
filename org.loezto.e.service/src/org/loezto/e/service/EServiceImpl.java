package org.loezto.e.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;
import org.loezto.e.events.EEvents;
import org.loezto.e.model.EService;
import org.loezto.e.model.Entry;
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

		boolean newItem = false;

		// Considering new topic, since 0 is special
		if (topic.getId() == 0)
			newItem = true;

		if (topic.getParent() == null) {
			topic.setParent(getRootTopic());
		}

		Topic parent = em.find(Topic.class, topic.getParent().getId());

		if (!inTransaction)
			em.getTransaction().begin();

		Topic added = em.merge(topic);
		parent.addChild(added);
		parent = em.merge(parent);

		if (newItem)
			em.merge(serviceEntry(
					added,
					String.format("New topic '%s' added under '%s'",
							added.getName(), parent.getName())));
		else
			em.merge(serviceEntry(
					added,
					String.format("Topic has been saved with name '%s'",
							topic.getName())));

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
						"Select t from Topic t where t.parent = :rootTopic order by t.name",
						Topic.class)
				.setHint(QueryHints.REFRESH, HintValues.TRUE)
				.setParameter("rootTopic",
						em.getReference(Topic.class, ROOT_TOPIC_ID))
				.getResultList();
		return list;
	}

	@Override
	public void move(Topic topic, Topic newParent) {
		Topic oldParent = topic.getParent();

		em.getTransaction().begin();
		oldParent.removeChild(topic);
		topic.setParent(newParent);
		newParent.addChild(topic);

		em.merge(oldParent);
		em.merge(topic);
		em.merge(newParent);

		em.merge(serviceEntry(
				topic,
				String.format("Topic '%s' moved from '%s' to '%s'",
						topic.getName(), oldParent.getName(),
						newParent.getName())));

		em.getTransaction().commit();

		broker.post(EEvents.TOPIC_MODIFY, oldParent);
		broker.post(EEvents.TOPIC_MODIFY, newParent);
		broker.post(EEvents.TOPIC_MODIFY, topic);

	}

	Entry serviceEntry(Topic topic, String text) {
		Entry entry = new Entry();
		entry.setText(text);
		entry.setTopic(topic);
		entry.setType(" A ");

		return entry;
	}

	@Override
	public List<Entry> getEntries(Topic t) {
		List<Entry> list = em
				.createQuery(
						"Select e from Entry e where e.topic = :topic order by e.creationDate",
						Entry.class).setParameter("topic", t)
				.setHint(QueryHints.REFRESH, HintValues.TRUE).getResultList();
		em.clear();
		return list;
	}

	public void save(Entry entry) {
		em.getTransaction().begin();
		em.merge(entry);
		em.getTransaction().commit();
		broker.post(EEvents.ENTRY_ADD, entry);
	}

	@Override
	public List<Entry> searchEntries(String text, Date date,
			ArrayList<Topic> list) {

		StringBuffer sb = new StringBuffer();

		sb.append("Select e From Entry e Where ");
		if (date != null)
			sb.append("and e.creationDate > :date");
		if (text != null)
			sb.append(" upper(e.text) like :text ");
		if (list != null)
			sb.append("and e.topic in :list");
		sb.append(" order by e.creationDate");

		TypedQuery<Entry> query = em.createQuery(sb.toString(), Entry.class);

		if (date != null)
			query.setParameter("date", date);
		if (text != null)
			query.setParameter("text", "%" + text.toUpperCase() + "%");
		if (list != null)
			// TODO
			;

		return query.getResultList();
	}
}
