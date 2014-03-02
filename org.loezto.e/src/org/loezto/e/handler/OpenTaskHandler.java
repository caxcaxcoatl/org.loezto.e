package org.loezto.e.handler;

import java.util.List;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.swt.widgets.Shell;
import org.loezto.e.dialog.OpenTaskDialog;
import org.loezto.e.model.EService;
import org.loezto.e.model.Task;

public class OpenTaskHandler {
	@Execute
	public void execute(EService eService, Shell shell, IEventBroker eBroker) {
		List<Task> list = eService.getOpenTasks();

		OpenTaskDialog otd = new OpenTaskDialog(shell);
		otd.setList(list);
		otd.open();
		if (otd.getTask() != null)
			eBroker.send("E_SELECT_TOPIC", otd.getTask().getTopic());
		eBroker.send("E_SELECT_TASK", otd.getTask());
	}
}