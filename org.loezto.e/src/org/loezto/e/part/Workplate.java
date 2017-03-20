package org.loezto.e.part;

import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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
	private WritableList<Task> wl;
	private TableViewer tableViewer;

	@Inject
	EService eService;

	@Inject
	IEventBroker eBroker;

	@Inject
	EMenuService menuService;

	@Inject
	ESelectionService selService;

	public Workplate() {
	}

	@PostConstruct
	void buildUI(Composite parent) {

		tableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
		table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		tblclmnDeadline = tableViewerColumn.getColumn();
		tblclmnDeadline.setWidth(100);
		tblclmnDeadline.setText("Deadline");

		TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(tableViewer, SWT.NONE);
		tblclmnTask = tableViewerColumn_1.getColumn();
		tblclmnTask.setWidth(100);
		tblclmnTask.setText("Task");

		TableViewerColumn tableViewerColumn_2 = new TableViewerColumn(tableViewer, SWT.NONE);
		tblclmnTopic = tableViewerColumn_2.getColumn();
		tblclmnTopic.setWidth(100);
		tblclmnTopic.setText("Topic");

		TableViewerColumn tableViewerColumn_3 = new TableViewerColumn(tableViewer, SWT.NONE);
		tblclmnFullname = tableViewerColumn_3.getColumn();
		tblclmnFullname.setWidth(100);
		tblclmnFullname.setText("Fullname");

		setupViewer();

		if (eService.isActive())
			wl.addAll(eService.incomingDeadlines());

		tableViewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				Task task;

				IStructuredSelection sel = (IStructuredSelection) tableViewer.getSelection();
				if (sel.size() != 1)
					return;

				task = (Task) sel.getFirstElement();

				eBroker.send("E_SELECT_TOPIC", task.getTopic());
				eBroker.post("E_SELECT_TASK", task);

			}
		});

	}

	private void setupViewer() {

		// This would need tasklist to be reworked to accept a parameter
		menuService.registerContextMenu(table, "org.loezto.e.popupmenu.workplate");

		wl = new WritableList<>(new ArrayList<Task>(), Entry.class);
		ViewerSupport.bind(tableViewer, wl,
				BeanProperties.values(new String[] { "dueDate", "name", "topic.fullName", "fullName" }));
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (sel != null)
					selService.setSelection(sel.getFirstElement());
			}
		});
	}

	@Inject
	@Optional
	void updateList(@UIEventTopic(EEvents.TASK_ALL) Task task) {
		wl.clear();
		wl.addAll(eService.incomingDeadlines());
	}

	@Inject
	@Optional
	private void openListener(@UIEventTopic("E_OPEN") String s) {
		wl.clear();
		wl.addAll(eService.incomingDeadlines());
	}

	@Inject
	@Optional
	private void closeListener(@UIEventTopic("E_CLOSE") String s) {
		wl.clear();
	}

	@Focus
	void focus() {
		table.setFocus();
	}

}
