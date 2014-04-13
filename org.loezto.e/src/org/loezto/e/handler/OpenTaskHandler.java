package org.loezto.e.handler;

import java.util.Collections;
import java.util.Comparator;
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
		Collections.sort(list, new Comparator<Task>() {

			@Override
			public int compare(Task o1, Task o2) {
				return o1.getFullName().compareTo(o2.getFullName());
			}
		});

		OpenTaskDialog otd = new OpenTaskDialog(shell);
		otd.setList(list);
		otd.open();
		if (otd.getTask() != null)
			eBroker.send("E_SELECT_TOPIC", otd.getTask().getTopic());
		eBroker.send("E_SELECT_TASK", otd.getTask());
	}
}