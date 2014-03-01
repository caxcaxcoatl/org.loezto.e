package org.loezto.e.handler;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.loezto.e.model.EDatabaseException;
import org.loezto.e.model.EService;
import org.osgi.service.jdbc.DataSourceFactory;

public class NewHandler {

	@Execute
	public void execute(Shell shell, MApplication app, EService eService,
			IEventBroker broker, DataSourceFactory dsf) {

		IEclipseContext appContext = app.getContext();
		DirectoryDialog dialog = new DirectoryDialog(shell);

		dialog.setMessage("The database directory will be named e.db, under the directory you chose");
		dialog.setText("Choose destination directory");

		String directory;

		while ((directory = dialog.open()) != null) {

			if (directory.equals(""))
				return;

			Path path = Paths.get(directory);

			String[] array = path.toFile().list();
			List<String> list = Arrays.asList(array);

			// Making sure we're not overwriting something...
			if (list.contains("e.db") || list.contains("service.properties")) {
				MessageDialog
						.openWarning(shell, "Error",
								"The indicated directory seems to already contain a database");
				continue;
			}

			String dbdir = directory + File.separator + "e.db";

			try {
				// Connect and acquire Entity Manager
				Properties props = new Properties();

				props.setProperty("javax.persistence.jdbc.driver",
						"org.apache.derby.jdbc.EmbeddedDriver");
				props.setProperty(DataSourceFactory.JDBC_URL, "jdbc:derby:"
						+ dbdir + ";create=true");
				props.setProperty("javax.persistence.jdbc.url", "jdbc:derby:"
						+ dbdir);

				eService.newDB(props);
				appContext.set(EService.ESERVICE_PROPERTIES, props);
				eService.activate();
				broker.post("E_OPEN", "E_OPEN");
				return;
			} catch (EDatabaseException e) {
				MessageDialog.openError(shell, "Unable to open database",
						e.getLocalizedMessage());
				e.printStackTrace();
			}

		}

	}

	@CanExecute
	boolean canExecute(EService eService) {
		return !eService.isActive();
	}
}