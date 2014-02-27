package org.loezto.e.part;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.loezto.e.model.EService;
import org.loezto.e.model.Entry;
import org.loezto.e.model.Task;
import org.loezto.e.model.Topic;

public class EntryTextPart {

	public EntryTextPart() {
	}

	boolean editable = false;
	boolean justSubmited = false;

	@Inject
	MDirtyable dirty;

	@Inject
	@Optional
	EService eService;

	@Inject
	IEclipseContext eContext;
	private Text text;
	private Label lblPath;

	@Inject
	Logger log;

	Color colorEditable;
	Color colorReadonly;
	private Label lblTask;

	// LocalResourceManager lrManager;

	@PostConstruct
	void buildUI(Composite parent, Display display) {

		colorEditable = display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		colorReadonly = display.getSystemColor(SWT.COLOR_INFO_BACKGROUND);

		parent.setLayout(new GridLayout(1, false));

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1));
		composite.setLayout(new GridLayout(3, false));

		lblPath = new Label(composite, SWT.NONE);
		lblPath.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false,
				1, 1));
		lblPath.setText("Path");
		lblPath.setSize(0, 21);

		Label lblSpace = new Label(composite, SWT.NONE);
		lblSpace.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
				false, 1, 1));
		lblSpace.setText("  ");

		lblTask = new Label(composite, SWT.RIGHT);
		lblTask.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false,
				1, 1));
		lblTask.setBounds(0, 0, 69, 21);
		lblTask.setText("Task");

		text = new Text(parent, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP
				| SWT.V_SCROLL | SWT.MULTI);
		text.setBackground(colorReadonly);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		text.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {

				if (e.stateMask == SWT.MOD1)
					editNew();
				super.mouseDoubleClick(e);
			}
		});

		// TODO Change this to a keybinding?
		text.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (((e.keyCode == SWT.CR) || (e.keyCode == SWT.KEYPAD_CR))
						&& (e.stateMask & SWT.MOD1) != 0) {
					e.doit = false; // Otherwise, the next entry will start with
									// an empty line, instead of nothing at all
					submit();
				}

			}

		});

		text.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				if (editable && !text.getText().trim().equals(""))
					dirty.setDirty(true);
				else
					dirty.setDirty(false);
			}
		});

	}

	// TODO: move all the label setting to a function
	@Inject
	@Optional
	void changeSelection(@Named("E_CURRENT_TOPIC") Topic topic) {
		if (topic == null)
			lblPath.setText("");
		else
			lblPath.setText(topic.getFullName());
		lblTask.setText("");
		lblTask.pack();
		lblPath.pack();
	}

	@Inject
	@Optional
	void changeSelection(@Named("E_CURRENT_TASK") Task task) {
		if (task == null)
			lblTask.setText("");
		else
			lblTask.setText(task.getName());
		lblTask.pack();
		lblPath.pack();
	}

	@Inject
	@Optional
	void changeSelection(@Named("E_CURRENT_ENTRY") Entry entry) {
		if (dirty.isDirty())
			submit();

		if (justSubmited) {
			justSubmited = false;
		} else {
			setEditable(false);
			if (entry == null) {
				text.setText("");
				// lblPath.setText("");
			} else {
				text.setText(entry.getText());
				lblPath.setText(entry.getTopic().getFullName());
				Task task = entry.getTask();
				if (task != null)
					lblTask.setText(task.getName());
				else
					lblTask.setText("");
			}
			lblPath.pack();
			lblTask.pack();
		}
	}

	void setEditable(boolean editable) {
		this.editable = editable;
		if (editable) {
			text.setBackground(colorEditable);

		} else {
			text.setBackground(colorReadonly);
		}
		Task task = (Task) eContext.get("E_CURRENT_TASK");
		if (task == null) {
			lblTask.setText("");
			Topic topic = (Topic) eContext.get("E_CURRENT_TOPIC");
			lblPath.setText(topic.getFullName());
		} else {
			lblTask.setText(task.getName());
			lblPath.setText(task.getTopic().getFullName());
		}

		text.setEditable(editable);
	}

	void editNew() {
		setEditable(true);
		text.setText("");
	}

	@Persist
	private void submit() {
		if (text.getText().trim().equals("")) {
			dirty.setDirty(false);
			return;
		}
		if (!editable)
			return;

		Entry entry = new Entry();
		entry.setText(text.getText());
		entry.setTopic((Topic) eContext.get("E_CURRENT_TOPIC"));
		entry.setTask((Task) eContext.get("E_CURRENT_TASK"));
		entry.setType(" N ");
		log.debug("Saving text '\n" + text.getText() + "\n'");
		eService.save(entry);

		// Keep editable, just clear the entry area
		text.setText("");
		dirty.setDirty(false);
		justSubmited = true;
	};

	@Focus
	void setFocus() {
		text.setFocus();
	}
}
