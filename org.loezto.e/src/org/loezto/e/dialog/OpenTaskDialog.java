package org.loezto.e.dialog;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.loezto.e.model.Task;

class SearchTaskName extends ViewerFilter {

	String search = "";

	void setSearch(String search) {
		this.search = search.toUpperCase();
	}

	@Override
	public boolean isFilterProperty(Object element, String property) {
		return (property.equals("name"));
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		Pattern p;
		try {
			p = Pattern.compile(search);
		} catch (PatternSyntaxException e) {
			return false;
		}

		if (element instanceof Task) {
			Task task = (Task) element;

			// Check element...
			// if (p.matcher(task.getName().toUpperCase()).find())
			// return true;

			// TODO should check for fullname instead?

			// ...its ancestry...
			for (Task t : task.getPath())
				if (p.matcher(t.getName().toUpperCase()).find())
					return true;

			// ...and its descedency
			// for (Task t : task.getDescendency())
			// if (p.matcher(t.getName().toUpperCase()).find())
			// return true;
		}
		return false;
	}
}

public class OpenTaskDialog extends Dialog {
	private Text text;
	private Table table;
	private List<Task> list;

	private SearchTaskName searchname = new SearchTaskName();
	private TableViewer tableViewer;
	private Task task;
	private TableColumn tblclmnTopic;
	private TableViewerColumn tableViewerColumn;
	private TableColumn tblclmnTask;
	private TableViewerColumn tableViewerColumn_1;

	public OpenTaskDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// Composite composite = new Composite(parent, SWT.NONE);
		Composite composite = (Composite) super.createDialogArea(parent);
		// parent.setLayout(new GridLayout(1, false));
		composite.setLayout(new GridLayout(1, false));

		text = new Text(composite, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		tableViewer = new TableViewer(composite, SWT.BORDER
				| SWT.FULL_SELECTION);
		table = tableViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1,
				1));
		GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_table.heightHint = (int) (parent.getShell().getDisplay().getBounds().height * 2f / 4f);
		gd_table.widthHint = (int) (parent.getShell().getDisplay().getBounds().width * 3f / 4f);
		table.setLayoutData(gd_table);

		tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		tblclmnTopic = tableViewerColumn.getColumn();
		tblclmnTopic.setWidth(gd_table.widthHint / 3);
		tblclmnTopic.setText("Topic");

		tableViewerColumn_1 = new TableViewerColumn(tableViewer, SWT.NONE);
		tblclmnTask = tableViewerColumn_1.getColumn();
		tblclmnTask.setWidth(gd_table.widthHint / 3 * 2);
		tblclmnTask.setText("Task");

		setupViewer();

		text.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				searchname.setSearch(text.getText());
				tableViewer.refresh();
			}
		});

		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ARROW_DOWN)
					table.setFocus();
			}
		});
		
		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ARROW_UP && table.getSelectionIndex() <= 0)
					text.setFocus();
			}
		});		

		return parent;
	}

	@Override
	protected void okPressed() {
		IStructuredSelection sel = (IStructuredSelection) tableViewer
				.getSelection();
		if (sel.isEmpty())
			this.task = (Task) tableViewer.getElementAt(0);
		else
			this.task = (Task) sel.getFirstElement();
		super.okPressed();
	}

	private void setupViewer() {
		tableViewer.addFilter(searchname);

		WritableList<Task> wl = new WritableList<>(list, Task.class);

		ViewerSupport.bind(
				tableViewer,
				wl,
				BeanProperties.values(new String[] { "topic.fullName",
						"fullName" }));

	}

	public void setList(List<Task> list) {
		this.list = list;
	}

	public Task getTask() {
		return task;
	}

}
