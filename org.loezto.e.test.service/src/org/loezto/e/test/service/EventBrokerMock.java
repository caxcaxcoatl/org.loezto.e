package org.loezto.e.test.service;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.osgi.service.event.EventHandler;

public class EventBrokerMock implements IEventBroker {

	@Override
	public boolean send(String topic, Object data) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean post(String topic, Object data) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean subscribe(String topic, EventHandler eventHandler) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean subscribe(String topic, String filter, EventHandler eventHandler, boolean headless) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean unsubscribe(EventHandler eventHandler) {
		// TODO Auto-generated method stub
		return true;
	}

}
