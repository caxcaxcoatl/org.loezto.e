package org.loezto.e.part;

import java.util.ArrayList;
import java.util.Calendar;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.loezto.e.events.EEvents;
import org.loezto.e.model.EService;
import org.loezto.e.model.Task;

public class QuickReviewPart {

	@Inject
	@Optional
	EService eService;

	@Inject
	@Optional
	IEventBroker eBroker;

	private Table table;
	private TableViewer tableViewer;
	private WritableList<Task> wl;

	private Spinner spinner;

	@PostConstruct
	private void buildUI(Composite composite) {
		composite.setLayout(new GridLayout(1, false));

		spinner = new Spinner(composite, SWT.BORDER);
		spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false,
				1, 1));
		spinner.setPageIncrement(7);
		spinner.setMaximum(1000);
		spinner.setMinimum(1);
		spinner.setSelection(1);

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

		spinner.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				populateList();
			}
		});

		setupViewer();

		populateList();

		tableViewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				Task task;

				IStructuredSelection sel = (IStructuredSelection) tableViewer
						.getSelection();
				if (sel.size() != 1)
					return;

				task = (Task) sel.getFirstElement();

				eBroker.send("E_SELECT_TOPIC", task.getTopic());
				eBroker.post("E_SELECT_TASK", task);

			}
		});

	}

	private void populateList() {
		if (eService.isActive()) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, -spinner.getSelection());
			wl.clear();

			wl.addAll(eService.getCompletedTasks(cal.getTime(), null));
		}
	}

	private void setupViewer() {
		wl = new WritableList<>(new ArrayList<Task>(), null);
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
