package org.loezto.e.part;

import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.loezto.e.events.EEvents;
import org.loezto.e.model.EService;
import org.loezto.e.model.Entry;
import org.loezto.e.model.Task;

public class Workplate {
	private Table table;
	private TableColumn tblclmnDeadline;
	private TableColumn tblclmnTask;
	private TableColumn tblclmnTopic;
	private TableColumn tblclmnFullname;
	private WritableList wl;
	private TableViewer tableViewer;

	@Inject
	EService eService;

	public Workplate() {
	}

	@PostConstruct
	void buildUI(Composite parent) {

		tableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
		table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableViewerColumn tableViewerColumn = new TableViewerColumn(
				tableViewer, SWT.NONE);
		tblclmnDeadline = tableViewerColumn.getColumn();
		tblclmnDeadline.setWidth(100);
		tblclmnDeadline.setText("Deadline");

		TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(
				tableViewer, SWT.NONE);
		tblclmnTask = tableViewerColumn_1.getColumn();
		tblclmnTask.setWidth(100);
		tblclmnTask.setText("Task");

		TableViewerColumn tableViewerColumn_2 = new TableViewerColumn(
				tableViewer, SWT.NONE);
		tblclmnTopic = tableViewerColumn_2.getColumn();
		tblclmnTopic.setWidth(100);
		tblclmnTopic.setText("Topic");

		TableViewerColumn tableViewerColumn_3 = new TableViewerColumn(
				tableViewer, SWT.NONE);
		tblclmnFullname = tableViewerColumn_3.getColumn();
		tblclmnFullname.setWidth(100);
		tblclmnFullname.setText("Fullname");

		setupViewer();

		wl.addAll(eService.incomingDeadlines());

	}

	private void setupViewer() {
		wl = new WritableList(new ArrayList<Task>(), Entry.class);
		ViewerSupport.bind(
				tableViewer,
				wl,
				BeanProperties.values(new String[] { "dueDate", "name",
						"topic.name", "name" }));
	}

	@Inject
	@Optional
	void updateList(@UIEventTopic(EEvents.TASK_ALL) Task task) {
		wl.clear();
		wl.addAll(eService.incomingDeadlines());
	}

}
