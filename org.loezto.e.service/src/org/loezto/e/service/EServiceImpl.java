package org.loezto.e.service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.sql.DataSource;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.internal.contexts.EclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;
import org.loezto.e.events.EEvents;
import org.loezto.e.model.CronoPlan;
import org.loezto.e.model.CronoType;
import org.loezto.e.model.EDatabaseException;
import org.loezto.e.model.EService;
import org.loezto.e.model.Entry;
import org.loezto.e.model.IncorrectVersionException;
import org.loezto.e.model.Task;
import org.loezto.e.model.Topic;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;

public class EServiceImpl implements EService {

	public static final String E_PERSISTENCE_UNIT = "org.loezto.e.model";
	public static final long SUPER_ROOT_ID = 0;
	public static final long ROOT_TOPIC_ID = 1;

	public static final String CURRENT_DB_VERSION = "0.2.0";

	static final String[] TOPIC_FIELDS = { "name" };
	static final String[] TASK_FIELDS = { "name", "dueDate", "completionDate" };

	boolean active = false;

	@Inject
	Logger log;

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

	private void closeEntityManagerObjects() {
		em.close();
		emf.close();
		this.active = false;
	}

	public void activate() throws EDatabaseException {
		activate(false);
	}

	public void activate(boolean doUpdate) throws EDatabaseException {

		log.debug("Activating EServiceImpl");

		Properties props = (Properties) eContext.get(EService.ESERVICE_PROPERTIES);

		log.debug("Getting EntityManager");

		EntityManagerFactoryBuilder emfb = getBundleService("(osgi.unit.name=" + E_PERSISTENCE_UNIT + ")",
				EntityManagerFactoryBuilder.class);

		if (emfb == null) {
			EDatabaseException edb = new EDatabaseException();
			edb.setReason("Unable to acquire EntityManagerFactoryBuilder");
			throw edb;
		}

		try {
			log.info("Connection props: " + props);
			emf = emfb.createEntityManagerFactory(props);
			em = emf.createEntityManager();
		} catch (PersistenceException e) {
			EDatabaseException edb = new EDatabaseException(e);

			Throwable current = e;
			Throwable root = e;

			while ((current = current.getCause()) != null)
				root = current;

			edb.setReason("Cannot create Entity Manager" + root.getMessage());
			log.error(e, "Unable to create Entity Manager");
			throw edb;
		}

		IEclipseContext schemaContext = new EclipseContext(eContext);
		schemaContext.set(EntityManager.class, em);

		SchemaManager schemaManager = ContextInjectionFactory.make(SchemaManager.class, eContext, schemaContext);
		try {
			schemaManager.connect(CURRENT_DB_VERSION);
			this.active = true;
		} catch (IncorrectVersionException e) {
			log.warn("Incorrect DB version");
			if (doUpdate) {
				try {
					schemaManager.upgrade(e.getDbVersion(), CURRENT_DB_VERSION);
					this.active = true;
				} catch (Throwable e2) {
					closeEntityManagerObjects();
					throw e2;
				}
			} else {
				closeEntityManagerObjects();
				throw e;
			}
		} catch (Throwable e) {
			closeEntityManagerObjects();
			throw e;
		}

	}

	private <T> T getBundleService(String filter, Class<T> klass) {

		BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();

		log.debug("getBundleService call for '" + filter + "', " + klass);
		log.debug("BundleContext: " + bundleContext);

		// Get reference for EntityManagerFactoryBuilder
		ServiceReference<?>[] refs = null;
		try {
			refs = bundleContext.getServiceReferences(klass.getName(), filter);
		} catch (InvalidSyntaxException isEx) {
			throw new RuntimeException("Filter error: No instance found for class " + klass + " and filter " + filter,
					isEx);
		}

		if (refs == null || refs.length == 0)
			throw new RuntimeException("No instance found for class " + klass + " and filter " + filter);

		if (refs.length > 1)
			log.warn("getBundleService() for class " + klass + " and filter '" + filter
					+ "' returned multiple responses: \n" + Arrays.deepToString(refs));

		// Create Entity Manager
		// TODO: Should treat for empty, multiple results
		T ret = klass.cast(bundleContext.getService(refs[0]));
		return ret;
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
		this.active = false;
		em.close();
		emf.close();
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

		Topic parent;
		if (topic.getParent() == null) {
			if (topic.getId() == 1)
				parent = em.find(Topic.class, 0L);
			else
				parent = getRootTopic();
			topic.setParent(parent);
		}
		parent = topic.getParent();

		String report = "";

		if (newItem) {
			report = BeanReporter.report(topic, TOPIC_FIELDS);
		} else {
			report = BeanComparer.compare(em.find(Topic.class, topic.getId()), topic, TOPIC_FIELDS);
		}

		if (!inTransaction)
			em.getTransaction().begin();

		Topic added = em.merge(topic);
		if (newItem) {
			parent.addChild(added);
			parent = em.merge(parent);
		}

		if (newItem)
			em.merge(serviceEntry(added,
					String.format("New topic '%s' added under '%s'\n\n" + report, added.getName(), parent.getName()),
					null));
		else
			em.merge(serviceEntry(added,
					String.format("Topic has been saved with name '%s'\n\n" + report, topic.getName()), null));

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
				.createQuery("Select t from Topic t where t.parent = :rootTopic order by t.name", Topic.class)
				.setHint(QueryHints.REFRESH, HintValues.TRUE)
				.setParameter("rootTopic", em.getReference(Topic.class, ROOT_TOPIC_ID)).getResultList();
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

		em.merge(serviceEntry(topic, String.format("Topic '%s' moved from '%s' to '%s'", topic.getName(),
				oldParent.getName(), newParent.getName()), null));

		em.getTransaction().commit();

		broker.post(EEvents.TOPIC_MODIFY, oldParent);
		broker.post(EEvents.TOPIC_MODIFY, newParent);
		broker.post(EEvents.TOPIC_MODIFY, topic);

	}

	Entry serviceEntry(Topic topic, String text, Task task) {
		Entry entry = new Entry();
		entry.setText(text);
		entry.setTopic(topic);
		entry.setType(" A ");
		entry.setTask(task);

		return entry;
	}

	@Override
	public List<Entry> getEntries(Topic t) {
		List<Entry> list = em
				.createQuery("Select e from Entry e where e.topic = :topic order by e.creationDate", Entry.class)
				.setParameter("topic", t).setHint(QueryHints.REFRESH, HintValues.TRUE).getResultList();
		em.clear();
		return list;
	}

	public Entry save(Entry entry) {
		String topicName = entry.getTopic().getFullName();
		String taskName = (entry.getTask() == null ? "" : entry.getTask().getFullName());
		log.info(String.format("Saving entry on task '%s' under topic '%s'\n%s", taskName, topicName, entry.getText()));
		Entry mergedEntry;
		em.getTransaction().begin();
		mergedEntry = em.merge(entry);
		em.getTransaction().commit();
		broker.post(EEvents.ENTRY_ADD, mergedEntry);
		return mergedEntry;
	}

	@Override
	public List<Entry> searchEntries(String text, Date date, List<Topic> list) {

		StringBuffer sb = new StringBuffer();

		sb.append("Select e From Entry e Where 1=1 ");
		if (date != null)
			sb.append(" and e.creationDate > :date ");
		if (text != null)
			sb.append(" and upper(e.text) like :text ");
		if (list != null)
			sb.append(" and e.topic in :list ");
		sb.append(" order by e.creationDate ");

		TypedQuery<Entry> query = em.createQuery(sb.toString(), Entry.class);

		if (date != null)
			query.setParameter("date", date);
		if (text != null)
			query.setParameter("text", "%" + text.toUpperCase() + "%");
		if (list != null)
			query.setParameter("list", list);

		return query.getResultList();
	}

	@Override
	public void save(Task task) {

		String report;
		boolean newItem = false;

		if (task.getId() == 0)
			newItem = true;

		if (newItem) {
			report = BeanReporter.report(task, TASK_FIELDS);
		} else {
			report = BeanComparer.compare(em.find(Task.class, task.getId()), task, TASK_FIELDS);
		}

		em.getTransaction().begin();

		Task mergedTask = em.merge(task);

		Task parent = task.getParent();
		if (parent != null)
			if (!parent.getChildren().contains(mergedTask))
				parent.getChildren().add(mergedTask);

		if (parent != null)
			em.merge(parent);

		String msg;
		if (newItem)
			if (task.getParent() == null)
				msg = String.format("New root task '%s' added on topic '%s'\n\n" + report, task.getName(),
						task.getTopic().getFullName());
			else
				msg = String.format("New task '%s' added under '%s' on topic '%s'\n\n" + report, task.getName(),
						task.getParent().getFullName(), task.getTopic().getFullName());
		else if (task.getParent() == null)
			msg = String.format("Task '%s' saved on topic '%s' \n\n" + report, task.getName(),
					task.getTopic().getFullName());
		else
			msg = String.format("Task '%s' saved under '%s' on topic '%s\n\n" + report, task.getName(),
					task.getParent().getFullName(), task.getTopic().getFullName());

		em.merge(serviceEntry(task.getTopic(), msg, task));

		em.getTransaction().commit();
		broker.post(EEvents.TASK_ADD, task);
	}

	@Override
	public List<Task> getRootTasks(Topic topic) {
		return em.createQuery("Select t from Task t where t.topic = :topic and t.parent is null order by t.name",
				Task.class).setParameter("topic", topic).getResultList();
	}

	@Override
	public List<Entry> getEntries(Task task) {
		List<Entry> list = em
				.createQuery("Select e from Entry e where e.task = :task order by e.creationDate", Entry.class)
				.setParameter("task", task).setHint(QueryHints.REFRESH, HintValues.TRUE).getResultList();
		em.clear();
		return list;
	}

	@Override
	public void move(Task task, Task newParent) {
		Task oldParent = task.getParent();

		String oldParentName;
		String newParentName;

		em.getTransaction().begin();

		if (oldParent == null) {
			oldParentName = "root";
		} else {
			oldParent.removeChild(task);
			oldParentName = oldParent.getName();
		}

		task.setParent(newParent);

		if (newParent == null) {
			newParentName = "root";
		} else {
			newParent.addChild(task);
			newParentName = newParent.getName();
		}

		if (oldParent != null)
			em.merge(oldParent);

		em.merge(task);
		if (newParent != null)
			em.merge(newParent);

		em.merge(serviceEntry(task.getTopic(),
				String.format("Task '%s' moved from '%s' to '%s'", task.getName(), oldParentName, newParentName),
				task));

		em.getTransaction().commit();

		// broker.post(EEvents.TASK_MODIFY, oldParent);
		// broker.post(EEvents.TASK_MODIFY, newParent);
		broker.post(EEvents.TASK_MODIFY, task);

	}

	@Override
	public List<Task> incomingDeadlines() {
		return em.createQuery(
				"Select t from Task t where t.dueDate is not null and t.completionDate is null order by t.dueDate",
				Task.class).getResultList();
	}

	@Override
	public List<Task> getCompletedTasks(Date begin, Date end) {
		StringBuffer sb = new StringBuffer("Select t from Task t where 1 =1 ");

		if (begin != null)
			sb.append(" and t.completionDate > :begin ");
		if (end != null)
			sb.append(" and t.completionDate < :end ");

		sb.append(" order by t.completionDate ");

		TypedQuery<Task> query = em.createQuery(sb.toString(), Task.class);

		if (begin != null)
			query.setParameter("begin", begin);
		if (end != null)
			query.setParameter("end", end);

		return query.getResultList();
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public void newDB(Properties props) throws EDatabaseException {
		DataSourceFactory dsf;
		DataSource ds;
		try {
			dsf = getBundleService(
					"(" + DataSourceFactory.OSGI_JDBC_DRIVER_CLASS + "=org.apache.derby.jdbc.EmbeddedDriver)",
					DataSourceFactory.class);

			ds = dsf.createDataSource(props);
			log.info("DataSource: " + ds);

		} catch (Exception e) {
			EDatabaseException e2 = new EDatabaseException(e);
			e2.setReason("No DataSourceFactory found: " + e.getMessage());
			throw (e2);
		}

		try (Connection con = ds.getConnection("e", "e")) {

			con.setAutoCommit(false);
			log.debug("Conn: " + con);

			DatabaseMetaData metadata = con.getMetaData();
			log.info("Driver accessed by sample Gemini DBAccess client:" + "\n\tName = " + metadata.getDriverName()
					+ "\n\tVersion = " + metadata.getDriverVersion() + "\n\tUser = " + metadata.getUserName());

			SchemaManager schemaManager = ContextInjectionFactory.make(SchemaManager.class, eContext);
			schemaManager.patchDB(con, null, null, false);
		} catch (Exception e) {
			EDatabaseException e2 = new EDatabaseException(e);
			e2.setReason("Unable to create database: " + e.getMessage());
			throw (e2);
		}

	}

	@Override
	public void backup(String directory) {

		em.createStoredProcedureQuery("SYSCS_UTIL.SYSCS_BACKUP_DATABASE")
				.registerStoredProcedureParameter(1, String.class, ParameterMode.IN).setParameter(1, directory)
				.execute();

		// em.createNativeQuery("CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE(?)")
		// .setParameter(1, directory).getResultList();
	}

	// Perhaps this could be changed to a cache, which is updated on every
	// addition and edition?
	//
	// Same could apply to a master task cache, which would contain only the
	// active tasks
	@Override
	public List<Topic> getAllTopics() {
		return em.createQuery("Select t from Topic t  where t.root = false order by t.name", Topic.class)
				.getResultList();
	}

	@Override
	public List<Task> getOpenTasks() {
		return em.createQuery("Select t from Task t  where t.completionDate is null order by t.name", Task.class)
				.getResultList();
	}

	@Override
	public List<Entry> getEntries(Date begin, Date end) {
		return em.createQuery(
				"Select e from Entry e where e.creationDate > :begin and e.creationDate < :end order by e.creationDate",
				Entry.class).setParameter("begin", begin).setParameter("end", end).getResultList();
	}

	@Override
	public CronoPlan getPlan(CronoType type, LocalDate ref) {
		try {
			return em.createQuery("select p from CronoPlan p where p.id = :id", CronoPlan.class)
					.setParameter("id", CronoPlan.CronoId.of(type, ref)).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public void save(CronoPlan plan) {
		em.getTransaction().begin();
		em.merge(plan);
		em.getTransaction().commit();

	}

}
