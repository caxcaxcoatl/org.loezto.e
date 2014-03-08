package org.loezto.e.handler;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.loezto.e.dialog.AboutDialog;

public class AboutHandler {
	@Execute
	public void execute(Shell shell) {

		Dialog d = new AboutDialog(shell);

		d.open();
	}
}