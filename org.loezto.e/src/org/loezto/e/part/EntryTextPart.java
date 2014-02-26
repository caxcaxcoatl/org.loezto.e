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

	// LocalResourceManager lrManager;

	@PostConstruct
	void buildUI(Composite parent, Display display) {

		colorEditable = display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		colorReadonly = display.getSystemColor(SWT.COLOR_INFO_BACKGROUND);

		parent.setLayout(new GridLayout(1, false));

		lblPath = new Label(parent, SWT.NONE);

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
					e.doit = false;
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

	@Inject
	@Optional
	void changeSelection(@Named("E_CURRENT_ENTRY") Entry entry) {
		if (dirty.isDirty())
			submit();

		if (justSubmited) {
			justSubmited = false;
		} else {
			setEditable(false);
			text.setText(entry.getText());
			lblPath.setText(entry.getTopic().getPathString());
			lblPath.pack();
		}
	}

	void setEditable(boolean editable) {
		this.editable = editable;
		if (editable) {
			text.setBackground(colorEditable);

		} else {
			text.setBackground(colorReadonly);
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
		entry.setType(" N ");
		log.debug("Saving text '" + text.getText() + "'");
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
