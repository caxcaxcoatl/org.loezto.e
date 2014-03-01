package org.loezto.e.handler;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.loezto.e.model.EService;

public class BackupHandler {
	@Execute
	public void execute(Shell shell, EService eService) {

		// TODO Zip it

		DirectoryDialog dialog = new DirectoryDialog(shell);

		SimpleDateFormat.getTimeInstance(DateFormat.SHORT, Locale.ROOT).format(
				new Date());

		String filename = "e.db."
				+ new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
						.format(new Date());

		dialog.setMessage(String.format(
				"The backup will be named %s, under the directory you chose",
				filename));
		dialog.setText("Choose destination directory");

		String directory;

		directory = dialog.open();

		if (directory == null)
			return;

		directory = directory + File.separator + filename;

		eService.backup(directory);

	}

	@CanExecute
	public boolean canExecute(EService eService) {
		return eService.isActive();
	}

}