package org.loezto.e.handler;

import java.util.List;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.swt.widgets.Shell;
import org.loezto.e.dialog.OpenTopicDialog;
import org.loezto.e.model.EService;
import org.loezto.e.model.Topic;

public class OpenTopicHandler {
	@Execute
	public void execute(EService eService, Shell shell, IEventBroker eBroker) {
		List<Topic> list = eService.getAllTopics();

		OpenTopicDialog otd = new OpenTopicDialog(shell);
		otd.setList(list);
		otd.open();
		if (otd.getTopic() != null)
			eBroker.post("E_SELECT_TOPIC", otd.getTopic());
	}
}