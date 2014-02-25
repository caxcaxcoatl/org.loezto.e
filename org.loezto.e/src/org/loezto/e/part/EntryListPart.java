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
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.loezto.e.events.EEvents;
import org.loezto.e.model.EService;
import org.loezto.e.model.Entry;
import org.loezto.e.model.Topic;

public class EntryListPart {
	private Table table;
	private TableViewer tableViewer;
	private TableViewerColumn vClnDate;
	private TableViewerColumn vClnLine;
	private WritableList wl;

	public EntryListPart() {
	}

	@Inject
	@Optional
	EService eService;

	@Inject
	IEclipseContext eContext;

	@PostConstruct
	void buildUI(Composite parent) {
		parent.setLayout(new FillLayout(SWT.HORIZONTAL));

		tableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
		table = tableViewer.getTable();
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
						if (firstElement instanceof Entry) {
							pContext.set("E_CURRENT_ENTRY",
									(Entry) firstElement);
						}

					}

				});

	}

	@Inject
	@Optional
	void changeSelection(@Named("E_CURRENT_TOPIC") Topic topic) {
		wl.clear();
		wl.addAll(eService.getEntries(topic));
		tableViewer.refresh();
		tableViewer.setSelection(new StructuredSelection(tableViewer
				.getElementAt(table.getItemCount() - 1)));
		table.showSelection();
	}

	@Inject
	@Optional
	void newEntry(@UIEventTopic(EEvents.ENTRY_ADD) Entry entry) {
		if (entry.getTopic() == eContext.get("E_CURRENT_TOPIC")) {
			wl.clear();
			wl.addAll(eService.getEntries(entry.getTopic()));
			tableViewer.refresh();
			tableViewer.setSelection(new StructuredSelection(tableViewer
					.getElementAt(table.getItemCount() - 1)));
			table.showSelection();

		}
	}

	@Focus
	void setFocus() {
		table.setFocus();
	}
}
