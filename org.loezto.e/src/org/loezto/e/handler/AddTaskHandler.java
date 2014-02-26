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

public class AddTaskHandler {

	@Execute
	void execute(Shell shell, EService eService,
			@Optional @Named("E_CURRENT_TOPIC") Topic topic,
			@Optional @Named("E_CURRENT_TASK") Task parent) {
		System.out.println(1);
		TaskPropertiesDialog dialog = new TaskPropertiesDialog(shell);

		dialog.setTopic(topic);
		dialog.setParent(parent);

		System.out.println(2);
		int res = dialog.open();

		if (res == Dialog.OK && !dialog.getName().trim().equals("")) {
			Task task = new Task();
			task.setTopic(topic);
			task.setParent(parent);
			task.setName(dialog.getName());
			task.setDueDate(dialog.getDue());
			task.setCompletionDate(dialog.getCompleted());
			eService.save(task);
		}

		System.out.println(dialog.getName());
		System.out.println(dialog.getDue());
		System.out.println(dialog.getCompleted());
	}

	@CanExecute
	boolean canExecute(@Named("E_CURRENT_TOPIC") Topic topic) {
		return (topic != null);
	}

}
