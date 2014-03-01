package org.loezto.e.part;

import java.util.ArrayList;
import java.util.Calendar;

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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.loezto.e.events.EEvents;
import org.loezto.e.model.EService;
import org.loezto.e.model.Task;

public class QuickReviewPart {

	@Inject
	@Optional
	EService eService;
	private Table table;
	private TableViewer tableViewer;
	private WritableList wl;

	@PostConstruct
	private void buildUI(Composite composite) {
		composite.setLayout(new GridLayout(1, false));
		new Label(composite, SWT.NONE);

		tableViewer = new TableViewer(composite, SWT.BORDER
				| SWT.FULL_SELECTION);
		table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		TableViewerColumn tableViewerColumn = new TableViewerColumn(
				tableViewer, SWT.NONE);
		TableColumn tblclmnCompletion = tableViewerColumn.getColumn();
		tblclmnCompletion.setWidth(100);
		tblclmnCompletion.setText("Completion");

		TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(
				tableViewer, SWT.NONE);
		TableColumn tblclmnName = tableViewerColumn_1.getColumn();
		tblclmnName.setWidth(100);
		tblclmnName.setText("Name");

		setupViewer();

		populateList();

	}

	private void populateList() {
		if (eService.isActive()) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, -3);
			wl.clear();

			wl.addAll(eService.getCompletedTasks(cal.getTime(), null));
		}
	}

	private void setupViewer() {
		wl = new WritableList(new ArrayList<Task>(), null);
		ViewerSupport.bind(tableViewer, wl, BeanProperties.values(new String[] {
				"completionDate", "name" }));
	}

	@Inject
	@Optional
	private void openListener(@UIEventTopic("E_OPEN") String s) {
		populateList();
	}

	@Inject
	@Optional
	private void closeListener(@UIEventTopic("E_CLOSE") String s) {
		wl.clear();
	}

	@Inject
	@Optional
	void updateList(@UIEventTopic(EEvents.TASK_ALL) Task task) {
		populateList();
	}

}
