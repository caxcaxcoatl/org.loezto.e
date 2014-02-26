package org.loezto.e.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.loezto.e.dialog.TaskPropertiesDialog;
import org.loezto.e.model.EService;
import org.loezto.e.model.Task;
import org.loezto.e.model.Topic;

public class TaskPropertiesHandler {

	@Execute
	void execute(Shell shell, EService eService,
			@Optional @Named("E_CURRENT_TOPIC") Topic topic,
			@Named("E_CURRENT_TASK") Task task) {

		TaskPropertiesDialog dialog = new TaskPropertiesDialog(shell);

		dialog.setTopic(task.getTopic());
		dialog.setParent(task.getParent());
		dialog.setName(task.getName());
		dialog.setDue(task.getDueDate());
		dialog.setCompleted(task.getCompletionDate());

		int res = dialog.open();

		if (res == Dialog.OK && !dialog.getName().trim().equals("")) {
			task.setName(dialog.getName());
			task.setDueDate(dialog.getDue());
			task.setCompletionDate(dialog.getCompleted());
			eService.save(task);
		}

	}

	@CanExecute
	boolean canExecute(@Named("E_CURRENT_TASK") Task task) {
		return (task != null);
	}
}
