package org.loezto.e.model;

import java.util.Date;
import java.util.List;

public interface EService {

	static public final String ESERVICE_PROPERTIES = "ESERVICE_PROPERTIES";

	public Topic getTopic(long id);

	public void begin();

	public void commit();

	public void abort();

	public void activate();

	public void disconnect();

	public void save(Topic topic);

	public Topic getRootTopic();

	public List<Topic> getRootTopics();

	public void move(Topic topic, Topic newParent);

	public List<Entry> getEntries(Topic t);

	public void save(Entry entry);

	public List<Entry> searchEntries(String text, Date date, List<Topic> list);

}
