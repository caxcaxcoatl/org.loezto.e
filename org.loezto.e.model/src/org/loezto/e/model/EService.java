package org.loezto.e.model;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public interface EService {

	static public final String ESERVICE_PROPERTIES = "ESERVICE_PROPERTIES";

	public Topic getTopic(long id);

	public void begin();

	public void commit();

	public void abort();

	public void activate() throws EDatabaseException;

	public void newDB(Properties props) throws EDatabaseException;

	public void disconnect();

	public void save(Topic topic);

	public Topic getRootTopic();

	public List<Topic> getRootTopics();

	public void move(Topic topic, Topic newParent);

	public List<Entry> getEntries(Topic t);

	public Entry save(Entry entry);

	public List<Entry> searchEntries(String text, Date date, List<Topic> list);

	public void save(Task task);

	public List<Task> getRootTasks(Topic currentTopic);

	public List<Entry> getEntries(Task task);

	public void move(Task task, Task target);

	public List<Task> incomingDeadlines();

	public List<Task> getCompletedTasks(Date begin, Date end);

	public boolean isActive();

	public void backup(String directory);

	public List<Topic> getAllTopics();

	public List<Task> getOpenTasks();

	public List<Entry> getEntries(Date begin, Date end);

	public CronoPlan getPlan(CronoType type, LocalDate date);

	public void save(CronoPlan plan);

}
