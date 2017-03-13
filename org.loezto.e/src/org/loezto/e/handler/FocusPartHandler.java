package org.loezto.e.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;

public class FocusPartHandler {

	@Execute
	public void execute(
			@Named("org.loezto.e.commandparameter.partname") String part,
			EPartService partService) {

		partService.showPart(part, PartState.ACTIVATE);

	}
	
	@CanExecute
	public boolean canExecute ()
	{
		return true;
	}

}