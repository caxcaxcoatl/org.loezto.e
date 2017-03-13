
package org.loezto.e.handler;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

public class SwitchPerspectiveHandler {

	@Inject
	EModelService modelService;

	@Inject
	EPartService partService;

	@Inject
	Logger log;

	@Execute
	public void execute(MWindow window) {
		
		if (modelService.getActivePerspective(window).getElementId().equals("org.loezto.e.perspective.planning"))
			partService.switchPerspective("org.loezto.e.perspective.main");
		else
			partService.switchPerspective("org.loezto.e.perspective.planning");
	}

}