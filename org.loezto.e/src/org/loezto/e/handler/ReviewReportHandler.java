package org.loezto.e.handler;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.loezto.e.dialog.PeriodSelectionDialog;
import org.loezto.e.dialog.ReportDialog;
import org.loezto.e.model.EService;
import org.loezto.e.model.Entry;

public class ReviewReportHandler {
	@Execute
	public void execute(Shell shell, EService eService) {
		ReportDialog dialog = new ReportDialog(shell);

		/*
		 * Report should have:
		 * 
		 * - Task marked as complete
		 * 
		 * - Tasks and Topics worked (meaning they have entries during the time)
		 * 
		 * - Tasks and topic created
		 */

		Calendar begin = Calendar.getInstance();
		Calendar end = Calendar.getInstance();

		begin.add(Calendar.DAY_OF_MONTH, -6); // Since end goes to 23:59:59.999,
												// it is 7 days

		PeriodSelectionDialog psDialog = new PeriodSelectionDialog(shell);
		psDialog.setBegin(begin);
		psDialog.setEnd(end);
		if (psDialog.open() != Dialog.OK)
			return;

		begin.setTime(psDialog.getBegin());
		end.setTime(psDialog.getEnd());

		List<Entry> list = eService.getEntries(begin.getTime(), end.getTime());

		Map<Date, Map<String, List<String>>> map = new TreeMap<>();

		for (Entry e : list) {
			Calendar eDate = Calendar.getInstance();
			eDate.setTime(e.getCreationDate());
			eDate.set(Calendar.HOUR_OF_DAY, 0);
			eDate.set(Calendar.MINUTE, 0);
			eDate.set(Calendar.SECOND, 0);
			eDate.set(Calendar.MILLISECOND, 0);

			Calendar nextDay = Calendar.getInstance();
			nextDay.setTime(eDate.getTime());
			nextDay.add(Calendar.DAY_OF_MONTH, 1);
			if (map.containsKey(eDate.getTime())) {
				if (map.get(eDate.getTime()).containsKey(
						e.getTopic().getFullName())) {
					if (e.getTask() != null)
						if (!map.get(eDate.getTime())
								.get(e.getTopic().getFullName())
								.contains(e.getTask().getFullName()))
							map.get(eDate.getTime())
									.get(e.getTopic().getFullName())
									.add(e.getTask().getFullName());
				} else {
					List<String> newList = new ArrayList<>();
					if (e.getTask() != null) {
						String flag = "";
						if (e.getTask().getCompletionDate() != null
								&& e.getTask().getCompletionDate()
										.after(eDate.getTime())
								&& e.getTask().getCompletionDate()
										.before(nextDay.getTime()))
							flag = "* ";
						newList.add(flag + e.getTask().getFullName());
					}
					map.get(eDate.getTime()).put(e.getTopic().getFullName(),
							newList);
				}
			} else {
				Map<String, List<String>> newMap = new TreeMap<>();
				List<String> newList = new ArrayList<>();
				if (e.getTask() != null) {
					String flag = "";
					if (e.getTask().getCompletionDate() != null
							&& e.getTask().getCompletionDate()
									.after(eDate.getTime())
							&& e.getTask().getCompletionDate()
									.before(nextDay.getTime()))
						flag = "* ";
					newList.add(flag + e.getTask().getFullName());
				}

				newMap.put(e.getTopic().getFullName(), newList);
				map.put(eDate.getTime(), newMap);
			}
		}

		StringBuffer sb = new StringBuffer();

		String week = "";
		for (Date d : map.keySet()) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(d);
			String currWeek = String.valueOf(cal.get(Calendar.WEEK_OF_YEAR));
			if (!currWeek.equals(week)) {
				week = currWeek;
				sb.append("\n=== W" + week + " ===\n");
			}
			sb.append("\n");
			sb.append(DateFormat.getDateInstance(DateFormat.FULL).format(d));
			sb.append("\n");

			for (String t : map.get(d).keySet()) {
				sb.append("\t");
				sb.append(t);
				sb.append("\n");

				Collections.sort(map.get(d).get(t));
				for (String task : map.get(d).get(t)) {
					sb.append("\t\t");
					sb.append(task);
					sb.append("\n");
				}
			}
		}
		dialog.setReport(sb.toString());
		dialog.open();

	}

	@CanExecute
	boolean canExecute(EService eService) {
		return (eService != null && eService.isActive());
	}
}