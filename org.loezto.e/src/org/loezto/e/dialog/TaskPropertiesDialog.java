package org.loezto.e.dialog;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.loezto.e.model.Task;
import org.loezto.e.model.Topic;

public class TaskPropertiesDialog extends Dialog {
	private Text txtName;
	private Button btnDueDate;
	private Button btnCompleted;
	private DateTime dtCompleted;
	private DateTime dtDue;
	private Label lblTopicName;
	private Label lblTopic;
	private Label lblParent;

	boolean hasDue = false;
	boolean hasCompleted = false;

	String name = "";
	Date due;
	Date completed;

	Topic topic;
	Task parent;

	Date today;

	public TaskPropertiesDialog(Shell parentShell) {
		super(parentShell);

		Calendar today = Calendar.getInstance();
		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);

		// This will be bound below, but need some starting value so the
		// DateTime controls do not return null on first request
		this.today = due = today.getTime();
		completed = new Date();

	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(2, false));

		Label lblName = new Label(container, SWT.NONE);
		lblName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
				1, 1));
		lblName.setText("Name");

		txtName = new Text(container, SWT.BORDER);
		txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));
		txtName.setText(getName());

		lblTopic = new Label(container, SWT.RIGHT);
		lblTopic.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblTopic.setText("Topic");

		lblTopicName = new Label(container, SWT.NONE);
		lblTopicName.setText("Topic Name");

		lblParent = new Label(container, SWT.NONE);
		lblParent.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblParent.setAlignment(SWT.RIGHT);
		lblParent.setText("Parent");

		Label lblParentNam = new Label(container, SWT.NONE);
		lblParentNam.setText("Parent Nam");

		btnDueDate = new Button(container, SWT.CHECK);
		btnDueDate.setText("&Due date");

		dtDue = new DateTime(container, SWT.BORDER | SWT.DROP_DOWN | SWT.LONG);
		dtDue.setEnabled(false);

		btnCompleted = new Button(container, SWT.CHECK);
		btnCompleted.setText("&Completed");

		dtCompleted = new DateTime(container, SWT.BORDER | SWT.DROP_DOWN);
		dtCompleted.setEnabled(false);

		// btnCompleted.addSelectionListener(new SelectionAdapter() {
		// @Override
		// public void widgetSelected(SelectionEvent e) {
		//
		// }
		// });

		DataBindingContext ctx = new DataBindingContext();

		// TODO Fix this mess

		// Binds the actual date
		ctx.bindValue(WidgetProperties.selection().observe(dtDue),
				PojoProperties.value("due").observe(this));
		// Binds the existence of the property. See setDue
		ctx.bindValue(WidgetProperties.selection().observe(btnDueDate),
				PojoProperties.value("hasDue").observe(this));
		// Binds the availability of selecting the date
		ctx.bindValue(WidgetProperties.enabled().observe(dtDue),
				WidgetProperties.selection().observe(btnDueDate));

		// Same as above
		ctx.bindValue(WidgetProperties.selection().observe(dtCompleted),
				PojoProperties.value("completed").observe(this));
		// Binds the existence of the property. See setDue
		ctx.bindValue(WidgetProperties.selection().observe(btnCompleted),
				PojoProperties.value("hasCompleted").observe(this));
		// Binds the availability of selecting the date
		ctx.bindValue(WidgetProperties.enabled().observe(dtCompleted),
				WidgetProperties.selection().observe(btnCompleted));
		// Info bindings
		ctx.bindValue(WidgetProperties.text().observe(lblTopicName),
				PojoProperties.value("topic.fullName").observe(this));
		ctx.bindValue(WidgetProperties.text().observe(lblParentNam),
				PojoProperties.value("parent.name").observe(this));

		// After the one-way binding is done; set this to the actual default
		// due = completed = null;
		// setDue(due);
		// setCompleted(completed);

		return parent;
	}

	@Override
	protected void okPressed() {
		name = txtName.getText();
		if (!hasDue)
			due = null;
		if (!hasCompleted)
			completed = null;
		super.okPressed();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getDue() {
		if (hasDue && due == null)
			return today;
		return due;
	}

	public void setDue(Date due) {
		this.due = due;
		this.hasDue = due != null;
	}

	public Date getCompleted() {
		if (hasCompleted && completed == null)
			return new Date();
		return completed;
	}

	public void setCompleted(Date completed) {
		this.completed = completed;
		this.hasCompleted = completed != null;
	}

	public Topic getTopic() {
		return topic;
	}

	public void setTopic(Topic topic) {
		this.topic = topic;
		if (this.parent != null && !this.parent.getTopic().equals(topic))
			this.parent = null;
	}

	public Task getParent() {
		return parent;
	}

	public void setParent(Task parent) {
		this.parent = parent;
		if (parent != null)
			this.topic = parent.getTopic();
	}

	public boolean isHasDue() {
		return hasDue;
	}

	public void setHasDue(boolean hasDue) {
		this.hasDue = hasDue;
	}

	public boolean isHasCompleted() {
		return hasCompleted;
	}

	public void setHasCompleted(boolean hasCompleted) {
		this.hasCompleted = hasCompleted;
	}

}
