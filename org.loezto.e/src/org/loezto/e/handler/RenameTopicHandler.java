package org.loezto.e.handler;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.loezto.e.dialog.TopicPropertiesDialog;
import org.loezto.e.events.EEvents;
import org.loezto.e.model.EService;
import org.loezto.e.model.Topic;

public class RenameTopicHandler {

	public RenameTopicHandler() {
	}

	@Execute
	void execute(IEclipseContext eContext, MPerspective perspective,
			Shell shell, EService eService, IEventBroker broker) {

		Topic topic = (Topic) eContext.get("E_CURRENT_TOPIC");
		if (topic == null) {
			MessageDialog.openError(shell, "Error", "No topic selected");
			return;
		}

		TopicPropertiesDialog dialog = new TopicPropertiesDialog(shell);
		dialog.setParent(topic.getParent());
		dialog.setSelectParent(false);
		dialog.setName(topic.getName());
		int response = dialog.open();
		if (response == Dialog.OK && !dialog.getName().trim().equals("")) {
			topic.setName(dialog.getName());
			eService.save(topic);
			broker.post(EEvents.TOPIC_MODIFY, topic);
		}

	}

	@CanExecute
	boolean canExecute(MPerspective perspective, IEclipseContext eContext) {
		if (eContext.get("E_CURRENT_TOPIC") != null)
			return true;
		return false;
	}

}
