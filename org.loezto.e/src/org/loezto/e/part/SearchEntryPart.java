package org.loezto.e.part;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.loezto.e.model.EService;
import org.loezto.e.model.Entry;
import org.loezto.e.model.Topic;

public class SearchEntryPart {
	private Table table;
	private Text text;
	private TableViewer tableViewer;
	private WritableList wl;

	@Inject
	IEclipseContext eContext;

	@Inject
	EService eService;
	private TableViewerColumn vClnDate;
	private TableViewerColumn vClnLine;

	public SearchEntryPart() {
	}

	@PostConstruct
	void buildUI(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(4, false));
		composite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true,
				false, 1, 1));

		text = new Text(composite, SWT.BORDER | SWT.SEARCH);
		GridData gd_text = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1,
				1);
		gd_text.minimumHeight = 200;
		gd_text.widthHint = 200;
		text.setLayoutData(gd_text);

		Spinner spinner = new Spinner(composite, SWT.BORDER);
		GridData gd_spinner = new GridData(SWT.LEFT, SWT.CENTER, false, false,
				1, 1);
		gd_spinner.widthHint = 30;
		spinner.setLayoutData(gd_spinner);
		spinner.setMinimum(1);
		spinner.setSelection(1);

		Button btnNewButton = new Button(composite, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ArrayList<Topic> list = new ArrayList<>();
				list.add((Topic) eContext.get("E_CURRENT_TOPIC"));
				wl.clear();
				// Date date = null;
				wl.addAll(eService.searchEntries(text.getText(), null, null));
				// wl.addAll(eService.searchEntries(text.getText(), date,
				// list));
			}
		});
		btnNewButton.setText("&Search");
		new Label(composite, SWT.NONE);

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
						if (firstElement instanceof Entry) {
							pContext.set("E_CURRENT_ENTRY",
									(Entry) firstElement);
						}

					}

				});

	}

}
