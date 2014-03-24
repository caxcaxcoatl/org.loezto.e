package org.loezto.e.handler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.swt.widgets.Shell;
import org.loezto.e.dialog.ReportDialog;
import org.loezto.e.model.EService;
import org.loezto.e.model.Task;

public class PlanningReportHandler {
	@Execute
	public void execute(Shell shell, EService eService) {

		ReportDialog dialog = new ReportDialog(shell);

		List<Task> allTasks = eService.getOpenTasks();

		// Map<Date, List<Task>> map = new HashMap<>();
		//
		// for (Task t : allTasks) {
		// Date tDate = t.getDueDate();
		//
		// if (map.containsKey(tDate)) {
		// map.get(tDate).add(t);
		// } else {
		// ArrayList<Task> list = new ArrayList<>();
		// map.put(tDate, list);
		// }
		// }
		//
		StringBuffer report = new StringBuffer();
		// for (Date d : map.keySet()) {
		// if (d == null)
		// continue;
		// else
		// report.append(SimpleDateFormat.getDateInstance().format(d));
		//
		// report.append("\n");
		//
		// for (Task t : map.get(d)) {
		// report.append("\t");
		// report.append(t.getTopic().getFullName());
		// report.append("\n\t\t");
		// report.append(t.getFullName());
		// // report.append ("\n\t\t\t");
		// report.append("\n");
		// }
		// report.append("\n");
		//
		// }
		//
		// report.append("\n\nUnscheduled tasks\n\n");
		//
		// for (Task t : map.get(null)) {
		// report.append("\t");
		// report.append(t.getTopic().getFullName());
		// report.append("\n\t\t");
		// report.append(t.getFullName());
		// // report.append ("\n\t\t\t");
		// report.append("\n");
		// }

		// Date > TopicName > Task
		TreeMap<Date, TreeMap<String, List<Task>>> map = new TreeMap<>();

		// TopicName > TaskName > Task
		TreeMap<String, TreeMap<String, Task>> noDateMap = new TreeMap<>();

		for (Task t : allTasks) {
			if (t.getDueDate() == null) {
				if (noDateMap.containsKey(t.getTopic().getFullName())) {
					noDateMap.get(t.getTopic().getFullName()).put(
							t.getFullName(), t);
				} else {
					TreeMap<String, Task> newMap = new TreeMap<>();
					newMap.put(t.getFullName(), t);
					noDateMap.put(t.getTopic().getFullName(), newMap);
				}
				continue;
			}

			if (map.containsKey(t.getDueDate())) {
				if (map.get(t.getDueDate()).containsKey(
						t.getTopic().getFullName())) {
					map.get(t.getDueDate()).get(t.getTopic().getFullName())
							.add(t);
				} else {
					List<Task> list = new ArrayList<>();
					list.add(t);
					map.get(t.getDueDate()).put(t.getTopic().getFullName(),
							list);
				}
			} else {
				List<Task> list = new ArrayList<>();
				list.add(t);
				TreeMap<String, List<Task>> subMap = new TreeMap<>();
				subMap.put(t.getTopic().getFullName(), list);
				map.put(t.getDueDate(), subMap);

			}
		}

		for (Date d : map.keySet()) {
			if (d == null)
				continue;

			report.append("W");
			Calendar cal = Calendar.getInstance();
			cal.setTime(d);
			report.append(cal.get(Calendar.WEEK_OF_YEAR));
			report.append(" ");
			report.append(d);
			report.append("\n");
			for (String topicName : map.get(d).keySet()) {
				report.append("\t");
				report.append(topicName);
				report.append("\n");
				for (Task t2 : map.get(d).get(topicName)) {
					report.append("\t\t");
					report.append(t2.getFullName());
					report.append("\n");
				}
			}
			report.append("\n");
		}

		report.append("Non-planned tasks");
		report.append("\n");
		for (String topicName : noDateMap.keySet()) {
			report.append("\t");
			report.append(topicName);
			report.append("\n");
			for (String tName : noDateMap.get(topicName).keySet()) {
				report.append("\t\t");
				report.append(noDateMap.get(topicName).get(tName).getFullName());
				report.append("\n");
			}
		}

		dialog.setReport(report.toString());

		dialog.open();
	}

	@CanExecute
	public boolean canExecute(@Optional EService eService) {
		if (eService == null)
			return false;
		return eService.isActive();
	}

}