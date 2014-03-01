package org.loezto.e.handler;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.swt.widgets.Shell;

public class FilterTaskEntriesHandler {

	@Execute
	public void execute(IEclipseContext eContext, Shell shell,
			IEventBroker broker) {

		System.out.println(1);
		broker.post("E_PART_FILTER_CHANGE", new Boolean(true));

	}
}