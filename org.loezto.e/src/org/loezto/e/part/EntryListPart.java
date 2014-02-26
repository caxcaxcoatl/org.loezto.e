package org.loezto.e.part;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.loezto.e.events.EEvents;
import org.loezto.e.model.EService;
import org.loezto.e.model.Entry;
import org.loezto.e.model.Task;
import org.loezto.e.model.Topic;

class TextContents extends ViewerFilter {

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		return false;
	}

}

class AutomaticEntries extends ViewerFilter {

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof Entry)
			if (((Entry) element).getType().matches(" N"))
				return true;
			else
				return false;
		return false;
	}
}

class SearchText extends ViewerFilter {

	String search = "";

	void setSearch(String search) {
		this.search = search.toUpperCase();
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof Entry)
			if (((Entry) element).getText().toUpperCase()
					.matches(".*" + search + ".*"))
				return true;
			else
				return false;
		return false;
	}

}

public class EntryListPart {
	private Table table;
	private TableViewer tableViewer;
	private TableViewerColumn vClnDate;
	private TableViewerColumn vClnLine;
	private WritableList wl;

	AutomaticEntries automaticEntries = new AutomaticEntries();
	SearchText searchText = new SearchText();

	public EntryListPart() {
	}

	@Inject
	@Optional
	EService eService;

	@Inject
	IEclipseContext eContext;
	private Text text;
	private Button btnFilterAuto;

	@PostConstruct
	void buildUI(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));
		composite.setLayout(new GridLayout(2, false));

		text = new Text(composite, SWT.BORDER);
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				searchText.setSearch(text.getText());
				tableViewer.refresh();
			}
		});
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		text.setBounds(0, 0, 75, 33);

		btnFilterAuto = new Button(composite, SWT.CHECK);
		btnFilterAuto.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (btnFilterAuto.getSelection())
					tableViewer.removeFilter(automaticEntries);
				else
					tableViewer.addFilter(automaticEntries);
			}
		});
		btnFilterAuto.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		btnFilterAuto.setText("Show auto");

		tableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
		table = tableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		vClnDate = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnDate = vClnDate.getColumn();
		Calendar today = Calendar.getInstance();
		String fullDate = SimpleDateFormat.getDateTimeInstance(DateFormat.FULL,
				DateFormat.FULL).format(today.getTime());
		GC gc = new GC(table);
		tblclmnDate.setWidth(gc.textExtent(fullDate).x);
		tblclmnDate.setText("Date");

		vClnLine = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnLine = vClnLine.getColumn();
		tblclmnLine.setWidth(100);
		tblclmnLine.setText("Line");

		viewerSetup();

	}

	private void viewerSetup() {

		wl = new WritableList(new ArrayList<Entry>(), Entry.class);
		ViewerSupport.bind(tableViewer, wl,
				BeanProperties.values(new String[] { "creationDate", "line" }));

		tableViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {

					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						IEclipseContext pContext = eContext.get(
								MPerspective.class).getContext();
						Object firstElement = ((IStructuredSelection) event
								.getSelection()).getFirstElement();
						if (firstElement == null)
							pContext.set("E_CURRENT_ENTRY", null);

						if (firstElement instanceof Entry) {
							pContext.set("E_CURRENT_ENTRY",
									(Entry) firstElement);
						}

					}

				});

		tableViewer.addFilter(automaticEntries);
		tableViewer.addFilter(searchText);

	}

	@Inject
	@Optional
	void changeSelection(@Named("E_CURRENT_TOPIC") Topic topic) {
		wl.clear();
		wl.addAll(eService.getEntries(topic));
		tableViewer.refresh();
		if (table.getItemCount() > 0)
			tableViewer.setSelection(new StructuredSelection(tableViewer
					.getElementAt(table.getItemCount() - 1)));
		table.showSelection();
	}

	@Inject
	@Optional
	void processTaskChanges(@UIEventTopic("TASK/*") Task task) {
		Topic currentTopic = (Topic) eContext.get("E_CURRENT_TOPIC");
		if (task.getTopic().equals(currentTopic)) {
			wl.clear();
			Task currentTask = (Task) eContext.get("E_CURRENT_TASK");
			if (currentTask == null)
				wl.addAll(eService.getEntries(currentTopic));
			else
				wl.addAll(eService.getEntries(currentTask));
			tableViewer.refresh();
			if (table.getItemCount() > 0)
				tableViewer.setSelection(new StructuredSelection(tableViewer
						.getElementAt(table.getItemCount() - 1)));
			table.showSelection();
		}

	}

	@Inject
	@Optional
	void newTaskSelected(@Named("E_CURRENT_TASK") Task task) {
		wl.clear();
		wl.addAll(eService.getEntries(task));
		tableViewer.refresh();
		if (table.getItemCount() > 0) {
			tableViewer.setSelection(new StructuredSelection(tableViewer
					.getElementAt(table.getItemCount() - 1)));
			table.showSelection();
		}
	}

	@Inject
	@Optional
	void newEntry(@UIEventTopic(EEvents.ENTRY_ADD) Entry entry) {
		if (entry.getTopic() == eContext.get("E_CURRENT_TOPIC")) {
			if (entry.getTask() == eContext.get("E_CURRENT_TASK")) {
				wl.clear();
				wl.addAll(eService.getEntries(entry.getTask()));
				tableViewer.refresh();
				if (table.getItemCount() > 0)
					tableViewer
							.setSelection(new StructuredSelection(tableViewer
									.getElementAt(table.getItemCount() - 1)));
				table.showSelection();
			} else {
				wl.clear();
				wl.addAll(eService.getEntries(entry.getTopic()));
				tableViewer.refresh();
				if (table.getItemCount() > 0)
					tableViewer
							.setSelection(new StructuredSelection(tableViewer
									.getElementAt(table.getItemCount() - 1)));
				table.showSelection();
			}

		}
	}

	@Focus
	void setFocus() {
		table.setFocus();
	}
}
