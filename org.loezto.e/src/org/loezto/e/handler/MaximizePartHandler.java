package org.loezto.e.handler;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

public class MaximizePartHandler {
	@Inject
	Logger log;

	@Inject
	EModelService modelService;

	@Execute
	public void execute(@Optional MPart part, @Optional MWindow window) {

		if (part == null) {
			log.warn("MaximizePartHandler.execute received null part");
		}

		log.debug("MaximizePartHandler.execute received part " + part);

		// I'm assuming that all of my views are placed on placeholders
		MPlaceholder placeholder = modelService.findPlaceholderFor(window, part);

		// These don't work
		// tags.add("NoClose");
		// part.getTags().add("NoClose");

		List<String> tags = placeholder.getParent().getTags();
		if (tags.contains("Maximized")) {
			tags.remove("Maximized");
		} else {
			tags.add("Maximized");
		}
	}
}