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
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.loezto.e.model.EDatabaseException;
import org.loezto.e.model.EService;

public class OpenHandler {

	@Execute
	public void execute(MApplication app, EService eService, IEventBroker broker, Shell shell,
			EPartService partService) {

		DirectoryDialog dialog = new DirectoryDialog(shell);
		String dir = dialog.open();

		// The user canceled
		if (dir == null) {
			// TODO this is for debug only
			// dir = "/home/danilo/COMP/db/e002";
			return;
		}

		// Basic check whether the DB exists..
		{
			Path path = Paths.get(dir);
			String[] array = path.toFile().list();
			List<String> list = Arrays.asList(array);

			if (list.contains("e.db"))
				dir = dir + File.separator + "e.db";
			else if (!list.contains("service.properties")) {
				MessageDialog.openWarning(shell, "No database found",
						"The indicated directory doesn't seem to contain an é database");
				return;
			}
		}

		// Connect and acquire Entity Manager
		Properties props = new Properties();

		props.setProperty("javax.persistence.jdbc.driver", "org.apache.derby.jdbc.EmbeddedDriver");
		props.setProperty("javax.persistence.jdbc.url", "jdbc:derby:" + dir);

		// Activate uses this
		IEclipseContext appContext = app.getContext();
		appContext.set(EService.ESERVICE_PROPERTIES, props);

		try {
			eService.activate();
			broker.post("E_OPEN", "E_OPEN");
			broker.post("E_SELECT_TOPIC", eService.getRootTopic());

			partService.showPart("org.loezto.e.part.entrylist", PartState.ACTIVATE);
		} catch (EDatabaseException e) {
			MessageDialog.openError(shell, "Unable to open database", e.getMessage());
		}

	}

	@CanExecute
	boolean canExecute(EService eService) {
		return !eService.isActive();
	}

}
