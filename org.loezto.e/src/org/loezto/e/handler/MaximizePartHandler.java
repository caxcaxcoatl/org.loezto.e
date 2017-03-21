package org.loezto.e.handler;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;

public class MaximizePartHandler {
	@Inject
	Logger log;

	@Execute
	public void execute(@Optional MPart part) {

		if (part == null) {
			log.warn("MaximizePartHandler.execute received null part");
		}

		log.debug("MaximizePartHandler.execute received part " + part);

		// Why getParent? No idea, but it works
		List<String> tags = part.getParent().getTags();
		if (tags.contains("Maximized")) {
			tags.remove("Maximized");
		} else {
			tags.add("Maximized");
		}
	}
}