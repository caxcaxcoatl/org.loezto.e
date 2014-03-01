package org.loezto.e.handler;

import java.util.List;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.loezto.e.model.EService;

public class CloseHandler {

	@Execute
	public void execute(EService eService, IEventBroker broker,
			EModelService modelService, MApplication app) {

		List<MPerspective> list = modelService.findElements(app,
				"org.loezto.e.perspective.main", MPerspective.class, null);

		for (MPerspective p : list) {

			IEclipseContext pContext = p.getContext();

			pContext.set("E_CURRENT_ENTRY", null);
			pContext.set("E_CURRENT_TASK", null);
			pContext.set("E_CURRENT_TOPIC", null);
		}

		// Synchronous to await finishing any ops
		broker.send("E_CLOSE", "E_CLOSE");

		eService.disconnect();
	}

	@CanExecute
	public boolean canExecute(EService eService) {
		return eService.isActive();
	}

}