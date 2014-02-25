package org.loezto.e.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.loezto.e.model.Topic;

public class TopicPropertiesDialog extends Dialog {
	private Text text;

	String name = "";
	Topic parent;

	Topic selectedParent;

	boolean selectParent = false;

	private Button btnRadioExisting;

	private Button btnRadioRoot;

	public Topic getSelectedParent() {
		return selectedParent;
	}

	public void setSelectedParent(Topic parent) {
		this.parent = selectedParent;
	}

	public Topic getParent() {
		return parent;
	}

	public void setParent(Topic parent) {
		this.parent = parent;
	}

	public boolean isSelectParent() {
		return selectParent;
	}

	public void setSelectParent(boolean selectParent) {
		this.selectParent = selectParent;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TopicPropertiesDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);

		container.setLayout(new GridLayout(1, false));

		Label lblParent = new Label(container, SWT.NONE);
		lblParent.setText("Parent topic");

		Composite compositeStack = new Composite(container, SWT.NONE);
		compositeStack.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				false, 1, 1));
		StackLayout sLayout = new StackLayout();
		compositeStack.setLayout(sLayout);

		Composite compositeSelect = new Composite(compositeStack, SWT.NONE);
		compositeSelect.setLayout(new RowLayout(SWT.VERTICAL));

		btnRadioRoot = new Button(compositeSelect, SWT.RADIO);
		btnRadioRoot.setText("&New root topic");
		btnRadioRoot.setSelection(true);

		btnRadioExisting = new Button(compositeSelect, SWT.RADIO);
		if (this.parent == null) {
			btnRadioExisting.setVisible(false);
		} else {
			btnRadioExisting.setSelection(true);
			btnRadioRoot.setSelection(false);
			btnRadioExisting.setText("&Topic " + this.parent.getName());
		}

		btnRadioExisting.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				text.setFocus();
			}
		});
		btnRadioRoot.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				text.setFocus();
			}
		});

		Composite compositeExisting = new Composite(compositeStack, SWT.NONE);
		compositeExisting.setLayout(new FillLayout(SWT.HORIZONTAL));

		Label lblParentName = new Label(compositeExisting, SWT.NONE);
		if (this.parent != null) {
			lblParentName.setText("  " + this.parent.getName());
		} else
			lblParentName.setText("");

		lblParentName.pack();

		Label lblName = new Label(container, SWT.NONE);
		lblName.setText("Name");

		text = new Text(container, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		if (selectParent) {
			sLayout.topControl = compositeSelect;
		} else
			sLayout.topControl = compositeExisting;

		text.setTextLimit(Topic.FIELD_NAME_MAX);
		text.setText(getName());
		text.setSelection(0, text.getText().length());

		text.setFocus();

		return parent;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Topic properties");
	}

	@Override
	protected void okPressed() {
		if (selectParent) {
			if (btnRadioExisting.getSelection())
				selectedParent = parent;
			else if (btnRadioRoot.getSelection())
				selectedParent = null;
			else
				assert false : "This should never have happened";
		} else
			selectedParent = null;

		this.name = text.getText();
		super.okPressed();
	}
}
