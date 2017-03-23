
package org.loezto.e.handler;

import java.util.ArrayList;

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

		ArrayList<String> perspectives = new ArrayList<>();

		perspectives.add("org.loezto.e.perspective.main");
		perspectives.add("org.loezto.e.perspective.planning");
		// perspectives.add("org.loezto.e.perspective.test");

		String currentPerspective = modelService.getActivePerspective(window).getElementId();

		if (perspectives.contains(currentPerspective)) {
			partService.switchPerspective(
					perspectives.get((perspectives.indexOf(currentPerspective) + 1) % perspectives.size()));
		} else
			partService.switchPerspective("org.loezto.e.perspective.main");
	}

}