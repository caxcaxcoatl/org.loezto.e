package org.loezto.e.handler;

import java.util.List;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;

public class MaximizePartHandler {
	@Execute
	public void execute(MPart part) {

		// Why getParent? No idea, but it works
		List<String> tags = part.getParent().getTags();
		if (tags.contains("Maximized")) {
			tags.remove("Maximized");
		} else {
			tags.add("Maximized");
		}
	}
}