package org.loezto.e.model;

public interface EService {

	static public final String ESERVICE_PROPERTIES = "ESERVICE_PROPERTIES";

	public Topic getTopic(long id);

	public void begin();

	public void commit();

	public void abort();

	public void activate();

	public void disconnect();

}
