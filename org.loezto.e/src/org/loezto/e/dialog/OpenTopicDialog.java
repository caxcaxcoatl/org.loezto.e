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
import org.eclipse.swt.widgets.Text;
import org.loezto.e.model.Topic;

class SearchName extends ViewerFilter {

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

		if (element instanceof Topic) {
			Topic topic = (Topic) element;

			// Check element...
			if (p.matcher(topic.getName().toUpperCase()).find())
				return true;

			// TODO should check for fullname instead?

			// ...its ancestry...
			for (Topic t : topic.getPath())
				if (p.matcher(t.getName().toUpperCase()).find())
					return true;

			// ...and its descedency
			// for (Topic t : topic.getDescendency())
			// if (p.matcher(t.getName().toUpperCase()).find())
			// return true;
		}
		return false;
	}
}

public class OpenTopicDialog extends Dialog {
	private Text text;
	private Table table;
	private List<Topic> list;

	private SearchName searchname = new SearchName();
	private TableViewer tableViewer;
	private Topic topic;

	public OpenTopicDialog(Shell parentShell) {
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
		table.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1,
				1));
		GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		int hint = (int) (parent.getShell().getDisplay().getBounds().height * 2f / 4f);
		System.out.println(hint);
		gd_table.heightHint = hint;
		table.setLayoutData(gd_table);

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
			this.topic = (Topic) tableViewer.getElementAt(0);
		else
			this.topic = (Topic) sel.getFirstElement();
		super.okPressed();
	}

	private void setupViewer() {
		tableViewer.addFilter(searchname);

		WritableList<Topic> wl = new WritableList<>(list, Topic.class);

		ViewerSupport.bind(tableViewer, wl,
				BeanProperties.values(new String[] { "fullName" }));

	}

	public void setList(List<Topic> list) {
		this.list = list;
	}

	public Topic getTopic() {
		return topic;
	}

}
