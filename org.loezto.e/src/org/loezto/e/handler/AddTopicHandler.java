package org.loezto.e.handler;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.loezto.e.dialog.TopicPropertiesDialog;
import org.loezto.e.model.EService;
import org.loezto.e.model.Topic;

public class AddTopicHandler {

	public AddTopicHandler() {
	}

	@Execute
	public void execute(Shell shell, EService eService,
			IEclipseContext eContext, Logger log) {

		IEclipseContext pContext = eContext.get(MPerspective.class)
				.getContext();

		IStructuredSelection sel = pContext.get(IStructuredSelection.class);
		TopicPropertiesDialog dialog = new TopicPropertiesDialog(shell);

		if (sel != null) {
			dialog.setParent((Topic) sel.getFirstElement());
		} else {
			dialog.setParent(null);
		}
		dialog.setSelectParent(true);
		int status = dialog.open();

		String name = dialog.getName();

		Topic parent = dialog.getSelectedParent();

		if (status == Dialog.OK && !dialog.getName().trim().equals("")) {
			Topic topic = new Topic();
			topic.setName(name);

			if (parent == null) {
				log.debug("Creating new root element " + name);
			} else {
				topic.setParent(parent);
				log.debug("Creating new element " + name + " under " + parent);
			}
			eService.save(topic);
		}
	}

	@CanExecute
	public boolean canExecute() {
		return true;
	}

}
