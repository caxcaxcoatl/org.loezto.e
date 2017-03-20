package org.loezto.e.handler;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.dialogs.Dialog;
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

		Topic parent = (Topic) eContext.get("E_CURRENT_TOPIC");
		System.out.println(parent);
		TopicPropertiesDialog dialog = new TopicPropertiesDialog(shell);
		dialog.setParent(parent);
		// if (parent == null) {
		// dialog.setParent(parent);
		// } else {
		// dialog.setParent(null);
		// }

		// IEclipseContext pContext = eContext.get(MPerspective.class)
		// .getContext();
		//
		// IStructuredSelection sel = pContext.get(IStructuredSelection.class);

		dialog.setSelectParent(true);
		int status = dialog.open();

		String name = dialog.getName();

		parent = dialog.getSelectedParent();
		System.out.println(parent);

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
