package org.loezto.e.part;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
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

/**
 * 
 * Listens for E_CURRENT_ENTRY, and shows it
 * 
 * Ctrl+Double click on text allows for a new entry Ctrl+Enter saves the new
 * entry.
 * <p>
 * Topic and task for the new entry are also from listeners
 * 
 * @author danilo
 */

public class EntryTextPart {

	// Is this view currently in editable mode?
	boolean editable = false;

	/*
	 * Current topic and task for this part
	 * 
	 * When entries are saved, they're going to these
	 */
	Topic currentTopic;
	Task currentTask;

	// Last entry submitted. This information lasts until the focus goes out of
	// the text control, and is used to keep it editable for subsequent submits
	Entry lastSubmit;

	// Services/Injection
	@Inject
	private MDirtyable dirty;

	@Inject
	@Optional
	private EService eService;

	@Inject
	private IEclipseContext eContext;

	@Inject
	private Logger log;

	// UI
	private Color colorEditable;
	private Color colorReadonly;
	private Label lblTask;
	private Label lblPath;
	private Text text;

	@PostConstruct
	void buildUI(Composite parent, Display display) {

		// This should probably go to an app color registry, to be standardized
		colorEditable = display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		colorReadonly = display.getSystemColor(SWT.COLOR_INFO_BACKGROUND);

		// Window Builder area
		parent.setLayout(new GridLayout(1, false));

		/*
		 * TODO Check this layout. Ideally, it should be either:
		 * 
		 * - Topic on the left, task on the right, no juxtaposition
		 * 
		 * - Topic and tasks on different lines
		 */
		Composite compositePathTask = new Composite(parent, SWT.BORDER);
		compositePathTask.setLayout(new GridLayout(3, false));
		compositePathTask.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				false, 1, 1));

		lblPath = new Label(compositePathTask, SWT.NONE);
		lblPath.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false,
				1, 1));
		lblPath.setText("Path");
		lblPath.setSize(0, 21);

		Label lblSpace = new Label(compositePathTask, SWT.NONE);
		lblSpace.setText("  ");

		lblTask = new Label(compositePathTask, SWT.RIGHT);
		lblTask.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1,
				1));
		lblTask.setBounds(0, 0, 69, 21);
		lblTask.setText("Task");

		text = new Text(parent, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP
				| SWT.V_SCROLL | SWT.MULTI);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		// My customizations
		text.setBackground(colorReadonly);

		// Listeners

		// Ctrl+Double Click Listener for new edit
		text.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {

				if (e.stateMask == SWT.MOD1)
					if (!dirty.isDirty())
						editNew();
				super.mouseDoubleClick(e);
			}
		});

		// TODO Change this to a keybinding?
		// Ctrl+Enter Listener to submit
		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (((e.keyCode == SWT.CR) || (e.keyCode == SWT.KEYPAD_CR))
						&& (e.stateMask & SWT.MOD1) != 0) {
					e.doit = false; // Otherwise, the next entry will start with
									// an empty line, instead of nothing at all
					submit();
				}
			}
		});

		// Modify listener to make the part dirty
		// Empty entries are never dirty
		//
		// - A new entry with no text makes no sense
		//
		// - A new version of an entry with no text neither
		text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (editable && !text.getText().trim().equals(""))
					dirty.setDirty(true);
				else
					dirty.setDirty(false);
			}
		});

		// Zeroes the last submit, so selecting another entry will not be put
		// into editing
		text.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				lastSubmit = null;
				super.focusLost(e);
			}
		});

	}

	/**
	 * Set the labels to the task and topic current to the part or perspective
	 * 
	 * @param local
	 *            Whether the labels should come from the local (part) or
	 *            context (perspective)
	 */
	private void setLabels(boolean local) {
		if (local)
			setLabels(currentTopic, currentTask);
		else
			setLabels((Topic) eContext.get("E_CURRENT_TOPIC"),
					(Task) eContext.get("E_CURRENT_TASK"));
	}

	/**
	 * Set the labels given a task and topic.
	 * <p>
	 * If a task is given, its topic is used instead of the parameter topic
	 */
	private void setLabels(Topic topic, Task task) {

		// This shouldn't happen, but...
		if (task == null && topic == null) {
			setLabels(".", ".");
			return;
		}

		if (task == null)
			setLabels(topic.getFullName(), null);
		else
			setLabels(task.getTopic().getFullName(), task.getFullName());
	}

	/**
	 * Set the labels with specified information
	 * <p>
	 * Do the actual setting and packing
	 */
	private void setLabels(String topic, String task) {
		if (lblPath == null || lblTask == null || lblPath.isDisposed()
				|| lblTask.isDisposed())
			return;

		if (topic != null)
			lblPath.setText(topic);
		else
			lblPath.setText("");

		if (task != null)
			lblTask.setText(task);
		else
			lblTask.setText("");

		lblPath.pack();
		lblTask.pack();
	}

	/**
	 * Responds to change in topic selection
	 */
	@Inject
	@Optional
	private void changeCurrent(@Named("E_CURRENT_TOPIC") Topic topic) {

		// First of all, if there is something to be saved, do it
		if (dirty.isDirty())
			submit();

		this.currentTopic = topic;
		setLabels(true);
	}

	/**
	 * Responds to change in task selection
	 */
	@Inject
	@Optional
	private void changeCurrent(@Named("E_CURRENT_TASK") Task task) {

		// First of all, if there is something to be saved, do it
		if (dirty.isDirty())
			submit();

		this.currentTask = task;
		setLabels(true);

	}

	/**
	 * New entry selected:
	 * 
	 * - Save transient data
	 * 
	 * - Show the new entry
	 * 
	 * - Ensure it is not editable
	 * 
	 * - Unless it is empty and/or has just been submitted. In that case, allow
	 * editing
	 */
	@Inject
	@Optional
	private void changeSelection(@Named("E_CURRENT_ENTRY") Entry entry) {

		// First of all, if there is something to be saved, do it
		if (dirty.isDirty())
			submit();

		// There is no entry selected; I'm guessing I can edit a new one with
		// whatever current local selection I have
		if (entry == null) {
			setLabels(true);
			setEditable(true);
			text.setText("");
			return;
		}

		// I had just submitted something, which will supposedly be selected on
		// the entry list, generating a selection and respective E_CURRENT_ENTRY
		// change. So, I have to ignore it
		if (lastSubmit != null && lastSubmit.equals(entry)) {
			// Do nothing. I keep the if/else because it is easier to read
			assert editable == true : "I was expecting to keep the control editable";
		} else {
			// Ok, this is really a new entry selected. Let's show it and get
			// the labels from it
			setEditable(false);
			text.setText(entry.getText());
			setLabels(entry.getTopic(), entry.getTask());
		}
	}

	/**
	 * Changes the text properties to (dis)allow editing
	 * 
	 * @param editable
	 *            True or false
	 */
	private void setEditable(boolean editable) {
		this.editable = editable;
		if (editable) {
			// Editing text; let's use the local selection
			text.setBackground(colorEditable);
			setLabels(true);
		} else {
			text.setBackground(colorReadonly);
			// I don't have a reason for this to be false, but it works.
			// Not sure if true would work too.
			setLabels(false);
		}

		text.setEditable(editable);
	}

	/**
	 * Zeroes the control and set it as editable for a new entry. No save
	 */
	private void editNew() {
		setEditable(true);
		text.setText("");
	}

	/**
	 * Saves the entry
	 */
	@Persist
	private void submit() {
		if (text.getText().trim().equals("")) {
			dirty.setDirty(false);
			return;
		}

		assert !editable : "Non-editable part should not be saving";

		// TODO There is already an assert above. It should probably be wise to
		// notify the user with a exception or dialog
		if (!editable)
			return;

		Entry entry = new Entry();
		entry.setText(text.getText());

		// This uses the part's saved current topic and task, as that's what the
		// user is seeing on the interface. This submit could be in the middle
		// of a new selection
		entry.setTopic(currentTopic);
		entry.setTask(currentTask);
		// TODO Change this to a constant, or remove it
		entry.setType(" N ");
		log.debug("Saving text '\n" + text.getText() + "\n'");
		lastSubmit = eService.save(entry);

		// Just clear the entry area. Editable or not is not my problem here.
		// The clearing is kept here, to ensure it's not done by mistake when
		// responding to some event/message
		text.setText("");
		dirty.setDirty(false);
	};

	@Inject
	@Optional
	private void closeListener(@UIEventTopic("E_CLOSE") String s) {
		// First of all, if there is something to be saved, do it
		if (dirty.isDirty())
			submit();

		setEditable(false);
		setLabels("", "");
		text.setText("");
	}

	/**
	 * Sets the focus to the text control
	 */
	@Focus
	private void setFocus() {
		text.setFocus();
	}
}
