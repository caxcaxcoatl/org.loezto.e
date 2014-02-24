package org.loezto.e.handler;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;

public class AddTopicHandler {

	public AddTopicHandler() {
	}

	@Execute
	public void execute() {
		System.out.println("Hey!");
	}

	@CanExecute
	public boolean canExecute() {
		return true;
	}

}
