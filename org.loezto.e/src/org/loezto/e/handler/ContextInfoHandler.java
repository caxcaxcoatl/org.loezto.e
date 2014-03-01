package org.loezto.e.handler;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;

public class ContextInfoHandler {

	@Execute
	public void execute(IEclipseContext eContext) {

		System.out.println(eContext);

	}

}
