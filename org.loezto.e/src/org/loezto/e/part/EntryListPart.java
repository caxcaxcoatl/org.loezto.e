package org.loezto.e.part;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
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
import org.loezto.e.viewerfilters.AutomaticEntries;
import org.loezto.e.viewerfilters.TaskEntryFilter;

class TextContents extends ViewerFilter {

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		return false;
	}

}

class SearchText extends ViewerFilter {

	String search = "";
	Pattern p = Pattern.compile(search);;

	void setSearch(String search) {
		this.search = search.toUpperCase();

		try {
			p = Pattern.compile(this.search);
		} catch (PatternSyntaxException e) {
			p = null;
		}
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (p == null)
			return false;

		if (element instanceof Entry)
			if (p.matcher(((Entry) element).getText().toUpperCase()).find())
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
	private WritableList<Entry> wl;

	SearchText searchText = new SearchText();
	TaskEntryFilter taskEntryFilter = new TaskEntryFilter();

	public EntryListPart() {
		automaticEntries = new AutomaticEntries();

	}

	@Inject
	IEventBroker eBroker;

	@Inject
	@Optional
	EService eService;

	@Inject
	IEclipseContext eContext;
	private Text text;
	private Button btnFilterAuto;
	private ControlDecoration decoPatternError;
	private AutomaticEntries automaticEntries;
	private TableColumn tblclmnT;
	private TableViewerColumn tableViewerColumn;

	@PostConstruct
	void buildUI(Composite parent) {
		eContext.set(automaticEntries.getClass().getName(), automaticEntries);

		parent.setLayout(new GridLayout(1, false));

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));
		composite.setLayout(new GridLayout(2, false));

		text = new Text(composite, SWT.BORDER | SWT.H_SCROLL | SWT.SEARCH
				| SWT.CANCEL);
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String pattern = text.getText();
				try {
					Pattern.compile(pattern);
					decoPatternError.hide();
				} catch (PatternSyntaxException ex) {
					decoPatternError.setDescriptionText(ex.getDescription());
					decoPatternError.show();
				}
				searchText.setSearch(pattern);
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
					// automaticEntries.setCheckAuto(false);
					tableViewer.removeFilter(automaticEntries);
				else
					// automaticEntries.setShowAuto(false);
					tableViewer.addFilter(automaticEntries);
			}
		});
		btnFilterAuto.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		btnFilterAuto.setText("Show auto");

		tableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION
				| SWT.MULTI);
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

		tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		tblclmnT = tableViewerColumn.getColumn();
		tblclmnT.setText("T");
		// tblclmnT.setWidth(25);
		tblclmnT.pack();

		vClnLine = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnLine = vClnLine.getColumn();
		tblclmnLine.setWidth(100);
		tblclmnLine.setText("Line");

		decoPatternError = new ControlDecoration(text, SWT.TOP | SWT.LEFT);
		decoPatternError.setImage(FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_WARNING)
				.getImage());
		decoPatternError.hide();

		table.addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == SWT.SPACE)
					// In the future, instead of null it should carry a
					// reference.
					//
					// In that way, if more than one listener returns
					// NEXT/PREVIOUS item, it can check and do it only once;
					// also other parts can use the message.
					if (e.stateMask == SWT.SHIFT)
						eBroker.post("E_UI_DETAIL_PAGE", new Integer(-1)); // Page
																			// UP
					else
						eBroker.post("E_UI_DETAIL_PAGE", new Integer(1)); // Page
																			// Down

			}
		});

		viewerSetup();

	}

	private void viewerSetup() {

		wl = new WritableList<>(new ArrayList<Entry>(), Entry.class);
		ViewerSupport.bind(
				tableViewer,
				wl,
				BeanProperties.values(new String[] { "creationDate", "task.t",
						"line" }));

		tableViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {

					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						IEclipseContext pContext = eContext.get(
								MPerspective.class).getContext();
						IStructuredSelection sel = ((IStructuredSelection) event
								.getSelection());
						Object firstElement = sel.getFirstElement();
						if (firstElement == null || sel.size() != 1)
							pContext.set("E_CURRENT_ENTRY", null);
						else if (firstElement instanceof Entry) {
							pContext.set("E_CURRENT_ENTRY",
									(Entry) firstElement);
						}

					}

				});

		Transfer[] transferTypes = new Transfer[] { TextTransfer.getInstance() };
		int operations = DND.DROP_COPY;
		tableViewer.addDragSupport(operations, transferTypes,
				new DragSourceListener() {

					@Override
					public void dragStart(DragSourceEvent event) {
					}

					@Override
					public void dragSetData(DragSourceEvent event) {
						if (TextTransfer.getInstance().isSupportedType(
								event.dataType)) {
							StringBuffer sb = new StringBuffer();
							IStructuredSelection sel = (IStructuredSelection) tableViewer
									.getSelection();
							Iterator<?> i = sel.iterator();
							Pattern p = Pattern.compile("(?m)^");
							while (i.hasNext()) {
								Entry e = (Entry) i.next();
								sb.append(e.getCreationDate());
								if (e.getTask() != null) {
									sb.append(" - ");
									sb.append(e.getTask().getName());
								}
								sb.append("\n");
								Matcher m = p.matcher(e.getText());

								sb.append(m.replaceAll("\t"));
								sb.append("\n\n");
							}
							event.data = sb.toString();
						}
					}

					@Override
					public void dragFinished(DragSourceEvent event) {
					}
				});

		tableViewer.addFilter(automaticEntries);
		tableViewer.addFilter(searchText);

	}

	@Inject
	@Optional
	void changeSelection(@Named("E_CURRENT_TOPIC") Topic topic) {
		System.out
				.println("Current topic changed on EntryListPart.  For debug: "
						+ topic);
		wl.clear();
		wl.addAll(eService.getEntries(topic));
		tableViewer.refresh();
		if (table.getItemCount() > 0)
			tableViewer.setSelection(
					new StructuredSelection(tableViewer.getElementAt(table
							.getItemCount() - 1)), true);
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
				tableViewer.setSelection(
						new StructuredSelection(tableViewer.getElementAt(table
								.getItemCount() - 1)), true);
			// table.showSelection();
		}

	}

	@Inject
	@Optional
	void newTaskSelected(@Named("E_CURRENT_TASK") Task task) {
		System.out
				.println("Current task changed on EntryListPart.  For debug: "
						+ task);

		wl.clear();
		if (task == null) {
			Topic topic = (Topic) eContext.get("E_CURRENT_TOPIC");
			if (topic != null)
				wl.addAll(eService.getEntries(topic));
		} else
			wl.addAll(eService.getEntries(task));

		tableViewer.refresh();
		if (table.getItemCount() > 0) {
			tableViewer.setSelection(
					new StructuredSelection(tableViewer.getElementAt(table
							.getItemCount() - 1)), true);
			// table.showSelection();
		}
	}

	@Inject
	@Optional
	void move(@UIEventTopic("E_UI_LIST_MOVE") Integer dir) {
		int newIndex = table.getSelectionIndex() + dir;

		if (newIndex >= 0 && newIndex < table.getItemCount())
			tableViewer
					.setSelection(
							new StructuredSelection(tableViewer
									.getElementAt(newIndex)), true);
	}

	@Inject
	@Optional
	private void newEntry(@UIEventTopic(EEvents.ENTRY_ADD) Entry entry) {
		// I'm showing the current topic. If entries are added to other topics,
		// I don't care
		if (entry.getTopic() != null
				&& entry.getTopic().equals(eContext.get("E_CURRENT_TOPIC"))) {
			// Same thing for current task. If it is null, go with current topic
			// Update only if entry's task is same as current, and not null
			// Update it with the task entries
			if (entry.getTask() != null
					&& entry.getTask().equals(eContext.get("E_CURRENT_TASK"))) {
				wl.clear();
				wl.addAll(eService.getEntries(entry.getTask()));
				tableViewer.refresh();
				// There is no need to select the new entry; just show it
				// However... Apparently it doesn't work on Linux :(
				//
				// These don't work:
				//
				// table.showItem(table.getItem(table.getItemCount() - 1));
				// tableViewer.reveal(entry);

				if (table.getItemCount() > 0)
					tableViewer.setSelection(
							new StructuredSelection(tableViewer
									.getElementAt(table.getItemCount() - 1)),
							true);
				// table.showSelection();

			}
			// So, the entry has no task and/or there is no current task
			// Update it with the topic entries
			else {
				wl.clear();
				wl.addAll(eService.getEntries(entry.getTopic()));
				tableViewer.refresh();
				if (table.getItemCount() > 0)
					tableViewer.setSelection(
							new StructuredSelection(tableViewer
									.getElementAt(table.getItemCount() - 1)),
							true);
				// table.showSelection();
			}

		}
	}

	@Inject
	@Optional
	private void filterChanged(
			@UIEventTopic("E_PART_FILTER_CHANGE") boolean change) {
		System.out.println("Hello");
		if (Arrays.asList(tableViewer.getFilters()).contains(taskEntryFilter))
			tableViewer.removeFilter(taskEntryFilter);
		else
			tableViewer.addFilter(taskEntryFilter);
		tableViewer.refresh();
	}

	@Focus
	void setFocus() {
		table.setFocus();
	}

	@Inject
	@Optional
	private void closeListener(@UIEventTopic("E_CLOSE") String s) {
		System.out.println(s);
		wl.clear();
		table.setEnabled(false);
	}

	@Inject
	@Optional
	private void openListener(@UIEventTopic("E_OPEN") String s) {
		table.setEnabled(true);
	}

}
