package org.loezto.e.handler;

import java.util.Collections;
import java.util.Comparator;
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
		Collections.sort(list, new Comparator<Topic>() {

			@Override
			public int compare(Topic o1, Topic o2) {
				return o1.getFullName().compareTo(o2.getFullName());
			}
		});

		OpenTopicDialog otd = new OpenTopicDialog(shell);
		otd.setList(list);
		otd.open();
		if (otd.getTopic() != null)
			eBroker.post("E_SELECT_TOPIC", otd.getTopic());
	}
}