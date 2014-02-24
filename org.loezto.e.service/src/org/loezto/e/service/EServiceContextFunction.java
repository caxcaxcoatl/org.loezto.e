package org.loezto.e.service;

import java.util.Properties;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.loezto.e.model.EService;

public class EServiceContextFunction extends ContextFunction {

	public EServiceContextFunction() {
	}

	@Override
	public Object compute(IEclipseContext context, String contextKey) {

		// Connect and acquire Entity Manager
		Properties props = new Properties();

		// TODO Change this to ConfigAdmin
		// TODO Allow for creation and selection of DBs
		props.setProperty("javax.persistence.jdbc.driver",
				"org.apache.derby.jdbc.EmbeddedDriver");
		props.setProperty("javax.persistence.jdbc.url",
				"jdbc:derby:/home/danilo/COMP/db/e002");

		context.set(EService.ESERVICE_PROPERTIES, props);

		EService eService = ContextInjectionFactory.make(EServiceImpl.class,
				context);
		context.get(MApplication.class).getContext()
				.set(EService.class, eService);
		eService.activate();

		return eService;
	}
}
